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

import com.growthbeat.model.Model;
import com.growthbeat.utils.DeviceUtils;
import com.growthreplay.GrowthReplay;

public class Client extends Model {

	private int applicationId;

	private long clientId;

	private String growthbeatClientId;

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

	private Client(JSONObject jsonObject) {
		setJsonObject(jsonObject);
	}

	public static Client authorize(Context context, String growthbeatClientId, String credentialId) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", growthbeatClientId);
		params.put("credentialId", credentialId);

		params.put("os", "android");
		params.put("network", DeviceUtils.connectedToWiFi(context) ? "wifi" : "carrier");

		JSONObject jsonObject = GrowthReplay.getInstance().getHttpClient().post("v3/records", params);

		return new Client(jsonObject);
	}

	public void cancel() {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", getClientId());
		params.put("token", getToken());
		params.put("recordScheduleToken", getRecordScheduleToken());

		GrowthReplay.getInstance().getHttpClient().post("v1/cancel", params);
	}

	public static Picture savePicture(String clientId, String credentialId, String recordScheduleToken, byte[] file, boolean recordedCheck) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("clientId", clientId));
		params.add(new BasicNameValuePair("credentialId", credentialId));
		params.add(new BasicNameValuePair("recordScheduleToken", recordScheduleToken));
		params.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
		params.add(new BasicNameValuePair("recordedCheck", String.valueOf(recordedCheck)));

		JSONObject response = GrowthReplay.getInstance().getHttpClient().multipartPost("v3/picture", params, file);
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

	public String getGrowthbeatClientId() {
		return growthbeatClientId;
	}

	public void setGrowthbeatClientId(String growthbeatClientId) {
		this.growthbeatClientId = growthbeatClientId;
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

	@Override
	public void setJsonObject(JSONObject jsonObject) {

		if (jsonObject == null)
			return;

		try {
			if (jsonObject.has("applicationId"))
				setApplicationId(jsonObject.getInt("applicationId"));
			if (jsonObject.has("clientId"))
				setClientId(jsonObject.getLong("clientId"));
			if (jsonObject.has("growthbeatClientId"))
				setGrowthbeatClientId(jsonObject.getString("growthbeatClientId"));
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

	@Override
	public JSONObject getJsonObject() {

		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("applicationId", getApplicationId());
			jsonObject.put("clientId", getClientId());
			jsonObject.put("growthbeatClientId", getGrowthbeatClientId());
			jsonObject.put("token", getToken());
		} catch (JSONException e) {
		}

		return jsonObject;
	}

}
