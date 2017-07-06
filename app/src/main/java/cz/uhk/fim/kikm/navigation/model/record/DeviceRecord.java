package cz.uhk.fim.kikm.navigation.model.record;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cz.uhk.fim.kikm.navigation.R;
import cz.uhk.fim.kikm.navigation.activity.App;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class DeviceRecord implements Serializable {
	private String id, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os;
	private int api;

	public DeviceRecord(Context context) {
		this.id = Build.ID;
		this.board = Build.BOARD;
		this.bootloader = Build.BOOTLOADER;
		this.brand = Build.BRAND;
		this.device = Build.DEVICE;
		this.display = Build.DISPLAY;
		this.fingerprint = Build.FINGERPRINT;
		this.hardware = Build.HARDWARE;
		this.host = Build.HOST;
		this.manufacturer = Build.MANUFACTURER;
		this.model = Build.MODEL;
		this.product = Build.PRODUCT;
		this.serial = Build.SERIAL;
		this.tags = Build.TAGS;
		this.type = Build.TYPE;
		this.telephone = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		this.user = Build.USER;
		this.os = Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE;
		this.api = Build.VERSION.SDK_INT;
	}

	public DeviceRecord(Object object) {
		Map<String, Object> deviceRecord = (HashMap<String, Object>) object;
		this.id = deviceRecord.get("id").toString();
		this.board = deviceRecord.get("board").toString();
		this.bootloader = deviceRecord.get("bootloader").toString();
		this.brand = deviceRecord.get("brand").toString();
		this.device = deviceRecord.get("device").toString();
		this.display = deviceRecord.get("display").toString();
		this.fingerprint = deviceRecord.get("fingerprint").toString();
		this.hardware = deviceRecord.get("hardware").toString();
		this.host = deviceRecord.get("host").toString();
		this.manufacturer = deviceRecord.get("manufacturer").toString();
		this.model = deviceRecord.get("model").toString();
		this.product = deviceRecord.get("product").toString();
		this.serial = deviceRecord.get("serial").toString();
		this.tags = deviceRecord.get("tags").toString();
		this.telephone = deviceRecord.get("telephone").toString();
		this.type = deviceRecord.get("type").toString();
		this.user = deviceRecord.get("user").toString();
		this.os = deviceRecord.get("os").toString();
		this.api = Integer.valueOf(deviceRecord.get("api").toString());
	}

	public String getId() {
		return id;
	}

	public DeviceRecord setId(String id) {
		this.id = id;
		return this;
	}

	public String getBoard() {
		return board;
	}

	public DeviceRecord setBoard(String board) {
		this.board = board;
		return this;
	}

	public String getBootloader() {
		return bootloader;
	}

	public DeviceRecord setBootloader(String bootloader) {
		this.bootloader = bootloader;
		return this;
	}

	public String getBrand() {
		return brand;
	}

	public DeviceRecord setBrand(String brand) {
		this.brand = brand;
		return this;
	}

	public String getDevice() {
		return device;
	}

	public DeviceRecord setDevice(String device) {
		this.device = device;
		return this;
	}

	public String getDisplay() {
		return display;
	}

	public DeviceRecord setDisplay(String display) {
		this.display = display;
		return this;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public DeviceRecord setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
		return this;
	}

	public String getHardware() {
		return hardware;
	}

	public DeviceRecord setHardware(String hardware) {
		this.hardware = hardware;
		return this;
	}

	public String getHost() {
		return host;
	}

	public DeviceRecord setHost(String host) {
		this.host = host;
		return this;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public DeviceRecord setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
		return this;
	}

	public String getModel() {
		return model;
	}

	public DeviceRecord setModel(String model) {
		this.model = model;
		return this;
	}

	public String getProduct() {
		return product;
	}

	public DeviceRecord setProduct(String product) {
		this.product = product;
		return this;
	}

	public String getSerial() {
		return serial;
	}

	public DeviceRecord setSerial(String serial) {
		this.serial = serial;
		return this;
	}

	public String getTags() {
		return tags;
	}

	public DeviceRecord setTags(String tags) {
		this.tags = tags;
		return this;
	}

	public String getTelephone() {
		return telephone;
	}

	public DeviceRecord setTelephone(String telephone) {
		this.telephone = telephone;
		return this;
	}

	public String getType() {
		return type;
	}

	public DeviceRecord setType(String type) {
		this.type = type;
		return this;
	}

	public String getUser() {
		return user;
	}

	public DeviceRecord setUser(String user) {
		this.user = user;
		return this;
	}

	public String getOs() {
		return os;
	}

	public DeviceRecord setOs(String os) {
		this.os = os;
		return this;
	}

	public int getApi() {
		return api;
	}

	public DeviceRecord setApi(int api) {
		this.api = api;
		return this;
	}

	public String getAsJson() {
		return String.format(App.getContext().getString(R.string.modelRecordDeviceJson), id, board, bootloader, brand, device, display, fingerprint, hardware, host, manufacturer, model, product, serial, tags, telephone, type, user, os, api);
	}

	public Map<String, Object> getAsMap() {
		Map<String, Object> deviceRecord = new HashMap<>();
		deviceRecord.put("id", id);
		deviceRecord.put("board", board);
		deviceRecord.put("bootloader", bootloader);
		deviceRecord.put("brand", brand);
		deviceRecord.put("device", device);
		deviceRecord.put("display", display);
		deviceRecord.put("fingerprint", fingerprint);
		deviceRecord.put("hardware", hardware);
		deviceRecord.put("host", host);
		deviceRecord.put("manufacturer", manufacturer);
		deviceRecord.put("model", model);
		deviceRecord.put("product", product);
		deviceRecord.put("serial", serial);
		deviceRecord.put("tags", tags);
		deviceRecord.put("telephone", telephone);
		deviceRecord.put("type", type);
		deviceRecord.put("user", user);
		deviceRecord.put("os", os);
		deviceRecord.put("api", api);
		return deviceRecord;
	}
}