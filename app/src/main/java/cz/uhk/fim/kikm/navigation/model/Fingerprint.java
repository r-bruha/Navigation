package cz.uhk.fim.kikm.navigation.model;

import android.preference.PreferenceManager;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.App;
import cz.uhk.fim.kikm.navigation.model.list.BluetoothRecords;
import cz.uhk.fim.kikm.navigation.model.list.CellularRecords;
import cz.uhk.fim.kikm.navigation.model.list.SensorRecords;
import cz.uhk.fim.kikm.navigation.model.list.WirelessRecords;
import cz.uhk.fim.kikm.navigation.model.record.BluetoothRecord;
import cz.uhk.fim.kikm.navigation.model.record.CellularRecord;
import cz.uhk.fim.kikm.navigation.model.record.DeviceRecord;
import cz.uhk.fim.kikm.navigation.model.record.WirelessRecord;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class Fingerprint implements Serializable {
	private String id, level, user;
	private int x, y;
	private Date timestamp, finish;
	private WirelessRecords<WirelessRecord> wirelessRecords;
	private BluetoothRecords<BluetoothRecord> bluetoothRecords;
	private CellularRecords<CellularRecord> cellularRecords;
	private SensorRecords sensorRecords;
	private DeviceRecord deviceRecord;
	
	public Fingerprint(String id, Date timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}
	
	public Fingerprint(Date timestamp, Date finish, String level, int x, int y, String user) {
		this.timestamp = timestamp;
		this.finish = finish;
		this.level = level;
		this.x = x;
		this.y = y;
		this.user = user;
	}
	
	public Fingerprint(int x, int y, String level, WirelessRecords<WirelessRecord> wirelessRecords, BluetoothRecords<BluetoothRecord> bluetoothRecords, CellularRecords<CellularRecord> cellularRecords,
					   SensorRecords sensorRecords, DeviceRecord deviceRecord) {
		this.x = x;
		this.y = y;
		this.level = level;
		this.user = PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("couchDatabase", "Unknown");
		this.timestamp = new Date();
		this.wirelessRecords = wirelessRecords;
		this.bluetoothRecords = bluetoothRecords;
		this.cellularRecords = cellularRecords;
		this.sensorRecords = sensorRecords;
		this.deviceRecord = deviceRecord;
	}
	
	public Fingerprint(Map<String, Object> map) {
		this.id = map.get("_id").toString();
		this.x = Integer.parseInt(map.get("x").toString());
		this.y = Integer.parseInt(map.get("y").toString());
		this.level = map.get("level").toString();
		this.user = map.get("user").toString();
		this.timestamp = new Date(Double.valueOf(map.get("timestamp").toString()).longValue());
		this.finish = new Date(Double.valueOf(map.get("finish").toString()).longValue());
		this.wirelessRecords = new WirelessRecords<>(map.get("wirelessRecords"));
		this.bluetoothRecords = new BluetoothRecords<>(map.get("bluetoothRecords"));
		this.cellularRecords = new CellularRecords<>(map.get("cellularRecords"));
		this.sensorRecords = new SensorRecords(map.get("sensorRecords"));
		this.deviceRecord = new DeviceRecord(map.get("deviceRecord"));
	}
	
	public String getId() {
		return id;
	}
	
	public Fingerprint setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getLevel() {
		return level;
	}
	
	public Fingerprint setLevel(String level) {
		this.level = level;
		return this;
	}
	
	public String getUser() {
		return user;
	}
	
	public Fingerprint setUser(String user) {
		this.user = user;
		return this;
	}
	
	public int getX() {
		return x;
	}
	
	public Fingerprint setX(int x) {
		this.x = x;
		return this;
	}
	
	public int getY() {
		return y;
	}
	
	public Fingerprint setY(int y) {
		this.y = y;
		return this;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public Fingerprint setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public Date getFinish() {
		return finish;
	}
	
	public Fingerprint setFinish(Date finish) {
		this.finish = finish;
		return this;
	}
	
	public WirelessRecords<WirelessRecord> getWirelessRecords() {
		return wirelessRecords;
	}
	
	public Fingerprint setWirelessRecords(WirelessRecords<WirelessRecord> wirelessRecords) {
		this.wirelessRecords = wirelessRecords;
		return this;
	}
	
	public BluetoothRecords<BluetoothRecord> getBluetoothRecords() {
		return bluetoothRecords;
	}
	
	public Fingerprint setBluetoothRecords(BluetoothRecords<BluetoothRecord> bluetoothRecords) {
		this.bluetoothRecords = bluetoothRecords;
		return this;
	}
	
	public CellularRecords<CellularRecord> getCellularRecords() {
		return cellularRecords;
	}
	
	public Fingerprint setCellularRecords(CellularRecords<CellularRecord> cellularRecords) {
		this.cellularRecords = cellularRecords;
		return this;
	}
	
	public SensorRecords getSensorRecords() {
		return sensorRecords;
	}
	
	public Fingerprint setSensorRecords(SensorRecords sensorRecords) {
		this.sensorRecords = sensorRecords;
		return this;
	}
	
	public DeviceRecord getDeviceRecord() {
		return deviceRecord;
	}
	
	public Fingerprint setDeviceRecord(DeviceRecord deviceRecord) {
		this.deviceRecord = deviceRecord;
		return this;
	}
	
	public String getAsJson() {
		return String.format(App.getContext().getString(R.string.modelFingerprintJson),
				x, y, level, user, timestamp.getTime(), wirelessRecords.getAsJson(), bluetoothRecords.getAsJson(), cellularRecords.getAsJson(), sensorRecords.getAsJson(), deviceRecord.getAsJson());
	}
	
	public Map<String, Object> getAsMap() {
		Map<String, Object> fingerprint = new HashMap<>();
		fingerprint.put("x", x);
		fingerprint.put("y", y);
		fingerprint.put("level", level);
		fingerprint.put("user", user);
		fingerprint.put("timestamp", timestamp);
		fingerprint.put("finish", finish);
		fingerprint.put("wirelessRecords", wirelessRecords.getAsMap());
		fingerprint.put("bluetoothRecords", bluetoothRecords.getAsMap());
		fingerprint.put("cellularRecords", cellularRecords.getAsMap());
		fingerprint.put("sensorRecords", sensorRecords.getAsMap());
		fingerprint.put("deviceRecord", deviceRecord.getAsMap());
		return fingerprint;
	}
}
