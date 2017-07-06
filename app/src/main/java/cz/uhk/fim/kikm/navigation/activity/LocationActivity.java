package cz.uhk.fim.kikm.navigation.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.adapter.LocationActivityListViewAdapter;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.util.CenteredToast;
import cz.uhk.fim.kikm.navigation.util.ExceptionHandler;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class LocationActivity extends AppCompatActivity {
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SQLiteDatabase sqLiteDatabase;
	private ListView listView;
	private String level, x, y;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.activity_action_bar);
		
		level = getIntent().getStringExtra("level");
		x = String.valueOf(getIntent().getIntExtra("x", 0));
		y = String.valueOf(getIntent().getIntExtra("y", 0));
		
		ArrayList<Fingerprint> fingerprints = new ArrayList<>();
		sqLiteDatabase = openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, timestamp FROM fingerprint WHERE level = ? AND x = ? AND y = ? ORDER BY timestamp DESC;", new String[] { level, x, y });
		if (cursor.getCount() == 0) CenteredToast.showLongText(this, getString(R.string.activityLocationNotFound));
		((TextView) findViewById(R.id.textViewActionBarTitle)).setText(String.format(getString(R.string.activityLocationTitle), level, getIntent().getIntExtra("x", 0), getIntent().getIntExtra("y", 0), cursor.getCount()));
		while (cursor.moveToNext()) {
			try {
				fingerprints.add(new Fingerprint(cursor.getString(0), simpleDateFormat.parse(cursor.getString(1))));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		
		listView = (ListView) findViewById(R.id.activityLocationListView);
		listView.setAdapter(new LocationActivityListViewAdapter(this, fingerprints, true));
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Fingerprint fingerprint = (Fingerprint) listView.getItemAtPosition(position);
				
				AlertDialog alertDialog = new AlertDialog.Builder(LocationActivity.this).setMessage(R.string.activityLocationAlertDialogTitle)
						.setPositiveButton(R.string.activityLocationAlertDialogButtonPositive, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sqLiteDatabase = openOrCreateDatabase(Settings.SQLITE_DATABASE_NAME, Context.MODE_PRIVATE, null);
								sqLiteDatabase.execSQL("DELETE FROM wireless WHERE fingerprint_id = ?;", new String[] { fingerprint.getId() });
								sqLiteDatabase.execSQL("DELETE FROM bluetooth WHERE fingerprint_id = ?;", new String[] { fingerprint.getId() });
								sqLiteDatabase.execSQL("DELETE FROM cellular WHERE fingerprint_id = ?;", new String[] { fingerprint.getId() });
								sqLiteDatabase.execSQL("DELETE FROM fingerprint WHERE id = ?;", new String[] { fingerprint.getId() });
								ArrayList<Fingerprint> fingerprints = new ArrayList<>();
								Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, timestamp FROM fingerprint WHERE level = ? AND x = ? AND y = ? ORDER BY timestamp DESC;", new String[] { level, x, y });
								while (cursor.moveToNext()) {
									try {
										fingerprints.add(new Fingerprint(cursor.getString(0), simpleDateFormat.parse(cursor.getString(1))));
									} catch (ParseException e) {
										e.printStackTrace();
									}
								}
								cursor.close();
								sqLiteDatabase.close();
								listView.setAdapter(new LocationActivityListViewAdapter(LocationActivity.this, fingerprints, true));
								CenteredToast.showLongText(LocationActivity.this, getString(R.string.activityLocationAlertDialogSuccess));
							}
						}).setNegativeButton(R.string.activityLocationAlertDialogButtonNegative, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) { /* Do Nothing */ }
						}).show();
				TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
				textView.setGravity(Gravity.CENTER);
				textView.setPadding(0, 32, 0, 0);
				return false;
			}
		});
		sqLiteDatabase.close();
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(LocationActivity.this, MainActivity.class));
	}
}