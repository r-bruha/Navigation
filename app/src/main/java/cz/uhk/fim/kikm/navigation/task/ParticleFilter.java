package cz.uhk.fim.kikm.navigation.task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.MainActivity;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.model.Location;
import cz.uhk.fim.kikm.navigation.util.CenteredToast;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class ParticleFilter extends AsyncTask<Void, Void, Void> {
	private Context context;
	private Fingerprint fingerprint;
	private Location location;
	private int algorithm, innerAlgorithm, time, levelCount, levelSummary;
	private String tableName = "particle";
	private List<float[]> locations = new ArrayList<>();
	
	public ParticleFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = Settings.SEARCH_ALGORITHM_PARTICLES;
		this.time = time;
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		if (algorithm == Settings.SEARCH_ALGORITHM_TOGETHER) {
			tableName += new Random().nextInt(1000000);
			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY, x INTEGER, y INTEGER, z REAL);");
		}
		
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
		cursor.moveToFirst();
		if (cursor.getInt(cursor.getColumnIndex("count")) == 0) {
			sqLiteDatabase.beginTransaction();
			for (int x = 0; x <= 3000; x += 50) for (int y = 0; y <= 3000; y += 50) sqLiteDatabase.execSQL("INSERT INTO " + tableName + " (x, y, z) VALUES (?, ?, ?);", new Object[] { x, y, 0 });
			sqLiteDatabase.setTransactionSuccessful();
			sqLiteDatabase.endTransaction();
		}
		cursor.close();
		sqLiteDatabase.close();
	}
	
	public ParticleFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int innerAlgorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = innerAlgorithm;
		this.time = time;
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		if (algorithm == Settings.SEARCH_ALGORITHM_TOGETHER) {
			tableName += new Random().nextInt(1000000);
			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY, x INTEGER, y INTEGER, z REAL);");
		}
		
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
		cursor.moveToFirst();
		if (cursor.getInt(cursor.getColumnIndex("count")) == 0) {
			sqLiteDatabase.beginTransaction();
			for (int x = 0; x <= 3000; x += 50) for (int y = 0; y <= 3000; y += 50) sqLiteDatabase.execSQL("INSERT INTO " + tableName + " (x, y, z) VALUES (?, ?, ?);", new Object[] { x, y, 0 });
			sqLiteDatabase.setTransactionSuccessful();
			sqLiteDatabase.endTransaction();
		}
		cursor.close();
		sqLiteDatabase.close();
	}
	
	private void calculateWireless() {
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT bssid, ABS(AVG(rssi)) rssi FROM currentWireless WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) GROUP BY bssid ORDER BY rssi LIMIT 5;", null);
		while (cursor.moveToNext()) {
			Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT AVG(l.level) level, AVG(l.x) x, AVG(l.y) y, 1.0 / (MAX(ABS(ABS(rssi) - CAST(CAST(? AS REAL) AS INTEGER))) + 1) coefficient, " +
					"MIN((f.x - l.x) * (f.x - l.x) + (f.y - l.y) * (f.y - l.y)) minDistance, MAX((f.x - l.x) * (f.x - l.x) + (f.y - l.y) * (f.y - l.y)) maxDistance FROM fingerprint f " +
					"JOIN wireless w ON w.fingerprint_id = f.id JOIN location l ON w.bssid = l.bssid WHERE w.bssid = ? " +
					"GROUP BY ABS(ABS(rssi) - CAST(CAST(? AS REAL) AS INTEGER)) ORDER BY coefficient DESC LIMIT 3;", new String[] {
					cursor.getString(cursor.getColumnIndex("rssi")), cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("rssi")) });
			while (cursorDetail.moveToNext()) {
				float minimum = (float) Math.sqrt(cursorDetail.getFloat(cursorDetail.getColumnIndex("minDistance")));
				float maximum = (float) Math.sqrt(cursorDetail.getFloat(cursorDetail.getColumnIndex("maxDistance")));
				float level = cursorDetail.getFloat(cursorDetail.getColumnIndex("level")), x = cursorDetail.getFloat(cursorDetail.getColumnIndex("x")), y = cursorDetail.getFloat(cursorDetail.getColumnIndex("y")), coefficient = cursorDetail.getFloat(cursorDetail.getColumnIndex("coefficient"));
				locations.add(new float[] { x, y, minimum, maximum, coefficient });
				levelCount++;
				levelSummary += level;
				Log.i("CurWir", String.format("%,5.0f | %,5.0f | %,5.0f | %,8.2f | %,8.2f | %,6.2f", level, x, y, minimum, maximum, coefficient));
				
				String stringX = String.valueOf(x), stringY = String.valueOf(y);
				sqLiteDatabase.execSQL("UPDATE " + tableName + " SET z = z + CAST(? AS REAL) WHERE (x - ?) * (x - ?) + (y - ?) * (y - ?) >= CAST(? AS REAL) AND (x - ?) * (x - ?) + (y - ?) * (y - ?) <= CAST(? AS REAL);", new String[] {
						String.valueOf(coefficient),
						stringX, stringX, stringY, stringY, String.valueOf(minimum * minimum),
						stringX, stringX, stringY, stringY, String.valueOf(maximum * maximum)
				});
				
			}
			cursorDetail.close();
		}
		cursor.close();
		sqLiteDatabase.close();
	}
	
	private void calculateBluetooth() {
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT bssid, ABS(AVG(rssi)) rssi FROM currentBluetooth WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) GROUP BY bssid ORDER BY rssi LIMIT 5;", null);
		while (cursor.moveToNext()) {
			Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT AVG(l.level) level, AVG(l.x) x, AVG(l.y) y, 1.0 / (MAX(ABS(ABS(rssi) - CAST(CAST(? AS REAL) AS INTEGER))) + 1) coefficient, " +
					"MIN((f.x - l.x) * (f.x - l.x) + (f.y - l.y) * (f.y - l.y)) minDistance, MAX((f.x - l.x) * (f.x - l.x) + (f.y - l.y) * (f.y - l.y)) maxDistance FROM fingerprint f " +
					"JOIN bluetooth b ON b.fingerprint_id = f.id JOIN location l ON b.bssid = l.bssid WHERE b.bssid = ? " +
					"GROUP BY ABS(ABS(rssi) - CAST(CAST(? AS REAL) AS INTEGER)) ORDER BY coefficient DESC LIMIT 3;", new String[] {
					cursor.getString(cursor.getColumnIndex("rssi")), cursor.getString(cursor.getColumnIndex("bssid")), cursor.getString(cursor.getColumnIndex("rssi")) });
			while (cursorDetail.moveToNext()) {
				float minimum = (float) Math.sqrt(cursorDetail.getFloat(cursorDetail.getColumnIndex("minDistance")));
				float maximum = (float) Math.sqrt(cursorDetail.getFloat(cursorDetail.getColumnIndex("maxDistance")));
				float level = cursorDetail.getFloat(cursorDetail.getColumnIndex("level")), x = cursorDetail.getFloat(cursorDetail.getColumnIndex("x")), y = cursorDetail.getFloat(cursorDetail.getColumnIndex("y")), coefficient = cursorDetail.getFloat(cursorDetail.getColumnIndex("coefficient"));
				locations.add(new float[] { x, y, minimum, maximum, coefficient });
				levelCount++;
				levelSummary += level;
				Log.i("CurBlu", String.format("%,5.0f | %,5.0f | %,5.0f | %,8.2f | %,8.2f | %,6.2f", level, x, y, minimum, maximum, coefficient));
				
				String stringX = String.valueOf(x), stringY = String.valueOf(y);
				sqLiteDatabase.execSQL("UPDATE " + tableName + " SET z = z + CAST(? AS REAL) WHERE (x - ?) * (x - ?) + (y - ?) * (y - ?) >= CAST(? AS REAL) AND (x - ?) * (x - ?) + (y - ?) * (y - ?) <= CAST(? AS REAL);", new String[] {
						String.valueOf(coefficient),
						stringX, stringX, stringY, stringY, String.valueOf(minimum * minimum),
						stringX, stringX, stringY, stringY, String.valueOf(maximum * maximum)
				});
				
			}
			cursorDetail.close();
		}
		cursor.close();
		sqLiteDatabase.close();
	}
	
	@Override
	protected Void doInBackground(Void... voids) {
		Log.i("CurDes", String.format("    F |    X |     Y |  Minimum |  Maximum |  Coeff "));
		switch (algorithm) {
			case Settings.SEARCH_ALGORITHM_TOGETHER:
				switch (innerAlgorithm) {
					case Settings.SEARCH_ALGORITHM_PARTICLES:
						calculateWireless();
						calculateWireless();
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS:
						calculateWireless();
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH:
						calculateBluetooth();
						break;
				}
				break;
			case Settings.SEARCH_ALGORITHM_PARTICLES:
			case Settings.SEARCH_ALGORITHM_PARTICLES_TOGETHER:
				calculateWireless();
				calculateBluetooth();
				break;
			case Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS:
				calculateWireless();
				break;
			case Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH:
				calculateBluetooth();
				break;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
		cursor.moveToFirst();
		int totalParticles = cursor.getInt(cursor.getColumnIndex("count"));
		cursor.close();
		
		sqLiteDatabase.execSQL("DELETE FROM " + tableName + " WHERE z != (SELECT MAX(z) FROM " + tableName + ");");
		
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
		cursor.moveToFirst();
		int goodParticles = cursor.getInt(cursor.getColumnIndex("count"));
		cursor.close();
		
		cursor = sqLiteDatabase.rawQuery("SELECT SUM(x * z) / SUM(z) x, SUM (y * z) / SUM(z) y, AVG(z) z FROM " + tableName + ";", null);
		cursor.moveToFirst();
		int averageLocation = Math.round(levelSummary / (float) levelCount);
		Location location = new Location(averageLocation == 1 ? "J1NP" : (averageLocation == 2 ? "J2NP" : (averageLocation == 3 ? "J3NP" : "J4NP")), cursor.getInt(cursor.getColumnIndex("x")), cursor.getInt(cursor.getColumnIndex("y")));
		Log.i("AvgTot", String.format("%,5.2f | %,5d | %,5d | %,5.2f", levelSummary / (float) levelCount, location.getX(), location.getY(), cursor.getFloat(cursor.getColumnIndex("z"))));
		cursor.close();
		
		while (true) {
			cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
			cursor.moveToFirst();
			if (cursor.getInt(cursor.getColumnIndex("count")) >= Settings.MAXIMUM_PARTICLES) {
				cursor.close();
				break;
			} else cursor.close();
			sqLiteDatabase.execSQL("INSERT INTO " + tableName + " (x, y, z) SELECT x + RANDOM() % 50, y + RANDOM() % 50, 0 FROM " + tableName + " ORDER BY id LIMIT ?;", new Object[] {
					goodParticles * 2 < Settings.MAXIMUM_PARTICLES ? goodParticles : Settings.MAXIMUM_PARTICLES - goodParticles
			});
		}
		
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) count FROM " + tableName + ";", null);
		cursor.moveToFirst();
		int newParticles = cursor.getInt(cursor.getColumnIndex("count")) - goodParticles;
		cursor.close();
		
		sqLiteDatabase.execSQL("UPDATE " + tableName + " SET z = 0;");
		
		double difference = Math.sqrt(Math.pow(location.getX() - this.location.getX(), 2) + Math.pow(location.getY() - this.location.getY(), 2)) / 50;
		CenteredToast.showLongText(context, String.format(context.getString(R.string.taskParticleFilterShowMessage), location.getX(), location.getY(), difference, totalParticles, goodParticles, newParticles));
		try {
			new File(Environment.getExternalStorageDirectory() + "/Navigation").mkdirs();
			FileWriter fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory() + "/Navigation", "Search.tsv"), true);
			String algorithm = "";
			switch (this.algorithm) {
				case Settings.SEARCH_ALGORITHM_TOGETHER:
					switch (innerAlgorithm) {
						case Settings.SEARCH_ALGORITHM_PARTICLES:
							algorithm = "Particle Filter";
							break;
						case Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS:
							algorithm = "Particle Filter WireLess";
							break;
						case Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH:
							algorithm = "Particle Filter BlueTooth";
							break;
					}
					break;
				case Settings.SEARCH_ALGORITHM_PARTICLES:
					algorithm = "Particle Filter";
					break;
				case Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS:
					algorithm = "Particle Filter WireLess";
					break;
				case Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH:
					algorithm = "Particle Filter BlueTooth";
					break;
				case Settings.SEARCH_ALGORITHM_PARTICLES_CELLULAR:
					algorithm = "Particle Filter Cellular";
					break;
			}
			fileWriter.append(String.format(context.getString(R.string.taskBaseFilterShowLog),
					new SimpleDateFormat("dd. MM. yyyy HH:mm:ss").format(new Date()), algorithm, time / 1000, this.location.getFloor(), this.location.getX(), this.location.getY(), location.getFloor(), location.getX(), location.getY(), difference, new GsonBuilder().create().toJson(fingerprint)));
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (algorithm == Settings.SEARCH_ALGORITHM_TOGETHER) sqLiteDatabase.rawQuery("DROP TABLE " + tableName + ";", null);
		((MainActivity) context).getRenderer()
				.setRings(algorithm != Settings.SEARCH_ALGORITHM_TOGETHER ? locations : new ArrayList<float[]>())
				.setCurrentLevel(location.getFloor()).setCurrentX(location.getX()).setCurrentY(location.getY()).reloadScene();
		sqLiteDatabase.close();
	}
}