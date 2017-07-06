package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class LinearAccelerationRecord extends BaseSensorRecord {
	public LinearAccelerationRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public LinearAccelerationRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"linearAcceleration\":%1$s", super.getAsJson());
	}
}