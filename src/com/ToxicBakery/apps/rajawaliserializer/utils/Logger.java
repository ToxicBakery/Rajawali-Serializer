package com.ToxicBakery.apps.rajawaliserializer.utils;

import android.util.Log;

public class Logger {

	private static final String LOG_NAME = "Rajwali Serializer";
	
	/**
	 * Log information to the console
	 * @param msg
	 */
	public static void i(Object msg) {
		Log.i(LOG_NAME, msg.toString());
	}
	
	/**
	 * Log error to the console
	 * @param msg
	 */
	public static void e(Object msg) {
		Log.e(LOG_NAME, msg.toString());
	}
	
}
