package com.android.emerson.dl.utils;

import java.util.ArrayList;
import java.util.List;

import com.android.emerson.dl.core.DownloadTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class ConfigUtils {

	public static final String	PREFERENCE_NAME	= "com.emerson.download";

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
	}

	public static String getString(Context context, String key) {
		SharedPreferences preferences = getPreferences(context);
		if (preferences != null)
			return preferences.getString(key, "");
		else
			return "";
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences preferences = getPreferences(context);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}

	public static final int		URL_COUNT	= DownloadTask.MAX_DOWNLOAD_THREAD_COUNT;
	public static final String	KEY_URL		= "url";

	public static void storeURL(Context context, int index, String url) {
		setString(context, KEY_URL + index, url);
	}

	public static void clearURL(Context context, int index) {
		setString(context, KEY_URL + index, "");
	}

	public static String getURL(Context context, int index) {
		return getString(context, KEY_URL + index);
	}

	public static List<String> getURLArray(Context context) {
		List<String> urlList = new ArrayList<String>();
		for (int i = 0; i < URL_COUNT; i++) {
			if (!TextUtils.isEmpty(getURL(context, i))) {
				urlList.add(getString(context, KEY_URL + i));
			}
		}
		return urlList;
	}
}
