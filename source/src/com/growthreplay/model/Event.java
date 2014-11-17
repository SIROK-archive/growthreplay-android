package com.growthreplay.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.growthbeat.model.Model;

public class Event extends Model {

	private int goalId;

	private long timestamp;

	private long clientId;

	private String value;

	public Event() {
	}

	public Event(String value) {
		setValue(value);
	}

	public Event send(long clientId, String token, String name) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", clientId);
		params.put("token", token);
		params.put("name", name);
		params.put("value", getValue());

		JSONObject json = post("v1", "event", params);
		setJson(json);

		return this;
	}

	public int getGoalId() {
		return goalId;
	}

	public void setGoalId(int goalId) {
		this.goalId = goalId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void setJsonObject(JSONObject json) {

		if (json == null)
			return;

		try {
			if (json.has("goalId") && !json.isNull("goalId"))
				setGoalId(json.getInt("goalId"));
			if (json.has("timestamp") && !json.isNull("timestamp"))
				setTimestamp(json.getLong("timestamp"));
			if (json.has("clientId") && !json.isNull("clientId"))
				setClientId(json.getLong("clientId"));
			if (json.has("value") && !json.isNull("value"))
				setValue(json.getString("value"));
		} catch (JSONException e) {
		}

	}

	@Override
	public JSONObject getJsonObject() {
		return null;
	}

}
