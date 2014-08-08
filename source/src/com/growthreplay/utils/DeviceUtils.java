package com.growthreplay.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.growthreplay.GrowthReplay;

public class DeviceUtils {

	public static boolean enabledWifiNetwork() {

		ConnectivityManager connectivityManager = (ConnectivityManager) GrowthReplay.getInstance().getContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			State wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (wifi == State.CONNECTED || wifi == State.CONNECTING)
				return true;
		} catch (Exception e) {
		}

		return false;

	}

	public static long getAvailableMemory() {
		ActivityManager activityManager = (ActivityManager) GrowthReplay.getInstance().getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem;
	}

	public static boolean lowMemory() {
		ActivityManager activityManager = (ActivityManager) GrowthReplay.getInstance().getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo.lowMemory;
	}

	@SuppressWarnings("deprecation")
	public static Point getDisplaySize() {
		WindowManager wm = (WindowManager) GrowthReplay.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);
		Point point = new Point();
		Display display = wm.getDefaultDisplay();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
			display.getSize(point);
		else {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}

		return point;
	}

}
