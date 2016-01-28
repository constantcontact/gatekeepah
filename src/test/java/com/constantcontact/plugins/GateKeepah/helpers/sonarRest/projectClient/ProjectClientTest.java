package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projectClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.TestDataHelper;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;

public class ProjectClientTest {

	private TestDataHelper testHelper;

	@Before
	public void testSetup() throws Exception {
		testHelper = new TestDataHelper();
	}

	@Test
	public void retrieveList() throws Exception {
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.retrieveIndexOfProjects();
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithNullKey() throws Exception {
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

		try {
			client.retrieveIndexOfProjects(null);
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithKeyId() throws Exception {
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.retrieveIndexOfProjects(testHelper.getProjectId());
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void retrieveListWithKeyResource() throws Exception {
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.retrieveIndexOfProjects(testHelper.getProjectResource());
		} catch (Exception e) {
			Assert.fail("An Exception should not have occurred");
		}
	}

	@Test
	public void createProject() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting" + System.currentTimeMillis());
		project.setK("com.constantcontact:GateKeepahTesting" + System.currentTimeMillis());
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

		Project createdProject = client.createProject(project);
		try {
			client.deleteProject(createdProject.getK());
		} catch (Exception e) {
			Assert.fail("An Exception should not occur");
		}
	}

	@Test
	public void createProjectEmptyKey() throws Exception {
		Project project = new Project();
		project.setNm("GateKeepahTesting01");
		project.setK("");
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
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
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
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
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
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
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
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
		project.setNm("GateKeepahTesting" + System.currentTimeMillis());
		project.setK("com.constantcontact:GateKeepahTesting" + System.currentTimeMillis());
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

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
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

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
		ProjectClient client = new ProjectClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

		try {
			client.deleteProject(project.getK());
			Assert.fail("An Exception should have occurred");
		} catch (Exception e) {
			Assert.assertEquals("Key is a required field and must be set", e.getMessage());
		}
	}
}
