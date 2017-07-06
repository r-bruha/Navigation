package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class GravitationRecord extends BaseSensorRecord {
	public GravitationRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public GravitationRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"gravitation\":%1$s", super.getAsJson());
	}
}