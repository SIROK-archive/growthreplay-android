package com.growthreplay.utils;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;

import com.growthbeat.GrowthbeatCore;

public class BitmapUtils {

	public static Bitmap createBitmapFromBytes(byte[] data, int width, int height) {

		int[] colors = new int[width * height];
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = (y * width + x) * 3;
				int r = (((byte) data[index + 0]) & 0x00FF) & getMask(8);
				int g = (((byte) data[index + 1]) & 0x00FF) & getMask(8);
				int b = (((byte) data[index + 2]) & 0x00FF) & getMask(8);
				colors[x + y * width] = Color.rgb(r, g, b);
			}
		}
		data = null;

		bitmap.setPixels(colors, 0, width, 0, 0, width, height);
		colors = null;

		return bitmap;
	}

	private static int getMask(int length) {
		return (1 << length) - 1;
	}

	public static float resizeScale(int width, int height, float configurationScale) {
		Configuration config = GrowthbeatCore.getInstance().getContext().getResources().getConfiguration();
		int baseWidth = 360;
		int baseHeight = 480;
		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			baseWidth = 480;
			baseHeight = 360;
		}

		float scale = configurationScale;
		if (width > baseWidth || height > baseHeight) {

			if (width == height)
				scale = (float) width / baseWidth;
			else
				scale = (float) width > height ? width / baseWidth : height / baseHeight;

			if (scale <= 0)
				scale = configurationScale;
			else
				scale = configurationScale / scale;
		}

		return scale;
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, float configurationScale) {

		Matrix matrix = new Matrix();
		float scale = resizeScale(bitmap.getWidth(), bitmap.getHeight(), configurationScale);
		matrix.postScale(scale, scale);

		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		bitmap = null;

		return scaledBitmap;
	}

}
