package com.growthreplay;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.growthbeat.CatchableThread;
import com.growthbeat.GrowthbeatCore;
import com.growthbeat.Logger;
import com.growthbeat.Preference;
import com.growthbeat.analytics.GrowthAnalytics;
import com.growthreplay.model.Client;
import com.growthreplay.model.Client.RecordStatus;
import com.growthreplay.model.Configuration;
import com.growthreplay.model.GrowthReplayHttpClient;
import com.growthreplay.model.Picture;

public class GrowthReplay {

	public static final String BASE_URL = "https://api.growthreplay.com/";
	private static final float SDK_VERSION = 0.3f;

	private static final String PREFERENCE_FILE_NAME = "growthreplay-preferences";
	private static final String PREFERENCE_CLIENT_KEY = "client";

	private Semaphore semaphore = new Semaphore(1);
	private CountDownLatch latch = new CountDownLatch(1);

	private Recorder recorder = null;
	private final SavePictureHandler pictureHandler = new SavePictureHandler() {
		@Override
		public void savePicture(byte[] data) {
			sendPicture(data);
		}
	};

	private final GrowthReplayHttpClient httpClient = new GrowthReplayHttpClient();
	private final Logger logger = new Logger("GrowthReplay");
	private final Preference preference = new Preference();

	private Context context = null;
	private String applicationId;
	private String credentialId;
	private Client client = null;
	private int pictureLimit = 0;
	private boolean recordedCheck = true;

	private static GrowthReplay instance = new GrowthReplay();

	public static GrowthReplay getInstance() {
		return instance;
	}

	public void initialize(final Context context, final String applicationId, final String credentialId) {

		this.context = context.getApplicationContext();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			this.logger.warning("Growth Replay SDK required API Level 14 or more.");
			return;
		}

		this.applicationId = applicationId;
		this.credentialId = credentialId;
		this.httpClient.setBaseUrl(BASE_URL);
		this.preference.setContext(context);
		this.preference.setFileName(PREFERENCE_FILE_NAME);

		this.recorder = new Recorder(this.context);
		this.recorder.setHandler(pictureHandler);

		GrowthbeatCore.getInstance().initialize(context, applicationId, credentialId);
		new Thread(new Runnable() {

			@Override
			public void run() {

				GrowthReplay.this.logger.debug("initializeGrowthbeat");
				com.growthbeat.model.Client growthbeatClient = GrowthbeatCore.getInstance().waitClient();
				com.growthbeat.model.Client storedClient = loadClient();
				if (storedClient != null && !growthbeatClient.getId().equals(storedClient.getId()))
					GrowthReplay.this.preference.remove(PREFERENCE_CLIENT_KEY);

				authorize(growthbeatClient.getId());

			}

		}).start();

	}

	private void authorize(final String growthbeatClientId) {
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					GrowthReplay.this.logger.info(String.format("client authorize. applicationId:%s", applicationId));
					Client client = Client.authorize(GrowthReplay.this.context, growthbeatClientId, credentialId);
					GrowthReplay.this.logger.info(String.format("client success (clientId: %d)", client.getClientId()));

					final Configuration configuration = client.getClientConfiguration();
					if (client.isRecorded()) {
						pictureLimit = client.getClientConfiguration().getNumberOfRemaining();
						GrowthReplay.this.logger.info("picture number of remaining " + pictureLimit);
						recorder.startWidthConfiguration(configuration);
					}

					if (client.getStatus() == RecordStatus.already)
						recordedCheck = false;

					GrowthReplay.this.client = client;
					latch.countDown();

				} finally {
					GrowthReplay.this.semaphore.release();
				}

			}
		}).start();
	}

	public void setTag(final String tagId, final String value) {
		GrowthAnalytics.getInstance().setTag(tagId, value);
	}

	public void setDeviceTags() {
		setTag("deviceModel", com.growthbeat.utils.DeviceUtils.getModel());
		setTag("deviceVersion", com.growthbeat.utils.DeviceUtils.getOsVersion());
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
			picture = Client.savePicture(this.client.getGrowthbeatClientId(), this.credentialId, this.client.getRecordScheduleToken(),
					data, false);
		else {
			picture = Client.savePicture(this.client.getGrowthbeatClientId(), this.credentialId, this.client.getRecordScheduleToken(),
					data, recordedCheck);
			recordedCheck = false;
		}

		if (picture.getStatus()) {
			this.pictureLimit--;
			this.logger.debug(String.format("success send image (picture limit size: %d)", this.pictureLimit));
		} else
			this.logger.debug("fail save image");

		if (!picture.isContinuation() || this.pictureLimit <= 0)
			this.recorder.stop();

	}

	private com.growthbeat.model.Client loadClient() {

		JSONObject json = this.preference.get(PREFERENCE_CLIENT_KEY);
		if (json == null)
			return null;

		return new com.growthbeat.model.Client(json);

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

	public GrowthReplayHttpClient getHttpClient() {
		return httpClient;
	}

	public Logger getLogger() {
		return logger;
	}

	public Preference getPreference() {
		return preference;
	}

	static interface SavePictureHandler {

		public void savePicture(byte[] data);

	}

	private static class Thread extends CatchableThread {

		public Thread(Runnable runnable) {
			super(runnable);
		}

		@Override
		public void uncaughtException(java.lang.Thread thread, Throwable e) {
			String message = "Uncaught Exception: " + e.getClass().getName();
			if (e.getMessage() != null)
				message += "; " + e.getMessage();
			GrowthReplay.getInstance().getLogger().warning(message);
			e.printStackTrace();
		}

	}

}
