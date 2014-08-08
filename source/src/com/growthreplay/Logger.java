package com.growthreplay;

import android.util.Log;

class Logger {

	private static final String TAG = "GrowthReplay";

	private static boolean debug = false;

	public static void debug(String message) {

		if (debug)
			Log.d(TAG, message);

	}

	public static void info(String message) {

		if (debug)
			Log.i(TAG, message);

	}

	public static void warning(String message) {

		if (debug)
			Log.w(TAG, message);

	}

	public static void error(String message) {

		if (debug)
			Log.e(TAG, message);

	}

	public static void setDebug(boolean debug) {
		Logger.debug = debug;
	}

}
