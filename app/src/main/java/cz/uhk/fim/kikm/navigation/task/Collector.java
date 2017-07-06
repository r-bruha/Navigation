package cz.uhk.fim.kikm.navigation.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.MainActivity;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.model.list.BluetoothRecords;
import cz.uhk.fim.kikm.navigation.model.list.CellularRecords;
import cz.uhk.fim.kikm.navigation.model.list.SensorRecords;
import cz.uhk.fim.kikm.navigation.model.list.WirelessRecords;
import cz.uhk.fim.kikm.navigation.model.record.BluetoothRecord;
import cz.uhk.fim.kikm.navigation.model.record.CellularRecord;
import cz.uhk.fim.kikm.navigation.model.record.DeviceRecord;
import cz.uhk.fim.kikm.navigation.model.record.WirelessRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.AccelerationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.GravitationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.GyroscopeRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.LinearAccelerationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.MagneticRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.RotationRecord;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class Collector extends AsyncTask<Void, Void, Void> {
	private static boolean wirelessShowDetail = false, bluetoothShowDetail = false, cellularShowDetail = false, sensorShowDetail = false;
	private Context context;
	private MainActivity activity;
	private BeaconManager beaconManager;
	private WifiManager wifiManager;
	private TelephonyManager telephonyManager;
	private SQLiteDatabase sqLiteDatabase;
	private long collectionStart, currentCellStrength;
	private BroadcastReceiver wirelessBroadcastReceiver;
	private SensorManager sensorManager;
	private RelativeLayout activityRelativeLayout, scrollViewRelativeLayout;
	private Cursor cursor;
	private String level, x, y;
	private RelativeLayout.LayoutParams layoutParameters;
	private TextView textView;
	private int textBoxChildren = 0;
	private int[] sensorCounter = new int[100];
	private boolean isSearch;
	private SensorEventListener[] sensorEventListeners;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private float averageWirelessDevices, averageWirelessRecords, averageWirelessRpS, averageWirelessSpR, averageWirelessStrength, averageWirelessDistance,
			averageBluetoothDevices, averageBluetoothRecords, averageBluetoothRpS, averageBluetoothSpR, averageBluetoothStrength, averageBluetoothDistance,
			averageCellularDevices, averageCellularRecords, averageCellularRpS, averageCellularSpR, averageCellularStrength, averageCellularDistance,
			averageSensorDevices, averageSensorRecords, averageSensorRpS, averageSensorSpR;
	private String averageWirelessDevicesString, averageWirelessRecordsString, averageWirelessRpSString, averageWirelessSpRString, averageWirelessStrengthString, averageWirelessDistanceString,
			averageBluetoothDevicesString, averageBluetoothRecordsString, averageBluetoothRpSString, averageBluetoothSpRString, averageBluetoothStrengthString, averageBluetoothDistanceString,
			averageCellularDevicesString, averageCellularRecordsString, averageCellularRpSString, averageCellularSpRString, averageCellularStrengthString, averageCellularDistanceString,
			averageSensorDevicesString, averageSensorRecordsString, averageSensorRpSString, averageSensorSpRString;
	private Map<String, Float> averageWirelessRecordsDetail = new HashMap<>(), averageWirelessRpSDetail = new HashMap<>(), averageWirelessSpRDetail = new HashMap<>(),
			averageWirelessStrengthDetail = new HashMap<>(), averageWirelessDistanceDetail = new HashMap<>(),
			averageBluetoothRecordsDetail = new HashMap<>(), averageBluetoothRpSDetail = new HashMap<>(), averageBluetoothSpRDetail = new HashMap<>(),
			averageBluetoothStrengthDetail = new HashMap<>(), averageBluetoothDistanceDetail = new HashMap<>(),
			averageCellularRecordsDetail = new HashMap<>(), averageCellularRpSDetail = new HashMap<>(), averageCellularSpRDetail = new HashMap<>(),
			averageCellularStrengthDetail = new HashMap<>(), averageCellularDistanceDetail = new HashMap<>(),
			averageSensorRecordsDetail = new HashMap<>(), averageSensorRpSDetail = new HashMap<>(), averageSensorSpRDetail = new HashMap<>();
	
	public Collector(Context context, String level, double x, double y, boolean isSearch) {
		this.context = context;
		this.level = level;
		this.x = String.valueOf(x);
		this.y = String.valueOf(y);
		this.isSearch = isSearch;
		
		activity = (MainActivity) context;
		sqLiteDatabase = activity.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		beaconManager = new BeaconManager(context);
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
		createWirelessCollector();
		createBluetoothCollector();
		createCellularCollector();
		createSensorCollector();
		
		wifiManager.startScan();
		collectionStart = System.currentTimeMillis();
		activityRelativeLayout = (RelativeLayout) activity.findViewById(R.id.activity_main);
		scrollViewRelativeLayout = (RelativeLayout) activity.findViewById(R.id.activityMainScrollViewLayout);
	}
	
	public Fingerprint getFingerprint(int time) {
		Fingerprint fingerprint = null;
		try {
			SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
			Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, timestamp, finish, level, x, y, user FROM currentFingerprint ORDER BY id;", null);
			cursor.moveToFirst();
			
			fingerprint = new Fingerprint(simpleDateFormat.parse(cursor.getString(1)), simpleDateFormat.parse(cursor.getString(2)), cursor.getString(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6));
			Cursor wirelessCursor = sqLiteDatabase.rawQuery("SELECT timestamp, ssid, bssid, rssi, distance, time, difference, frequency FROM currentWireless WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) ORDER BY timestamp DESC;", null);
			
			WirelessRecords wirelessRecords = new WirelessRecords();
			while (wirelessCursor.moveToNext()) wirelessRecords.add(new WirelessRecord(simpleDateFormat.parse(wirelessCursor.getString(0)), wirelessCursor.getString(1), wirelessCursor.getString(2), wirelessCursor.getInt(3), wirelessCursor.getFloat(4), wirelessCursor.getInt(5), wirelessCursor.getInt(6), wirelessCursor.getInt(7)));
			wirelessCursor.close();
			
			Cursor bluetoothCursor = sqLiteDatabase.rawQuery("SELECT timestamp, bssid, rssi, distance, time, difference FROM currentBluetooth WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) ORDER BY timestamp DESC;", null);
			BluetoothRecords bluetoothRecords = new BluetoothRecords();
			while (bluetoothCursor.moveToNext()) bluetoothRecords.add(new BluetoothRecord(simpleDateFormat.parse(bluetoothCursor.getString(0)), bluetoothCursor.getString(1), bluetoothCursor.getInt(2), bluetoothCursor.getFloat(3), bluetoothCursor.getInt(4), bluetoothCursor.getInt(5)));
			bluetoothCursor.close();
			
			Cursor cellularCursor = sqLiteDatabase.rawQuery("SELECT timestamp, cid, lac, psc, type, rssi, distance, time, difference FROM currentCellular WHERE timestamp < (SELECT DATETIME(timestamp, '+" + time / 1000 + " seconds') FROM currentFingerprint) ORDER BY timestamp DESC;", null);
			CellularRecords cellularRecords = new CellularRecords();
			while (cellularCursor.moveToNext()) cellularRecords.add(new CellularRecord(simpleDateFormat.parse(cellularCursor.getString(0)), cellularCursor.getString(1), cellularCursor.getString(2), cellularCursor.getString(3), cellularCursor.getString(4), cellularCursor.getInt(5), cellularCursor.getInt(6), cellularCursor.getInt(7), cellularCursor.getInt(8)));
			cellularCursor.close();
			
			Cursor sensorCursor = sqLiteDatabase.rawQuery("SELECT timestamp, type, AVG(x), AVG(y), AVG(z), AVG(time), AVG(difference) FROM currentSensor GROUP BY type ORDER BY timestamp DESC;", null);
			
			LinearAccelerationRecord linearAccelerationRecord = null;
			AccelerationRecord accelerationRecord = null;
			GravitationRecord gravitationRecord = null;
			GyroscopeRecord gyroscopeRecord = null;
			MagneticRecord magneticRecord = null;
			RotationRecord rotationRecord = null;
			while (sensorCursor.moveToNext()) {
				switch (sensorCursor.getInt(1)) {
					case Sensor.TYPE_LINEAR_ACCELERATION:
						linearAccelerationRecord = new LinearAccelerationRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					case Sensor.TYPE_ACCELEROMETER:
						accelerationRecord = new AccelerationRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					case Sensor.TYPE_GRAVITY:
						gravitationRecord = new GravitationRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					case Sensor.TYPE_GYROSCOPE:
						gyroscopeRecord = new GyroscopeRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					case Sensor.TYPE_MAGNETIC_FIELD:
						magneticRecord = new MagneticRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					case Sensor.TYPE_ROTATION_VECTOR:
						rotationRecord = new RotationRecord(sensorCursor.getFloat(2), sensorCursor.getFloat(3), sensorCursor.getFloat(4), sensorCursor.getInt(5), sensorCursor.getInt(6));
						break;
					default:
						break;
				}
			}
			sensorCursor.close();
			SensorRecords sensorRecords = new SensorRecords(linearAccelerationRecord, accelerationRecord, gravitationRecord, gyroscopeRecord, magneticRecord, rotationRecord);
			
			DeviceRecord deviceRecord = new DeviceRecord(context);
			Cursor deviceCursor = sqLiteDatabase.rawQuery("SELECT did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api FROM currentDevice;", null);
			deviceCursor.moveToFirst();
			
			deviceRecord.setId(deviceCursor.getString(0)).setBoard(deviceCursor.getString(1)).setBootloader(deviceCursor.getString(2))
					.setBrand(deviceCursor.getString(3)).setDevice(deviceCursor.getString(4)).setDisplay(deviceCursor.getString(5))
					.setFingerprint(deviceCursor.getString(6)).setHardware(deviceCursor.getString(7)).setHost(deviceCursor.getString(8))
					.setManufacturer(deviceCursor.getString(9)).setModel(deviceCursor.getString(10)).setProduct(deviceCursor.getString(11))
					.setSerial(deviceCursor.getString(12)).setTags(deviceCursor.getString(13)).setTelephone(deviceCursor.getString(14))
					.setType(deviceCursor.getString(15)).setUser(deviceCursor.getString(16)).setOs(deviceCursor.getString(17)).setApi(deviceCursor.getInt(18));
			deviceCursor.close();
			fingerprint.setWirelessRecords(wirelessRecords).setBluetoothRecords(bluetoothRecords).setCellularRecords(cellularRecords).setSensorRecords(sensorRecords).setDeviceRecord(deviceRecord);
			cursor.close();
			sqLiteDatabase.close();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return fingerprint;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		activity.setCollectionRunning(true);
		
		sqLiteDatabase.execSQL("DELETE FROM currentFingerprint;");
		sqLiteDatabase.execSQL("DELETE FROM currentWireless;");
		sqLiteDatabase.execSQL("DELETE FROM currentBluetooth;");
		sqLiteDatabase.execSQL("DELETE FROM currentCellular;");
		sqLiteDatabase.execSQL("DELETE FROM currentSensor;");
		sqLiteDatabase.execSQL("DELETE FROM currentDevice;");
		
		sqLiteDatabase.execSQL("INSERT INTO currentFingerprint (finish, level, x, y, user) VALUES (DATETIME('NOW', 'LOCALTIME', '+" + Settings.CREATING_TIME / 1000 + " SECOND'), ?, ?, ?, ?)",
				new Object[] { level, x, y, PreferenceManager.getDefaultSharedPreferences(context).getString("couchDatabase", "Unknown") });
		
		DeviceRecord dR = new DeviceRecord(context);
		sqLiteDatabase.execSQL("INSERT INTO currentDevice (did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", new Object[] {
				dR.getId(), dR.getBoard(), dR.getBootloader(), dR.getBrand(), dR.getDevice(), dR.getDisplay(), dR.getFingerprint(), dR.getHardware(), dR.getHost(),
				dR.getManufacturer(), dR.getModel(), dR.getProduct(), dR.getSerial(), dR.getTags(), dR.getTelephone(), dR.getType(), dR.getUser(), dR.getOs(), dR.getApi()
		});
		
		createSummaryAverage();
		createDetailAverage();
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		while (System.currentTimeMillis() < collectionStart + Settings.CREATING_TIME) {
			if (!isCancelled()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else break;
			publishProgress();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		scrollViewRelativeLayout.removeAllViewsInLayout();
		
		int wirelessColor = ((ColorDrawable) createWirelessSummary().getBackground()).getColor();
		int bluetoothColor = ((ColorDrawable) createBluetoothSummary().getBackground()).getColor();
		int cellularColor = ((ColorDrawable) createCellularSummary().getBackground()).getColor();
		int sensorColor = ((ColorDrawable) createSensorSummary().getBackground()).getColor();
		
		if (wirelessShowDetail) {
			scrollViewRelativeLayout.removeAllViewsInLayout();
			createWirelessDetail();
			activityRelativeLayout.setBackgroundColor(wirelessColor);
			scrollViewRelativeLayout.setBackgroundColor(wirelessColor);
		} else if (bluetoothShowDetail) {
			scrollViewRelativeLayout.removeAllViewsInLayout();
			createBluetoothDetail();
			activityRelativeLayout.setBackgroundColor(bluetoothColor);
			scrollViewRelativeLayout.setBackgroundColor(bluetoothColor);
		} else if (cellularShowDetail) {
			scrollViewRelativeLayout.removeAllViewsInLayout();
			createCellularDetail();
			activityRelativeLayout.setBackgroundColor(cellularColor);
			scrollViewRelativeLayout.setBackgroundColor(cellularColor);
		} else if (sensorShowDetail) {
			scrollViewRelativeLayout.removeAllViewsInLayout();
			createSensorDetail();
			activityRelativeLayout.setBackgroundColor(sensorColor);
			scrollViewRelativeLayout.setBackgroundColor(sensorColor);
		}
		
		activityRelativeLayout.setBackgroundColor(wirelessColor == Color.RED || bluetoothColor == Color.RED || cellularColor == Color.RED ? Color.RED : (
				wirelessColor == Color.YELLOW || bluetoothColor == Color.YELLOW || cellularColor == Color.YELLOW ? Color.YELLOW : Color.GREEN)
		);
		scrollViewRelativeLayout.setBackgroundColor(wirelessColor == Color.RED || bluetoothColor == Color.RED || cellularColor == Color.RED ? Color.RED : (
				wirelessColor == Color.YELLOW || bluetoothColor == Color.YELLOW || cellularColor == Color.YELLOW ? Color.YELLOW : Color.GREEN)
		);
		
		if (isSearch) scrollViewRelativeLayout.removeAllViewsInLayout();
		
		ProgressBar progressBarTotal = (ProgressBar) activityRelativeLayout.findViewById(R.id.activityMainProgressBarTotal);
		progressBarTotal.setProgress(activity.getCollectionCounter());
		progressBarTotal.setMax(activity.getCollectionTarget());
		
		ProgressBar progressBarSession = (ProgressBar) activityRelativeLayout.findViewById(R.id.activityMainProgressBarSession);
		progressBarSession.setProgress((int) (System.currentTimeMillis() - collectionStart));
		progressBarSession.setMax(Settings.CREATING_TIME);
	}
	
	@Override
	protected void onPostExecute(Void aVoid) {
		onFinish();
		activity.startCollection(isSearch);
		
	}
	
	@Override
	protected void onCancelled(Void aVoid) {
		onFinish();
	}
	
	private void onFinish() {
		try {
			activityRelativeLayout.setBackgroundColor(Color.rgb(250, 250, 250));
			sqLiteDatabase.beginTransaction();
			
			if (!isSearch) {
				sqLiteDatabase.execSQL("UPDATE currentFingerprint SET finish = DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME');");
				sqLiteDatabase.execSQL("INSERT INTO fingerprint (timestamp, finish, level, x, y, user) SELECT timestamp, finish, level, x, y, user FROM currentFingerprint;");
				
				Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(id) FROM fingerprint;", new String[] {});
				cursor.moveToFirst();
				String currentID = cursor.getString(0);
				cursor.close();
				
				sqLiteDatabase.execSQL("INSERT INTO wireless (timestamp, fingerprint_id, ssid, bssid, rssi, distance, time, difference, frequency) " +
						"SELECT timestamp, ?, ssid, bssid, rssi, distance, time, difference, frequency FROM currentWireless;", new Object[] { currentID });
				
				sqLiteDatabase.execSQL("INSERT INTO bluetooth (timestamp, fingerprint_id, bssid, rssi, distance, time, difference) " +
						"SELECT timestamp, ?, bssid, rssi, distance, time, difference FROM currentBluetooth;", new Object[] { currentID });
				
				sqLiteDatabase.execSQL("INSERT INTO cellular (timestamp, fingerprint_id, cid, lac, psc, type, rssi, distance, time, difference) " +
						"SELECT timestamp, ?, cid, lac, psc, type, rssi, distance, time, difference FROM currentCellular;", new Object[] { currentID });
				
				sqLiteDatabase.execSQL("INSERT INTO sensor (timestamp, fingerprint_id, type, x, y, z, time, difference) " +
						"SELECT timestamp, ?, type, x, y, z, time, difference FROM currentSensor;", new Object[] { currentID });
				
				sqLiteDatabase.execSQL("INSERT INTO device (timestamp, fingerprint_id, did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api) " +
						"SELECT timestamp, ?, did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api FROM currentDevice;", new Object[] { currentID });
			}
			
			sqLiteDatabase.setTransactionSuccessful();
			sqLiteDatabase.endTransaction();
			sqLiteDatabase.close();
			
			activity.unregisterReceiver(wirelessBroadcastReceiver);
			beaconManager.disconnect();
			telephonyManager.listen(new PhoneStateListener() {
			}, PhoneStateListener.LISTEN_NONE);
			for (SensorEventListener sensorEventListener : sensorEventListeners) sensorManager.unregisterListener(sensorEventListener);
			activity.setCollectionRunning(false);
			
			((ProgressBar) activityRelativeLayout.findViewById(R.id.activityMainProgressBarTotal)).setProgress(0);
			((ProgressBar) activityRelativeLayout.findViewById(R.id.activityMainProgressBarSession)).setProgress(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createWirelessCollector() {
		activity.registerReceiver(wirelessBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				for (ScanResult scanResult : wifiManager.getScanResults()) {
					Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentWireless WHERE bssid = ?;", new String[] { scanResult.BSSID.toUpperCase() });
					cursor.moveToFirst();
					int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
					int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
					sqLiteDatabase.execSQL("INSERT INTO currentWireless (ssid, bssid, rssi, distance, time, difference, frequency) VALUES (?, ?, ?, ?, ?, ?, ?);",
							new Object[] { scanResult.SSID, scanResult.BSSID.toUpperCase(), scanResult.level, (float) (Math.pow(10.0d, (27.55d - 40d * Math.log10(scanResult.frequency) + 6.7d - scanResult.level) / 20.0d) * 1000.0), collectionLength, collectionLength - previousCollectionLength, scanResult.frequency });
					cursor.close();
				}
				
				if (telephonyManager.getAllCellInfo() != null) {
					// TODO: Untested, unavailable phone with LTE or with working getAllCellInfo() implementation
					for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
						if (cellInfo instanceof CellInfoGsm) {
							CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
							CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
							Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentCellular WHERE cid = ?;", new String[] { String.valueOf(cellIdentityGsm.getCid()) });
							cursor.moveToFirst();
							int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
							int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
							sqLiteDatabase.execSQL("INSERT INTO currentCellular (cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
									new Object[] { cellIdentityGsm.getCid(), cellIdentityGsm.getLac(), cellIdentityGsm.getPsc(), 0, cellInfoGsm.getCellSignalStrength().getDbm(), 1000, collectionLength, collectionLength - previousCollectionLength });
							cursor.close();
						} else if (cellInfo instanceof CellInfoLte) {
							CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
							CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
							Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentCellular WHERE cid = ?;", new String[] { String.valueOf(cellIdentityLte.getCi()) });
							cursor.moveToFirst();
							int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
							int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
							sqLiteDatabase.execSQL("INSERT INTO currentCellular (cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
									new Object[] { cellIdentityLte.getCi(), cellIdentityLte.getTac(), 0, 0, cellInfoLte.getCellSignalStrength().getDbm(), 1000, collectionLength, collectionLength - previousCollectionLength });
							cursor.close();
						} else if (cellInfo instanceof CellInfoWcdma) {
							CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
							CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
							Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentCellular WHERE cid = ?;", new String[] { String.valueOf(cellIdentityWcdma.getCid()) });
							cursor.moveToFirst();
							int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
							int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
							sqLiteDatabase.execSQL("INSERT INTO currentCellular (cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
									new Object[] { cellIdentityWcdma.getCid(), cellIdentityWcdma.getLac(), cellIdentityWcdma.getPsc(), 0, cellInfoWcdma.getCellSignalStrength().getDbm(), 1000, collectionLength, collectionLength - previousCollectionLength });
							cursor.close();
						}
					}
				} else {
					if (telephonyManager.getNeighboringCellInfo().size() >= 1) {
						for (NeighboringCellInfo cellInfo : telephonyManager.getNeighboringCellInfo()) {
							Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentCellular WHERE cid = ?;", new String[] { String.valueOf(cellInfo.getCid()) });
							cursor.moveToFirst();
							int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
							int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
							sqLiteDatabase.execSQL("INSERT INTO currentCellular (cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
									new Object[] { cellInfo.getCid(), cellInfo.getLac(), cellInfo.getPsc(), cellInfo.getNetworkType(), cellInfo.getRssi(), 1000, collectionLength, collectionLength - previousCollectionLength });
							cursor.close();
						}
					} else {
						GsmCellLocation gsmCellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
						Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentCellular WHERE cid = ?;", new String[] { String.valueOf(gsmCellLocation.getCid()) });
						cursor.moveToFirst();
						int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
						int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
						sqLiteDatabase.execSQL("INSERT INTO currentCellular (cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
								new Object[] { gsmCellLocation.getCid(), gsmCellLocation.getLac(), gsmCellLocation.getPsc(), 0, currentCellStrength, 1000, collectionLength, collectionLength - previousCollectionLength });
						cursor.close();
					}
				}
				wifiManager.startScan();
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	private void createBluetoothCollector() {
		beaconManager.setForegroundScanPeriod(200, 0);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				for (Beacon beacon : beacons) {
					Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentBluetooth WHERE bssid = ?;", new String[] { beacon.getMacAddress().toStandardString() });
					cursor.moveToFirst();
					int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
					int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
					sqLiteDatabase.execSQL("INSERT INTO currentBluetooth (bssid, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?)",
							new Object[] { beacon.getMacAddress().toStandardString(), beacon.getRssi(), Utils.computeAccuracy(beacon), collectionLength, collectionLength - previousCollectionLength });
					cursor.close();
				}
			}
		});
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				beaconManager.startRanging(new Region("BlueTooth", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));
			}
		});
	}
	
	private void createCellularCollector() {
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				currentCellStrength = signalStrength.isGsm() ? (signalStrength.getGsmSignalStrength() != 99 ? signalStrength.getGsmSignalStrength() * 2 - 113 : signalStrength.getGsmSignalStrength()) : signalStrength.getCdmaDbm();
			}
		}, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	private void createSensorCollector() {
		List<Sensor> sensors = new ArrayList<>();
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
		sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
		sensorEventListeners = new SensorEventListener[sensors.size()];
		int counter = 0;
		for (final Sensor sensor : sensors) {
			sensorManager.registerListener(sensorEventListeners[counter++] = new SensorEventListener() {
				@Override
				public void onSensorChanged(SensorEvent event) {
					if (sensorCounter[event.sensor.getType()]++ <= 24) return;
					Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(time) FROM currentSensor WHERE type = ?;", new String[] { String.valueOf(event.sensor.getType()) });
					cursor.moveToFirst();
					int collectionLength = (int) (System.currentTimeMillis() - collectionStart);
					int previousCollectionLength = cursor.getCount() == 0 ? 0 : cursor.getInt(0);
					cursor.close();
					sqLiteDatabase.execSQL("INSERT INTO currentSensor (type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?);",
							new Object[] { event.sensor.getType(), event.values[0], (event.values.length >= 2 ? event.values[1] : 0), (event.values.length >= 3 ? event.values[2] : 0), collectionLength, collectionLength - previousCollectionLength });
					sensorCounter[event.sensor.getType()] = 0;
				}
				
				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) { /* Do Nothing */ }
			}, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	
	private void createSummaryAverage() {
		Cursor wirelessCursor = sqLiteDatabase.rawQuery("SELECT AVG(devices) devices, AVG(records) records, AVG(rps) * 1000 rps, AVG(spr) / 1000 spr, AVG(rssi) rssi, AVG(distance) distance FROM " +
				"(SELECT COUNT(DISTINCT bssid) devices, COUNT(*) records, 1 / AVG(difference) rps, AVG(difference) spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"wireless w JOIN fingerprint f ON w.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY f.id);", new String[] { level, x, y });
		wirelessCursor.moveToFirst();
		averageWirelessDevices = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("devices"));
		averageWirelessDevicesString = String.valueOf(averageWirelessDevices);
		averageWirelessRecords = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("records"));
		averageWirelessRecordsString = String.valueOf(averageWirelessRecords);
		averageWirelessRpS = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("rps"));
		averageWirelessRpSString = String.valueOf(averageWirelessRpS);
		averageWirelessSpR = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("spr"));
		averageWirelessSpRString = String.valueOf(averageWirelessSpR);
		averageWirelessStrength = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("rssi"));
		averageWirelessStrengthString = String.valueOf(averageWirelessStrength);
		averageWirelessDistance = wirelessCursor.getFloat(wirelessCursor.getColumnIndex("distance"));
		averageWirelessDistanceString = String.valueOf(averageWirelessDistance);
		wirelessCursor.close();
		
		Cursor bluetoothCursor = sqLiteDatabase.rawQuery("SELECT AVG(devices) devices, AVG(records) records, AVG(rps) * 1000 rps, AVG(spr) / 1000 spr, AVG(rssi) rssi, AVG(distance) distance FROM " +
				"(SELECT COUNT(DISTINCT bssid) devices, COUNT(*) records, 1 / AVG(difference) rps, AVG(difference) spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"bluetooth b JOIN fingerprint f ON b.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY f.id);", new String[] { level, x, y });
		bluetoothCursor.moveToFirst();
		averageBluetoothDevices = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("devices"));
		averageBluetoothDevicesString = String.valueOf(averageBluetoothDevices);
		averageBluetoothRecords = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("records"));
		averageBluetoothRecordsString = String.valueOf(averageBluetoothRecords);
		averageBluetoothRpS = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("rps"));
		averageBluetoothRpSString = String.valueOf(averageBluetoothRpS);
		averageBluetoothSpR = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("spr"));
		averageBluetoothSpRString = String.valueOf(averageBluetoothSpR);
		averageBluetoothStrength = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("rssi"));
		averageBluetoothStrengthString = String.valueOf(averageBluetoothStrength);
		averageBluetoothDistance = bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("distance"));
		averageBluetoothDistanceString = String.valueOf(averageBluetoothDistance);
		bluetoothCursor.close();
		
		Cursor cellularCursor = sqLiteDatabase.rawQuery("SELECT AVG(devices) devices, AVG(records) records, AVG(rps) * 1000 rps, AVG(spr) / 1000 spr, AVG(rssi) rssi, AVG(distance) distance FROM " +
				"(SELECT COUNT(DISTINCT cid) devices, COUNT(*) records, 1 / AVG(difference) rps, AVG(difference) spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"cellular c JOIN fingerprint f ON c.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY f.id);", new String[] { level, x, y });
		cellularCursor.moveToFirst();
		averageCellularDevices = cellularCursor.getFloat(cellularCursor.getColumnIndex("devices"));
		averageCellularDevicesString = String.valueOf(averageCellularDevices);
		averageCellularRecords = cellularCursor.getFloat(cellularCursor.getColumnIndex("records"));
		averageCellularRecordsString = String.valueOf(averageCellularRecords);
		averageCellularRpS = cellularCursor.getFloat(cellularCursor.getColumnIndex("rps"));
		averageCellularRpSString = String.valueOf(averageCellularRpS);
		averageCellularSpR = cellularCursor.getFloat(cellularCursor.getColumnIndex("spr"));
		averageCellularSpRString = String.valueOf(averageCellularSpR);
		averageCellularStrength = cellularCursor.getFloat(cellularCursor.getColumnIndex("rssi"));
		averageCellularStrengthString = String.valueOf(averageCellularStrength);
		averageCellularDistance = cellularCursor.getFloat(cellularCursor.getColumnIndex("distance"));
		averageCellularDistanceString = String.valueOf(averageCellularDistance);
		cellularCursor.close();
		
		Cursor sensorCursor = sqLiteDatabase.rawQuery("SELECT AVG(devices) devices, AVG(records) records, AVG(rps) * 1000 rps, AVG(spr) / 1000 spr FROM " +
				"(SELECT COUNT(DISTINCT type) devices, COUNT(*) records, 1 / AVG(difference) rps, AVG(difference) spr FROM " +
				"sensor s JOIN fingerprint f ON s.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY f.id);", new String[] { level, x, y });
		sensorCursor.moveToFirst();
		averageSensorDevices = sensorCursor.getFloat(sensorCursor.getColumnIndex("devices"));
		averageSensorDevicesString = String.valueOf(averageSensorDevices);
		averageSensorRecords = sensorCursor.getFloat(sensorCursor.getColumnIndex("records"));
		averageSensorRecordsString = String.valueOf(averageSensorRecords);
		averageSensorRpS = sensorCursor.getFloat(sensorCursor.getColumnIndex("rps"));
		averageSensorRpSString = String.valueOf(averageSensorRpS);
		averageSensorSpR = sensorCursor.getFloat(sensorCursor.getColumnIndex("spr"));
		averageSensorSpRString = String.valueOf(averageSensorSpR);
		sensorCursor.close();
	}
	
	private void createDetailAverage() {
		Cursor wirelessCursor = sqLiteDatabase.rawQuery("SELECT bssid, COUNT(*) / COUNT(DISTINCT fingerprint_id) records, 1 / AVG(difference) * 1000 rps, AVG(difference) / 1000 spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"wireless w JOIN fingerprint f ON w.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY w.bssid;", new String[] { level, x, y });
		while (wirelessCursor.moveToNext()) {
			averageWirelessRecordsDetail.put(wirelessCursor.getString(wirelessCursor.getColumnIndex("bssid")), wirelessCursor.getFloat(wirelessCursor.getColumnIndex("records")));
			averageWirelessRpSDetail.put(wirelessCursor.getString(wirelessCursor.getColumnIndex("bssid")), wirelessCursor.getFloat(wirelessCursor.getColumnIndex("rps")));
			averageWirelessSpRDetail.put(wirelessCursor.getString(wirelessCursor.getColumnIndex("bssid")), wirelessCursor.getFloat(wirelessCursor.getColumnIndex("spr")));
			averageWirelessStrengthDetail.put(wirelessCursor.getString(wirelessCursor.getColumnIndex("bssid")), wirelessCursor.getFloat(wirelessCursor.getColumnIndex("rssi")));
			averageWirelessDistanceDetail.put(wirelessCursor.getString(wirelessCursor.getColumnIndex("bssid")), wirelessCursor.getFloat(wirelessCursor.getColumnIndex("distance")));
		}
		wirelessCursor.close();
		
		Cursor bluetoothCursor = sqLiteDatabase.rawQuery("SELECT bssid, COUNT(*) / COUNT(DISTINCT fingerprint_id) records, 1 / AVG(difference) * 1000 rps, AVG(difference) / 1000 spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"bluetooth b JOIN fingerprint f ON b.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY b.bssid;", new String[] { level, x, y });
		while (bluetoothCursor.moveToNext()) {
			averageBluetoothRecordsDetail.put(bluetoothCursor.getString(bluetoothCursor.getColumnIndex("bssid")), bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("records")));
			averageBluetoothRpSDetail.put(bluetoothCursor.getString(bluetoothCursor.getColumnIndex("bssid")), bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("rps")));
			averageBluetoothSpRDetail.put(bluetoothCursor.getString(bluetoothCursor.getColumnIndex("bssid")), bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("spr")));
			averageBluetoothStrengthDetail.put(bluetoothCursor.getString(bluetoothCursor.getColumnIndex("bssid")), bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("rssi")));
			averageBluetoothDistanceDetail.put(bluetoothCursor.getString(bluetoothCursor.getColumnIndex("bssid")), bluetoothCursor.getFloat(bluetoothCursor.getColumnIndex("distance")));
		}
		bluetoothCursor.close();
		
		Cursor cellularCursor = sqLiteDatabase.rawQuery("SELECT cid, COUNT(*) / COUNT(DISTINCT fingerprint_id) records, 1 / AVG(difference) * 1000 rps, AVG(difference) / 1000 spr, ABS(AVG(rssi)) rssi, AVG(distance) distance FROM " +
				"cellular c JOIN fingerprint f ON c.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY c.cid;", new String[] { level, x, y });
		while (cellularCursor.moveToNext()) {
			averageCellularRecordsDetail.put(cellularCursor.getString(cellularCursor.getColumnIndex("cid")), cellularCursor.getFloat(cellularCursor.getColumnIndex("records")));
			averageCellularRpSDetail.put(cellularCursor.getString(cellularCursor.getColumnIndex("cid")), cellularCursor.getFloat(cellularCursor.getColumnIndex("rps")));
			averageCellularSpRDetail.put(cellularCursor.getString(cellularCursor.getColumnIndex("cid")), cellularCursor.getFloat(cellularCursor.getColumnIndex("spr")));
			averageCellularStrengthDetail.put(cellularCursor.getString(cellularCursor.getColumnIndex("cid")), cellularCursor.getFloat(cellularCursor.getColumnIndex("rssi")));
			averageCellularDistanceDetail.put(cellularCursor.getString(cellularCursor.getColumnIndex("cid")), cellularCursor.getFloat(cellularCursor.getColumnIndex("distance")));
		}
		cellularCursor.close();
		
		Cursor sensorCursor = sqLiteDatabase.rawQuery("SELECT type, COUNT(*) / COUNT(DISTINCT fingerprint_id) records, 1 / AVG(difference) * 1000 rps, AVG(difference) / 1000 spr FROM " +
				"sensor s JOIN fingerprint f ON s.fingerprint_id = f.id WHERE f.level = ? AND f.x = ? AND f.y = ? GROUP BY s.type;", new String[] { level, x, y });
		while (sensorCursor.moveToNext()) {
			averageSensorRecordsDetail.put(sensorCursor.getString(sensorCursor.getColumnIndex("type")), sensorCursor.getFloat(sensorCursor.getColumnIndex("records")));
			averageSensorRpSDetail.put(sensorCursor.getString(sensorCursor.getColumnIndex("type")), sensorCursor.getFloat(sensorCursor.getColumnIndex("rps")));
			averageSensorSpRDetail.put(sensorCursor.getString(sensorCursor.getColumnIndex("type")), sensorCursor.getFloat(sensorCursor.getColumnIndex("spr")));
		}
		sensorCursor.close();
	}
	
	
	private TextView createWirelessSummary() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid) device, ABS(COUNT(DISTINCT bssid) / ? * 100 - 100) deviceDifference, " +
						"COUNT(*) record, ABS(COUNT(*) / ? * 100 - 100) recordDifference, " +
						"CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid) rps, ABS((CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid)) / ? * 100 - 100) rpsDifference, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid) spr, ABS((CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid)) / ? * 100 - 100) sprDifference, " +
						"ABS(AVG(rssi)) strength, ABS(ABS(AVG(rssi)) / ? * 100 - 100) strengthDifference, " +
						"AVG(distance) distance, ABS(AVG(distance) / ? * 100 - 100) distanceDifference FROM currentWireless;",
				new String[] { averageWirelessDevicesString, averageWirelessRecordsString,
						collectionLength, collectionLength, averageWirelessRpSString, collectionLength, collectionLength, averageWirelessSpRString,
						averageWirelessStrengthString, averageWirelessDistanceString
				});
		cursor.moveToFirst();
		
		textView = new TextView(context);
		textView.setId(View.generateViewId());
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setPadding(0, 12, 0, 12);
		textView.setText(String.format(context.getResources().getString(R.string.taskCollectorWirelessSummary),
				cursor.getFloat(cursor.getColumnIndex("device")), averageWirelessDevices, cursor.getFloat(cursor.getColumnIndex("deviceDifference")),
				cursor.getFloat(cursor.getColumnIndex("record")), averageWirelessRecords, cursor.getFloat(cursor.getColumnIndex("recordDifference")),
				cursor.getFloat(cursor.getColumnIndex("rps")), averageWirelessRpS, cursor.getFloat(cursor.getColumnIndex("rpsDifference")),
				cursor.getFloat(cursor.getColumnIndex("spr")), averageWirelessSpR, cursor.getFloat(cursor.getColumnIndex("sprDifference")),
				cursor.getFloat(cursor.getColumnIndex("strength")), averageWirelessStrength, cursor.getFloat(cursor.getColumnIndex("strengthDifference")),
				cursor.getFloat(cursor.getColumnIndex("distance")), averageWirelessDistance, cursor.getFloat(cursor.getColumnIndex("distanceDifference"))
		));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				wirelessShowDetail = !wirelessShowDetail;
			}
		});
		
		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.setMargins(0, 0, 0, wirelessShowDetail ? 0 : 0);
		if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
		textView.setLayoutParams(layoutParameters);
		textView.setBackgroundColor(cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		cursor.close();
		return textView;
	}
	
	private TextView createBluetoothSummary() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid) device, ABS(COUNT(DISTINCT bssid) / ? * 100 - 100) deviceDifference, " +
						"COUNT(*) record, ABS(COUNT(*) / ? * 100 - 100) recordDifference, " +
						"CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid) rps, ABS((CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid)) / ? * 100 - 100) rpsDifference, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid) spr, ABS((CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid)) / ? * 100 - 100) sprDifference, " +
						"ABS(AVG(rssi)) strength, ABS(ABS(AVG(rssi)) / ? * 100 - 100) strengthDifference, " +
						"AVG(distance) distance, ABS(AVG(distance) / ? * 100 - 100) distanceDifference FROM currentBluetooth;",
				new String[] { averageBluetoothDevicesString, averageBluetoothRecordsString,
						collectionLength, collectionLength, averageBluetoothRpSString, collectionLength, collectionLength, averageBluetoothSpRString,
						averageBluetoothStrengthString, averageBluetoothDistanceString
				});
		cursor.moveToFirst();
		
		textView = new TextView(context);
		textView.setId(View.generateViewId());
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setPadding(0, 12, 0, 12);
		textView.setText(String.format(context.getResources().getString(R.string.taskCollectorBluetoothSummary),
				cursor.getFloat(cursor.getColumnIndex("device")), averageBluetoothDevices, cursor.getFloat(cursor.getColumnIndex("deviceDifference")),
				cursor.getFloat(cursor.getColumnIndex("record")), averageBluetoothRecords, cursor.getFloat(cursor.getColumnIndex("recordDifference")),
				cursor.getFloat(cursor.getColumnIndex("rps")), averageBluetoothRpS, cursor.getFloat(cursor.getColumnIndex("rpsDifference")),
				cursor.getFloat(cursor.getColumnIndex("spr")), averageBluetoothSpR, cursor.getFloat(cursor.getColumnIndex("sprDifference")),
				cursor.getFloat(cursor.getColumnIndex("strength")), averageBluetoothStrength, cursor.getFloat(cursor.getColumnIndex("strengthDifference")),
				cursor.getFloat(cursor.getColumnIndex("distance")), averageBluetoothDistance, cursor.getFloat(cursor.getColumnIndex("distanceDifference"))
		));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				bluetoothShowDetail = !bluetoothShowDetail;
			}
		});
		
		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.setMargins(0, 0, 0, bluetoothShowDetail ? 0 : 0);
		if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
		textView.setLayoutParams(layoutParameters);
		textView.setBackgroundColor(cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		cursor.close();
		return textView;
	}
	
	private TextView createCellularSummary() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT cid) device, ABS(COUNT(DISTINCT cid) / ? * 100 - 100) deviceDifference, " +
						"COUNT(*) record, ABS(COUNT(*) / ? * 100 - 100) recordDifference, " +
						"CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT cid) rps, ABS((CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT cid)) / ? * 100 - 100) rpsDifference, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT cid) spr, ABS((CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT cid)) / ? * 100 - 100) sprDifference, " +
						"ABS(AVG(rssi)) strength, ABS(ABS(AVG(rssi)) / ? * 100 - 100) strengthDifference, " +
						"AVG(distance) distance, ABS(AVG(distance) / ? * 100 - 100) distanceDifference FROM currentCellular;",
				new String[] { averageCellularDevicesString, averageCellularRecordsString,
						collectionLength, collectionLength, averageCellularRpSString, collectionLength, collectionLength, averageCellularSpRString,
						averageCellularStrengthString, averageCellularDistanceString
				});
		cursor.moveToFirst();
		
		textView = new TextView(context);
		textView.setId(View.generateViewId());
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setPadding(0, 12, 0, 12);
		textView.setText(String.format(context.getResources().getString(R.string.taskCollectorCellularSummary),
				cursor.getFloat(cursor.getColumnIndex("device")), averageCellularDevices, cursor.getFloat(cursor.getColumnIndex("deviceDifference")),
				cursor.getFloat(cursor.getColumnIndex("record")), averageCellularRecords, cursor.getFloat(cursor.getColumnIndex("recordDifference")),
				cursor.getFloat(cursor.getColumnIndex("rps")), averageCellularRpS, cursor.getFloat(cursor.getColumnIndex("rpsDifference")),
				cursor.getFloat(cursor.getColumnIndex("spr")), averageCellularSpR, cursor.getFloat(cursor.getColumnIndex("sprDifference")),
				cursor.getFloat(cursor.getColumnIndex("strength")), averageCellularStrength, cursor.getFloat(cursor.getColumnIndex("strengthDifference")),
				cursor.getFloat(cursor.getColumnIndex("distance")), averageCellularDistance, cursor.getFloat(cursor.getColumnIndex("distanceDifference"))
		));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cellularShowDetail = !cellularShowDetail;
			}
		});
		
		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.setMargins(0, 0, 0, cellularShowDetail ? 0 : 0);
		if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
		textView.setLayoutParams(layoutParameters);
		textView.setBackgroundColor(cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(cursor.getColumnIndex("strengthDifference")) <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		cursor.close();
		return textView;
	}
	
	private TextView createSensorSummary() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT type) device, ABS(COUNT(DISTINCT type) / ? * 100 - 100) deviceDifference, " +
						"COUNT(*) record, ABS(COUNT(*) / ? * 100 - 100) recordDifference, " +
						"CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT type) rps, ABS((CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT type)) / ? * 100 - 100) rpsDifference, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT type) spr, ABS((CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT type)) / ? * 100 - 100) sprDifference FROM currentSensor;",
				new String[] { averageSensorDevicesString, averageSensorRecordsString,
						collectionLength, collectionLength, averageSensorRpSString, collectionLength, collectionLength, averageSensorSpRString
				});
		cursor.moveToFirst();
		
		textView = new TextView(context);
		textView.setId(View.generateViewId());
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setPadding(0, 12, 0, 12);
		textView.setText(String.format(context.getResources().getString(R.string.taskCollectorSensorSummary),
				cursor.getFloat(cursor.getColumnIndex("device")), averageSensorDevices, cursor.getFloat(cursor.getColumnIndex("deviceDifference")),
				cursor.getFloat(cursor.getColumnIndex("record")), averageSensorRecords, cursor.getFloat(cursor.getColumnIndex("recordDifference")),
				cursor.getFloat(cursor.getColumnIndex("rps")), averageSensorRpS, cursor.getFloat(cursor.getColumnIndex("rpsDifference")),
				cursor.getFloat(cursor.getColumnIndex("spr")), averageSensorSpR, cursor.getFloat(cursor.getColumnIndex("sprDifference"))
		));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sensorShowDetail = !sensorShowDetail;
			}
		});
		
		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.setMargins(0, 0, 0, sensorShowDetail ? 0 : 0);
		if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
		textView.setLayoutParams(layoutParameters);
		textView.setBackgroundColor(Color.GREEN);
		scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		cursor.close();
		return textView;
	}
	
	
	private void createWirelessDetail() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT ssid, bssid, COUNT(*) record, CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid) rps, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid) spr, ABS(AVG(rssi)) strength, " +
						"AVG(distance) distance FROM currentWireless GROUP BY bssid ORDER BY record DESC;",
				new String[] { collectionLength, collectionLength });
		
		while (cursor.moveToNext()) {
			float localAverageWirelessRecordsDetail = 0, localAverageWirelessRpSDetail = 0, localAverageWirelessSpRDetail = 0, localAverageWirelessStrengthDetail = 0, localAverageWirelessDistanceDetail = 0,
					localAverageWirelessStrengthDetailDifference = 0;
			if (averageWirelessRecordsDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageWirelessRecordsDetail = averageWirelessRecordsDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageWirelessRpSDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageWirelessRpSDetail = averageWirelessRpSDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageWirelessSpRDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageWirelessSpRDetail = averageWirelessSpRDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageWirelessStrengthDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageWirelessStrengthDetail = averageWirelessStrengthDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageWirelessDistanceDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageWirelessDistanceDetail = averageWirelessDistanceDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			
			textView = new TextView(context);
			textView.setId(View.generateViewId());
			textView.setTypeface(Typeface.MONOSPACE);
			textView.setPadding(0, 12, 0, 12);
			textView.setText(String.format(context.getResources().getString(R.string.taskCollectorWirelessDetail),
					cursor.getString(cursor.getColumnIndex("ssid")), cursor.getString(cursor.getColumnIndex("bssid")),
					cursor.getFloat(cursor.getColumnIndex("record")), localAverageWirelessRecordsDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("record")) / localAverageWirelessRecordsDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("rps")), localAverageWirelessRpSDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("rps")) / localAverageWirelessRpSDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("spr")), localAverageWirelessSpRDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("spr")) / localAverageWirelessSpRDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("strength")), localAverageWirelessStrengthDetail, localAverageWirelessStrengthDetailDifference = Math.abs(cursor.getFloat(cursor.getColumnIndex("strength")) / localAverageWirelessStrengthDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("distance")), localAverageWirelessDistanceDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("distance")) / localAverageWirelessDistanceDetail * 100 - 100)
			));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					wirelessShowDetail = !wirelessShowDetail;
				}
			});
			
			layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
			textView.setLayoutParams(layoutParameters);
			textView.setBackgroundColor(localAverageWirelessStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (localAverageWirelessStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
			scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		}
		cursor.close();
	}
	
	private void createBluetoothDetail() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT bssid, COUNT(*) record, CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT bssid) rps, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT bssid) spr, ABS(AVG(rssi)) strength, " +
						"AVG(distance) distance FROM currentBluetooth GROUP BY bssid ORDER BY record DESC;",
				new String[] { collectionLength, collectionLength });
		
		while (cursor.moveToNext()) {
			float localAverageBluetoothRecordsDetail = 0, localAverageBluetoothRpSDetail = 0, localAverageBluetoothSpRDetail = 0, localAverageBluetoothStrengthDetail = 0, localAverageBluetoothDistanceDetail = 0,
					localAverageBluetoothStrengthDetailDifference = 0;
			if (averageBluetoothRecordsDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageBluetoothRecordsDetail = averageBluetoothRecordsDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageBluetoothRpSDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageBluetoothRpSDetail = averageBluetoothRpSDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageBluetoothSpRDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageBluetoothSpRDetail = averageBluetoothSpRDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageBluetoothStrengthDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageBluetoothStrengthDetail = averageBluetoothStrengthDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			if (averageBluetoothDistanceDetail.containsKey(cursor.getString(cursor.getColumnIndex("bssid")))) localAverageBluetoothDistanceDetail = averageBluetoothDistanceDetail.get(cursor.getString(cursor.getColumnIndex("bssid")));
			
			textView = new TextView(context);
			textView.setId(View.generateViewId());
			textView.setTypeface(Typeface.MONOSPACE);
			textView.setPadding(0, 12, 0, 12);
			textView.setText(String.format(context.getResources().getString(R.string.taskCollectorBluetoothDetail),
					cursor.getString(cursor.getColumnIndex("bssid")),
					cursor.getFloat(cursor.getColumnIndex("record")), localAverageBluetoothRecordsDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("record")) / localAverageBluetoothRecordsDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("rps")), localAverageBluetoothRpSDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("rps")) / localAverageBluetoothRpSDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("spr")), localAverageBluetoothSpRDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("spr")) / localAverageBluetoothSpRDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("strength")), localAverageBluetoothStrengthDetail, localAverageBluetoothStrengthDetailDifference = Math.abs(cursor.getFloat(cursor.getColumnIndex("strength")) / localAverageBluetoothStrengthDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("distance")), localAverageBluetoothDistanceDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("distance")) / localAverageBluetoothDistanceDetail * 100 - 100)
			));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					bluetoothShowDetail = !bluetoothShowDetail;
				}
			});
			
			layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
			textView.setLayoutParams(layoutParameters);
			textView.setBackgroundColor(localAverageBluetoothStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (localAverageBluetoothStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
			scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		}
		cursor.close();
	}
	
	private void createCellularDetail() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT cid, lac, COUNT(*) record, CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT cid) rps, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT cid) spr, ABS(AVG(rssi)) strength, " +
						"AVG(distance) distance FROM currentCellular GROUP BY cid ORDER BY record DESC;",
				new String[] { collectionLength, collectionLength });
		
		while (cursor.moveToNext()) {
			float localAverageCellularRecordsDetail = 0, localAverageCellularRpSDetail = 0, localAverageCellularSpRDetail = 0, localAverageCellularStrengthDetail = 0, localAverageCellularDistanceDetail = 0,
					localAverageCellularStrengthDetailDifference = 0;
			if (averageCellularRecordsDetail.containsKey(cursor.getString(cursor.getColumnIndex("cid")))) localAverageCellularRecordsDetail = averageCellularRecordsDetail.get(cursor.getString(cursor.getColumnIndex("cid")));
			if (averageCellularRpSDetail.containsKey(cursor.getString(cursor.getColumnIndex("cid")))) localAverageCellularRpSDetail = averageCellularRpSDetail.get(cursor.getString(cursor.getColumnIndex("cid")));
			if (averageCellularSpRDetail.containsKey(cursor.getString(cursor.getColumnIndex("cid")))) localAverageCellularSpRDetail = averageCellularSpRDetail.get(cursor.getString(cursor.getColumnIndex("cid")));
			if (averageCellularStrengthDetail.containsKey(cursor.getString(cursor.getColumnIndex("cid")))) localAverageCellularStrengthDetail = averageCellularStrengthDetail.get(cursor.getString(cursor.getColumnIndex("cid")));
			if (averageCellularDistanceDetail.containsKey(cursor.getString(cursor.getColumnIndex("cid")))) localAverageCellularDistanceDetail = averageCellularDistanceDetail.get(cursor.getString(cursor.getColumnIndex("cid")));
			
			textView = new TextView(context);
			textView.setId(View.generateViewId());
			textView.setTypeface(Typeface.MONOSPACE);
			textView.setPadding(0, 12, 0, 12);
			textView.setText(String.format(context.getResources().getString(R.string.taskCollectorCellularDetail),
					cursor.getFloat(cursor.getColumnIndex("cid")), cursor.getFloat(cursor.getColumnIndex("lac")),
					cursor.getFloat(cursor.getColumnIndex("record")), localAverageCellularRecordsDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("record")) / localAverageCellularRecordsDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("rps")), localAverageCellularRpSDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("rps")) / localAverageCellularRpSDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("spr")), localAverageCellularSpRDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("spr")) / localAverageCellularSpRDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("strength")), localAverageCellularStrengthDetail, localAverageCellularStrengthDetailDifference = Math.abs(cursor.getFloat(cursor.getColumnIndex("strength")) / localAverageCellularStrengthDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("distance")), localAverageCellularDistanceDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("distance")) / localAverageCellularDistanceDetail * 100 - 100)
			));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					cellularShowDetail = !cellularShowDetail;
				}
			});
			
			layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
			textView.setLayoutParams(layoutParameters);
			textView.setBackgroundColor(localAverageCellularStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (localAverageCellularStrengthDetailDifference <= Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
			scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		}
		cursor.close();
	}
	
	private void createSensorDetail() {
		String collectionLength = String.valueOf(System.currentTimeMillis() - collectionStart);
		cursor = sqLiteDatabase.rawQuery("SELECT type, COUNT(*) record, CAST(COUNT(id) AS REAL) / ? * 1000 / COUNT(DISTINCT type) rps, " +
						"CAST(? AS REAL) / COUNT(id) / 1000 * COUNT(DISTINCT type) spr FROM currentSensor GROUP BY type ORDER BY record DESC;",
				new String[] { collectionLength, collectionLength });
		
		while (cursor.moveToNext()) {
			float localAverageSensorRecordsDetail = 0, localAverageSensorRpSDetail = 0, localAverageSensorSpRDetail = 0;
			if (averageSensorRecordsDetail.containsKey(cursor.getString(cursor.getColumnIndex("type")))) localAverageSensorRecordsDetail = averageSensorRecordsDetail.get(cursor.getString(cursor.getColumnIndex("type")));
			if (averageSensorRpSDetail.containsKey(cursor.getString(cursor.getColumnIndex("type")))) localAverageSensorRpSDetail = averageSensorRpSDetail.get(cursor.getString(cursor.getColumnIndex("type")));
			if (averageSensorSpRDetail.containsKey(cursor.getString(cursor.getColumnIndex("type")))) localAverageSensorSpRDetail = averageSensorSpRDetail.get(cursor.getString(cursor.getColumnIndex("type")));
			
			textView = new TextView(context);
			textView.setId(View.generateViewId());
			textView.setTypeface(Typeface.MONOSPACE);
			textView.setText(String.format(context.getResources().getString(R.string.taskCollectorSensorDetail),
					cz.uhk.fim.kikm.navigation.util.Utils.convertSensorName(cursor.getInt(cursor.getColumnIndex("type"))),
					cursor.getFloat(cursor.getColumnIndex("record")), localAverageSensorRecordsDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("record")) / localAverageSensorRecordsDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("rps")), localAverageSensorRpSDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("rps")) / localAverageSensorRpSDetail * 100 - 100),
					cursor.getFloat(cursor.getColumnIndex("spr")), localAverageSensorSpRDetail, Math.abs(cursor.getFloat(cursor.getColumnIndex("spr")) / localAverageSensorSpRDetail * 100 - 100)
			));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					sensorShowDetail = !sensorShowDetail;
				}
			});
			
			layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if ((textBoxChildren = scrollViewRelativeLayout.getChildCount()) != 0) layoutParameters.addRule(RelativeLayout.BELOW, scrollViewRelativeLayout.getChildAt(textBoxChildren - 1).getId());
			textView.setLayoutParams(layoutParameters);
			textView.setBackgroundColor(Color.GREEN);
			scrollViewRelativeLayout.addView(textView, scrollViewRelativeLayout.getChildCount());
		}
		cursor.close();
	}
}