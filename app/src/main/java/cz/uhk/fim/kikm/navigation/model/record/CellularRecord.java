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
public class CellularRecord implements Serializable {
	private String cid, lac, psc, type;
	private int rssi, time, difference;
	private float distance;
	private Date timestamp;

	public CellularRecord(Date timestamp, String cid, String lac, String psc, String type, int rssi, int distance, int time, int difference) {
		this.timestamp = timestamp;
		this.cid = cid;
		this.lac = lac;
		this.psc = psc;
		this.type = type;
		this.rssi = rssi;
		this.distance = distance;
		this.time = time;
		this.difference = difference;
	}

	public CellularRecord(Object object) {
		Map<String, Object> cellularRecord = (HashMap<String, Object>) object;
		this.timestamp = new Date(Double.valueOf(cellularRecord.get("timestamp").toString()).longValue());
		this.cid = cellularRecord.get("cid").toString();
		this.lac = cellularRecord.get("lac").toString();
		this.psc = cellularRecord.get("psc").toString();
		this.type = cellularRecord.get("type").toString();
		this.rssi = Integer.valueOf(cellularRecord.get("rssi").toString());
		this.distance = Float.valueOf(cellularRecord.get("distance").toString());
		this.time = Integer.valueOf(cellularRecord.get("time").toString());
		this.difference = Integer.valueOf(cellularRecord.get("difference").toString());
	}

	public String getCid() {
		return cid;
	}

	public CellularRecord setCid(String cid) {
		this.cid = cid;
		return this;
	}

	public String getLac() {
		return lac;
	}

	public CellularRecord setLac(String lac) {
		this.lac = lac;
		return this;
	}

	public String getPsc() {
		return psc;
	}

	public CellularRecord setPsc(String psc) {
		this.psc = psc;
		return this;
	}

	public String getType() {
		return type;
	}

	public CellularRecord setType(String type) {
		this.type = type;
		return this;
	}

	public int getRssi() {
		return rssi;
	}

	public CellularRecord setRssi(int rssi) {
		this.rssi = rssi;
		return this;
	}

	public int getTime() {
		return time;
	}

	public CellularRecord setTime(int time) {
		this.time = time;
		return this;
	}

	public int getDifference() {
		return difference;
	}

	public CellularRecord setDifference(int difference) {
		this.difference = difference;
		return this;
	}

	public float getDistance() {
		return distance;
	}

	public CellularRecord setDistance(float distance) {
		this.distance = distance;
		return this;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public CellularRecord setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getAsJson() {
		return String.format(Locale.ROOT, App.getContext().getString(R.string.modelRecordCellularJson), cid, lac, psc, type, rssi, time);
	}

	public Map<String, Object> getAsMap() {
		Map<String, Object> cellularRecord = new HashMap<>();
		cellularRecord.put("timestamp", timestamp);
		cellularRecord.put("cid", cid);
		cellularRecord.put("lac", lac);
		cellularRecord.put("psc", psc);
		cellularRecord.put("type", type);
		cellularRecord.put("rssi", rssi);
		cellularRecord.put("distance", distance);
		cellularRecord.put("time", time);
		cellularRecord.put("difference", difference);
		return cellularRecord;
	}
}