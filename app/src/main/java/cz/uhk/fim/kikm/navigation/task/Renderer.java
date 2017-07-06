package cz.uhk.fim.kikm.navigation.task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.OrthographicCamera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Torus;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.util.CenteredToast;
import cz.uhk.fim.kikm.navigation.util.Settings;
import cz.uhk.fim.kikm.navigation.util.Utils;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class Renderer extends org.rajawali3d.renderer.Renderer {
	private Context context;
	private SQLiteDatabase sqLiteDatabase;
	private ScaleGestureDetector scaleGestureDetector;
	private Object3D location;
	private List<Ring> rings = new ArrayList<>(), innerRings = new ArrayList<>(), particles = new ArrayList<>(), innerParticles = new ArrayList<>();
	private List<Square> squares = new ArrayList<>(), innerSquares = new ArrayList<>();
	private List<Cube> arrows = new ArrayList<>();
	private double currentX = 1500, currentY = 1500, lastTouchX = 0, lastTouchY = 0, lastTouch = 0, scale = 1;
	private boolean levelNeeded = false, refreshNeeded = false, hideNeeded = false, showNeeded = false, arrowShowNeeded = false, arrowHideNeeded = false;
	private String currentLevel = "J3NP";
	private int currentArrow = 0;
	
	public Renderer(Context context) {
		super(context);
		this.context = context;
		this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
			public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
				scale = location.getScaleX() * scaleGestureDetector.getScaleFactor();
				reloadScene();
				return true;
			}
			
			public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
				lastTouch = -9999;
				return true;
			}
			
			public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
				lastTouch = 0;
			}
		});
	}
	
	public String getCurrentLevel() {
		return currentLevel;
	}
	
	public Renderer setCurrentLevel(String currentLevel) {
		if (!this.currentLevel.equals(currentLevel)) levelNeeded = true;
		this.currentLevel = currentLevel;
		return this;
	}
	
	public double getCurrentY() {
		return currentY;
	}
	
	public Renderer setCurrentY(double currentY) {
		this.currentY = currentY;
		return this;
	}
	
	public double getCurrentX() {
		return currentX;
	}
	
	public Renderer setCurrentX(double currentX) {
		this.currentX = currentX;
		return this;
	}
	
	public Renderer setCurrentArrow(int[] first, int[] second) {
		int x1 = first[0], y1 = first[1], x2 = second[0], y2 = second[1];
		if (x1 == x2 && y1 > y2) currentArrow = 0;
		if (x1 == x2 && y1 < y2) currentArrow = 2;
		if (x1 < x2 && y1 == y2) currentArrow = 1;
		if (x1 > x2 && y1 == y2) currentArrow = 3;
		arrowShowNeeded = true;
		return this;
	}
	
	public Renderer setNoArrow() {
		arrowHideNeeded = true;
		return this;
	}
	
	public double getScale() {
		return scale;
	}
	
	public Renderer setScale(double scale) {
		this.scale = scale;
		return this;
	}
	
	public Renderer setRings(List<float[]> rings) {
		innerRings.clear();
		float sum = 0;
		for (float[] ring : rings) sum += ring[4];
		for (float[] ring : rings) innerRings.add(new Ring(ring[0], ring[1], 30, 1 / (3000 / ring[2]), 1 / (3000 / ring[3]), (int) ring[4], Color.argb((int) (191 / sum * ring[4]), 0, 0, 0)));
		refreshNeeded = true;
		return this;
	}
	
	public Renderer setRings(List<float[]> rings, List<float[]> resultRings) {
		innerRings.clear();
		float sum = 0;
		for (float[] ring : rings) sum += ring[4];
		for (float[] ring : rings) innerRings.add(new Ring(ring[0], ring[1], 30, 1 / (3000 / ring[2]), 1 / (3000 / ring[3]), (int) ring[4], Color.argb((int) (191 / sum * ring[4]), 0, 0, 0)));
		for (int i = 0; i < resultRings.size(); i++) { // WireLess (Yellow) -> BlueTooth (Green) -> Cellular (Red) -> Total (Blue)
			int color = i == 0 ? Color.argb(255, 255, 255, 0) : (i == 1 ? Color.argb(255, 0, 255, 0) : (i == 2 ? Color.argb(255, 255, 0, 0) : Color.argb(255, 0, 0, 255)));
			float[] ring = resultRings.get(i);
			innerRings.add(new Ring(ring[0], ring[1], 30, 1 / (3000 / ring[2]), 1 / (3000 / ring[3]), (int) ring[4], color));
		}
		refreshNeeded = true;
		return this;
	}
	
	@Override
	protected void initScene() {
		getCurrentScene().setBackgroundColor(Color.WHITE);
		createCamera();
		createLevel();
		createCross();
		createPoints();
		createArrows();
		reloadScene();
	}
	
	@Override
	protected void onRender(final long realTime, final double deltaTime) {
		super.onRender(realTime, deltaTime);
		
		try {
			if (levelNeeded) {
				getCurrentScene().removeCamera(getCurrentCamera());
				getCurrentScene().clearChildren();
				squares.clear();
				createCamera();
				createLevel();
				createCross();
				createPoints();
				for (Square square : squares) getCurrentScene().addChild(square);
				levelNeeded = false;
			}
			
			if (refreshNeeded) {
				for (Ring ring : rings) getCurrentScene().removeChild(ring);
				for (Ring innerRing : innerRings) {
					Ring ring = new Ring(innerRing);
					getCurrentScene().addChild(ring);
					rings.add(ring);
				}
				refreshNeeded = false;
			}
			
			if (hideNeeded) {
				for (Square square : squares) getCurrentScene().removeChild(square);
				hideNeeded = false;
			}
			
			if (showNeeded) {
				for (Square square : squares) getCurrentScene().addChild(square);
				showNeeded = false;
			}
			
			if (arrowShowNeeded) {
				getCurrentScene().removeChild(arrows.get(0));
				getCurrentScene().removeChild(arrows.get(1));
				getCurrentScene().removeChild(arrows.get(2));
				getCurrentScene().removeChild(arrows.get(3));
				getCurrentScene().addChild(arrows.get(currentArrow));
				arrowShowNeeded = false;
			}
			
			if (arrowHideNeeded) {
				getCurrentScene().removeChild(arrows.get(0));
				getCurrentScene().removeChild(arrows.get(1));
				getCurrentScene().removeChild(arrows.get(2));
				getCurrentScene().removeChild(arrows.get(3));
				arrowHideNeeded = false;
			}
			
			reloadScene();
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}

		/*((MainActivity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((MainActivity) context).getSupportActionBar().setTitle(String.format("%,.0f", 1 / deltaTime));
			}
		});*/
	}
	
	@Override
	public void onTouchEvent(MotionEvent event) {
		scaleGestureDetector.onTouchEvent(event);
		if (lastTouch >= 2) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastTouchX = event.getX();
					lastTouchY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					if (lastTouchX != 0 && lastTouchY != 0) {
						location.moveRight((lastTouchX - event.getX()) / -500);
						location.moveUp((lastTouchY - event.getY()) / 500);
						currentX = getReversedRealX(location.getX());
						currentY = getRealY(location.getY());
					}
					lastTouchX = event.getX();
					lastTouchY = event.getY();
					break;
				case MotionEvent.ACTION_UP:
					if (currentX <= 0) location.setX(0.5 * location.getScaleX());
					if (currentX >= 3000) location.setX(-0.5 * location.getScaleX());
					if (currentY <= 0) location.setY(-0.5 * location.getScaleX());
					if (currentY >= 3000) location.setY(0.5 * location.getScaleX());
					location.setX(getReversedFakeX(currentX = Utils.roundTo(getReversedRealX(location.getX()), 50)));
					location.setY(getFakeY(currentY = Utils.roundTo(getRealY(location.getY()), 50)));
					CenteredToast.showShortText(context, currentX + " | " + currentY);
					break;
			}
		}
		reloadScene();
		lastTouch++;
	}
	
	@Override
	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) { /* Do Nothing */ }
	
	public void reloadScene() {
		location.setScaleX(scale = scale <= 2.5 ? 2.5 : (scale >= 10 ? 10 : scale)).setScaleY(scale);
		location.setX(getReversedFakeX(currentX));
		location.setY(getFakeY(currentY));
		
		try {
			for (Square square : squares) {
				square.setX(getFakeX(square.getRealX() + (1500 - getReversedRealX(location.getX()))));
				square.setY(getReversedFakeY(square.getRealY() + (1500 - getRealY(location.getY()))));
				square.setScale(scale);
			}
			
			for (Ring ring : rings) {
				ring.setX(getFakeX(ring.getRealX() + (1500 - getReversedRealX(location.getX()))));
				ring.setY(getReversedFakeY(ring.getRealY() + (1500 - getRealY(location.getY()))));
				ring.setScale(scale);
			}
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}
	
	private void createCamera() {
		OrthographicCamera orthographicCamera = new OrthographicCamera();
		orthographicCamera.setPosition(0, 0, 100);
		orthographicCamera.setLookAt(0, 0, 0);
		getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), orthographicCamera);
	}
	
	private void createLevel() {
		Material material = new Material();
		Texture texture = new Texture("LevelTexture", currentLevel.equals("J1NP") ? R.drawable.j1np : (currentLevel.equals("J2NP") ? R.drawable.j2np : (currentLevel.equals("J3NP") ? R.drawable.j3np : R.drawable.j4np)));
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		location = new Cube(1);
		location.setPosition(getReversedFakeX(currentX), getFakeY(currentY), 0);
		location.setScale(2.5, 2.5, 2.5);
		location.setMaterial(material);
		getCurrentScene().addChild(location);
	}
	
	private void createCross() {
		Material material = new Material();
		Texture texture = new Texture("CrossTexture", R.drawable.cross);
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0x00000000);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		Cube cube = new Cube(0.1f);
		cube.setPosition(0, 0, 40);
		cube.setMaterial(material);
		cube.setTransparent(true);
		getCurrentScene().addChild(cube);
	}
	
	private void createPoints() {
		this.sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT level, x, y, COUNT(*) count FROM fingerprint WHERE level = ? GROUP BY level, x, y;", new String[] { currentLevel });
		while (cursor.moveToNext()) {
			Material material = new Material();
			material.setColor(Color.argb(127, cursor.getInt(3) < 20 ? 255 : 0, cursor.getInt(3) >= 10 ? 255 : 0, 0));
			Square square = new Square(cursor.getFloat(cursor.getColumnIndex("x")) - 12.5f, cursor.getFloat(cursor.getColumnIndex("y")) + 12.5f, 10, 0.008333f, Color.argb(127, cursor.getInt(3) < 20 ? 255 : 0, cursor.getInt(3) >= 10 ? 255 : 0, 0));
			square.setMaterial(material);
			square.setPosition(getFakeX(cursor.getFloat(cursor.getColumnIndex("x"))), getReversedFakeY(cursor.getFloat(cursor.getColumnIndex("y"))), 10);
			getCurrentScene().addChild(square);
			squares.add(square);
			
			createSubPoints("WireLess", cursor.getString(cursor.getColumnIndex("level")), cursor.getDouble(cursor.getColumnIndex("x")), cursor.getDouble(cursor.getColumnIndex("y")));
			createSubPoints("BlueTooth", cursor.getString(cursor.getColumnIndex("level")), cursor.getDouble(cursor.getColumnIndex("x")), cursor.getDouble(cursor.getColumnIndex("y")));
			createSubPoints("Cellular", cursor.getString(cursor.getColumnIndex("level")), cursor.getDouble(cursor.getColumnIndex("x")), cursor.getDouble(cursor.getColumnIndex("y")));
		}
		cursor.close();
		sqLiteDatabase.close();
	}
	
	private void createSubPoints(String type, String level, double x, double y) {
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT ABS(AVG(d.rssi)) rssi FROM fingerprint f JOIN " + type.toLowerCase() + " d ON d.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ?;",
				new String[] { level, String.valueOf(x), String.valueOf(y) });
		cursor.moveToFirst();
		float averageStrength = cursor.getFloat(cursor.getColumnIndex("rssi"));
		String averageStrengthString = String.valueOf(averageStrength);
		cursor.close();
		
		Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT ABS(ABS(AVG(d.rssi)) - ?) / (? / 100.0) difference FROM fingerprint f JOIN " + type.toLowerCase() + " d ON d.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY d.fingerprint_id;",
				new String[] { averageStrengthString, averageStrengthString, level, String.valueOf(x), String.valueOf(y) });
		float positive = 0, neutral = 0, negative = 0;
		while (cursorDetail.moveToNext()) {
			float notNeeded = cursorDetail.getFloat(cursorDetail.getColumnIndex("difference")) < Settings.DIFFERENCE_TOLERANCE_LOW ? positive++ : (cursorDetail.getFloat(0) < Settings.DIFFERENCE_TOLERANCE_HIGH ? neutral++ : negative++);
		}
		cursorDetail.close();
		long t3 = System.nanoTime();
		
		if (type.equals("WireLess")) {
			x -= 12.5;
			y -= 12.5;
		}
		if (type.equals("BlueTooth")) {
			x += 12.5;
			y -= 12.5;
		}
		if (type.equals("Cellular")) {
			x += 12.5;
			y += 12.5;
		}
		
		Material material = new Material();
		material.setColor(Color.argb(127, negative != 0 || neutral != 0 ? 255 : 0, negative == 0 ? 255 : 0, 0));
		Square square = new Square((float) x, (float) y, 20, 0.008333f, Color.argb(127, negative != 0 || neutral != 0 ? 255 : 0, negative == 0 ? 255 : 0, 0));
		square.setMaterial(material);
		square.setPosition(getFakeX(x), getReversedFakeY(y), 20);
		getCurrentScene().addChild(square);
		squares.add(square);
	}
	
	private void createArrows() {
		Material material = new Material();
		Texture texture = new Texture("ArrowUpTexture", R.drawable.arrow_down);
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		Cube cube = new Cube(0.2f);
		cube.setPosition(0, 0.5, 50);
		cube.setMaterial(material);
		cube.setTransparent(true);
		arrows.add(cube);
		
		material = new Material();
		texture = new Texture("ArrowRightTexture", R.drawable.arrow_left);
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		cube = new Cube(0.2f);
		cube.setPosition(0, 0.5, 50);
		cube.setMaterial(material);
		cube.setTransparent(true);
		arrows.add(cube);
		
		material = new Material();
		texture = new Texture("ArrowDownTexture", R.drawable.arrow_up);
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		cube = new Cube(0.2f);
		cube.setPosition(0, 0.5, 50);
		cube.setMaterial(material);
		cube.setTransparent(true);
		arrows.add(cube);
		
		material = new Material();
		texture = new Texture("ArrowLeftTexture", R.drawable.arrow_right);
		texture.shouldRecycle(true);
		try {
			material.addTexture(texture);
			material.setColor(0);
		} catch (ATexture.TextureException e) {
			e.printStackTrace();
		}
		
		cube = new Cube(0.2f);
		cube.setPosition(0, 0.5, 50);
		cube.setMaterial(material);
		cube.setTransparent(true);
		arrows.add(cube);
	}
	
	public void hidePoints() {
		hideNeeded = true;
	}
	
	public void showPoints() {
		showNeeded = true;
	}
	
	private double getFakeX(double realX) {
		return (realX / 3000 - 0.5) * location.getScaleX();
	}
	
	private double getReversedFakeX(double realX) {
		return (realX / 3000 - 0.5) * -location.getScaleX();
	}
	
	private double getFakeY(double realY) {
		return (realY / 3000 - 0.5) * location.getScaleY();
	}
	
	private double getReversedFakeY(double realY) {
		return (realY / 3000 - 0.5) * -location.getScaleY();
	}
	
	private double getRealX(double fakeX) {
		return (fakeX / location.getScaleX() + 0.5) * 3000;
	}
	
	private double getReversedRealX(double fakeX) {
		return (fakeX / -location.getScaleX() + 0.5) * 3000;
	}
	
	private double getRealY(double fakeY) {
		return (fakeY / location.getScaleY() + 0.5) * 3000;
	}
	
	private double getReversedRealY(double fakeY) {
		return (fakeY / -location.getScaleY() + 0.5) * 3000;
	}
	
	private class Square extends Cube {
		private float realX, realY, innerSize;
		
		public Square(float realX, float realY, float fakeZ, float size, int color) {
			super(size);
			this.realX = realX;
			this.realY = realY;
			this.innerSize = size;
			Material material = new Material();
			material.setColor(color);
			setMaterial(material);
			setTransparent(true);
			setPosition(0, 0, fakeZ);
		}
		
		public Square(Square square) {
			super(square.getInnerSize());
			this.realX = square.realX;
			this.realY = square.realY;
			setMaterial(square.getMaterial());
			setTransparent(true);
			setPosition(getFakeX(square.realX), getReversedFakeY(square.realY), square.getZ());
		}
		
		public float getRealX() {
			return realX;
		}
		
		public float getRealY() {
			return realY;
		}
		
		public float getInnerSize() {
			return innerSize;
		}
	}
	
	private class Ring extends Torus {
		private float realX, realY, innerRadius, outerRadius, weight;
		
		public Ring(float realX, float realY, float fakeZ, float innerRadius, float outerRadius, int weight, int color) {
			super(outerRadius - (outerRadius - innerRadius) / 2, (outerRadius - innerRadius) / 2, 8, 8);
			this.realX = realX;
			this.realY = realY;
			this.innerRadius = (outerRadius - innerRadius) / 2;
			this.outerRadius = outerRadius - (outerRadius - innerRadius) / 2;
			this.weight = weight;
			Material material = new Material();
			material.setColor(color);
			setMaterial(material);
			setTransparent(true);
			setPosition(0, 0, fakeZ);
		}
		
		public Ring(Ring ring) {
			super(ring.getOuterRadius(), ring.getInnerRadius(), 128, 128);
			this.realX = ring.realX;
			this.realY = ring.realY;
			this.innerRadius = ring.innerRadius;
			this.outerRadius = ring.outerRadius;
			this.weight = ring.weight;
			setMaterial(ring.getMaterial());
			setTransparent(true);
			setPosition(getFakeX(ring.realX), getReversedFakeY(ring.realY), ring.getZ());
		}
		
		public float getRealX() {
			return realX;
		}
		
		public float getRealY() {
			return realY;
		}
		
		public float getInnerRadius() {
			return innerRadius;
		}
		
		public float getOuterRadius() {
			return outerRadius;
		}
		
		public float getWeight() {
			return weight;
		}
	}
}
