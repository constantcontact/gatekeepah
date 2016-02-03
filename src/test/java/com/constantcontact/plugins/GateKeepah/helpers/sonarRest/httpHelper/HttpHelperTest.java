package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.httpHelper;

import org.junit.Assert;
import org.junit.Test;

import com.constantcontact.plugins.Messages;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.HttpHelper;

public class HttpHelperTest {

	@Test
	public void httpHelperConfiguration() throws Exception {
		new HttpHelper("http://localhost:80", "rdavis", "password");
	}

	@Test
	public void httpHelperConfigurationNullHost() throws Exception {
		try {
			new HttpHelper(null, "rdavis", "password");
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_host()));
		}
	}

	@Test
	public void httpHelperConfigurationEmptyHost() throws Exception {
		try {
			new HttpHelper("", "rdavis", "password");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_host()));
		}
	}

	@Test
	public void httpHelperConfigurationHostNoPort() throws Exception {
		try {
			new HttpHelper("http://localhost", "rdavis", "password");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_bad_uri2()));
		}
	}

	@Test
	public void httpHelperConfigurationHostNoHTTP() throws Exception {
		try {
			new HttpHelper("localhost:80", "rdavis", "password");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_bad_uri2()));
		}
	}

	@Test
	public void httpHelperConfigurationNullUsername() throws Exception {
		try {
			new HttpHelper("http://localhost:8080", null, "password");
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_username()));
		}
	}

	@Test
	public void httpHelperConfigurationNullPassword() throws Exception {
		try {
			new HttpHelper("http://localhost:8080", "rdavis", null);
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_password()));
		}
	}

	@Test
	public void httpHelperConfigurationEmptyUsername() throws Exception {
		try {
			new HttpHelper("http://localhost:8080", "", "password");
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_username()));
		}
	}

	@Test
	public void httpHelperConfigurationEmptyPassword() throws Exception {
		try {
			new HttpHelper("http://localhost:8080", "rdavis", "");
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.httphelper_null_password()));
		}
	}

}
