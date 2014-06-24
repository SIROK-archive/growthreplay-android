package com.growthreplay.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.growthreplay.GrowthReplay;
import com.growthreplay.GrowthReplayException;
import com.growthreplay.utils.IOUtils;

/**
 * Created by Shigeru Ogawa on 13/08/12.
 */
public class Model {

	private final HttpClient httpClient = new DefaultHttpClient();
	private int TIMEOUT = 5 * 60 * 1000;

	private static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";
	public HashMap<HttpUriRequest, JSONObject> results = new HashMap<HttpUriRequest, JSONObject>();

	public Model() {

		super();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), TIMEOUT);

	}

	public JSONObject post(final String api, Map<String, Object> params) {
		return post("v1", api, params);
	}

	public JSONObject post(final String version, final String api, Map<String, Object> params) {

		JSONObject body = new JSONObject();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			try {
				body.put(entry.getKey(), entry.getValue());
			} catch (JSONException e) {
			}
		}

		HttpPost post = new HttpPost(GrowthReplay.BASE_URL + version + "/" + api);
		post.addHeader("Accept", "application/json");
		post.addHeader(HTTP.CONTENT_TYPE, String.format("application/json; charset=%s;", HTTP.UTF_8));
		try {
			post.setEntity(new StringEntity(body.toString(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}

		return request(post);

	}

	private JSONObject request(final HttpUriRequest httpRequest) {

		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpRequest, certificate());
		} catch (IOException e) {
			throw new GrowthReplayException("Feiled to execute HTTP request. " + e.getMessage(), e);
		}

		JSONObject jsonObject = null;
		try {
			String json = IOUtils.toString(httpResponse.getEntity().getContent());
			jsonObject = new JSONObject(json);
		} catch (IOException e) {
			throw new GrowthReplayException("Failed to read HTTP response. " + e.getMessage(), e);
		} catch (JSONException e) {
			throw new GrowthReplayException("Failed to parse response JSON. " + e.getMessage(), e);
		} finally {
			try {
				httpResponse.getEntity().consumeContent();
			} catch (IOException e) {
				throw new GrowthReplayException("Failed to close connection. " + e.getMessage(), e);
			}
		}

		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode < 200 || statusCode >= 300) {
			Error error = new Error(jsonObject);
			throw new GrowthReplayException(error.getMessage());
		}

		return jsonObject;

	}

	public JSONObject multipartPost(String api, final List<NameValuePair> params, byte[] file) {
		return multipartPost("v1", api, params, file);
	}

	public JSONObject multipartPost(String version, String api, final List<NameValuePair> params, byte[] file) {
		URLConnection connection = null;
		try {

			connection = new URL(GrowthReplay.BASE_URL +  version + "/" + api).openConnection();
			connection.setConnectTimeout(60 * 1000);
			connection.setReadTimeout(60 * 1000);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			((HttpsURLConnection) connection).setRequestMethod("POST");
			((HttpsURLConnection) connection).setSSLSocketFactory(getSSLSocketFactory());
			((HttpsURLConnection) connection).setHostnameVerifier(getHostNameVerifier());

			connection.setDoOutput(true);

			connection.connect();
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(createBoundaryMessage(params).getBytes());
			outputStream.write(file);
			String endBoundary = "\r\n--" + BOUNDARY + "--\r\n";
			outputStream.write(endBoundary.getBytes());
			outputStream.close();

			InputStream inputStream = connection.getInputStream();
			String json = IOUtils.toString(inputStream);
			if (json == null)
				return null;

			return new JSONObject(json);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				((HttpsURLConnection) connection).disconnect();
		}

		return null;
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

	private javax.net.ssl.SSLSocketFactory getSSLSocketFactory() {

		try {
			javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
			javax.net.ssl.X509TrustManager bogusTm = new javax.net.ssl.X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)

				throws java.security.cert.CertificateException {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)

				throws java.security.cert.CertificateException {
				}

			};

			ctx.init(null, new javax.net.ssl.TrustManager[] { bogusTm }, null);

			return ctx.getSocketFactory();
		}

		catch (Exception e) {
			return null;
		}

	}

	private HostnameVerifier getHostNameVerifier() {
		javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {
			public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
				return true;
			}
		};
		return hv;
	}

	private HttpContext certificate() {

		SSLSocketFactory sf = null;
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			sf = new CustomSSLSocketFactory(trustStore);
		} catch (Exception e) {
		}

		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme https = new Scheme("https", sf, 443);
		httpClient.getConnectionManager().getSchemeRegistry().register(https);

		HttpContext httpcontext = new BasicHttpContext();
		httpcontext.setAttribute(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpcontext.setAttribute(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		httpcontext.setAttribute(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);

		return httpcontext;
	}

	private static class CustomSSLSocketFactory extends SSLSocketFactory {

		SSLContext sslContext = SSLContext.getInstance("TLS");

		public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
				UnrecoverableKeyException {
			super(truststore);
			TrustManager tm = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

	}

}
