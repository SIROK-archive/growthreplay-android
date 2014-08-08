package com.growthreplay.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.growthbeat.utils.DeviceUtils;
import com.growthreplay.GrowthReplay;

public class Client extends Model {

	private int applicationId;

	private long clientId;

	private String token;

	private boolean recorded;

	private String recordScheduleToken;

	public enum RecordStatus {
		none, added, already;
	}

	private RecordStatus status;

	private Configuration clientConfiguration;

	public Client() {
		super();
	}

	public Client authorize(Context context, int applicationId, String secret) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("applicationId", applicationId);
		params.put("secret", secret);
		params.put("clientId", getClientId() > 0 ? getClientId() : null);
		params.put("token", getToken() != null ? getToken() : null);

		params.put("os", "android");
		params.put("network", DeviceUtils.connectedToWiFi(context) ? "wifi" : "carrier");
		params.put("memory", DeviceUtils.getAvailableMemory(context));
		params.put("version", com.growthbeat.utils.DeviceUtils.getOsVersion());
		params.put("model", com.growthbeat.utils.DeviceUtils.getModel());

		JSONObject jsonObject = post("v2", "authorize", params);
		setJsonObject(jsonObject);
		GrowthReplay.getInstance().getPreference().saveClient(this);

		return this;
	}

	public void cancel() {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", getClientId());
		params.put("token", getToken());
		params.put("recordScheduleToken", getRecordScheduleToken());

		post("cancel", params);
	}

	public Picture savePicture(byte[] file, boolean recordedCheck) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("clientId", String.valueOf(getClientId())));
		params.add(new BasicNameValuePair("token", getToken()));
		params.add(new BasicNameValuePair("recordScheduleToken", getRecordScheduleToken()));
		params.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
		params.add(new BasicNameValuePair("recordedCheck", String.valueOf(recordedCheck)));

		JSONObject response = multipartPost("v2", "picture", params, file);
		if (response == null)
			return null;

		return new Picture(response);

	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isRecorded() {
		return recorded;
	}

	public void setRecorded(boolean recorded) {
		this.recorded = recorded;
	}

	public String getRecordScheduleToken() {
		return recordScheduleToken;
	}

	public void setRecordScheduleToken(String recordScheduleToken) {
		this.recordScheduleToken = recordScheduleToken;
	}

	public RecordStatus getStatus() {
		return status;
	}

	public void setStatus(RecordStatus status) {
		this.status = status;
	}

	public Configuration getClientConfiguration() {
		return clientConfiguration;
	}

	public void setClientConfiguration(Configuration clientConfiguration) {
		this.clientConfiguration = clientConfiguration;
	}

	public void setJsonObject(JSONObject jsonObject) {

		if (jsonObject == null)
			return;

		try {
			if (jsonObject.has("applicationId"))
				setApplicationId(jsonObject.getInt("applicationId"));
			if (jsonObject.has("clientId"))
				setClientId(jsonObject.getLong("clientId"));
			if (jsonObject.has("token") && !jsonObject.isNull("token"))
				setToken(jsonObject.getString("token"));
			if (jsonObject.has("recorded") && !jsonObject.isNull("recorded"))
				setRecorded(jsonObject.getBoolean("recorded"));
			if (jsonObject.has("recordClientStatus") && !jsonObject.isNull("recordClientStatus"))
				setStatus(Client.RecordStatus.valueOf(jsonObject.getString("recordClientStatus")));
			if (jsonObject.has("recordScheduleToken") && !jsonObject.isNull("recordScheduleToken"))
				setRecordScheduleToken(jsonObject.getString("recordScheduleToken"));
			if (jsonObject.has("clientConfiguration") && !jsonObject.isNull("clientConfiguration"))
				setClientConfiguration(new Configuration(jsonObject.getJSONObject("clientConfiguration")));

		} catch (JSONException e) {
		}

	}

	public JSONObject getJsonObject() {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("applicationId", getApplicationId());
			jsonObject.put("clientId", getClientId());
			jsonObject.put("token", getToken());
		} catch (JSONException e) {
		}

		return jsonObject;
	}

}
