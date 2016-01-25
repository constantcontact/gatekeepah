package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;


import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SonarTest {

	private Properties props;

	@Before
	public void testSetup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
	}

	@Test
	public void sonarClientSetup() {
		try {
			new Sonar(props.get("sonar.test.host").toString(), props.get("sonar.test.username").toString(),
					props.get("sonar.test.password").toString());
		} catch (Exception e) {
			Assert.fail("No Exception should have occurred");
		}
	}

	@Test
	public void sonarClientNullHost() {
		try {
			new Sonar(null, props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Host must be setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientNullUser() {
		try {
			new Sonar(props.get("sonar.test.host").toString(), null, props.get("sonar.test.password").toString());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Username must be setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientNullPass() {
		try {
			new Sonar(props.get("sonar.test.host").toString(), props.get("sonar.test.username").toString(), null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Password must setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientEmptyHost() {
		try {
			new Sonar("", props.get("sonar.test.username").toString(), null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Host must be setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientEmptyUser() {
		try {
			new Sonar(props.get("sonar.test.host").toString(), "", null);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Username must be setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientEmptyPass() {
		try {
			new Sonar(props.get("sonar.test.host").toString(), props.get("sonar.test.username").toString(), "");
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Password must setup and can not be empty"));
		}
	}

	@Test
	public void sonarClientNoPort() {
		try {
			new Sonar("http://localhost", props.get("sonar.test.username").toString(),
					props.get("sonar.test.password").toString());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage()
					.contains("Host must be an address with a port seperated by a ':' e.g. http://localhost:9000"));
		}
	}

	@Test
	public void sonarClientNoHttp() {
		try {
			new Sonar("localhost:9000", props.get("sonar.test.username").toString(),
					props.get("sonar.test.password").toString());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage()
					.contains("Host must be an address with a port seperated by a ':' e.g. http://localhost:9000"));
		}
	}

}
