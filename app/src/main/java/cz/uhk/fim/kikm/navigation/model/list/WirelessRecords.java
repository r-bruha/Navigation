package cz.uhk.fim.kikm.navigation.model.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.model.record.WirelessRecord;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class WirelessRecords<Type> extends ArrayList<Type> {
	public WirelessRecords() {
	}
	
	public WirelessRecords(int capacity) {
		super(capacity);
	}
	
	public WirelessRecords(Collection<? extends Type> collection) {
		super(collection);
	}
	
	public WirelessRecords(Object wirelessRecords) {
		for (Object object : (List) wirelessRecords) this.add((Type) new WirelessRecord(object));
	}
	
	public String getAsJson() {
		String string = "[";
		for (Type record : this) string += ((WirelessRecord) record).getAsJson() + ",";
		string += "]";
		return string.replace("},]", "}]");
	}
	
	public List getAsMap() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Type record : this) list.add(((WirelessRecord) record).getAsMap());
		return list;
	}
}