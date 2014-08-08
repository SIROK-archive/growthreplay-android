package com.growthreplay;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.view.View;

import com.growthreplay.GrowthReplay.SavePictureHandler;
import com.growthreplay.model.Configuration;
import com.growthreplay.utils.BitmapUtils;
import com.growthreplay.utils.DeviceUtils;

class Recorder {

	private Activity activity;
	private Configuration configuration;
	private Timer timer;
	private boolean continuation = true;
	private boolean isRec = false;
	private String spot = null;

	private SavePictureHandler handler;

	public Recorder() {
	}

	public void startWidthConfiguration(Configuration configuration) {

		setConfiguration(configuration);

		long schedule = (long) (this.configuration.getRecordTerm() * 1000);
		if (schedule <= 0)
			return;

		if (this.timer == null) {

			this.timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					if (!Recorder.this.continuation) {
						this.cancel();
						Recorder.this.timer.cancel();
						Recorder.this.timer.purge();
						return;
					}

					takeScreenshot();

				}
			}, 0, schedule);

		}

	}

	private void takeScreenshot() {
		if (activity == null || !activity.hasWindowFocus() || activity.isFinishing() || !checkSpot())
			return;

		if (!continuation) {
			GrowthReplay.getInstance().getLogger().debug("screenshot limit size!");
			Recorder.this.timer.cancel();
			return;
		}

		if (!DeviceUtils.enabledWifiNetwork() || !isRec)
			return;

		final View view = activity.getWindow().getDecorView().getRootView();
		if (view == null)
			return;
		if (DeviceUtils.lowMemory())
			return;

		final Point display = DeviceUtils.getDisplaySize();
		float scale = BitmapUtils.resizeScale(display.x, display.y, configuration.getScale());
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		int width = (int) (display.x * scale);
		int height = (int) (display.y * scale);
		final Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		final Canvas canvas = new Canvas(bitmap);
		canvas.setMatrix(matrix);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				view.draw(canvas);
				new Thread(new Runnable() {
					@Override
					public void run() {
						send(bitmap);
					}
				}).start();
			}
		});

	}

	private boolean checkSpot() {

		List<String> wheres = this.configuration.getWheres();
		if (wheres != null) {
			for (String spot : wheres) {
				if (spot.equals(this.spot))
					return true;
			}
			return false;
		}

		return true;

	}

	public void send(Bitmap bitmap) {
		if (continuation) {
			if (DeviceUtils.enabledWifiNetwork()) {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, this.configuration.getCompressibility(), byteArrayOutputStream);
				sendPicture(byteArrayOutputStream.toByteArray());
			}
		}
		bitmap.recycle();
		bitmap = null;
	}

	private void sendPicture(byte[] file) {
		if (isRec && handler != null && file != null) {
			handler.savePicture(file);
		}
	}

	public void stop() {
		Recorder.this.continuation = false;
		Recorder.this.timer.cancel();
		Recorder.this.timer.purge();
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setHandler(SavePictureHandler savePictureHandler) {
		this.handler = savePictureHandler;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public void setRec(boolean isRec) {
		this.isRec = isRec;
	}

	public void setSpot(String spot) {
		this.spot = spot;
	}

}
