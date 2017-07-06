package cz.uhk.fim.kikm.navigation.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.MainActivity;
import cz.uhk.fim.kikm.navigation.model.Fingerprint;
import cz.uhk.fim.kikm.navigation.model.Location;
import cz.uhk.fim.kikm.navigation.model.list.BluetoothRecords;
import cz.uhk.fim.kikm.navigation.model.list.CellularRecords;
import cz.uhk.fim.kikm.navigation.model.list.WirelessRecords;
import cz.uhk.fim.kikm.navigation.model.record.BluetoothRecord;
import cz.uhk.fim.kikm.navigation.model.record.CellularRecord;
import cz.uhk.fim.kikm.navigation.model.record.WirelessRecord;
import cz.uhk.fim.kikm.navigation.util.CenteredToast;
import cz.uhk.fim.kikm.navigation.util.Settings;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class NeighborFilter extends AsyncTask<String, Void, String> {
	private Context context;
	private Fingerprint fingerprint;
	private Location location;
	private int algorithm, innerAlgorithm, time;
	
	public NeighborFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = Settings.SEARCH_ALGORITHM_NEIGHBOURS;
		this.time = time;
	}
	
	public NeighborFilter(Context context, Fingerprint fingerprint, Location location, int algorithm, int innerAlgorithm, int time) {
		this.context = context;
		this.fingerprint = fingerprint;
		this.location = location;
		this.algorithm = algorithm;
		this.innerAlgorithm = innerAlgorithm;
		this.time = time;
	}
	
	private static HashMap<String, String> parseJson(String response) {
		HashMap<String, String> hashMap = new HashMap<>();
		try {
			JSONObject jsonObject = new JSONObject(response);
			Iterator<String> keys = jsonObject.keys();
			String key;
			while (keys.hasNext()) hashMap.put((key = keys.next()), jsonObject.getString(key));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return hashMap;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(String... parameters) {
		
		try {
			switch (this.algorithm) {
				case Settings.SEARCH_ALGORITHM_TOGETHER:
					switch (innerAlgorithm) {
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS:
							fingerprint.setCellularRecords(new CellularRecords<CellularRecord>());
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS:
							fingerprint.setBluetoothRecords(new BluetoothRecords<BluetoothRecord>()).setCellularRecords(new CellularRecords<CellularRecord>());
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH:
							fingerprint.setWirelessRecords(new WirelessRecords<WirelessRecord>()).setCellularRecords(new CellularRecords<CellularRecord>());
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_CELLULAR:
							fingerprint.setCellularRecords(new CellularRecords<CellularRecord>());
							break;
					}
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS:
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_TOGETHER:
					fingerprint.setCellularRecords(new CellularRecords<CellularRecord>());
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS:
					fingerprint.setBluetoothRecords(new BluetoothRecords<BluetoothRecord>()).setCellularRecords(new CellularRecords<CellularRecord>());
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH:
					fingerprint.setWirelessRecords(new WirelessRecords<WirelessRecord>()).setCellularRecords(new CellularRecords<CellularRecord>());
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_CELLULAR:
					fingerprint.setCellularRecords(new CellularRecords<CellularRecord>());
					break;
			}
			
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(parameters[0]).openConnection();
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput(true);
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
			bufferedWriter.write("fingerprint=" + new GsonBuilder().create().toJson(fingerprint));
			bufferedWriter.close();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String responseLine;
			while ((responseLine = bufferedReader.readLine()) != null) stringBuilder.append(responseLine);
			return stringBuilder.toString();
			
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"floor\":\"Unknown\",\"x\":-1,\"y\":-1}";
		}
	}
	
	@Override
	protected void onPostExecute(String response) {
		super.onPostExecute(response);
		HashMap<String, String> hashMap = parseJson(response);
		Location location;
		try {
			location = new Location(hashMap.get("floor"), Integer.parseInt(hashMap.get("x")), Integer.parseInt(hashMap.get("y")));
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			location = new Location("Unknown", -1, -1);
		}
		
		ArrayList<float[]> locations = new ArrayList<>();
		if (hashMap.get("neighbors") != null) {
			JsonArray jsonArray = (JsonArray) new JsonParser().parse(hashMap.get("neighbors"));
			for (JsonElement jsonElement : jsonArray) {
				float[] neighbor = new float[5];
				JsonArray jsonArray1 = jsonElement.getAsJsonArray();
				for (int i = 0; i < jsonArray1.size(); i++) neighbor[i] = jsonArray1.get(i).getAsFloat();
				neighbor[4] = neighbor[2];
				neighbor[2] = 0;
				neighbor[3] = 50;
				locations.add(neighbor);
			}
		}
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		sharedPreferencesEditor.putString("level", location.getFloor());
		sharedPreferencesEditor.putInt("x", location.getX());
		sharedPreferencesEditor.putInt("y", location.getY());
		sharedPreferencesEditor.apply();
		
		double difference = Math.sqrt(Math.pow(location.getX() - this.location.getX(), 2) + Math.pow(location.getY() - this.location.getY(), 2)) / 50;
		try {
			new File(Environment.getExternalStorageDirectory() + "/Navigation").mkdirs();
			FileWriter fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory() + "/Navigation", "Search.tsv"), true);
			String algorithm = "";
			switch (this.algorithm) {
				case Settings.SEARCH_ALGORITHM_TOGETHER:
					switch (innerAlgorithm) {
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS:
							algorithm = "Neighbour Filter";
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS:
							algorithm = "Neighbour Filter WireLess";
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH:
							algorithm = "Neighbour Filter BlueTooth";
							break;
						case Settings.SEARCH_ALGORITHM_NEIGHBOURS_CELLULAR:
							algorithm = "Neighbour Filter Cellular";
							break;
					}
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS:
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_TOGETHER:
					algorithm = "Neighbour Filter";
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_WIRELESS:
					algorithm = "Neighbour Filter WireLess";
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_BlUETOOTH:
					algorithm = "Neighbour Filter BlueTooth";
					break;
				case Settings.SEARCH_ALGORITHM_NEIGHBOURS_CELLULAR:
					algorithm = "Neighbour Filter Cellular";
					break;
			}
			fileWriter.append(String.format(context.getString(R.string.taskBaseFilterShowLog),
					new SimpleDateFormat("dd. MM. yyyy HH:mm:ss").format(new Date()), algorithm, time / 1000, this.location.getFloor(), this.location.getX(), this.location.getY(), location.getFloor(), location.getX(), location.getY(), difference, new GsonBuilder().create().toJson(fingerprint)));
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		CenteredToast.showLongText(context, String.format(context.getString(R.string.taskBaseFilterShowMessage), location.getX(), location.getY(), difference));
		((MainActivity) context).getRenderer().setRings(locations).setCurrentLevel(location.getFloor()).setCurrentX(location.getX()).setCurrentY(location.getY()).reloadScene();
	}
}