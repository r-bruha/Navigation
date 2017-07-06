package cz.uhk.fim.kikm.navigation.util;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.CrashActivity;


/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
	private Context context;
	
	public ExceptionHandler(Context context) {
		this.context = context;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		try {
			throwable.printStackTrace();
			ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
			((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
			FileWriter fileWriter = new FileWriter(new File(context.getFilesDir(), "crash.log"));
			fileWriter.write(String.format(context.getString(R.string.activityCrashEmailAttachment),
					new SimpleDateFormat("dd. MM. yyyy HH:mm:ss").format(new Date()), Character.toUpperCase(Build.BRAND.charAt(0)) + Build.BRAND.substring(1), Build.MODEL,
					Build.VERSION.RELEASE, Build.VERSION.SDK_INT, memoryInfo.availMem / 1048576, memoryInfo.totalMem / 1048576, Log.getStackTraceString(throwable)));
			fileWriter.close();
			context.startActivity(new Intent(context, CrashActivity.class));
			Process.killProcess(Process.myPid());
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}