package cz.uhk.fim.kikm.navigation.model.record;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.App;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class BluetoothRecord implements Serializable {
	private String bssid;
	private int rssi, time, difference;
	private float distance;
	private Date timestamp;

	public BluetoothRecord(Date timestamp, String bssid, int rssi, float distance, int time, int difference) {
		this.timestamp = timestamp;
		this.bssid = bssid;
		this.rssi = rssi;
		this.distance = distance;
		this.time = time;
		this.difference = difference;
	}

	public BluetoothRecord(Object object) {
		Map<String, Object> bluetoothRecord = (HashMap<String, Object>) object;
		this.timestamp = new Date(Double.valueOf(bluetoothRecord.get("timestamp").toString()).longValue());
		this.bssid = bluetoothRecord.get("bssid").toString();
		this.rssi = Integer.valueOf(bluetoothRecord.get("rssi").toString());
		this.distance = Float.valueOf(bluetoothRecord.get("distance").toString());
		this.time = Integer.valueOf(bluetoothRecord.get("time").toString());
		this.difference = Integer.valueOf(bluetoothRecord.get("difference").toString());
	}

	public String getBssid() {
		return bssid;
	}

	public BluetoothRecord setBssid(String bssid) {
		this.bssid = bssid;
		return this;
	}

	public int getRssi() {
		return rssi;
	}

	public BluetoothRecord setRssi(int rssi) {
		this.rssi = rssi;
		return this;
	}

	public int getTime() {
		return time;
	}

	public BluetoothRecord setTime(int time) {
		this.time = time;
		return this;
	}

	public int getDifference() {
		return difference;
	}

	public BluetoothRecord setDifference(int difference) {
		this.difference = difference;
		return this;
	}

	public float getDistance() {
		return distance;
	}

	public BluetoothRecord setDistance(float distance) {
		this.distance = distance;
		return this;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public BluetoothRecord setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getAsJson() {
		return String.format(Locale.ROOT, App.getContext().getString(R.string.modelRecordBluetoothJson), "Unknown", bssid, rssi, distance, time);
	}

	public Map<String, Object> getAsMap() {
		Map<String, Object> bluetoothRecord = new HashMap<>();
		bluetoothRecord.put("timestamp", timestamp);
		bluetoothRecord.put("bssid", bssid);
		bluetoothRecord.put("rssi", rssi);
		bluetoothRecord.put("distance", distance);
		bluetoothRecord.put("time", time);
		bluetoothRecord.put("difference", difference);
		return bluetoothRecord;
	}
}