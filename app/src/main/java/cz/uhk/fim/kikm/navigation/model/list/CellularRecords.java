package cz.uhk.fim.kikm.navigation.model.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.model.record.CellularRecord;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class CellularRecords<Type> extends ArrayList<Type> {
	public CellularRecords() {
	}
	
	public CellularRecords(int capacity) {
		super(capacity);
	}
	
	public CellularRecords(Collection<? extends Type> collection) {
		super(collection);
	}
	
	public CellularRecords(Object cellularRecords) {
		for (Object object : (List) cellularRecords) this.add((Type) new CellularRecord(object));
	}
	
	public String getAsJson() {
		String string = "[";
		for (Type record : this) string += ((CellularRecord) record).getAsJson() + ",";
		string += "]";
		return string.replace("},]", "}]");
	}
	
	public List getAsMap() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Type record : this) list.add(((CellularRecord) record).getAsMap());
		return list;
	}
}