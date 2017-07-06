package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class AccelerationRecord extends BaseSensorRecord {
	public AccelerationRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public AccelerationRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"acceleration\":%1$s", super.getAsJson());
	}
}