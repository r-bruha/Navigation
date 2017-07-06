package cz.uhk.fim.kikm.navigation.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.EstimoteSDK;

import org.rajawali3d.view.SurfaceView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.adapter.AlgorithmsAdapter;
import cz.uhk.fim.kikm.navigation.model.Location;
import cz.uhk.fim.kikm.navigation.task.Collector;
import cz.uhk.fim.kikm.navigation.task.CouchDatabase;
import cz.uhk.fim.kikm.navigation.task.CustomFilter;
import cz.uhk.fim.kikm.navigation.task.NeighborFilter;
import cz.uhk.fim.kikm.navigation.task.ParticleFilter;
import cz.uhk.fim.kikm.navigation.task.Renderer;
import cz.uhk.fim.kikm.navigation.util.ExceptionHandler;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class MainActivity extends AppCompatActivity {
	private SharedPreferences sharedPreferences;
	private Collector collector;
	private Renderer renderer;
	private ScrollView scrollView;
	private Location collectionLocation;
	private boolean isCollectionRunning = false;
	private int collectionCounter = 1, collectionTarget = 1;
	
	public MainActivity setCollectionRunning(boolean collectionRunning) {
		isCollectionRunning = collectionRunning;
		return this;
	}
	
	public int getCollectionCounter() {
		return collectionCounter;
	}
	
	public int getCollectionTarget() {
		return collectionTarget;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		
		SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS fingerprint (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), finish TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), level TEXT, x INTEGER, y INTEGER, user TEXT);");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_fingerprint ON fingerprint (level, x, y);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS wireless (id INTEGER PRIMARY KEY, fingerprint_id INTEGER, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), ssid TEXT, bssid TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER, frequency INTEGER, FOREIGN KEY (fingerprint_id) REFERENCES fingerprint (id));");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_wireless ON wireless (fingerprint_id, bssid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS bluetooth (id INTEGER PRIMARY KEY, fingerprint_id INTEGER, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), bssid TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER, FOREIGN KEY (fingerprint_id) REFERENCES fingerprint (id));");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_bluetooth ON bluetooth (fingerprint_id, bssid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS cellular (id INTEGER PRIMARY KEY, fingerprint_id INTEGER, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), cid TEXT, lac TEXT, psc TEXT, type TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER, FOREIGN KEY (fingerprint_id) REFERENCES fingerprint (id));");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_cellular ON cellular (fingerprint_id, cid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sensor (id INTEGER PRIMARY KEY, fingerprint_id INTEGER, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), type INTEGER, x REAL, y REAL, z REAL, time INTEGER, difference INTEGER, FOREIGN KEY (fingerprint_id) REFERENCES fingerprint (id));");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_sensor ON sensor (fingerprint_id, type, x, y, z);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS device (id INTEGER PRIMARY KEY, fingerprint_id INTEGER, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), did TEXT, board TEXT, bootloader TEXT, brand TEXT, device TEXT, display TEXT, fingerprint TEXT, hardware TEXT, host TEXT, manufacturer TEXT, model TEXT, product TEXT, serial TEXT, tags TEXT, telephone TEXT, type TEXT, user TEXT, os TEXT, api INTEGER, FOREIGN KEY (fingerprint_id) REFERENCES fingerprint (id));");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_device ON device (fingerprint_id);");
		
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentFingerprint (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), finish TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), level TEXT, x INTEGER, y INTEGER, user TEXT);");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_currentFingerprint ON currentFingerprint (level, x, y);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentWireless (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), ssid TEXT, bssid TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER, frequency INTEGER);");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_currentWireless ON currentWireless (bssid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentBluetooth (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), bssid TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER);");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_currentBluetooth ON currentBluetooth (bssid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentCellular (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), cid TEXT, lac TEXT, psc TEXT, type TEXT, rssi INTEGER, distance REAL, time INTEGER, difference INTEGER);");
		sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_currentCellular ON currentCellular (cid, rssi, distance);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentSensor (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), type INTEGER, x REAL, y REAL, z REAL, time INTEGER, difference INTEGER);");
		// sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_currentSensor ON sensor (type, x, y, z);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS currentDevice (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), did TEXT, board TEXT, bootloader TEXT, brand TEXT, device TEXT, display TEXT, fingerprint TEXT, hardware TEXT, host TEXT, manufacturer TEXT, model TEXT, product TEXT, serial TEXT, tags TEXT, telephone TEXT, type TEXT, user TEXT, os, api INTEGER);");
		
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS particle (id INTEGER PRIMARY KEY, x INTEGER, y INTEGER, z REAL);");
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS location (id INTEGER PRIMARY KEY, bssid TEXT, level INTEGER, x INTEGER, y INTEGER);");
		sqLiteDatabase.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS UK_location ON location (bssid);");
		
		sqLiteDatabase.execSQL("DELETE FROM location;");
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "CE:FD:39:A0:CF:D5", "3", "2150", "2225" }); // 01
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "D3:37:BE:D1:6A:85", "3", "2150", "1800" }); // 02
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "D2:6E:DB:29:FF:AA", "3", "2150", "1300" }); // 03
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "CA:91:06:B0:83:A7", "3", "2175", "725" }); // 04
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "FD:1D:71:EC:33:60", "3", "1700", "750" }); // 05
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "D1:98:1D:B8:77:29", "3", "1275", "750" }); // 06
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "FB:A8:E3:75:67:AE", "3", "800", "750" }); // 07
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "C1:C2:C2:30:7E:37", "3", "800", "1300" }); // 08
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "DA:06:AB:63:78:C5", "3", "775", "1800" }); // 09
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "DF:55:9F:F1:B4:FF", "3", "825", "2225" }); // 10
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "C2:95:26:4E:65:D4", "3", "1150", "2400" }); // 11
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "E2:B0:39:C5:EB:44", "3", "1825", "2400" }); // 12
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "D1:3B:27:8C:AB:F1", "3", "1400", "2600" }); // 13
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "C5:D5:21:51:D7:90", "3", "1825", "300" }); // 14
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "F5:B8:49:90:E9:F7", "3", "1575", "300" }); // 15
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "ED:22:71:95:91:D3", "3", "1300", "300" }); // 16
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "EF:F8:A4:20:26:8C", "3", "575", "625" }); // 17
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "F7:08:6B:05:E3:69", "3", "1575", "300" }); // ??
		
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:A0", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:AF", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:50", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:5F", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:80", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:21:A0:F9:54:C0", "3", "2150", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:A6:30", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D2:D0", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:6F", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:70", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:7F", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:8F", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D3:60", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D9:60", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:D9:6F", "3", "1625", "2450" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:E7:20", "3", "800", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:E7:2F", "3", "800", "1600" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:E7:60", "3", "1625", "750" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:1A:E3:D2:E7:6F", "3", "1625", "750" });
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "00:25:45:24:7E:B0", "3", "1625", "750" });
		
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "F4:B5:F4:3B:8D:36", "3", "0", "800" }); // 91
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "E2:E1:27:7E:E9:F9", "3", "200", "800" }); // 92
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "CD:CC:31:10:C1:44", "3", "200", "400" }); // 93
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "E8:F1:D1:D3:CE:4A", "3", "0", "400" }); // 94
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "EF:50:1E:4B:96:87", "3", "100", "600" }); // 95
		sqLiteDatabase.execSQL("INSERT INTO location (bssid, level, x, y) VALUES (?, ?, ?, ?);", new String[] { "C8:D3:A3:34:FC:48", "3", "200", "600" }); // _802.11g_
		
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS settings (key TEXT, value TEXT);");
		sqLiteDatabase.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS UK_settings ON settings (key);");
		
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT value FROM settings WHERE key = ?;", new String[] { "lastUpload" });
		cursor.moveToFirst();
		if (cursor.getCount() == 0) sqLiteDatabase.execSQL("INSERT INTO settings (key, value) VALUES (?, ?);", new Object[] { "lastUpload", new Date().getTime() / 1000 });
		cursor.close();
		
		sqLiteDatabase.close();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.activityMainSurfaceView);
		surfaceView.setSurfaceRenderer(renderer = new Renderer(this)
				.setCurrentLevel(sharedPreferences.getString("level", "J3NP"))
				.setCurrentX(sharedPreferences.getInt("x", 1500))
				.setCurrentY(sharedPreferences.getInt("y", 1500))
				.setScale(sharedPreferences.getFloat("scale", 2.5f)));
		scrollView = (ScrollView) findViewById(R.id.activityMainScrollView);
		EstimoteSDK.initialize(this, "testapp1-8p3", "3c3955034c015c5b0b6604ad3324058e");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (isCollectionRunning) collector.cancel(false);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isCollectionRunning) collector.cancel(false);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isCollectionRunning) renderer.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_activity_main_create:
				menuCreate();
				break;
			case R.id.menu_activity_main_location:
				menuLocation();
				break;
			case R.id.menu_activity_main_download:
				menuDownload();
				break;
			case R.id.menu_activity_main_upload:
				menuUpload();
				break;
			case R.id.menu_activity_main_search:
				menuSearch();
				break;
			case R.id.menu_activity_main_j1np:
				renderer.setCurrentLevel("J1NP");
				break;
			case R.id.menu_activity_main_j2np:
				renderer.setCurrentLevel("J2NP");
				break;
			case R.id.menu_activity_main_j3np:
				renderer.setCurrentLevel("J3NP");
				break;
			case R.id.menu_activity_main_j4np:
				renderer.setCurrentLevel("J4NP");
				break;
			case R.id.menu_activity_main_walk:
				menuWalk();
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	public void startCollection(boolean isSearch) {
		if (isSearch) {
			try {
				switch (Settings.SEARCH_ALGORITHM) {
					case Settings.SEARCH_ALGORITHM_NEIGHBOURS:
						new NeighborFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_NEIGHBOURS, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL);
						break;
					case Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS:
						new NeighborFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL);
						break;
					case Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH:
						new NeighborFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL);
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES:
						new ParticleFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_PARTICLES, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS:
						new ParticleFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH:
						new ParticleFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_PARTICLES_TOGETHER:
						new ParticleFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_PARTICLES_TOGETHER, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM:
						new CustomFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_CUSTOM, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS:
						new CustomFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH:
						new CustomFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR:
						new CustomFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_CUSTOM_CELLULAR, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER:
						new CustomFilter(this, collector.getFingerprint(Settings.CREATING_TIME), collectionLocation, Settings.SEARCH_ALGORITHM_CUSTOM_TOGETHER, Settings.CREATING_TIME).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						break;
					case Settings.SEARCH_ALGORITHM_TOGETHER:
						for (int collectTime = 10000; collectTime <= Settings.CREATING_TIME; collectTime += 10000) {
							new NeighborFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_NEIGHBOURS, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL).get();
							new NeighborFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL).get();
							new NeighborFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Settings.SEARCH_SERVER_URL).get();
							new ParticleFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_PARTICLES, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
							new ParticleFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_PARTICLES_WIRELESS, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
							new ParticleFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_PARTICLES_BLUETOOTH, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
							new CustomFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_CUSTOM, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
							new CustomFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_CUSTOM_WIRELESS, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
							new CustomFilter(this, collector.getFingerprint(collectTime), collectionLocation, Settings.SEARCH_ALGORITHM_TOGETHER, Settings.SEARCH_ALGORITHM_CUSTOM_BLUETOOTH, collectTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
						}
						break;
				}
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (++collectionCounter <= collectionTarget) {
			final boolean innerSearch = isSearch;
			if (isSearch) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						(collector = new Collector(MainActivity.this, renderer.getCurrentLevel(), renderer.getCurrentX(), renderer.getCurrentY(), innerSearch)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}, 5000);
			} else (collector = new Collector(this, renderer.getCurrentLevel(), renderer.getCurrentX(), renderer.getCurrentY(), isSearch)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			collectionCounter = 1;
			if (!isSearch) {
				SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
				sharedPreferencesEditor.putString("level", renderer.getCurrentLevel());
				sharedPreferencesEditor.putInt("x", (int) renderer.getCurrentX());
				sharedPreferencesEditor.putInt("y", (int) renderer.getCurrentY());
				sharedPreferencesEditor.putFloat("scale", (float) renderer.getScale());
				sharedPreferencesEditor.apply();
				
				scrollView.setVisibility(View.INVISIBLE);
				RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
				if (Settings.SHOW_FINGERPRINT) startActivity(new Intent(this, LocationActivity.class)
						.putExtra("level", renderer.getCurrentLevel())
						.putExtra("x", (int) renderer.getCurrentX())
						.putExtra("y", (int) renderer.getCurrentY()));
			} else renderer.showPoints();
			scrollView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void menuSearch() {
		if (!isCollectionRunning) {
			collectionLocation = new Location(renderer.getCurrentLevel(), (int) renderer.getCurrentX(), (int) renderer.getCurrentY());
			final View view = this.getLayoutInflater().inflate(R.layout.activity_main_dialog_search, null);
			Spinner spinner = (Spinner) view.findViewById(R.id.activityMainDialogSearchSpinner);
			spinner.setAdapter(new AlgorithmsAdapter(this, getResources().getStringArray(R.array.activityMainAlertDialogSpinnerValues)));
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
					String[] values = getResources().getStringArray(R.array.activityMainAlertDialogSpinnerValues);
					for (int i = 0; i < values.length; i++) {
						if (values[i].equals(adapterView.getItemAtPosition(position))) {
							Settings.SEARCH_ALGORITHM = Integer.parseInt(getResources().getStringArray(R.array.activityMainAlertDialogSpinnerKeys)[i]);
							break;
						}
					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) { /* Do Nothing */ }
			});
			String[] values = getResources().getStringArray(R.array.activityMainAlertDialogSpinnerValues);
			for (int i = 0; i < values.length; i++) {
				if (Settings.SEARCH_ALGORITHM == Integer.parseInt(getResources().getStringArray(R.array.activityMainAlertDialogSpinnerKeys)[i])) {
					spinner.setSelection(i);
					break;
				}
			}
			
			AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view)
					.setPositiveButton(R.string.activityMainAlertDialogButtonPositive, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SQLiteDatabase sqLiteDatabase = MainActivity.this.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
							sqLiteDatabase.execSQL("DELETE FROM particle;");
							sqLiteDatabase.close();
							renderer.hidePoints();
							(collector = new Collector(MainActivity.this, renderer.getCurrentLevel(), renderer.getCurrentX(), renderer.getCurrentY(), true)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}).setNegativeButton(R.string.activityMainAlertDialogButtonNegative, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scrollView.setVisibility(View.INVISIBLE);
						}
					}).create();
			final SeekBar seekBarTime = (SeekBar) view.findViewById(R.id.activityMainAlertDialogSearchSeekBarTime);
			final SeekBar seekBarDozen = (SeekBar) view.findViewById(R.id.activityMainAlertDialogSearchSeekBarDozen);
			final SeekBar seekBarUnit = (SeekBar) view.findViewById(R.id.activityMainAlertDialogSearchSeekBarUnit);
			final TextView textView = (TextView) view.findViewById(R.id.activityMainAlertDialogSearchTextView);
			seekBarTime.setProgress(Settings.CREATING_TIME / 10000);
			seekBarDozen.setProgress(Settings.CREATING_REPETITION_DOZEN);
			seekBarUnit.setProgress(Settings.CREATING_REPETITION_UNIT);
			textView.setText(String.format(this.getString(R.string.activityMainAlertDialogTextView),
					collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
			seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_TIME = seekBar.getProgress() * 10000;
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			seekBarDozen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_REPETITION_DOZEN = seekBar.getProgress();
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			seekBarUnit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_REPETITION_UNIT = seekBar.getProgress();
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			alertDialog.show();
		} else {
			scrollView.setVisibility(View.INVISIBLE);
			collector.cancel(false);
			collectionCounter = 1;
			renderer.showPoints();
		}
	}
	
	private void menuCreate() {
		if (!isCollectionRunning) {
			final View view = this.getLayoutInflater().inflate(R.layout.activity_main_dialog_create, null);
			AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view)
					.setPositiveButton(R.string.activityMainAlertDialogButtonPositive, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							renderer.hidePoints();
							scrollView.setVisibility(View.VISIBLE);
							(collector = new Collector(MainActivity.this, renderer.getCurrentLevel(), renderer.getCurrentX(), renderer.getCurrentY(), false)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}).setNegativeButton(R.string.activityMainAlertDialogButtonNegative, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							scrollView.setVisibility(View.INVISIBLE);
						}
					}).create();
			final SeekBar seekBarTime = (SeekBar) view.findViewById(R.id.activityMainAlertDialogCreateSeekBarTime);
			final SeekBar seekBarDozen = (SeekBar) view.findViewById(R.id.activityMainAlertDialogCreateSeekBarDozen);
			final SeekBar seekBarUnit = (SeekBar) view.findViewById(R.id.activityMainAlertDialogCreateSeekBarUnit);
			final TextView textView = (TextView) view.findViewById(R.id.activityMainAlertDialogCreateTextView);
			seekBarTime.setProgress(Settings.CREATING_TIME / 10000);
			seekBarDozen.setProgress(Settings.CREATING_REPETITION_DOZEN);
			seekBarUnit.setProgress(Settings.CREATING_REPETITION_UNIT);
			textView.setText(String.format(this.getString(R.string.activityMainAlertDialogTextView),
					collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
			seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_TIME = seekBar.getProgress() * 10000;
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			seekBarDozen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_REPETITION_DOZEN = seekBar.getProgress();
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			seekBarUnit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
					Settings.CREATING_REPETITION_UNIT = seekBar.getProgress();
					textView.setText(String.format(MainActivity.this.getString(R.string.activityMainAlertDialogTextView),
							collectionTarget = seekBarDozen.getProgress() * 10 + seekBarUnit.getProgress(), Settings.CREATING_TIME / 1000, collectionTarget * Settings.CREATING_TIME / 60000.0));
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { /* Do Nothing */ }
			});
			alertDialog.show();
		} else {
			scrollView.setVisibility(View.INVISIBLE);
			collector.cancel(false);
			collectionCounter = 1;
			renderer.showPoints();
		}
	}
	
	private void menuLocation() {
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		sharedPreferencesEditor.putString("level", renderer.getCurrentLevel());
		sharedPreferencesEditor.putInt("x", (int) renderer.getCurrentX());
		sharedPreferencesEditor.putInt("y", (int) renderer.getCurrentY());
		sharedPreferencesEditor.putFloat("scale", (float) renderer.getScale());
		sharedPreferencesEditor.apply();
		
		startActivity(new Intent(this, LocationActivity.class)
				.putExtra("level", renderer.getCurrentLevel())
				.putExtra("x", (int) renderer.getCurrentX())
				.putExtra("y", (int) renderer.getCurrentY()));
	}
	
	private void menuDownload() {
		SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor settingCursor = sqLiteDatabase.rawQuery("SELECT value FROM settings WHERE key = ?;", new String[] { "lastUpload" });
		settingCursor.moveToFirst();
		Cursor fingerprintCursor = sqLiteDatabase.rawQuery("SELECT COUNT(*), MAX(timestamp) FROM fingerprint;", new String[] {});
		fingerprintCursor.moveToFirst();
		try {
			if (fingerprintCursor.getInt(0) != 0 && settingCursor.getInt(0) <= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fingerprintCursor.getString(1)).getTime() / 1000) {
				AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage("You have some un-uploaded data.\nUn-uploaded data will be deleted.\nDo you want continue?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								renderer.hidePoints();
								new CouchDatabase(MainActivity.this, CouchDatabase.MODE_DOWNLOAD).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) { /* Do Nothing */ }
						}).show();
				TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
				textView.setGravity(Gravity.CENTER);
				textView.setPadding(0, 32, 0, 32);
			} else new CouchDatabase(this, CouchDatabase.MODE_DOWNLOAD).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		settingCursor.close();
		fingerprintCursor.close();
		sqLiteDatabase.close();
	}
	
	private void menuUpload() {
		renderer.hidePoints();
		new CouchDatabase(this, CouchDatabase.MODE_UPLOAD).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void menuWalk() {
		final int previousCreatingTime = Settings.CREATING_TIME;
		Settings.CREATING_TIME = 10000;
		Settings.SHOW_FINGERPRINT = false;
		
		final ArrayList<int[]> path = new ArrayList<>();
		path.add(new int[] { 350, 750 }); // 1
		path.add(new int[] { 300, 750 });
		path.add(new int[] { 250, 750 });
		path.add(new int[] { 250, 700 });
		path.add(new int[] { 250, 650 });
		path.add(new int[] { 250, 600 }); // 2
		path.add(new int[] { 250, 550 });
		path.add(new int[] { 250, 500 });
		path.add(new int[] { 250, 450 });
		path.add(new int[] { 300, 450 });
		path.add(new int[] { 350, 450 }); // 3
		
		path.add(new int[] { 300, 450 });
		path.add(new int[] { 250, 450 });
		path.add(new int[] { 250, 500 });
		path.add(new int[] { 250, 550 });
		path.add(new int[] { 250, 600 });  // 4
		path.add(new int[] { 250, 650 });
		path.add(new int[] { 250, 750 });
		path.add(new int[] { 300, 750 });
		path.add(new int[] { 350, 750 });
		
		for (int c = 0; c < path.size(); c++) {
			final int innerC = c;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					renderer.setCurrentX(path.get(innerC)[0]).setCurrentY(path.get(innerC)[1]);
					if (innerC + 1 < path.size()) {
						renderer.setCurrentArrow(path.get(innerC), path.get(innerC + 1));
					} else renderer.setNoArrow();
					if (innerC % 5 == 0) (collector = new Collector(MainActivity.this, renderer.getCurrentLevel(), path.get(innerC + 2)[0], path.get(innerC + 2)[1], false)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					if (innerC == path.size() - 1) {
						Settings.CREATING_TIME = previousCreatingTime;
						Settings.SHOW_FINGERPRINT = true;
					}
				}
			}, c * 2500);
		}
	}
}