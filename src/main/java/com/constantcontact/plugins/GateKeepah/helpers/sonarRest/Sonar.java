package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.net.URI;
import java.net.URISyntaxException;

public class Sonar {
	private String host;
	private String username;
	private String password;

	public Sonar(final String host, final String username, final String password) throws Exception {
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

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

}

