package com.constantcontact.plugins.GateKeepah;

import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.constantcontact.plugins.Messages;

public class GateKeepahBuilderPropertiesTest {

	@Test
	public void testReadPropertiesHappy() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahPropertiesHandler handler = new GateKeepahPropertiesHandler(url.getPath(), null);
		Map<String, String> propertyMap = handler.invoke(null, null);
		Properties props = new Properties();
		props.putAll(propertyMap);

		Assert.assertEquals("test", props.getProperty("test"));

	}

	@Test
	public void testReadPropertiesHappyAdded() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		GateKeepahPropertiesHandler handler = new GateKeepahPropertiesHandler(url.getPath(), "test3=test3");
		Map<String, String> propertyMap = handler.invoke(null, null);
		Properties props = new Properties();
		props.putAll(propertyMap);

		Assert.assertEquals("test3", props.getProperty("test3"));
	}

	@Test
	public void testReadPropertiesFileMissingValue() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfigMissingValue.properties");

		GateKeepahPropertiesHandler handler = new GateKeepahPropertiesHandler(url.getPath(), "");
		Map<String, String> propertyMap = handler.invoke(null, null);
		Properties props = new Properties();
		props.putAll(propertyMap);

		Assert.assertEquals(null, props.get("test="));

	}

	@Test
	public void testReadPropertiesFileEmpty() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfigEmpty.properties");

		GateKeepahPropertiesHandler handler = new GateKeepahPropertiesHandler(url.getPath(), "");
		Map<String, String> propertyMap = handler.invoke(null, null);
		Properties props = new Properties();
		props.putAll(propertyMap);

		Assert.assertEquals(0, props.size());

	}

	@Test
	public void testReadPropertiesAdditionalProperties() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL url = classloader.getResource("testconfig.properties");

		try {
			GateKeepahPropertiesHandler handler = new GateKeepahPropertiesHandler(url.getPath(), "test4=\n");
			Map<String, String> propertyMap = handler.invoke(null, null);
			Properties props = new Properties();
			props.putAll(propertyMap);
			Assert.fail("Should have thrown an exception");
		} catch (InterruptedException ie) {
			Assert.assertEquals(true, ie.getLocalizedMessage().contains(
					Messages.handler_property_not_set() + "test4=" + Messages.handler_property_missing_value()));
		}
	}

}
