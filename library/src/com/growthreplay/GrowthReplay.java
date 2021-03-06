package com.growthreplay;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.growthreplay.model.Client;
import com.growthreplay.model.Client.RecordStatus;
import com.growthreplay.model.Configuration;
import com.growthreplay.model.Picture;
import com.growthreplay.model.Tag;
import com.growthreplay.utils.DeviceUtils;

public class GrowthReplay {

	public static final String BASE_URL = "https://api.stg.growthreplay.com/";
	private static final float SDK_VERSION = 0.3f;

	private Semaphore semaphore = new Semaphore(1);
	private CountDownLatch latch = new CountDownLatch(1);

	private Recorder recorder = new Recorder();
	private final SavePictureHandler pictureHandler = new SavePictureHandler() {
		@Override
		public void savePicture(byte[] data) {
			sendPicture(data);
		}
	};

	private Context context = null;
	private int applicationId;
	private String secret;
	private Client client = null;
	private int pictureLimit = 0;
	private boolean recordedCheck = true;

	private static GrowthReplay instance = new GrowthReplay();

	public static GrowthReplay getInstance() {
		return instance;
	}

	public GrowthReplay initialize(final Context context, final int applicationId, final String secret, final boolean debug) {

		this.context = context.getApplicationContext();
		this.applicationId = applicationId;
		this.secret = secret;
		Logger.setDebug(debug);
		Preference.getInstance().setContext(context);
		this.recorder.setHandler(pictureHandler);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Logger.warning("Growth Replay SDK required API Level 14 or more.");
			return this;
		}

		authorize();
		return this;
	}

	private void authorize() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				Client refClient = Preference.getInstance().fetchClient();
				if (refClient == null || (refClient != null && refClient.getApplicationId() != applicationId))
					refClient = new Client();

				try {
					semaphore.acquire();

					Logger.info(String.format("client authorize. applicationId:%d", applicationId));
					refClient = refClient.authorize(applicationId, secret);
					Logger.info(String.format("client success (clientId: %d)", refClient.getClientId()));

					final Configuration configuration = refClient.getClientConfiguration();
					if (refClient.isRecorded()) {
						pictureLimit = refClient.getClientConfiguration().getNumberOfRemaining();
						Logger.info("picture number of remaining " + pictureLimit);
						recorder.startWidthConfiguration(configuration);
					}

					if (refClient.getStatus() == RecordStatus.already)
						recordedCheck = false;

					client = refClient;
					latch.countDown();

				} catch (InterruptedException e) {
				} finally {
					GrowthReplay.this.semaphore.release();
				}

			}
		}).start();
	}

	public void setTag(final String name, final String value) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Tag tag = new Tag(name, value);
				waitRegisterClient();
				tag.send(client.getClientId(), client.getToken());
			}
		}).start();
	}

	public void setDeviceTags() {
		setTag("deviceModel", DeviceUtils.getDevice());
		setTag("deviceVersion", DeviceUtils.getOSVersion());
		setTag("os", "android");
		setTag("sdkVersion", String.valueOf(SDK_VERSION));

		if (context != null) {
			try {
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				GrowthReplay.getInstance().setTag("appVersion", pInfo.versionName);
			} catch (NameNotFoundException e) {
			}
		}

	}

	public void setActivity(final Activity activity) {
		if (this.recorder != null) {
			this.recorder.setActivity(activity);
		}
	}

	public void start() {
		if (this.recorder != null)
			this.recorder.setRec(true);
	}

	public void stop() {
		if (this.recorder != null)
			this.recorder.setRec(false);
	}

	public void setSpot(String spot) {
		if (this.recorder != null)
			this.recorder.setSpot(spot);
	}

	private void sendPicture(byte[] data) {

		if (this.pictureLimit <= 0)
			this.recorder.stop();

		if (data == null || data.length <= 0)
			return;

		Picture picture = null;
		if (client.getClientConfiguration().getWheres() == null)
			picture = this.client.savePicture(data, false);
		else {
			picture = this.client.savePicture(data, recordedCheck);
			recordedCheck = false;
		}

		if (picture.getStatus()) {
			this.pictureLimit--;
			Logger.debug(String.format("success send image (picture limit size: %d)", this.pictureLimit));
		} else
			Logger.debug("fail save image");

		if (!picture.isContinuation() || this.pictureLimit <= 0)
			this.recorder.stop();

	}

	public Context getContext() {
		return context;
	}

	private void waitRegisterClient() {

		if (this.client == null) {
			try {
				latch.await();
			} catch (InterruptedException e) {
			}
		}

	}

	static interface SavePictureHandler {

		public void savePicture(byte[] data);

	}

}
