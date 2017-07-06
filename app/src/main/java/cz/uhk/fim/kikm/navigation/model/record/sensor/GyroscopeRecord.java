package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class GyroscopeRecord extends BaseSensorRecord {
	public GyroscopeRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public GyroscopeRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"gyroscope\":%1$s", super.getAsJson());
	}
}