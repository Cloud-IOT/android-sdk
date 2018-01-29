package com.dtston.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dtston.dtcloud.DeviceManager;
import com.dtston.dtcloud.push.DTProtocolVersion;

import org.json.JSONException;
import org.json.JSONObject;


public class SharedPreferencesUtils {
	
	public static final String PREFERENCE_NAME = "dhome";
	public static final String PREFERENCE_KEY_WIFI_PASSWORD = "key_wifi_password";
	public static final String PREFERENCE_KEY_REMOTE_CONTROL = "key_remote_control";
	public static final String PREFERENCE_KEY_EXIT_USERNAME = "key_exit_username";
	private static String PREFERENCE_KEY_SOUND = "preference_key_sound"; 
	
	private static String PREFERENCE_KEY_WIFI_MODULE = "preference_key_wifi_module"; 
	private static String PREFERENCE_KEY_PROTOCOL = "preference_key_protocol";

	public static void editSound(Context context, boolean hasSound) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putBoolean(PREFERENCE_KEY_SOUND, hasSound);
		editor.commit();//提交修改
	}
	
	public static boolean hasSound(Context context) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean hasSound = share.getBoolean(PREFERENCE_KEY_SOUND, true);
		return hasSound;
	}
	
	public static String getExitUserName(Context context) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		String userName = share.getString(PREFERENCE_KEY_EXIT_USERNAME, "");
		return userName;
	}
	
	public static void editExitUserName(Context context, String userName) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putString(PREFERENCE_KEY_EXIT_USERNAME, userName);
		editor.commit();//提交修改
	}
	
	public static void rememberPassword(Context context, String ssid, String password) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		String passwordsStr = share.getString(PREFERENCE_KEY_WIFI_PASSWORD, "");
		JSONObject newPasswordJson = null;
		try {
			newPasswordJson = new JSONObject(passwordsStr);
			if (newPasswordJson.has(ssid)) {
				newPasswordJson.remove(ssid);
			}
		} catch (Throwable e) {
			newPasswordJson = new JSONObject();
		}
		try {
			newPasswordJson.put(ssid, password);
		} catch (JSONException e) {
		}
		share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putString(PREFERENCE_KEY_WIFI_PASSWORD, newPasswordJson.toString());
		editor.commit();//提交修改
	}
	
	public static String getPassword(Context context, String ssid) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		String passwordsStr = share.getString(PREFERENCE_KEY_WIFI_PASSWORD, "");
		String password = "";
		try {
			JSONObject jsonObject = new JSONObject(passwordsStr);
			password = jsonObject.getString(ssid);
		} catch (Throwable e) {
		}
		return password;
	}
	
	public static void editDeviceShake(Context context, String mac, boolean open) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.putBoolean(mac, open);
		editor.commit();//提交修改
	}
	
	public static boolean getDeviceShake(Context context, String mac) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean shake = share.getBoolean(mac, false);
		return shake;
	}
	
	public static void remove(Context context, String key) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = share.edit();
		editor.remove(key).commit();
	}
	
	public static int getWifiModule(Context context) {
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int wifiModule = share.getInt(PREFERENCE_KEY_WIFI_MODULE, DeviceManager.WIFI_LX);
		return wifiModule;
	}
	
	public static void editWifiModule(Context context, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(PREFERENCE_KEY_WIFI_MODULE, value);
		editor.commit();//提交修改
	}

	public static int getProtocolVersion(Context context,String mac){
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int protocolVersion = share.getInt(mac, DTProtocolVersion.TYPE_THIRD);
		return protocolVersion;
	}
	public static void editProtocolVersion(Context context, String mac,int protocolVersion) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(mac, protocolVersion);
		editor.apply();//提交修改
	}
	
}
