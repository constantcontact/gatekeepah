package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpHelper {

	private String host;
	private String username;
	private String password;

	public HttpHelper(final String host, final String username, final String password) throws Exception {
		if (null == host || host.isEmpty()) {
			throw new Exception("Host must be setup and can not be empty");
		}

		if (null == username || username.isEmpty()) {
			throw new Exception("Username must be setup and can not be empty");
		}

		if (null == password || password.isEmpty()) {
			throw new Exception("Password must setup and can not be empty");
		}

		try {
			URI uri = new URI(host);
			if (uri.getHost() == null || uri.getPort() == -1) {
				throw new URISyntaxException(uri.toString(), "URI must have host and port parts");
			}

		} catch (URISyntaxException e) {
			throw new Exception("Host must be an address with a port seperated by a ':' e.g. http://localhost:9000");
		}

		this.host = host;
		this.username = username;
		this.password = password;
	}

	public String doGet(final String url) throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCredentialsProvider(buildCredentialsProvider(this.host, this.username, this.password))
				.build();
		try {
			HttpGet httpGet = new HttpGet(url);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						HttpEntity entity = response.getEntity();
						String responseBody = entity != null ? EntityUtils.toString(entity) : null;

						throw new ClientProtocolException(
								"Unexpected response status: " + status + "\n" + responseBody);
					}
				}

			};
			return httpClient.execute(httpGet, responseHandler);
		} finally {
			httpClient.close();
		}

	}

	public String doPost(final String url, final HttpEntity payload)
			throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCredentialsProvider(buildCredentialsProvider(this.host, this.username, this.password))
				.build();
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(payload);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						HttpEntity entity = response.getEntity();
						String responseBody = entity != null ? EntityUtils.toString(entity) : null;

						throw new ClientProtocolException(
								"Unexpected response status: " + status + "\n" + responseBody);
					}
				}

			};
			return httpClient.execute(httpPost, responseHandler);
		} finally {
			httpClient.close();
		}

	}

	private static CredentialsProvider buildCredentialsProvider(final String host, final String username,
			final String password) throws URISyntaxException {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		URI uri = new URI(host);
		credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
				new UsernamePasswordCredentials(username, password));
		return credsProvider;

	}

}
