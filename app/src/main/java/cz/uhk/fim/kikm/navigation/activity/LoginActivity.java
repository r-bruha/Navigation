package cz.uhk.fim.kikm.navigation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.util.ExceptionHandler;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class LoginActivity extends AppCompatActivity {
	private String couchDatabase, session, cookie, expire;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		
		getSupportActionBar().hide();
		WebView webView = (WebView) findViewById(R.id.activityLoginWebView);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(Settings.LOGIN_SERVER_URL);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void setCouchId(String couchId) {
				couchDatabase = couchId;
			}
			
			@JavascriptInterface
			public void setSessionId(String sessionId) {
				session = sessionId;
			}
			
			@JavascriptInterface
			public void setCookieName(String cookieName) {
				cookie = cookieName;
			}
			
			@JavascriptInterface
			public void setExpires(String expires) {
				expire = expires;
			}
			
			@JavascriptInterface
			public void done() {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
				SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
				
				sharedPreferencesEditor.putString("couchDatabase", couchDatabase);
				sharedPreferencesEditor.putString("session", session);
				sharedPreferencesEditor.putString("cookie", cookie);
				sharedPreferencesEditor.putString("expire", expire);
				sharedPreferencesEditor.apply();
				
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
			}
		}, "Android");
	}
}