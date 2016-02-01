package com.constantcontact.plugins.GateKeepah;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class GateKeepahBuilderPropertiesTest {

	@Test
	public void testReadPropertiesHappy() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(url.getPath(), "");
		final String filePath = url.getPath().replace(File.separator + "testconfig.properties", "");
		Properties props = gateKeepahBuilder.readPropertiesFile(filePath, "testconfig.properties", "");

		Assert.assertEquals("test", props.getProperty("test"));

	}

	@Test
	public void testReadPropertiesHappyAdded() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(url.getPath(), "test3=test3");
		final String filePath = url.getPath().replace(File.separator + "testconfig.properties", "");
		Properties props = gateKeepahBuilder.readPropertiesFile(filePath, "testconfig.properties", "test3=test3");

		Assert.assertEquals("test3", props.getProperty("test3"));
	}

	@Test
	public void testReadPropertiesHappyOnlyAdditional() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		final String filePath = url.getPath().replace(File.separator + "testconfig.properties", "");
		Properties props = gateKeepahBuilder.readPropertiesFile(filePath, null, "test3=test3");

		Assert.assertEquals("test3", props.getProperty("test3"));
	}

	@Test
	public void testReadPropertiesFileMissingValue() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfigMissingValue.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(url.getPath(), "");
		final String filePath = url.getPath().replace(File.separator + "testconfigMissingValue.properties", "");
		Properties props = gateKeepahBuilder.readPropertiesFile(filePath, "testconfigMissingValue.properties", "");
		Assert.assertEquals(null, props.get("test="));

	}

	@Test
	public void testReadPropertiesFileEmpty() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfigEmpty.properties");
		
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(url.getPath(), "");
		final String filePath = url.getPath().replace(File.separator + "testconfigEmpty.properties", "");
		Properties props = gateKeepahBuilder.readPropertiesFile(filePath, "testconfigEmpty.properties", "");
		Assert.assertEquals(0, props.size());

	}

	@Test
	public void testReadPropertiesAdditionalProperties() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(url.getPath(), "");
		final String filePath = url.getPath().replace(File.separator + "testconfig.properties", "");
		try {
			gateKeepahBuilder.readPropertiesFile(filePath, "testconfig.properties", "test4=\n");
			Assert.fail("Should have thrown an exception");
		} catch (InterruptedException ie) {
			Assert.assertEquals(true, ie.getLocalizedMessage().contains("Property could not be set for test4= because it was missing its value"));
		}
	}

	@Test
	public void testReadPropertiesHappyOnlyAdditionalMissingValue() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		final String filePath = url.getPath().replace(File.separator + "testconfig.properties", "");
		try {
			gateKeepahBuilder.readPropertiesFile(filePath, null, "test4=\n");
			Assert.fail("Should have thrown en exception");
		} catch (InterruptedException ie) {
			Assert.assertEquals(true, ie.getLocalizedMessage().contains("Property could not be set for test4= because it was missing its value"));
		}

	}

}
