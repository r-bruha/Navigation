package cz.uhk.fim.kikm.navigation.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.util.Log;

import java.util.Arrays;

import static android.content.ContentValues.TAG;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class Utils {
	@SafeVarargs
	public static <Type> Type[] concatArrays(Type[] pArray, Type[]... pArrays) {
		int length = pArray.length;
		for (Type[] array : pArrays) length += array.length;
		Type[] result = Arrays.copyOf(pArray, length);
		int offset = pArray.length;
		for (Type[] array : pArrays) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	
	public static String createPlaceholders(int count) {
		String string = "";
		for (int i = 0; i < count; i++) string += "?, ";
		return count == 0 ? "-1" : string.substring(0, string.length() - 2) + "";
	}
	
	public static int roundTo(double number, int round) {
		return (int) Math.round(number / round) * round;
	}
	
	public static String convertSensorName(int sensor) {
		String string = "";
		switch (sensor) {
			case Sensor.TYPE_LINEAR_ACCELERATION:
				string = "Linear Acceleration";
				break;
			case Sensor.TYPE_ACCELEROMETER:
				string = "Acceleration";
				break;
			case Sensor.TYPE_GRAVITY:
				string = "Gravity";
				break;
			case Sensor.TYPE_GYROSCOPE:
				string = "Gyroscope";
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				string = "Magnetic";
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				string = "Rotation";
				break;
			default:
				string = "Unknown";
				break;
		}
		return string;
	}
	
	public static void executeExplainQueryPlanStatement(SQLiteDatabase database, String query, String[] strings) {
		Cursor cursor = database.rawQuery("EXPLAIN QUERY PLAN " + query, strings);
		Log.i(TAG, query);
		while (cursor.moveToNext()) {
			Log.i(TAG, String.format("%d | %d | %d | %s",
					cursor.getInt(cursor.getColumnIndex("selectid")),
					cursor.getInt(cursor.getColumnIndex("order")),
					cursor.getInt(cursor.getColumnIndex("from")),
					cursor.getString(cursor.getColumnIndex("detail"))
			));
		}
		cursor.close();
	}
}