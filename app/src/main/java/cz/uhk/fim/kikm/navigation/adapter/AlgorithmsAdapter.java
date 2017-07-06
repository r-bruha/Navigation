package cz.uhk.fim.kikm.navigation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cz.uhk.fim.kikm.navigation.R;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class AlgorithmsAdapter extends ArrayAdapter {
	public AlgorithmsAdapter(Context context, String[] algorithms) {
		super(context, 0, algorithms);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_main_text_view, parent, false);
		((TextView) convertView.findViewById(R.id.activityMainTextViewTextView)).setText(getItem(position).toString());
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_main_text_view, parent, false);
		((TextView) convertView.findViewById(R.id.activityMainTextViewTextView)).setText(getItem(position).toString());
		return convertView;
	}
}