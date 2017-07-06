package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class RotationRecord extends BaseSensorRecord {
	public RotationRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public RotationRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"rotation\":%1$s", super.getAsJson());
	}
}