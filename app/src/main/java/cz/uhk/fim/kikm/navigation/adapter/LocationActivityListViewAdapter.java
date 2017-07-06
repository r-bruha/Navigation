package cz.uhk.fim.kikm.navigation.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.util.Settings;
import cz.uhk.fim.kikm.navigation.util.Utils;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class LocationActivityListViewAdapter extends ArrayAdapter {
	private static boolean wirelessText = true, bluetoothText = true, cellularText = true, sensorText = true;
	private Context context;
	private SQLiteDatabase sqLiteDatabase;
	private Fingerprint fingerprint;
	private View returnView;
	
	private boolean includeFirst = true;
	private String[] fingerprintsInLocation, fingerprintsInLocationSize;
	private String averageWirelessDevicesString, averageWirelessRecordsString, averageWirelessRpSString, averageWirelessSpRString, averageWirelessStrengthString, averageWirelessDistanceString,
			averageBluetoothDevicesString, averageBluetoothRecordsString, averageBluetoothRpSString, averageBluetoothSpRString, averageBluetoothStrengthString, averageBluetoothDistanceString,
			averageCellularDevicesString, averageCellularRecordsString, averageCellularRpSString, averageCellularSpRString, averageCellularStrengthString, averageCellularDistanceString,
			averageSensorDevicesString, averageSensorRecordsString, averageSensorRpSString, averageSensorSpRString, inPlaceholders;
	private float averageWirelessDevices, averageWirelessRecords, averageWirelessRpS, averageWirelessSpR, averageWirelessStrength, averageWirelessDistance,
			averageBluetoothDevices, averageBluetoothRecords, averageBluetoothRpS, averageBluetoothSpR, averageBluetoothStrength, averageBluetoothDistance,
			averageCellularDevices, averageCellularRecords, averageCellularRpS, averageCellularSpR, averageCellularStrength, averageCellularDistance,
			averageSensorDevices, averageSensorRecords, averageSensorRpS, averageSensorSpR,
			averageWirelessRecordsDetail, averageWirelessRpSDetail, averageWirelessSpRDetail, averageWirelessStrengthDetail, averageWirelessDistanceDetail,
			averageBluetoothRecordsDetail, averageBluetoothRpSDetail, averageBluetoothSpRDetail, averageBluetoothStrengthDetail, averageBluetoothDistanceDetail,
			averageCellularRecordsDetail, averageCellularRpSDetail, averageCellularSpRDetail, averageCellularStrengthDetail, averageCellularDistanceDetail,
			averageSensorRecordsDetail, averageSensorRpSDetail, averageSensorSpRDetail;
	
	public LocationActivityListViewAdapter(Context context, ArrayList<Fingerprint> fingerprints, boolean includeFirst) {
		super(context, 0, fingerprints);
		this.context = context;
		this.includeFirst = includeFirst;
		this.sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		
		fingerprintsInLocation = new String[includeFirst ? fingerprints.size() : fingerprints.size() - 1];
		fingerprintsInLocationSize = new String[] { String.valueOf(includeFirst ? fingerprints.size() : fingerprints.size() - 1) };
		inPlaceholders = Utils.createPlaceholders(fingerprintsInLocation.length);
		for (int i = 0; i < (includeFirst ? fingerprints.size() : fingerprints.size() - 1); i++) fingerprintsInLocation[i] = fingerprints.get(includeFirst ? i : i + 1).getId();
		
		Cursor wirelessCursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid), COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM wireless WHERE fingerprint_id IN (" + inPlaceholders + ");",
				Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation));
		wirelessCursor.moveToFirst();
		averageWirelessDevices = wirelessCursor.getFloat(0);
		averageWirelessDevicesString = String.valueOf(averageWirelessDevices);
		averageWirelessRecords = wirelessCursor.getFloat(1);
		averageWirelessRecordsString = String.valueOf(averageWirelessRecords);
		averageWirelessRpS = wirelessCursor.getFloat(2);
		averageWirelessRpSString = String.valueOf(averageWirelessRpS);
		averageWirelessSpR = wirelessCursor.getFloat(3);
		averageWirelessSpRString = String.valueOf(averageWirelessSpRString);
		averageWirelessStrength = wirelessCursor.getFloat(4);
		averageWirelessStrengthString = String.valueOf(averageWirelessStrength);
		averageWirelessDistance = wirelessCursor.getFloat(5);
		averageWirelessDistanceString = String.valueOf(averageWirelessDistance);
		wirelessCursor.close();
		
		Cursor bluetoothCursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid), COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM bluetooth WHERE fingerprint_id IN (" + inPlaceholders + ");",
				Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation));
		bluetoothCursor.moveToFirst();
		averageBluetoothDevices = bluetoothCursor.getFloat(0);
		averageBluetoothDevicesString = String.valueOf(averageBluetoothDevices);
		averageBluetoothRecords = bluetoothCursor.getFloat(1);
		averageBluetoothRecordsString = String.valueOf(averageBluetoothRecords);
		averageBluetoothRpS = bluetoothCursor.getFloat(2);
		averageBluetoothRpSString = String.valueOf(averageBluetoothRpS);
		averageBluetoothSpR = bluetoothCursor.getFloat(3);
		averageBluetoothSpRString = String.valueOf(averageBluetoothSpRString);
		averageBluetoothStrength = bluetoothCursor.getFloat(4);
		averageBluetoothStrengthString = String.valueOf(averageBluetoothStrength);
		averageBluetoothDistance = bluetoothCursor.getFloat(5);
		averageBluetoothDistanceString = String.valueOf(averageBluetoothDistance);
		bluetoothCursor.close();
		
		Cursor cellularCursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT cid), COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM cellular WHERE fingerprint_id IN (" + inPlaceholders + ");",
				Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation));
		cellularCursor.moveToFirst();
		averageCellularDevices = cellularCursor.getFloat(0);
		averageCellularDevicesString = String.valueOf(averageCellularDevices);
		averageCellularRecords = cellularCursor.getFloat(1);
		averageCellularRecordsString = String.valueOf(averageCellularRecords);
		averageCellularRpS = cellularCursor.getFloat(2);
		averageCellularRpSString = String.valueOf(averageCellularRpS);
		averageCellularSpR = cellularCursor.getFloat(3);
		averageCellularSpRString = String.valueOf(averageCellularSpRString);
		averageCellularStrength = cellularCursor.getFloat(4);
		averageCellularStrengthString = String.valueOf(averageCellularStrength);
		averageCellularDistance = cellularCursor.getFloat(5);
		averageCellularDistanceString = String.valueOf(averageCellularDistance);
		cellularCursor.close();
		
		Cursor sensorCursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT type), COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0 FROM sensor WHERE fingerprint_id IN (" + inPlaceholders + ");",
				Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation));
		sensorCursor.moveToFirst();
		averageSensorDevices = sensorCursor.getFloat(0);
		averageSensorDevicesString = String.valueOf(averageSensorDevices);
		averageSensorRecords = sensorCursor.getFloat(1);
		averageSensorRecordsString = String.valueOf(averageSensorRecords);
		averageSensorRpS = sensorCursor.getFloat(2);
		averageSensorRpSString = String.valueOf(averageSensorRpS);
		averageSensorSpR = sensorCursor.getFloat(3);
		averageSensorSpRString = String.valueOf(averageSensorSpRString);
		sensorCursor.close();
		
		this.sqLiteDatabase.close();
	}
	
	@Override // TODO: Incredible laggy, holder patter not helping, try own cache
	public View getView(int position, View convertView, ViewGroup parent) {
		returnView = LayoutInflater.from(getContext()).inflate(R.layout.activity_location_list_view, parent, false);
		fingerprint = (Fingerprint) getItem(position);
		
		sqLiteDatabase = context.openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		createFingerprint();
		// TODO: Add getColumnIndex() instead of number index access
		int wirelessColor = ((ColorDrawable) createWireless().getBackground()).getColor();
		int bluetoothColor = ((ColorDrawable) createBluetooth().getBackground()).getColor();
		int cellularColor = ((ColorDrawable) createCellular().getBackground()).getColor();
		createSensor();
		sqLiteDatabase.close();
		
		returnView.setBackgroundColor(wirelessColor == Color.RED || bluetoothColor == Color.RED || cellularColor == Color.RED ? Color.RED : (
				wirelessColor == Color.YELLOW || bluetoothColor == Color.YELLOW || cellularColor == Color.YELLOW ? Color.YELLOW : Color.GREEN
		));
		
		return returnView;
	}
	
	private void createFingerprint() {
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT timestamp, finish FROM fingerprint WHERE id = ?;", new String[] { fingerprint.getId() });
		cursor.moveToFirst();
		final TextView fingerprintTextView = (TextView) returnView.findViewById(R.id.activityLocationListViewTextViewFingerprint);
		try {
			fingerprintTextView.setText(String.format(context.getString(R.string.activityLocationListViewAdapterTextViewFingerprint),
					new SimpleDateFormat("dd. MM. yyyy").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(0))),
					new SimpleDateFormat("HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(0))),
					new SimpleDateFormat("HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(1))),
					Utils.roundTo((int) (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(1)).getTime() - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cursor.getString(0)).getTime()) / 1000, 10)
			));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cursor.close();
	}
	
	private TextView createWireless() {
		// Total WireLess
		String[] averageTotals = new String[] {
				averageWirelessDevicesString, averageWirelessDevicesString, averageWirelessRecordsString, averageWirelessRecordsString,
				averageWirelessRpSString, averageWirelessRpSString, averageWirelessSpRString, averageWirelessSpRString,
				averageWirelessStrengthString, averageWirelessStrengthString, averageWirelessDistanceString, averageWirelessDistanceString
		};
		final Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid), ABS((COUNT(DISTINCT bssid) - ?) / (? / 100.0)), COUNT(*), ABS((COUNT(*) - ?) / (? / 100.0)), ABS(1.0 / (AVG(difference) / 1000.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)),  ABS(AVG(difference) / 1000.0), ABS((AVG(difference) / 1000.0 - ?) / (? / 100.0)), ABS(AVG(rssi)), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), AVG(distance), ABS((AVG(distance) - ?) / (? / 100.0)) FROM " + (includeFirst ? "wireless" : "currentWireless") + " WHERE fingerprint_id = ?;",
				Utils.concatArrays(averageTotals, new String[] { fingerprint.getId() }));
		cursor.moveToFirst();
		final TextView textView = (TextView) returnView.findViewById(R.id.activityLocationListViewTextViewWireless);
		textView.setText(String.format(context.getString(R.string.activityLocationListViewAdapterTextViewWireless),
				cursor.getFloat(0), averageWirelessDevices, cursor.getFloat(1),
				cursor.getFloat(2), averageWirelessRecords, cursor.getFloat(3),
				cursor.getFloat(4), averageWirelessRpS, cursor.getFloat(5),
				cursor.getFloat(6), averageWirelessSpR, cursor.getFloat(7),
				cursor.getFloat(8), averageWirelessStrength, cursor.getFloat(9),
				cursor.getFloat(10), averageWirelessDistance, cursor.getFloat(11)
		));
		
		if (includeFirst) {
			// Detail WireLess
			Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT COUNT(*), 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance), bssid FROM " + (includeFirst ? "wireless" : "currentWireless") + " WHERE fingerprint_id = ? GROUP BY bssid ORDER BY COUNT(*) DESC, ABS(AVG(rssi));",
					new String[] { fingerprint.getId() });
			
			while (cursorDetail.moveToNext()) {
				Cursor cursorDetailAverage = sqLiteDatabase.rawQuery("SELECT COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM wireless WHERE fingerprint_id IN (" + inPlaceholders + ") AND bssid = ?;",
						Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation, new String[] { cursorDetail.getString(5) }));
				cursorDetailAverage.moveToFirst();
				
				averageWirelessRecordsDetail = cursorDetailAverage.getFloat(0);
				averageWirelessRpSDetail = cursorDetailAverage.getFloat(1);
				averageWirelessSpRDetail = cursorDetailAverage.getFloat(2);
				averageWirelessStrengthDetail = cursorDetailAverage.getFloat(3);
				averageWirelessDistanceDetail = cursorDetailAverage.getFloat(4);
				
				String[] averageDetails = new String[] {
						String.valueOf(averageWirelessRecordsDetail), String.valueOf(averageWirelessRecordsDetail),
						String.valueOf(averageWirelessRpSDetail), String.valueOf(averageWirelessRpSDetail),
						String.valueOf(averageWirelessSpRDetail), String.valueOf(averageWirelessSpRDetail),
						String.valueOf(averageWirelessStrengthDetail), String.valueOf(averageWirelessStrengthDetail),
						String.valueOf(averageWirelessDistanceDetail), String.valueOf(averageWirelessDistanceDetail)
				};
				cursorDetailAverage.close();
				
				Cursor cursorDetailDifference = sqLiteDatabase.rawQuery("SELECT ssid, bssid, frequency, ABS((COUNT(*) - ?) / (? / 100.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)), ABS((AVG(difference) / 1000.0 - ?)) / (? / 100.0), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), ABS((AVG(distance) - ?) / (? / 100.0)) FROM wireless WHERE fingerprint_id = ? AND bssid = ?;",
						Utils.concatArrays(averageDetails, new String[] { fingerprint.getId(), cursorDetail.getString(5) }));
				cursorDetailDifference.moveToFirst();
				textView.setHint((textView.getHint() != null ? textView.getHint() : "") + String.format(context.getString(R.string.activityLocationListViewAdapterTextViewWirelessDetail),
						cursorDetailDifference.getString(0), cursorDetailDifference.getString(1), cursorDetailDifference.getFloat(2),
						cursorDetail.getFloat(0), averageWirelessRecordsDetail, cursorDetailDifference.getFloat(3),
						cursorDetail.getFloat(1), averageWirelessRpSDetail, cursorDetailDifference.getFloat(4),
						cursorDetail.getFloat(2), averageWirelessSpRDetail, cursorDetailDifference.getFloat(5),
						cursorDetail.getFloat(3), averageWirelessStrengthDetail, cursorDetailDifference.getFloat(6),
						cursorDetail.getFloat(4), averageWirelessDistanceDetail, cursorDetailDifference.getFloat(7)
				));
				cursorDetailDifference.close();
			}
			cursorDetail.close();
		}
		
		if (!wirelessText) {
			CharSequence charSequence = textView.getText();
			textView.setText(textView.getHint());
			textView.setHint(charSequence);
			textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
		}
		
		textView.setHeight((textView.getText().toString().split("\r\n|\r|\n").length + 1) * textView.getLineHeight());
		textView.setBackgroundColor(cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence charSequence = textView.getText();
				textView.setText(textView.getHint());
				textView.setHint(charSequence);
				textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
				wirelessText = !wirelessText;
			}
		});
		
		cursor.close();
		
		return textView;
	}
	
	private TextView createBluetooth() {
		// Total BlueTooth
		String[] averageTotals = new String[] {
				averageBluetoothDevicesString, averageBluetoothDevicesString, averageBluetoothRecordsString, averageBluetoothRecordsString,
				averageBluetoothRpSString, averageBluetoothRpSString, averageBluetoothSpRString, averageBluetoothSpRString,
				averageBluetoothStrengthString, averageBluetoothStrengthString, averageBluetoothDistanceString, averageBluetoothDistanceString
		};
		final Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT bssid), ABS((COUNT(DISTINCT bssid) - ?) / (? / 100.0)), COUNT(*), ABS((COUNT(*) - ?) / (? / 100.0)), ABS(1.0 / (AVG(difference) / 1000.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)),  ABS(AVG(difference) / 1000.0), ABS((AVG(difference) / 1000.0 - ?) / (? / 100.0)), ABS(AVG(rssi)), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), AVG(distance), ABS((AVG(distance) - ?) / (? / 100.0)) FROM " + (includeFirst ? "bluetooth" : "currentBluetooth") + " WHERE fingerprint_id = ?;",
				Utils.concatArrays(averageTotals, new String[] { fingerprint.getId() }));
		cursor.moveToFirst();
		final TextView textView = (TextView) returnView.findViewById(R.id.activityLocationListViewTextViewBluetooth);
		textView.setText(String.format(context.getString(R.string.activityLocationListViewAdapterTextViewBluetooth),
				cursor.getFloat(0), averageBluetoothDevices, cursor.getFloat(1),
				cursor.getFloat(2), averageBluetoothRecords, cursor.getFloat(3),
				cursor.getFloat(4), averageBluetoothRpS, cursor.getFloat(5),
				cursor.getFloat(6), averageBluetoothSpR, cursor.getFloat(7),
				cursor.getFloat(8), averageBluetoothStrength, cursor.getFloat(9),
				cursor.getFloat(10), averageBluetoothDistance, cursor.getFloat(11)
		));
		
		// Detail BlueTooth
		if (includeFirst) {
			Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT COUNT(*), 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance), bssid FROM " + (includeFirst ? "bluetooth" : "currentBluetooth") + " WHERE fingerprint_id = ? GROUP BY bssid ORDER BY COUNT(*) DESC, ABS(AVG(rssi));",
					new String[] { fingerprint.getId() });
			
			while (cursorDetail.moveToNext()) {
				Cursor cursorDetailAverage = sqLiteDatabase.rawQuery("SELECT COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM bluetooth WHERE fingerprint_id IN (" + inPlaceholders + ") AND bssid = ?;",
						Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation, new String[] { cursorDetail.getString(5) }));
				cursorDetailAverage.moveToFirst();
				
				averageBluetoothRecordsDetail = cursorDetailAverage.getFloat(0);
				averageBluetoothRpSDetail = cursorDetailAverage.getFloat(1);
				averageBluetoothSpRDetail = cursorDetailAverage.getFloat(2);
				averageBluetoothStrengthDetail = cursorDetailAverage.getFloat(3);
				averageBluetoothDistanceDetail = cursorDetailAverage.getFloat(4);
				
				String[] averageDetails = new String[] {
						String.valueOf(averageBluetoothRecordsDetail), String.valueOf(averageBluetoothRecordsDetail),
						String.valueOf(averageBluetoothRpSDetail), String.valueOf(averageBluetoothRpSDetail),
						String.valueOf(averageBluetoothSpRDetail), String.valueOf(averageBluetoothSpRDetail),
						String.valueOf(averageBluetoothStrengthDetail), String.valueOf(averageBluetoothStrengthDetail),
						String.valueOf(averageBluetoothDistanceDetail), String.valueOf(averageBluetoothDistanceDetail)
				};
				cursorDetailAverage.close();
				
				Cursor cursorDetailDifference = sqLiteDatabase.rawQuery("SELECT bssid, ABS((COUNT(*) - ?) / (? / 100.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)), ABS((AVG(difference) / 1000.0 - ?)) / (? / 100.0), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), ABS((AVG(distance) - ?) / (? / 100.0)) FROM bluetooth WHERE fingerprint_id = ? AND bssid = ?;",
						Utils.concatArrays(averageDetails, new String[] { fingerprint.getId(), cursorDetail.getString(5) }));
				cursorDetailDifference.moveToFirst();
				textView.setHint((textView.getHint() != null ? textView.getHint() : "") + String.format(context.getString(R.string.activityLocationListViewAdapterTextViewBluetoothDetail),
						cursorDetailDifference.getString(0),
						cursorDetail.getFloat(0), averageBluetoothRecordsDetail, cursorDetailDifference.getFloat(1),
						cursorDetail.getFloat(1), averageBluetoothRpSDetail, cursorDetailDifference.getFloat(2),
						cursorDetail.getFloat(2), averageBluetoothSpRDetail, cursorDetailDifference.getFloat(3),
						cursorDetail.getFloat(3), averageBluetoothStrengthDetail, cursorDetailDifference.getFloat(4),
						cursorDetail.getFloat(4), averageBluetoothDistanceDetail, cursorDetailDifference.getFloat(5)
				));
				cursorDetailDifference.close();
			}
			cursorDetail.close();
		}
		if (!bluetoothText) {
			CharSequence charSequence = textView.getText();
			textView.setText(textView.getHint());
			textView.setHint(charSequence);
			textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
		}
		
		textView.setHeight((textView.getText().toString().split("\r\n|\r|\n").length + 1) * textView.getLineHeight());
		textView.setBackgroundColor(cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence charSequence = textView.getText();
				textView.setText(textView.getHint());
				textView.setHint(charSequence);
				textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
				bluetoothText = !bluetoothText;
			}
		});
		
		cursor.close();
		return textView;
	}
	
	private TextView createCellular() {
		// Total Cellular
		String[] averageTotals = new String[] {
				averageCellularDevicesString, averageCellularDevicesString, averageCellularRecordsString, averageCellularRecordsString,
				averageCellularRpSString, averageCellularRpSString, averageCellularSpRString, averageCellularSpRString,
				averageCellularStrengthString, averageCellularStrengthString, averageCellularDistanceString, averageCellularDistanceString
		};
		final Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT cid), ABS((COUNT(DISTINCT cid) - ?) / (? / 100.0)), COUNT(*), ABS((COUNT(*) - ?) / (? / 100.0)), ABS(1.0 / (AVG(difference) / 1000.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)),  ABS(AVG(difference) / 1000.0), ABS((AVG(difference) / 1000.0 - ?) / (? / 100.0)), ABS(AVG(rssi)), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), AVG(distance), ABS((AVG(distance) - ?) / (? / 100.0)) FROM " + (includeFirst ? "cellular" : "currentCellular") + " WHERE fingerprint_id = ?;",
				Utils.concatArrays(averageTotals, new String[] { fingerprint.getId() }));
		cursor.moveToFirst();
		final TextView textView = (TextView) returnView.findViewById(R.id.activityLocationListViewTextViewCellular);
		textView.setText(String.format(context.getString(R.string.activityLocationListViewAdapterTextViewCellular),
				cursor.getFloat(0), averageCellularDevices, cursor.getFloat(1),
				cursor.getFloat(2), averageCellularRecords, cursor.getFloat(3),
				cursor.getFloat(4), averageCellularRpS, cursor.getFloat(5),
				cursor.getFloat(6), averageCellularSpR, cursor.getFloat(7),
				cursor.getFloat(8), averageCellularStrength, cursor.getFloat(9),
				cursor.getFloat(10), averageCellularDistance, cursor.getFloat(11)
		));
		
		// Detail Cellular
		if (includeFirst) {
			Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT COUNT(*), 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance), cid FROM " + (includeFirst ? "cellular" : "currentCellular") + " WHERE fingerprint_id = ? GROUP BY cid ORDER BY COUNT(*) DESC, ABS(AVG(rssi));",
					new String[] { fingerprint.getId() });
			
			while (cursorDetail.moveToNext()) {
				Cursor cursorDetailAverage = sqLiteDatabase.rawQuery("SELECT COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, ABS(AVG(rssi)), AVG(distance) FROM cellular WHERE fingerprint_id IN (" + inPlaceholders + ") AND cid = ?;",
						Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation, new String[] { cursorDetail.getString(5) }));
				cursorDetailAverage.moveToFirst();
				averageCellularRecordsDetail = cursorDetailAverage.getFloat(0);
				averageCellularRpSDetail = cursorDetailAverage.getFloat(1);
				averageCellularSpRDetail = cursorDetailAverage.getFloat(2);
				averageCellularStrengthDetail = cursorDetailAverage.getFloat(3);
				averageCellularDistanceDetail = cursorDetailAverage.getFloat(4);
				
				
				String[] averageDetails = new String[] {
						String.valueOf(averageCellularRecordsDetail), String.valueOf(averageCellularRecordsDetail),
						String.valueOf(averageCellularRpSDetail), String.valueOf(averageCellularRpSDetail),
						String.valueOf(averageCellularSpRDetail), String.valueOf(averageCellularSpRDetail),
						String.valueOf(averageCellularStrengthDetail), String.valueOf(averageCellularStrengthDetail),
						String.valueOf(averageCellularDistanceDetail), String.valueOf(averageCellularDistanceDetail)
				};
				cursorDetailAverage.close();
				
				Cursor cursorDetailDifference = sqLiteDatabase.rawQuery("SELECT cid, lac, ABS((COUNT(*) - ?) / (? / 100.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)), ABS((AVG(difference) / 1000.0 - ?)) / (? / 100.0), ABS((ABS(AVG(rssi)) - ?) / (? / 100.0)), ABS((AVG(distance) - ?) / (? / 100.0)) FROM cellular WHERE fingerprint_id = ? AND cid = ?;",
						Utils.concatArrays(averageDetails, new String[] { fingerprint.getId(), cursorDetail.getString(5) }));
				cursorDetailDifference.moveToFirst();
				textView.setHint((textView.getHint() != null ? textView.getHint() : "") + String.format(context.getString(R.string.activityLocationListViewAdapterTextViewCellularDetail),
						cursorDetailDifference.getFloat(0), cursorDetailDifference.getFloat(1),
						cursorDetail.getFloat(0), averageCellularRecordsDetail, cursorDetailDifference.getFloat(2),
						cursorDetail.getFloat(1), averageCellularRpSDetail, cursorDetailDifference.getFloat(3),
						cursorDetail.getFloat(2), averageCellularSpRDetail, cursorDetailDifference.getFloat(4),
						cursorDetail.getFloat(3), averageCellularStrengthDetail, cursorDetailDifference.getFloat(5),
						cursorDetail.getFloat(4), averageCellularDistanceDetail, cursorDetailDifference.getFloat(6)
				));
				cursorDetailDifference.close();
			}
			cursorDetail.close();
		}
		if (!cellularText) {
			CharSequence charSequence = textView.getText();
			textView.setText(textView.getHint());
			textView.setHint(charSequence);
			textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
		}
		
		textView.setHeight((textView.getText().toString().split("\r\n|\r|\n").length + 1) * textView.getLineHeight());
		textView.setBackgroundColor(cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(9) < Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence charSequence = textView.getText();
				textView.setText(textView.getHint());
				textView.setHint(charSequence);
				textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
				cellularText = !cellularText;
			}
		});
		cursor.close();
		
		return textView;
	}
	
	private TextView createSensor() {
		// Total Sensor
		String[] averageTotals = new String[] {
				averageSensorDevicesString, averageSensorDevicesString, averageSensorRecordsString, averageSensorRecordsString,
				averageSensorRpSString, averageSensorRpSString, averageSensorSpRString, averageSensorSpRString
		};
		final Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(DISTINCT type), ABS((COUNT(DISTINCT type) - ?) / (? / 100.0)), COUNT(*), ABS((COUNT(*) - ?) / (? / 100.0)), ABS(1.0 / (AVG(difference) / 1000.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)),  ABS(AVG(difference) / 1000.0), ABS((AVG(difference) / 1000.0 - ?) / (? / 100.0)) FROM " + (includeFirst ? "sensor" : "currentSensor") + " WHERE fingerprint_id = ?;",
				Utils.concatArrays(averageTotals, new String[] { fingerprint.getId() }));
		cursor.moveToFirst();
		final TextView textView = (TextView) returnView.findViewById(R.id.activityLocationListViewTextViewSensor);
		textView.setText(String.format(context.getString(R.string.activityLocationListViewAdapterTextViewSensor),
				cursor.getFloat(0), averageSensorDevices, cursor.getFloat(1),
				cursor.getFloat(2), averageSensorRecords, cursor.getFloat(3),
				cursor.getFloat(4), averageSensorRpS, cursor.getFloat(5),
				cursor.getFloat(6), averageSensorSpR, cursor.getFloat(7)
		));
		
		// Detail Sensor
		Cursor cursorDetail = sqLiteDatabase.rawQuery("SELECT COUNT(*), 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0, type FROM " + (includeFirst ? "sensor" : "currentSensor") + " WHERE fingerprint_id = ? AND type IN (1, 2, 4, 9, 10, 11) GROUP BY type ORDER BY COUNT(*) DESC;",
				new String[] { fingerprint.getId() });
		
		while (cursorDetail.moveToNext()) {
			Cursor cursorDetailAverage = sqLiteDatabase.rawQuery("SELECT COUNT(*) / ?, 1.0 / (AVG(difference) / 1000.0), AVG(difference) / 1000.0 FROM sensor WHERE fingerprint_id IN (" + inPlaceholders + ") AND type = ?;",
					Utils.concatArrays(fingerprintsInLocationSize, fingerprintsInLocation, new String[] { cursorDetail.getString(3) }));
			cursorDetailAverage.moveToFirst();
			
			averageSensorRecordsDetail = cursorDetailAverage.getFloat(0);
			averageSensorRpSDetail = cursorDetailAverage.getFloat(1);
			averageSensorSpRDetail = cursorDetailAverage.getFloat(2);
			
			String[] averageDetails = new String[] {
					String.valueOf(averageSensorRecordsDetail), String.valueOf(averageSensorRecordsDetail),
					String.valueOf(averageSensorRpSDetail), String.valueOf(averageSensorRpSDetail),
					String.valueOf(averageSensorSpRDetail), String.valueOf(averageSensorSpRDetail)
			};
			cursorDetailAverage.close();
			
			Cursor cursorDetailDifference = sqLiteDatabase.rawQuery("SELECT type, ABS((COUNT(*) - ?) / (? / 100.0)), ABS((1.0 / (AVG(difference) / 1000.0) - ?) / (? / 100.0)), ABS((AVG(difference) / 1000.0 - ?)) / (? / 100.0) FROM sensor WHERE fingerprint_id = ? AND type = ?;",
					Utils.concatArrays(averageDetails, new String[] { fingerprint.getId(), cursorDetail.getString(3) }));
			cursorDetailDifference.moveToFirst();
			
			String type = Utils.convertSensorName(cursorDetailDifference.getInt(0));
			textView.setHint((textView.getHint() != null ? textView.getHint() : "") + String.format(context.getString(R.string.activityLocationListViewAdapterTextViewSensorDetail),
					type,
					cursorDetail.getFloat(0), averageSensorRecordsDetail, cursorDetailDifference.getFloat(1),
					cursorDetail.getFloat(1), averageSensorRpSDetail, cursorDetailDifference.getFloat(2),
					cursorDetail.getFloat(2), averageSensorSpRDetail, cursorDetailDifference.getFloat(3)
			));
			cursorDetailDifference.close();
		}
		cursorDetail.close();
		
		if (!sensorText) {
			CharSequence charSequence = textView.getText();
			textView.setText(textView.getHint());
			textView.setHint(charSequence);
			textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
		}
		
		textView.setHeight((textView.getText().toString().split("\r\n|\r|\n").length + 1) * textView.getLineHeight());
		textView.setBackgroundColor(cursor.getFloat(5) < Settings.DIFFERENCE_TOLERANCE_LOW ? Color.GREEN : (cursor.getFloat(5) < Settings.DIFFERENCE_TOLERANCE_HIGH ? Color.YELLOW : Color.RED));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence charSequence = textView.getText();
				textView.setText(textView.getHint());
				textView.setHint(charSequence);
				textView.setHeight((textView.getLineCount() - 1) * textView.getLineHeight());
				sensorText = !sensorText;
			}
		});
		cursor.close();
		return textView;
	}
}