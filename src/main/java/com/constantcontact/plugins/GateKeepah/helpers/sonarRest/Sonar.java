package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.net.URI;
import java.net.URISyntaxException;

import com.constantcontact.plugins.Messages;

public class Sonar {
	private String host;
	private String username;
	private String password;

	public Sonar(final String host, final String username, final String password) throws Exception {
		if (null == host || host.isEmpty()) {
			throw new Exception(Messages.sonarclient_host_required());
		}

		if (null == username || username.isEmpty()) {
			throw new Exception(Messages.sonarclient_username_required());
		}

		if (null == password || password.isEmpty()) {
			throw new Exception(Messages.sonarclient_password_required());
		}

		try {
			URI uri = new URI(host);
			if (uri.getHost() == null || uri.getPort() == -1) {
				throw new URISyntaxException(uri.toString(), Messages.sonarclient_bad_uri());
			}

		} catch (URISyntaxException e) {
			throw new Exception(Messages.sonarclient_bad_uri2());
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

