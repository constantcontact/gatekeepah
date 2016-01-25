package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projectClient;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;

public class ProjectClientTest {

	private Properties props;

	@Before
	public void testSetup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
	}

	@Test
	public void retrieveList() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveIndexOfProjects();
	}    

	@Test
	public void retrieveListWithNullKey() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveIndexOfProjects(null);
	}

	@Test
	public void retrieveListWithKeyId() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveIndexOfProjects(props.getProperty("sonar.test.project.id"));
	}

	@Test
	public void retrieveListWithKeyResource() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveIndexOfProjects(props.getProperty("sonar.test.project.resource"));
	}
}
