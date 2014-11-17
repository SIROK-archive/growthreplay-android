package com.growthreplay.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.growthbeat.http.BaseHttpClient;
import com.growthbeat.http.HttpRequest;
import com.growthbeat.http.HttpResponse;
import com.growthbeat.model.Error;
import com.growthreplay.GrowthReplayException;

public class GrowthReplayHttpClient extends BaseHttpClient {

	private static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";

	public GrowthReplayHttpClient() {
		super();
	}

	public GrowthReplayHttpClient(String baseUrl) {
		this();
		setBaseUrl(baseUrl);
	}

	public JSONObject post(String api, Map<String, Object> params) {

		JSONObject body = new JSONObject();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			try {
				body.put(entry.getKey(), entry.getValue());
			} catch (JSONException e) {
			}
		}

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json; charset=UTF-8");
		HttpEntity entity = null;
		try {
			entity = new StringEntity(body.toString(), HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new GrowthReplayException("Failed to encode request body.", e);
		}
		HttpRequest httpRequest = new HttpRequest().withMethod("POST").withPath(api).withHeaders(headers).withEntity(entity);
		HttpResponse httpResponse = super.request(httpRequest);
		return fetchJSONObject(httpResponse);

	}

	public JSONObject multipartPost(String api, final List<NameValuePair> params, byte[] file) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			byteArrayOutputStream.write(createBoundaryMessage(params).getBytes());
			byteArrayOutputStream.write(file);
			byteArrayOutputStream.write("\r\n--".getBytes());
			byteArrayOutputStream.write(BOUNDARY.getBytes());
			byteArrayOutputStream.write("--\r\n".getBytes());
		} catch (IOException e) {
			throw new GrowthReplayException("Failed to encode request. ", e);
		}

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		HttpEntity entity = null;
		entity = new ByteArrayEntity(byteArrayOutputStream.toByteArray());
		HttpRequest httpRequest = new HttpRequest().withMethod("POST").withPath(api).withHeaders(headers).withEntity(entity);
		HttpResponse httpResponse = super.request(httpRequest);
		return fetchJSONObject(httpResponse);

	}

	private JSONObject fetchJSONObject(HttpResponse httpResponse) {

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(httpResponse.getBody());
		} catch (JSONException e) {
			throw new GrowthReplayException("Failed to parse response JSON. " + e.getMessage(), e);
		}

		if (httpResponse.getStatus() < 200 || httpResponse.getStatus() >= 300) {
			Error error = new Error(jsonObject);
			throw new GrowthReplayException(error.getMessage());
		}

		return jsonObject;

	}

	private String createBoundaryMessage(List<NameValuePair> params) {
		StringBuffer stringBuffer = new StringBuffer("--").append(BOUNDARY).append("\r\n");
		for (NameValuePair nv : params) {
			stringBuffer.append("Content-Disposition: form-data; name=\"").append(nv.getName()).append("\"\r\n").append("\r\n")
					.append(nv.getValue()).append("\r\n").append("--").append(BOUNDARY).append("\r\n");
		}
		String fileType = "image/jpeg";
		stringBuffer.append("Content-Disposition: form-data; name=\"").append("file").append("\"; filename=\"")
				.append(System.currentTimeMillis() + ".jpg").append("\"\r\n").append("Content-Type: ").append(fileType).append("\r\n\r\n");
		return stringBuffer.toString();
	}

}
