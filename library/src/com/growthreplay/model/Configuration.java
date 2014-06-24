package com.growthreplay.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {

	private float recordTerm;

	private int compressibility;

	private float scale;

	private int numberOfRemaining;

	private List<String> wheres;

	public Configuration(JSONObject jsonObject) {
		setJsonObject(jsonObject);
	}

	public float getRecordTerm() {
		return recordTerm;
	}

	public void setRecordTerm(float recordTerm) {
		this.recordTerm = recordTerm;
	}

	public int getCompressibility() {
		return compressibility;
	}

	public void setCompressibility(int compressibility) {
		this.compressibility = compressibility;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getNumberOfRemaining() {
		return numberOfRemaining;
	}

	public void setNumberOfRemaining(int numberOfRemaining) {
		this.numberOfRemaining = numberOfRemaining;
	}

	public List<String> getWheres() {
		return wheres;
	}

	public void setWheres(List<String> wheres) {
		this.wheres = wheres;
	}

	public void setJsonObject(JSONObject jsonObject) {

		if (jsonObject == null)
			return;

		try {
			if (jsonObject.has("recordTerm") && !jsonObject.isNull("recordTerm"))
				setRecordTerm(Float.valueOf(jsonObject.getString("recordTerm")));
			if (jsonObject.has("compressibility") && !jsonObject.isNull("compressibility"))
				setCompressibility(jsonObject.getInt("compressibility"));
			if (jsonObject.has("scale") && !jsonObject.isNull("scale"))
				setScale(Float.valueOf(jsonObject.getString("scale")));
			if (jsonObject.has("numberOfRemaining") && !jsonObject.isNull("numberOfRemaining"))
				setNumberOfRemaining(jsonObject.getInt("numberOfRemaining"));
			if (jsonObject.has("wheres") && !jsonObject.isNull("wheres")) {
				List<String> wheres = new ArrayList<String>();
				JSONArray array = jsonObject.getJSONArray("wheres");
				for (int i = 0; i < array.length(); i++)
					wheres.add(array.get(i).toString());
				setWheres(wheres);
			}
		} catch (JSONException e) {
		}

	}

	public JSONObject getJsonObject() {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("recordTerm", getRecordTerm());
			jsonObject.put("compressibility", getCompressibility());
			jsonObject.put("scale", getScale());
			jsonObject.put("numberOfRemaining", getNumberOfRemaining());
		} catch (JSONException e) {
		}

		return jsonObject;

	}

}
