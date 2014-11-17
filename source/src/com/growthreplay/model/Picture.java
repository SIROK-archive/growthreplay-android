package com.growthreplay.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.growthbeat.model.Model;

public class Picture extends Model {

	private boolean continuation;

	private boolean status;

	private boolean recordedClient;

	public Picture() {
	}

	public Picture(JSONObject jsonObject) {
		setJsonObject(jsonObject);
	}

	public boolean isContinuation() {
		return continuation;
	}

	public void setContinuation(boolean continuation) {
		this.continuation = continuation;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean getRecordedClient() {
		return recordedClient;
	}

	public void setRecordedClient(boolean recordedClient) {
		this.recordedClient = recordedClient;
	}

	@Override
	public void setJsonObject(JSONObject jsonObject) {

		if (jsonObject == null)
			return;

		try {
			if (jsonObject.has("continuation") && !jsonObject.isNull("continuation"))
				setContinuation(jsonObject.getBoolean("continuation"));
			if (jsonObject.has("status") && !jsonObject.isNull("status"))
				setStatus(jsonObject.getBoolean("status"));
			if (jsonObject.has("recordedClient") && !jsonObject.isNull("recordedClient"))
				setRecordedClient(jsonObject.getBoolean("recordedClient"));
		} catch (JSONException e) {
		}

	}

	@Override
	public JSONObject getJsonObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
