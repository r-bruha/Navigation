package cz.uhk.fim.kikm.navigation.model.record;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.App;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class WirelessRecord implements Serializable {
	private String ssid, bssid, technology;
	private int rssi, time, difference, frequency, channel;
	private float distance;
	private Date timestamp;
	
	public WirelessRecord(Date timestamp, String ssid, String bssid, int rssi, float distance, int time, int difference, int frequency) {
		this.timestamp = timestamp;
		this.ssid = ssid;
		this.bssid = bssid;
		this.rssi = rssi;
		this.distance = distance;
		this.time = time;
		this.difference = difference;
		this.frequency = frequency;
		this.parseChannelAndTechnology();
	}
	
	public WirelessRecord(Object object) {
		Map<String, Object> wirelessRecord = (HashMap<String, Object>) object;
		this.timestamp = new Date(Double.valueOf(wirelessRecord.get("timestamp").toString()).longValue());
		this.bssid = wirelessRecord.get("bssid").toString();
		this.ssid = wirelessRecord.get("ssid").toString();
		this.rssi = Integer.valueOf(wirelessRecord.get("rssi").toString());
		this.distance = Float.valueOf(wirelessRecord.get("distance").toString());
		this.time = Integer.valueOf(wirelessRecord.get("time").toString());
		this.difference = Integer.valueOf(wirelessRecord.get("difference").toString());
		this.frequency = Integer.valueOf(wirelessRecord.get("frequency").toString());
		this.channel = Integer.valueOf(wirelessRecord.get("channel").toString());
		this.technology = wirelessRecord.get("technology").toString();
	}
	
	public String getSsid() {
		return ssid;
	}
	
	public WirelessRecord setSsid(String ssid) {
		this.ssid = ssid;
		return this;
	}
	
	public String getBssid() {
		return bssid;
	}
	
	public WirelessRecord setBssid(String bssid) {
		this.bssid = bssid;
		return this;
	}
	
	public String getTechnology() {
		return technology;
	}
	
	public WirelessRecord setTechnology(String technology) {
		this.technology = technology;
		return this;
	}
	
	public int getRssi() {
		return rssi;
	}
	
	public WirelessRecord setRssi(int rssi) {
		this.rssi = rssi;
		return this;
	}
	
	public int getTime() {
		return time;
	}
	
	public WirelessRecord setTime(int time) {
		this.time = time;
		return this;
	}
	
	public int getDifference() {
		return difference;
	}
	
	public WirelessRecord setDifference(int difference) {
		this.difference = difference;
		return this;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public WirelessRecord setFrequency(int frequency) {
		this.frequency = frequency;
		return this;
	}
	
	public int getChannel() {
		return channel;
	}
	
	public WirelessRecord setChannel(int channel) {
		this.channel = channel;
		return this;
	}
	
	public float getDistance() {
		return distance;
	}
	
	public WirelessRecord setDistance(float distance) {
		this.distance = distance;
		return this;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public WirelessRecord setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public String getAsJson() {
		return String.format(Locale.ROOT, App.getContext().getString(R.string.modelRecordWirelessJson), ssid, bssid, rssi, distance, frequency, channel, technology, time);
	}
	
	public Map<String, Object> getAsMap() {
		Map<String, Object> wirelessRecord = new HashMap<>();
		wirelessRecord.put("timestamp", timestamp);
		wirelessRecord.put("ssid", ssid);
		wirelessRecord.put("bssid", bssid);
		wirelessRecord.put("rssi", rssi);
		wirelessRecord.put("distance", distance);
		wirelessRecord.put("time", time);
		wirelessRecord.put("difference", difference);
		wirelessRecord.put("frequency", frequency);
		wirelessRecord.put("channel", channel);
		wirelessRecord.put("technology", technology);
		return wirelessRecord;
	}
	
	/**
	 * Parse WiFi channel and technology from its frequency
	 *
	 * @see https://en.wikipedia.org/wiki/List_of_WLAN_channels#Interference_concerns
	 * @see https://en.wikipedia.org/wiki/List_of_WLAN_channels#5.C2.A0GHz_.28802.11a.2Fh.2Fj.2Fn.2Fac.29.5B17.5D
	 */
	private void parseChannelAndTechnology() {
		switch (this.frequency) {
			case 2412:
				this.channel = 1;
				this.technology = "g/n";
				break;
			case 2417:
				this.channel = 2;
				this.technology = "g/n";
				break;
			case 2422:
				this.channel = 3;
				this.technology = "g/n";
				break;
			case 2427:
				this.channel = 4;
				this.technology = "g/n";
				break;
			case 2432:
				this.channel = 5;
				this.technology = "g/n";
				break;
			case 2437:
				this.channel = 6;
				this.technology = "g/n";
				break;
			case 2442:
				this.channel = 7;
				this.technology = "g/n";
				break;
			case 2447:
				this.channel = 8;
				this.technology = "g/n";
				break;
			case 2452:
				this.channel = 9;
				this.technology = "g/n";
				break;
			case 2457:
				this.channel = 10;
				this.technology = "g/n";
				break;
			case 2462:
				this.channel = 11;
				this.technology = "g/n";
				break;
			case 2467:
				this.channel = 12;
				this.technology = "g/n";
				break;
			case 2472:
				this.channel = 13;
				this.technology = "g/n";
				break;
			case 2484:
				this.channel = 14;
				this.technology = "g/n";
				break;
			case 5035:
				this.channel = 7;
				this.technology = "a/n";
				break;
			case 5040:
				this.channel = 8;
				this.technology = "a/n";
				break;
			case 5045:
				this.channel = 9;
				this.technology = "a/n";
				break;
			case 5055:
				this.channel = 11;
				this.technology = "a/n";
				break;
			case 5060:
				this.channel = 12;
				this.technology = "a/n";
				break;
			case 5080:
				this.channel = 16;
				this.technology = "a/n";
				break;
			case 5170:
				this.channel = 34;
				this.technology = "a/n";
				break;
			case 5180:
				this.channel = 36;
				this.technology = "a/n";
				break;
			case 5190:
				this.channel = 38;
				this.technology = "a/n";
				break;
			case 5200:
				this.channel = 40;
				this.technology = "a/n";
				break;
			case 5210:
				this.channel = 42;
				this.technology = "a/n";
				break;
			case 5220:
				this.channel = 44;
				this.technology = "a/n";
				break;
			case 5230:
				this.channel = 46;
				this.technology = "a/n";
				break;
			case 5240:
				this.channel = 48;
				this.technology = "a/n";
				break;
			case 5250:
				this.channel = 50;
				this.technology = "a/n";
				break;
			case 5260:
				this.channel = 52;
				this.technology = "a/n";
				break;
			case 5270:
				this.channel = 54;
				this.technology = "a/n";
				break;
			case 5280:
				this.channel = 56;
				this.technology = "a/n";
				break;
			case 5290:
				this.channel = 58;
				this.technology = "a/n";
				break;
			case 5300:
				this.channel = 60;
				this.technology = "a/n";
				break;
			case 5310:
				this.channel = 62;
				this.technology = "a/n";
				break;
			case 5320:
				this.channel = 64;
				this.technology = "a/n";
				break;
			case 5500:
				this.channel = 100;
				this.technology = "a/n";
				break;
			case 5510:
				this.channel = 102;
				this.technology = "a/n";
				break;
			case 5520:
				this.channel = 104;
				this.technology = "a/n";
				break;
			case 5530:
				this.channel = 106;
				this.technology = "a/n";
				break;
			case 5540:
				this.channel = 108;
				this.technology = "a/n";
				break;
			case 5550:
				this.channel = 110;
				this.technology = "a/n";
				break;
			case 5560:
				this.channel = 112;
				this.technology = "a/n";
				break;
			case 5570:
				this.channel = 114;
				this.technology = "a/n";
				break;
			case 5580:
				this.channel = 116;
				this.technology = "a/n";
				break;
			case 5590:
				this.channel = 118;
				this.technology = "a/n";
				break;
			case 5600:
				this.channel = 120;
				this.technology = "a/n";
				break;
			case 5610:
				this.channel = 122;
				this.technology = "a/n";
				break;
			case 5620:
				this.channel = 124;
				this.technology = "a/n";
				break;
			case 5630:
				this.channel = 126;
				this.technology = "a/n";
				break;
			case 5640:
				this.channel = 128;
				this.technology = "a/n";
				break;
			case 5660:
				this.channel = 132;
				this.technology = "a/n";
				break;
			case 5670:
				this.channel = 134;
				this.technology = "a/n";
				break;
			case 5680:
				this.channel = 136;
				this.technology = "a/n";
				break;
			case 5690:
				this.channel = 138;
				this.technology = "a/n";
				break;
			case 5700:
				this.channel = 140;
				this.technology = "a/n";
				break;
			case 5710:
				this.channel = 142;
				this.technology = "a/n";
				break;
			case 5720:
				this.channel = 144;
				this.technology = "a/n";
				break;
			case 5745:
				this.channel = 149;
				this.technology = "a/n";
				break;
			case 5755:
				this.channel = 151;
				this.technology = "a/n";
				break;
			case 5765:
				this.channel = 153;
				this.technology = "a/n";
				break;
			case 5775:
				this.channel = 155;
				this.technology = "a/n";
				break;
			case 5785:
				this.channel = 157;
				this.technology = "a/n";
				break;
			case 5795:
				this.channel = 159;
				this.technology = "a/n";
				break;
			case 5805:
				this.channel = 161;
				this.technology = "a/n";
				break;
			case 5825:
				this.channel = 165;
				this.technology = "a/n";
				break;
			case 4915:
				this.channel = 183;
				this.technology = "a/n";
				break;
			case 4920:
				this.channel = 184;
				this.technology = "a/n";
				break;
			case 4925:
				this.channel = 185;
				this.technology = "a/n";
				break;
			case 4935:
				this.channel = 187;
				this.technology = "a/n";
				break;
			case 4940:
				this.channel = 188;
				this.technology = "a/n";
				break;
			case 4945:
				this.channel = 189;
				this.technology = "a/n";
				break;
			case 4960:
				this.channel = 192;
				this.technology = "a/n";
				break;
			case 4980:
				this.channel = 196;
				this.technology = "a/n";
			default:
				this.channel = -1;
				this.technology = "";
				break;
		}
	}
}