package cz.uhk.fim.kikm.navigation.model.list;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.model.record.sensor.AccelerationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.GravitationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.GyroscopeRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.LinearAccelerationRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.MagneticRecord;
import cz.uhk.fim.kikm.navigation.model.record.sensor.RotationRecord;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class SensorRecords implements Serializable {
	private LinearAccelerationRecord linearAccelerationRecord;
	private AccelerationRecord accelerationRecord;
	private GravitationRecord gravitationRecord;
	private GyroscopeRecord gyroscopeRecord;
	private MagneticRecord magneticRecord;
	private RotationRecord rotationRecord;
	
	public SensorRecords(LinearAccelerationRecord linearAccelerationRecord, AccelerationRecord accelerationRecord, GravitationRecord gravitationRecord, GyroscopeRecord gyroscopeRecord, MagneticRecord magneticRecord, RotationRecord rotationRecord) {
		this.linearAccelerationRecord = linearAccelerationRecord;
		this.accelerationRecord = accelerationRecord;
		this.gravitationRecord = gravitationRecord;
		this.gyroscopeRecord = gyroscopeRecord;
		this.magneticRecord = magneticRecord;
		this.rotationRecord = rotationRecord;
	}
	
	public SensorRecords(Object object) {
		Map<String, Object> sensorRecord = (HashMap<String, Object>) object;
		this.linearAccelerationRecord = new LinearAccelerationRecord(sensorRecord.get("linearAcceleration"));
		this.accelerationRecord = new AccelerationRecord(sensorRecord.get("acceleration"));
		this.gravitationRecord = new GravitationRecord(sensorRecord.get("gravitation"));
		this.gyroscopeRecord = new GyroscopeRecord(sensorRecord.get("gyroscope"));
		this.magneticRecord = new MagneticRecord(sensorRecord.get("magnetic"));
		this.rotationRecord = new RotationRecord(sensorRecord.get("rotation"));
	}
	
	public LinearAccelerationRecord getLinearAccelerationRecord() {
		return linearAccelerationRecord;
	}
	
	public void setLinearAccelerationRecord(LinearAccelerationRecord linearAccelerationRecord) {
		this.linearAccelerationRecord = linearAccelerationRecord;
	}
	
	public AccelerationRecord getAccelerationRecord() {
		return accelerationRecord;
	}
	
	public void setAccelerationRecord(AccelerationRecord accelerationRecord) {
		this.accelerationRecord = accelerationRecord;
	}
	
	public GravitationRecord getGravitationRecord() {
		return gravitationRecord;
	}
	
	public void setGravitationRecord(GravitationRecord gravitationRecord) {
		this.gravitationRecord = gravitationRecord;
	}
	
	public GyroscopeRecord getGyroscopeRecord() {
		return gyroscopeRecord;
	}
	
	public void setGyroscopeRecord(GyroscopeRecord gyroscopeRecord) {
		this.gyroscopeRecord = gyroscopeRecord;
	}
	
	public MagneticRecord getMagneticRecord() {
		return magneticRecord;
	}
	
	public void setMagneticRecord(MagneticRecord magneticRecord) {
		this.magneticRecord = magneticRecord;
	}
	
	public RotationRecord getRotationRecord() {
		return rotationRecord;
	}
	
	public void setRotationRecord(RotationRecord rotationRecord) {
		this.rotationRecord = rotationRecord;
	}
	
	public String getAsJson() {
		String string = "{";
		string += linearAccelerationRecord.getAsJson() + ",";
		string += accelerationRecord.getAsJson() + ",";
		string += gravitationRecord.getAsJson() + ",";
		string += gyroscopeRecord.getAsJson() + ",";
		string += magneticRecord.getAsJson() + ",";
		string += rotationRecord.getAsJson();
		return string + "}";
	}
	
	public Map<String, Object> getAsMap() {
		Map<String, Object> sensorRecord = new HashMap<>();
		sensorRecord.put("linearAcceleration", linearAccelerationRecord.getAsMap());
		sensorRecord.put("acceleration", accelerationRecord.getAsMap());
		sensorRecord.put("gravitation", gravitationRecord.getAsMap());
		sensorRecord.put("gyroscope", gyroscopeRecord.getAsMap());
		sensorRecord.put("magnetic", magneticRecord.getAsMap());
		sensorRecord.put("rotation", rotationRecord.getAsMap());
		return sensorRecord;
	}
}