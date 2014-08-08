package com.growthreplay.model;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.growthreplay.GrowthReplay;

public class Model {

	public Model() {
		super();
	}

	public JSONObject post(String version, String api, Map<String, Object> params) {
		return GrowthReplay.getInstance().getHttpClient().post(version + "/" + api, params);
	}

	public JSONObject multipartPost(String version, String api, List<NameValuePair> params, byte[] file) {
		return GrowthReplay.getInstance().getHttpClient().multipartPost(version + "/" + api, params, file);
	}

}
