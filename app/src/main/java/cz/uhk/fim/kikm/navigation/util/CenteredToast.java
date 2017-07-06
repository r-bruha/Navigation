package cz.uhk.fim.kikm.navigation.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class CenteredToast extends Toast {
	public CenteredToast(Context context) {
		super(context);
	}
	
	public static Toast makeText(Context context, CharSequence text, int duration) {
		Toast toast = Toast.makeText(context, text, duration);
		((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER_HORIZONTAL);
		return toast;
	}
	
	public static Toast makeText(Context context, int resId, int duration) {
		Toast toast = Toast.makeText(context, context.getResources().getText(resId), duration);
		((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER_HORIZONTAL);
		return toast;
	}
	
	public static void showShortText(Context context, CharSequence text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER_HORIZONTAL);
		toast.show();
	}
	
	public static void showLongText(Context context, CharSequence text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER_HORIZONTAL);
		toast.show();
	}
}