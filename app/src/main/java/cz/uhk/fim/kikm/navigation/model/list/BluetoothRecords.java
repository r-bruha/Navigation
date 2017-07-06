package cz.uhk.fim.kikm.navigation.model.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.model.record.BluetoothRecord;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class BluetoothRecords<Type> extends ArrayList<Type> {
	public BluetoothRecords() {
	}
	
	public BluetoothRecords(int capacity) {
		super(capacity);
	}
	
	public BluetoothRecords(Collection<? extends Type> collection) {
		super(collection);
	}
	
	public BluetoothRecords(Object bluetoothRecords) {
		for (Object object : (List) bluetoothRecords) this.add((Type) new BluetoothRecord(object));
	}
	
	public String getAsJson() {
		String string = "[";
		for (Type record : this) string += ((BluetoothRecord) record).getAsJson() + ",";
		string += "]";
		return string.replace("},]", "}]");
	}
	
	public List getAsMap() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Type record : this) list.add(((BluetoothRecord) record).getAsMap());
		return list;
	}
}