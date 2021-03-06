package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.Messages;
import com.constantcontact.plugins.GateKeepah.TestDataHelper;

public class SonarTest {

	private TestDataHelper testHelper;

	@Before
	public void testSetup() throws Exception {
		testHelper = new TestDataHelper();

	}

	@Test
	public void sonarClientSetup() {
		try {
			new Sonar(testHelper.getHost(), testHelper.getUserName(), testHelper.getPassword());
		} catch (Exception e) {
			Assert.fail("No Exception should have occurred");
		}
	}

	@Test
	public void sonarClientNullHost() {
		try {
			new Sonar(null, testHelper.getUserName(), testHelper.getPassword());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_host_required()));
		}
	}

	@Test
	public void sonarClientNullUser() {
		try {
			new Sonar(testHelper.getHost(), null, testHelper.getPassword());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_username_required()));
		}
	}

	@Test
	public void sonarClientNullPass() {
		try {
			new Sonar(testHelper.getHost(), testHelper.getUserName(), null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_password_required()));
		}
	}

	@Test
	public void sonarClientEmptyHost() {
		try {
			new Sonar("", testHelper.getUserName(), null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_host_required()));
		}
	}

	@Test
	public void sonarClientEmptyUser() {
		try {
			new Sonar(testHelper.getHost(), "", null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_username_required()));
		}
	}

	@Test
	public void sonarClientEmptyPass() {
		try {
			new Sonar(testHelper.getHost(), testHelper.getUserName(), "");
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_password_required()));
		}
	}

	@Test
	public void sonarClientNoPort() {
		try {
			new Sonar("http://localhost", testHelper.getUserName(), testHelper.getPassword());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_bad_uri2()));
		}
	}

	@Test
	public void sonarClientNoHttp() {
		try {
			new Sonar("localhost:9000", testHelper.getUserName(), testHelper.getPassword());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(Messages.sonarclient_bad_uri2()));
		}
	}

}
