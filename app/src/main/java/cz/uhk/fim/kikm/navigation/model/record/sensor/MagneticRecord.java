package cz.uhk.fim.kikm.navigation.model.record.sensor;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class MagneticRecord extends BaseSensorRecord {
	public MagneticRecord(float x, float y, float z, int time, int difference) {
		super(x, y, z, time, difference);
	}
	
	public MagneticRecord(Object object) {
		super(object);
	}
	
	public String getAsJson() {
		return String.format("\"magnetic\":%1$s", super.getAsJson());
	}
}