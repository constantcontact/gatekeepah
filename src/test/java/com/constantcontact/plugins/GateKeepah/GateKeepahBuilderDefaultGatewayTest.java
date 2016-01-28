package com.constantcontact.plugins.GateKeepah;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;

public class GateKeepahBuilderDefaultGatewayTest {

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	public List<FreeStyleProject> projectsToDestroy = new ArrayList<FreeStyleProject>();
	public TestDataHelper testHelper;

	@Before
	public void testSetup() throws Exception {

		testHelper = new TestDataHelper();

		HtmlPage configPage = jenkinsRule.createWebClient().goTo("configure");
		HtmlForm configForm = configPage.getFormByName("config");
		HtmlTextInput sonarHostInput = configPage.getElementByName("_.sonarHost");
		sonarHostInput.setValueAttribute(testHelper.getHost());
		HtmlTextInput sonarUserName = configPage.getElementByName("_.sonarUserName");
		sonarUserName.setValueAttribute(testHelper.getUserName());
		HtmlPasswordInput sonarPassword = configPage.getElementByName("_.sonarPassword");
		sonarPassword.setValueAttribute(testHelper.getPassword());
		HtmlTextInput defaultQualityGateName = configPage.getElementByName("_.defaultQualityGateName");
		defaultQualityGateName.setValueAttribute(testHelper.getGateName());
		jenkinsRule.submit(configForm);
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "sonar.projectKey="
				+ testHelper.getProjectResource() + "\nsonar.projectName=" + testHelper.getProjectName());
		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains("echo hello"));
	}

	@Test
	public void testAbilityToAddGateKeepahNoResourceKey() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder("", "test=test");
		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true,
				s.contains("Aborting the build, sonar.projectKey must be set to associate default quality gate"));
	}

	@Test
	public void testAbilityToAddGateKeepahGenericKey() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder("", "sonar.projectKey=com");
		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains("Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName"));
	}

	@Test
	public void testAbilityToAddGateKeepahBadResourceKey() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder("", "sonar.projectKey=205891111");
		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains("Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName"));
	}

	@Test
	public void testAbilityToAddGateKeepahBadResourceKey2() throws Exception {
		FreeStyleProject project = jenkinsRule.createFreeStyleProject();
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder("", "sonar.projectKey=blue");
		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains("Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName"));
	}

}
