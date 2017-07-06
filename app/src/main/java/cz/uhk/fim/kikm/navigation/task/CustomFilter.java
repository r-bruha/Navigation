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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.MainActivity;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.model.Location;
import cz.uhk.fim.kikm.navigation.util.CenteredToast;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class CustomFilter extends AsyncTask<Void, Void, Void> {
	private Context context;
	private Fingerprint fingerprint;
	private Location location;
	private List<float[]> wirelessLocations = new ArrayList<>(), bluetoothLocations = new ArrayList<>(), cellularLocations = new ArrayList<>();
	private int algorithm, innerAlgorithm, time, levelCount, levelSummary;
	
	public CustomFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int innerAlgorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = innerAlgorithm;
		this.time = time;
	}
	
	public CustomFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = Settings.SEARCH_ALGORITHM_CUSTOM;
		this.time = time;
	}
	
	private void calculateWireless() {
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor c = sqLiteDatabase.rawQuery("SELECT AVG(CASE level WHEN 'J1NP' THEN 1 WHEN 'J2NP' THEN 2 WHEN 'J3NP' THEN 3 ELSE 4 END) averageLevel, level, x, y, 1 / SUM(ABS(IFNULL(c.rssi, 1) - t.rssi) / IFNULL(c.rssi, 1)) coefficient FROM (" +
				"(SELECT level, x, y, bssid, AVG(ABS(rssi)) rssi FROM fingerprint f LEFT JOIN wireless w ON w.fingerprint_id = f.id GROUP BY level, x, y, bssid) t LEFT JOIN " +
				"(SELECT bssid, AVG(ABS(rssi)) rssi FROM currentWireless WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) GROUP BY bssid) c ON t.bssid = c.bssid" +
				") GROUP BY level, x, y ORDER BY coefficient DESC LIMIT 3;", new String[] {});
		if (c.getCount() == 0) {
			c.close();
			sqLiteDatabase.close();
			return;
		}
		c.moveToFirst();
		
		float level = c.getFloat(c.getColumnIndex("averageLevel")), x = c.getFloat(c.getColumnIndex("x")), y = c.getFloat(c.getColumnIndex("y")), maximum = c.getFloat(c.getColumnIndex("coefficient")), coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
		Log.i("CurWir", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
		wirelessLocations.add(new float[] { x, y, 0, 50, coefficient });
		levelCount++;
		levelSummary += level;
		while (c.moveToNext()) {
			level = c.getFloat(c.getColumnIndex("averageLevel"));
			x = c.getFloat(c.getColumnIndex("x"));
			y = c.getFloat(c.getColumnIndex("y"));
			coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
			Log.i("CurWir", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
			wirelessLocations.add(new float[] { x, y, 0, 50, coefficient });
			levelCount++;
			levelSummary += level;
		}
		c.close();
		sqLiteDatabase.close();
	}
	
	private void calculateBluetooth() {
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor c = sqLiteDatabase.rawQuery("SELECT AVG(CASE level WHEN 'J1NP' THEN 1 WHEN 'J2NP' THEN 2 WHEN 'J3NP' THEN 3 ELSE 4 END) averageLevel, level, x, y, 1 / SUM(ABS(IFNULL(c.rssi, 1) - t.rssi) / IFNULL(c.rssi, 1)) coefficient FROM (" +
				"(SELECT level, x, y, bssid, AVG(ABS(rssi)) rssi FROM fingerprint f LEFT JOIN bluetooth w ON w.fingerprint_id = f.id GROUP BY level, x, y, bssid) t LEFT JOIN " +
				"(SELECT bssid, AVG(ABS(rssi)) rssi FROM currentBluetooth WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) GROUP BY bssid) c ON t.bssid = c.bssid" +
				") GROUP BY level, x, y ORDER BY coefficient DESC LIMIT 3;", new String[] {});
		if (c.getCount() == 0) {
			c.close();
			sqLiteDatabase.close();
			return;
		}
		c.moveToFirst();
		
		float level = c.getFloat(c.getColumnIndex("averageLevel")), x = c.getFloat(c.getColumnIndex("x")), y = c.getFloat(c.getColumnIndex("y")), maximum = c.getFloat(c.getColumnIndex("coefficient")), coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
		Log.i("CurBlu", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
		bluetoothLocations.add(new float[] { x, y, 0, 50, coefficient });
		levelCount++;
		levelSummary += level;
		while (c.moveToNext()) {
			level = c.getFloat(c.getColumnIndex("averageLevel"));
			x = c.getFloat(c.getColumnIndex("x"));
			y = c.getFloat(c.getColumnIndex("y"));
			coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
			Log.i("CurBlu", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
			bluetoothLocations.add(new float[] { x, y, 0, 50, coefficient });
			levelCount++;
			levelSummary += level;
		}
		c.close();
		sqLiteDatabase.close();
	}
	
	private void calculateCellular() {
		SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor c = sqLiteDatabase.rawQuery("SELECT AVG(CASE level WHEN 'J1NP' THEN 1 WHEN 'J2NP' THEN 2 WHEN 'J3NP' THEN 3 ELSE 4 END) averageLevel, level, x, y, 1 / SUM(ABS(IFNULL(c.rssi, 1) - t.rssi) / IFNULL(c.rssi, 1)) coefficient FROM (" +
				"(SELECT level, x, y, cid, AVG(ABS(rssi)) rssi FROM fingerprint f LEFT JOIN cellular w ON w.fingerprint_id = f.id GROUP BY level, x, y, cid) t LEFT JOIN " +
				"(SELECT cid, AVG(ABS(rssi)) rssi FROM currentCellular WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) GROUP BY cid) c ON t.cid = c.cid" +
				") GROUP BY level, x, y ORDER BY coefficient DESC LIMIT 3;", new String[] {});
		if (c.getCount() == 0) {
			c.close();
			sqLiteDatabase.close();
			return;
		}
		c.moveToFirst();
		
		float level = c.getFloat(c.getColumnIndex("averageLevel")), x = c.getFloat(c.getColumnIndex("x")), y = c.getFloat(c.getColumnIndex("y")), maximum = c.getFloat(c.getColumnIndex("coefficient")), coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
		Log.i("CurCel", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
		cellularLocations.add(new float[] { x, y, 0, 50, coefficient });
		levelCount++;
		levelSummary += level;
		while (c.moveToNext()) {
			level = c.getFloat(c.getColumnIndex("averageLevel"));
			x = c.getFloat(c.getColumnIndex("x"));
			y = c.getFloat(c.getColumnIndex("y"));
			coefficient = c.getFloat(c.getColumnIndex("coefficient")) / maximum;
			Log.i("CurCel", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", level, x, y, coefficient));
			cellularLocations.add(new float[] { x, y, 0, 50, coefficient });
			levelCount++;
			levelSummary += level;
		}
		c.close();
		sqLiteDatabase.close();
	}
	
	@Override
	protected Void doInBackground(Void... voids) {
		Log.i("CurDes", "    F |    X |     Y |  Coeff ");
		switch (algorithm) {
			case Settings.SEARCH_ALGORITHM_TOGETHER:
				switch (innerAlgorithm) {
					case Settings.SEARCH_ALGORITHM_CUSTOM:
						calculateWireless();
						calculateBluetooth();
						calculateCellular();
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
						calculateWireless();
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
						calculateBluetooth();
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
						calculateCellular();
						break;
				}
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM:
			case Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER:
				calculateWireless();
				calculateBluetooth();
				calculateCellular();
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
				calculateWireless();
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
				calculateBluetooth();
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
				calculateCellular();
				break;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		List<float[]> locations = new ArrayList<>(), result = new ArrayList<>();
		float fAverage = 0, xSummary = 0, ySummary = 0, zSummary = 0, xAverage = 0, yAverage = 0;
		switch (algorithm) {
			case Settings.SEARCH_ALGORITHM_TOGETHER:
				switch (innerAlgorithm) {
					case Settings.SEARCH_ALGORITHM_CUSTOM:
						locations.addAll(wirelessLocations);
						locations.addAll(bluetoothLocations);
						locations.addAll(cellularLocations);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
						locations.addAll(wirelessLocations);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
						locations.addAll(bluetoothLocations);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
						locations.addAll(cellularLocations);
						break;
				}
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM:
			case Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER:
				locations.addAll(wirelessLocations);
				locations.addAll(bluetoothLocations);
				locations.addAll(cellularLocations);
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
				locations.addAll(wirelessLocations);
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
				locations.addAll(bluetoothLocations);
				break;
			case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
				locations.addAll(cellularLocations);
				break;
		}
		
		Comparator<float[]> comparator = new Comparator<float[]>() {
			@Override
			public int compare(float[] float1, float[] float2) {
				return float1[4] == float2[4] ? 0 : (float1[4] < float2[4] ? 1 : -1);
			}
		};
		Collections.sort(locations, comparator);
		Collections.sort(wirelessLocations, comparator);
		Collections.sort(bluetoothLocations, comparator);
		Collections.sort(cellularLocations, comparator);
		
		if (algorithm == Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER) {
			Log.i("CntTot", String.format("%,2d | %,2d | %,2d", wirelessLocations.size(), bluetoothLocations.size(), cellularLocations.size()));
			
			for (float[] location : wirelessLocations) {
				xSummary += location[0] * location[4];
				ySummary += location[1] * location[4];
				zSummary += location[4];
			}
			xAverage = xSummary / zSummary;
			yAverage = ySummary / zSummary;
			result.add(new float[] { xAverage, yAverage, 0, 25, 0 });
			Log.i("AvgWir", String.format("%,5.0f | %,5.0f | %,6.2f", xAverage, yAverage, zSummary / wirelessLocations.size()));
			
			xSummary = ySummary = zSummary = 0;
			for (float[] location : bluetoothLocations) {
				xSummary += location[0] * location[4];
				ySummary += location[1] * location[4];
				zSummary += location[4];
			}
			xAverage = xSummary / zSummary;
			yAverage = ySummary / zSummary;
			result.add(new float[] { xAverage, yAverage, 0, 25, 0 });
			Log.i("AvgBlu", String.format("%,5.0f | %,5.0f | %,6.2f", xAverage, yAverage, zSummary / bluetoothLocations.size()));
			
			xSummary = ySummary = zSummary = 0;
			for (float[] location : cellularLocations) {
				xSummary += location[0] * location[4];
				ySummary += location[1] * location[4];
				zSummary += location[4];
			}
			xAverage = xSummary / zSummary;
			yAverage = ySummary / zSummary;
			result.add(new float[] { xAverage, yAverage, 0, 25, 0 });
			Log.i("AvgCel", String.format("%,5.0f | %,5.0f | %,6.2f", xAverage, yAverage, zSummary / cellularLocations.size()));
			
			xSummary = ySummary = zSummary = 0;
			for (float[] location : locations) {
				xSummary += location[0] * location[4];
				ySummary += location[1] * location[4];
				zSummary += location[4];
			}
			fAverage = Math.round(levelSummary / (float) levelCount);
			xAverage = xSummary / zSummary;
			yAverage = ySummary / zSummary;
			result.add(new float[] { xAverage, yAverage, 0, 25, 0 });
			Log.i("AvgTot", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", 0.0, xAverage, yAverage, zSummary / locations.size()));
			((MainActivity) context).getRenderer()
					.setRings(algorithm != Settings.SEARCH_ALGORITHM_TOGETHER ? locations : new ArrayList<float[]>(), algorithm != Settings.SEARCH_ALGORITHM_TOGETHER ? result : new ArrayList<float[]>())
					.setCurrentLevel(fAverage == 1 ? "J1NP" : (fAverage == 2 ? "J2NP" : (fAverage == 3 ? "J3NP" : "J4NP"))).setCurrentX(xAverage).setCurrentY(yAverage).reloadScene();
		} else {
			for (float[] location : locations) {
				xSummary += location[0] * location[4];
				ySummary += location[1] * location[4];
				zSummary += location[4];
			}
			fAverage = Math.round(levelSummary / (float) levelCount);
			xAverage = xSummary / zSummary;
			yAverage = ySummary / zSummary;
			Log.i("AvgTot", String.format("%,5.0f | %,5.0f | %,5.0f | %,6.2f", 0.0, xAverage, yAverage, zSummary / locations.size()));
			((MainActivity) context).getRenderer()
					.setRings(algorithm != Settings.SEARCH_ALGORITHM_TOGETHER ? locations : new ArrayList<float[]>())
					.setCurrentLevel(fAverage == 1 ? "J1NP" : (fAverage == 2 ? "J2NP" : (fAverage == 3 ? "J3NP" : "J4NP"))).setCurrentX(xAverage).setCurrentY(yAverage).reloadScene();
		}
		
		Location location = new Location(fAverage == 1 ? "J1NP" : (fAverage == 2 ? "J2NP" : (fAverage == 3 ? "J3NP" : "J4NP")), (int) xAverage, (int) yAverage);
		double difference = Math.sqrt(Math.pow(location.getX() - this.location.getX(), 2) + Math.pow(location.getY() - this.location.getY(), 2)) / 50;
		CenteredToast.showLongText(context, String.format(context.getString(R.string.taskBaseFilterShowMessage), location.getX(), location.getY(), difference));
		try {
			new File(Environment.getExternalStorageDirectory() + "/Navigation").mkdirs();
			FileWriter fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory() + "/Navigation", "Search.tsv"), true);
			String algorithm = "";
			switch (this.algorithm) {
				case Settings.SEARCH_ALGORITHM_TOGETHER:
					switch (innerAlgorithm) {
						case Settings.SEARCH_ALGORITHM_CUSTOM:
							algorithm = "Custom Filter";
							break;
						case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
							algorithm = "Custom Filter WireLess";
							break;
						case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
							algorithm = "Custom Filter BlueTooth";
							break;
						case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
							algorithm = "Custom Filter Cellular";
							break;
					}
					break;
				case Settings.SEARCH_ALGORITHM_CUSTOM:
				case Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER:
					algorithm = "Custom Filter";
					break;
				case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
					algorithm = "Custom Filter WireLess";
					break;
				case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
					algorithm = "Custom Filter BlueTooth";
					break;
				case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
					algorithm = "Custom Filter Cellular";
					break;
			}
			fileWriter.append(String.format(context.getString(R.string.taskBaseFilterShowLog),
					new SimpleDateFormat("dd. MM. yyyy HH:mm:ss").format(new Date()), algorithm, time / 1000, this.location.getFloor(), this.location.getX(), this.location.getY(), location.getFloor(), location.getX(), location.getY(), difference, new GsonBuilder().create().toJson(fingerprint)));
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}