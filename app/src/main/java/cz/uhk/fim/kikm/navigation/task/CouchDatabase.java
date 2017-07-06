package cz.uhk.fim.kikm.navigation.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.os.AsyncTask;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
public class CouchDatabase extends AsyncTask<Void, Void, Void> {
	public static final int MODE_DOWNLOAD = 1, MODE_UPLOAD = 2;
	private Context context;
	private Database database;
	private Replication replication;
	private ProgressDialog progressDialog;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private int mode = MODE_UPLOAD;
	
	public CouchDatabase(Context context, int mode) {
		try {
			this.context = context;
			this.mode = mode;
			this.database = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS).getDatabase(Settings.COUCH_DATABASE_NAME);
			this.database.getView(Settings.COUCH_DATABASE_NAME).setMap(new Mapper() {
				@Override
				public void map(Map<String, Object> document, Emitter emitter) {
					emitter.emit(document.get("timestamp"), document);
				}
			}, "1");
		} catch (IOException | CouchbaseLiteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(context.getString(mode == MODE_DOWNLOAD ? R.string.taskCouchDatabaseDownloadProgressTitle : R.string.taskCouchDatabaseUploadProgressTitle));
		progressDialog.setProgress(0);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), 0, 0));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);
		progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.taskCouchDatabaseNeutralButton), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (replication != null) replication.stop();
				progressDialog.dismiss();
			}
		});
		progressDialog.show();
	}
	
	@Override
	protected Void doInBackground(Void... voids) {
		if (mode == MODE_DOWNLOAD) {
			downloadDatabase();
		} else uploadDatabase();
		return null;
	}
	
	@Override
	protected void onPostExecute(Void aVoid) {
		
	}
	
	@SuppressWarnings({ "ConstantConditions", "unchecked" })
	private void downloadDatabase() {
		try {
			replication = database.createPullReplication(new URL(Settings.COUCH_DATABASE_URL));
			replication.addChangeListener(new Replication.ChangeListener() {
				@Override
				public void changed(Replication.ChangeEvent changeEvent) {
					if (replication.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE) {
						progressDialog.setMax(changeEvent.getChangeCount());
						progressDialog.setProgress(changeEvent.getCompletedChangeCount());
						progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
					} else {
						SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
						sqLiteDatabase.execSQL("DELETE FROM fingerprint;");
						sqLiteDatabase.execSQL("DELETE FROM wireless;");
						sqLiteDatabase.execSQL("DELETE FROM bluetooth;");
						sqLiteDatabase.execSQL("DELETE FROM cellular;");
						sqLiteDatabase.execSQL("DELETE FROM sensor;");
						sqLiteDatabase.execSQL("DELETE FROM device;");
						sqLiteDatabase.execSQL("DROP INDEX IK_fingerprint;");
						sqLiteDatabase.execSQL("DROP INDEX IK_wireless;");
						sqLiteDatabase.execSQL("DROP INDEX IK_bluetooth;");
						sqLiteDatabase.execSQL("DROP INDEX IK_cellular;");
						sqLiteDatabase.execSQL("DROP INDEX IK_sensor;");
						sqLiteDatabase.execSQL("DROP INDEX IK_device;");
						sqLiteDatabase.execSQL("VACUUM;");
						
						int documentsCount = database.getDocumentCount();
						progressDialog.setMax(progressDialog.getMax() + database.getDocumentCount());
						progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
						for (int i = 0; i < documentsCount; i += Settings.COUCH_DATABASE_BATCH_SIZE) {
							ArrayList<Fingerprint> fingerprints = getDocuments(Settings.COUCH_DATABASE_BATCH_SIZE, i);
							sqLiteDatabase.beginTransaction();
							for (Fingerprint fingerprint : fingerprints) {
								sqLiteDatabase.execSQL("INSERT INTO fingerprint (timestamp, finish, level, x, y, user) VALUES (?, ?, ?, ?, ?, ?);",
										new Object[] { simpleDateFormat.format(fingerprint.getTimestamp()), simpleDateFormat.format(fingerprint.getFinish()), fingerprint.getLevel(), fingerprint.getX(), fingerprint.getY(), fingerprint.getUser() });
								Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX(id) FROM fingerprint;", new String[] {});
								cursor.moveToFirst();
								String lastID = cursor.getString(0);
								cursor.close();
								
								for (WirelessRecord wR : fingerprint.getWirelessRecords()) {
									sqLiteDatabase.execSQL("INSERT INTO wireless (fingerprint_id, timestamp, ssid, bssid, rssi, distance, time, difference, frequency) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
											new Object[] { lastID, simpleDateFormat.format(wR.getTimestamp()), wR.getSsid(), wR.getBssid(), wR.getRssi(), wR.getDistance(), wR.getTime(), wR.getDifference(), wR.getFrequency() });
								}
								
								for (BluetoothRecord bR : fingerprint.getBluetoothRecords()) {
									sqLiteDatabase.execSQL("INSERT INTO bluetooth (fingerprint_id, timestamp, bssid, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?);",
											new Object[] { lastID, simpleDateFormat.format(bR.getTimestamp()), bR.getBssid(), bR.getRssi(), bR.getDistance(), bR.getTime(), bR.getDifference() });
								}
								
								for (CellularRecord cR : fingerprint.getCellularRecords()) {
									sqLiteDatabase.execSQL("INSERT INTO cellular (fingerprint_id, timestamp, cid, lac, psc, type, rssi, distance, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
											new Object[] { lastID, simpleDateFormat.format(cR.getTimestamp()), cR.getCid(), cR.getLac(), cR.getPsc(), cR.getType(), cR.getRssi(), cR.getDistance(), cR.getTime(), cR.getDifference() });
								}
								
								SensorRecords sensorRecords = fingerprint.getSensorRecords();
								LinearAccelerationRecord lR = sensorRecords.getLinearAccelerationRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_LINEAR_ACCELERATION, lR.getX(), lR.getY(), lR.getZ(), lR.getTime(), lR.getDifference() });
								
								AccelerationRecord aR = sensorRecords.getAccelerationRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_ACCELEROMETER, aR.getX(), aR.getY(), aR.getZ(), aR.getTime(), aR.getDifference() });
								
								GravitationRecord grR = sensorRecords.getGravitationRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_GRAVITY, grR.getX(), grR.getY(), grR.getZ(), grR.getTime(), grR.getDifference() });
								
								GyroscopeRecord gyR = sensorRecords.getGyroscopeRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_GYROSCOPE, gyR.getX(), gyR.getY(), gyR.getZ(), gyR.getTime(), gyR.getDifference() });
								
								MagneticRecord mR = sensorRecords.getMagneticRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_MAGNETIC_FIELD, mR.getX(), mR.getY(), mR.getZ(), mR.getTime(), mR.getDifference() });
								
								RotationRecord rR = sensorRecords.getRotationRecord();
								sqLiteDatabase.execSQL("INSERT INTO sensor (fingerprint_id, timestamp, type, x, y, z, time, difference) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
										new Object[] { lastID, simpleDateFormat.format(new Date()), Sensor.TYPE_ROTATION_VECTOR, rR.getX(), rR.getY(), rR.getZ(), rR.getTime(), rR.getDifference() });
								
								
								DeviceRecord dR = fingerprint.getDeviceRecord();
								sqLiteDatabase.execSQL("INSERT INTO device (fingerprint_id, did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", new Object[] {
										lastID, dR.getId(), dR.getBoard(), dR.getBootloader(), dR.getBrand(), dR.getDevice(), dR.getDisplay(), dR.getFingerprint(), dR.getHardware(), dR.getHost(),
										dR.getManufacturer(), dR.getModel(), dR.getProduct(), dR.getSerial(), dR.getTags(), dR.getTelephone(), dR.getType(), dR.getUser(), dR.getOs(), dR.getApi()
								});
								
								progressDialog.setProgress(progressDialog.getProgress() + 1);
								progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
							}
							sqLiteDatabase.setTransactionSuccessful();
							sqLiteDatabase.endTransaction();
						}
						
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_fingerprint ON fingerprint (level, x, y);");
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_wireless ON wireless (fingerprint_id, bssid, rssi, distance);");
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_bluetooth ON bluetooth (fingerprint_id, bssid, rssi, distance);");
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_cellular ON cellular (fingerprint_id, cid, rssi, distance);");
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_sensor ON sensor (fingerprint_id, type, x, y, z);");
						sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS IK_device ON device (fingerprint_id);");
						sqLiteDatabase.close();
						progressDialog.dismiss();
						((MainActivity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((MainActivity) context).recreate();
							}
						});
					}
				}
			});
			replication.start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "ConstantConditions", "unchecked" })
	private void uploadDatabase() {
		try {
			SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
			Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, timestamp, finish, level, x, y, user FROM fingerprint ORDER BY id;", new String[] {});
			int documentsCount = database.getDocumentCount();
			
			progressDialog.setMax(cursor.getCount() + documentsCount);
			for (int i = 0; i < documentsCount; i += Settings.COUCH_DATABASE_BATCH_SIZE) {
				ArrayList<Fingerprint> fingerprints = getDocuments(Settings.COUCH_DATABASE_BATCH_SIZE, documentsCount - i - Settings.COUCH_DATABASE_BATCH_SIZE);
				progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
				for (Fingerprint fingerprint : fingerprints) {
					removeDocument(fingerprint);
					progressDialog.setProgress(progressDialog.getProgress() + 1);
					progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
				}
			}
			
			while (cursor.moveToNext()) {
				Fingerprint fingerprint = new Fingerprint(simpleDateFormat.parse(cursor.getString(1)), simpleDateFormat.parse(cursor.getString(2)), cursor.getString(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6));
				Cursor wirelessCursor = sqLiteDatabase.rawQuery("SELECT timestamp, ssid, bssid, rssi, distance, time, difference, frequency FROM wireless WHERE fingerprint_id = ? ORDER BY timestamp DESC;", new String[] { cursor.getString(0) });
				
				WirelessRecords wirelessRecords = new WirelessRecords();
				while (wirelessCursor.moveToNext()) wirelessRecords.add(new WirelessRecord(simpleDateFormat.parse(wirelessCursor.getString(0)), wirelessCursor.getString(1), wirelessCursor.getString(2), wirelessCursor.getInt(3), wirelessCursor.getFloat(4), wirelessCursor.getInt(5), wirelessCursor.getInt(6), wirelessCursor.getInt(7)));
				wirelessCursor.close();
				
				Cursor bluetoothCursor = sqLiteDatabase.rawQuery("SELECT timestamp, bssid, rssi, distance, time, difference FROM bluetooth WHERE fingerprint_id = ? ORDER BY timestamp DESC;", new String[] { cursor.getString(0) });
				BluetoothRecords bluetoothRecords = new BluetoothRecords();
				while (bluetoothCursor.moveToNext()) bluetoothRecords.add(new BluetoothRecord(simpleDateFormat.parse(bluetoothCursor.getString(0)), bluetoothCursor.getString(1), bluetoothCursor.getInt(2), bluetoothCursor.getFloat(3), bluetoothCursor.getInt(4), bluetoothCursor.getInt(5)));
				bluetoothCursor.close();
				
				Cursor cellularCursor = sqLiteDatabase.rawQuery("SELECT timestamp, cid, lac, psc, type, rssi, distance, time, difference FROM cellular WHERE fingerprint_id = ? ORDER BY timestamp DESC;", new String[] { cursor.getString(0) });
				CellularRecords cellularRecords = new CellularRecords();
				while (cellularCursor.moveToNext()) cellularRecords.add(new CellularRecord(simpleDateFormat.parse(cellularCursor.getString(0)), cellularCursor.getString(1), cellularCursor.getString(2), cellularCursor.getString(3), cellularCursor.getString(4), cellularCursor.getInt(5), cellularCursor.getInt(6), cellularCursor.getInt(7), cellularCursor.getInt(8)));
				cellularCursor.close();
				
				Cursor sensorCursor = sqLiteDatabase.rawQuery("SELECT timestamp, type, AVG(x), AVG(y), AVG(z), AVG(time), AVG(difference) FROM sensor WHERE fingerprint_id = ? GROUP BY type ORDER BY timestamp DESC;", new String[] { cursor.getString(0) });
				
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
				Cursor deviceCursor = sqLiteDatabase.rawQuery("SELECT did, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api FROM device WHERE fingerprint_id = ?", new String[] { cursor.getString(0) });
				deviceCursor.moveToFirst();
				
				deviceRecord.setId(deviceCursor.getString(0)).setBoard(deviceCursor.getString(1)).setBootloader(deviceCursor.getString(2))
						.setBrand(deviceCursor.getString(3)).setDevice(deviceCursor.getString(4)).setDisplay(deviceCursor.getString(5))
						.setFingerprint(deviceCursor.getString(6)).setHardware(deviceCursor.getString(7)).setHost(deviceCursor.getString(8))
						.setManufacturer(deviceCursor.getString(9)).setModel(deviceCursor.getString(10)).setProduct(deviceCursor.getString(11))
						.setSerial(deviceCursor.getString(12)).setTags(deviceCursor.getString(13)).setTelephone(deviceCursor.getString(14))
						.setType(deviceCursor.getString(15)).setUser(deviceCursor.getString(16)).setOs(deviceCursor.getString(17)).setApi(deviceCursor.getInt(18));
				deviceCursor.close();
				
				progressDialog.setProgress(progressDialog.getProgress() + 1);
				progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
				
				fingerprint.setWirelessRecords(wirelessRecords).setBluetoothRecords(bluetoothRecords).setCellularRecords(cellularRecords).setSensorRecords(sensorRecords).setDeviceRecord(deviceRecord);
				createDocument(fingerprint);
			}
			cursor.close();
			sqLiteDatabase.close();
			
			final int progressMaximum = progressDialog.getMax(), progressCurrent = progressDialog.getProgress();
			replication = database.createPushReplication(new URL(Settings.COUCH_DATABASE_URL));
			replication.addChangeListener(new Replication.ChangeListener() {
				@Override
				public void changed(Replication.ChangeEvent changeEvent) {
					if (replication.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE) {
						progressDialog.setMax(progressMaximum + changeEvent.getChangeCount());
						progressDialog.setProgress(progressCurrent + changeEvent.getCompletedChangeCount());
						progressDialog.setProgressNumberFormat(String.format(context.getString(R.string.taskCouchDatabaseProgressStatus), progressDialog.getProgress(), progressDialog.getMax()));
					} else {
						SQLiteDatabase innerSqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
						innerSqLiteDatabase.execSQL("UPDATE settings SET value = ? WHERE key = ?;", new Object[] { new Date().getTime() / 1000, "lastUpload" });
						innerSqLiteDatabase.close();
						progressDialog.dismiss();
						((MainActivity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								((MainActivity) context).recreate();
							}
						});
					}
				}
			});
			replication.start();
		} catch (MalformedURLException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<Fingerprint> getDocuments(int limit, int offset) {
		ArrayList<Fingerprint> fingerprints = new ArrayList<>();
		try { // TODO: Rebuilding index is very slow with large amount of documents, maybe parse JSON instead?
			Query query = database.getView(Settings.COUCH_DATABASE_NAME).createQuery();
			query.setMapOnly(true);
			query.setDescending(true);
			query.setLimit(limit);
			query.setSkip(offset);
			Iterator<QueryRow> iterator = query.run();
			while (iterator.hasNext()) fingerprints.add(new Fingerprint(iterator.next().getDocument().getProperties()));
		} catch (CouchbaseLiteException e) {
			e.printStackTrace();
		}
		return fingerprints;
	}
	
	private void createDocument(Fingerprint fingerprint) {
		try {
			database.createDocument().putProperties(fingerprint.getAsMap());
		} catch (CouchbaseLiteException e) {
			e.printStackTrace();
		}
	}
	
	private void removeDocument(Fingerprint fingerprint) {
		try {
			database.getDocument(fingerprint.getId()).delete();
		} catch (CouchbaseLiteException e) {
			e.printStackTrace();
		}
	}
}