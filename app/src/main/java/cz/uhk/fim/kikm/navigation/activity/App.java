package cz.uhk.fim.kikm.navigation.activity;

import android.app.Application;
import android.content.Context;

import cz.uhk.fim.kikm.navigation.util.ExceptionHandler;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class App extends Application {
	private static Context context;
	
	public static Context getContext() {
		return context;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
	}
}