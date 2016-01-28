package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projectClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;

public class ProjectClientTest {

	private Properties props;
	private List<Project> projectsToDelete = new ArrayList<Project>();

	@Before
	public void testSetup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
	}

	@After
	public void testCleanup() throws Exception {
		if (null != projectsToDelete && projectsToDelete.size() > 1) {
			for (Project project : projectsToDelete) {
				ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
						props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
				client.deleteProject(project.getK());
			}
		}
	}

	@Test
	public void retrieveList() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.retrieveIndexOfProjects();
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithNullKey() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());

		try {
			client.retrieveIndexOfProjects(null);
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithKeyId() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.retrieveIndexOfProjects(props.getProperty("sonar.test.project.id"));
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithKeyResource() throws Exception {
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.retrieveIndexOfProjects(props.getProperty("sonar.test.project.resource"));
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void createProject() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting03");
		project.setK("com.constantcontact:GateKeepahTesting03");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());

		Project createdProject = client.createProject(project);
		projectsToDelete.add(createdProject);
	}

	@Test
	public void createProjectEmptyKey() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting01");
		project.setK("");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createProject(project);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Key is a required field and must be set", e.getMessage());
		}
	}

	@Test
	public void createProjectNullKey() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting01");
		project.setK(null);
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createProject(project);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Key is a required field and must be set", e.getMessage());
		}
	}

	@Test
	public void createProjectEmptyName() throws Exception {
		Project project = new Project();
		project.setNm("");
		project.setK("com.constantcontact:GateKeepahTesting01");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createProject(project);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Name is a required field and must be set", e.getMessage());
		}
	}

	@Test
	public void createProjectNullName() throws Exception {
		Project project = new Project();
		project.setNm(null);
		project.setK("com.constantcontact:GateKeepahTesting01");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createProject(project);
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Name is a required field and must be set", e.getMessage());
		}
	}

	@Test
	public void deleteProject() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting04515");
		project.setK("com.constantcontact:GateKeepahTesting04515");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());

		Project createdProject = client.createProject(project);

		try {
			client.deleteProject(createdProject.getK());
		} catch (Exception e) {
			Assert.fail("An Exception should not occur");
		}
	}

	@Test
	public void deleteProjectKeyEmpty() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTestingToDelete01");
		project.setK("");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());

		try {
			client.deleteProject(project.getK());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Key is a required field and must be set", e.getMessage());
		}
	}

	@Test
	public void deleteProjectKeyNull() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTestingToDelete01");
		project.setK(null);
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());

		try {
			client.deleteProject(project.getK());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Key is a required field and must be set", e.getMessage());
		}
	}
}
