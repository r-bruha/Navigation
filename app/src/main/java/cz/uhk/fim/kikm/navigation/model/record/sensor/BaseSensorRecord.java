package cz.uhk.fim.kikm.navigation.model.record.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.App;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class BaseSensorRecord implements Serializable {
	private float x, y, z;
	private int time, difference;
	
	public BaseSensorRecord() {
		
	}
	
	public BaseSensorRecord(float x, float y, float z, int time, int difference) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.time = time;
		this.difference = difference;
	}
	
	@SuppressWarnings("unchecked")
	public BaseSensorRecord(Object object) {
		Map<String, Object> baseSensorRecord = (HashMap<String, Object>) object;
		this.x = Float.parseFloat(baseSensorRecord.get("x").toString());
		this.y = Float.parseFloat(baseSensorRecord.get("y").toString());
		this.z = Float.parseFloat(baseSensorRecord.get("z").toString());
		//this.time = Integer.parseInt(baseSensorRecord.get("time").toString());
		//this.difference = Integer.parseInt(baseSensorRecord.get("difference").toString());
	}
	
	public float getX() {
		return x;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public float getZ() {
		return z;
	}
	
	public void setZ(float z) {
		this.z = z;
	}
	
	public int getTime() {
		return time;
	}
	
	public BaseSensorRecord setTime(int time) {
		this.time = time;
		return this;
	}
	
	public int getDifference() {
		return difference;
	}
	
	public BaseSensorRecord setDifference(int difference) {
		this.difference = difference;
		return this;
	}
	
	public String getAsJson() {
		return String.format(Locale.ROOT, App.getContext().getString(R.string.modelRecordBaseSensorJson), x, y, z);
	}
	
	public Map<String, Object> getAsMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		//map.put("time", time);
		//map.put("difference", difference);
		return map;
	}
}