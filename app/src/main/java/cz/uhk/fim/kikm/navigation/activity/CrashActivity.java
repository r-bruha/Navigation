package cz.uhk.fim.kikm.navigation.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.util.Settings;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class CrashActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crash);
		
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.activity_action_bar);
		((TextView) findViewById(R.id.textViewActionBarTitle)).setText(R.string.activityCrashTitle);
		
		findViewById(R.id.activityCrashButtonEmailSend).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = getUriForFile(CrashActivity.this, "cz.uhk.fim.kikm.navigation.provider", new File(CrashActivity.this.getFilesDir(), "crash.log"));
				
				Intent intent = new Intent(Intent.ACTION_SEND)
						.setType("message/rfc822")
						.putExtra(Intent.EXTRA_EMAIL, Settings.CRASH_EMAIL)
						.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.activityCrashEmailSubject))
						.putExtra(Intent.EXTRA_TEXT, getString(R.string.activityCrashEmailContent))
						.putExtra(Intent.EXTRA_STREAM, uri);
				
				for (ResolveInfo resolveInfo : CrashActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
					CrashActivity.this.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
				}
				
				startActivity(intent);
				Process.killProcess(Process.myPid());
				System.exit(0);
			}
		});
	}
}