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

public class GateKeepahBuilderTest {

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
		jenkinsRule.submit(configForm);
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		runTest(new GateKeepahBuilder("",
				"sonar.qualityGateName=autobots\nsonar.projectKey=" + testHelper.getProjectId()
						+ "\nsonar.codeCoverageBreakLevel=" + testHelper.getCodeCoverageBreakLevel()
						+ "\nsonar.codeCoverageGoal=" + testHelper.getCodeCoverageGoal() + "\nsonar.projectName="
						+ testHelper.getProjectName()),
				"+ echo hello");
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.qualityGateName=autobots\nsonar.projectKey=" + testHelper.getProjectId()
						+ "\nsonar.codeCoverageBreakLevel=" + testHelper.getCodeCoverageBreakLevel()
						+ "\nsonar.codeCoverageGoal=" + testHelper.getCodeCoverageGoal() + "\nsonar.projectName="
						+ testHelper.getProjectName()),
				"+ echo hello");
	}

	@Test
	public void testGateKeepahEmptyGateName() throws Exception {
		runTest(buildGateKeepahProperties("", testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"),
				constructEmptyPropValue("sonar.qualityGateName="));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "", testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"), constructEmptyPropValue("sonar.projectKey="));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), "",
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"),
				constructEmptyPropValue("sonar.codeCoverageBreakLevel="));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), testHelper.getCodeCoverageGoal(), "",
				"gatekeepah"), constructEmptyPropValue("sonar.codeCoverageGoal="));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), ""), constructEmptyPropValue("sonar.projectName="));
	}

	@Test
	public void testGateKeepahNullGateName() throws Exception {
		runTest(buildGateKeepahProperties(null, testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"), "sonar.qualityGateName is empty or null");
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", null, testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"), "sonar.projectKey is empty or null");
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), null,
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"), "sonar.codeCoverageBreakLevel is empty or null");
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), testHelper.getCodeCoverageGoal(), null,
				"gatekeepah"), "sonar.codeCoverageGoal is empty or null");
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), null), "sonar.projectName is empty or null");
	}

	@Test
	public void testAbilityToAddGateKeepahBadFilePath() throws Exception {
		runTest(new GateKeepahBuilder("file.properties", null),
				"Aborting the build, no properties were set to utilize quality gates");
	}

	@Test
	public void testAbilityToAddGateKeepahNoData() throws Exception {
		runTest(new GateKeepahBuilder(null, null),
				"Aborting the build, no properties were set to utilize quality gates");
	}

	@Test
	public void testAbilityToAddGateKeepahEmptyPropertiesFile() throws Exception {
		runTest(new GateKeepahBuilder("", null), "Aborting the build, no properties were set to utilize quality gates");
	}

	@Test
	public void testAbilityToAddGateKeepahEmptyProps() throws Exception {
		runTest(new GateKeepahBuilder(null, ""), "Aborting the build, no properties were set to utilize quality gates");
	}

	@Test
	public void testAbilityToAddGateKeepahAllEmpty() throws Exception {
		runTest(new GateKeepahBuilder("", ""), "Aborting the build, no properties were set to utilize quality gates");
	}

	private void runTest(final GateKeepahBuilder gateKeepahBuilder, final String assertion) throws Exception {

		FreeStyleProject project = jenkinsRule.createFreeStyleProject();

		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains(assertion));
	}

	private GateKeepahBuilder buildGateKeepahProperties(final String qualityGateName, final String projectKey,
			final String breakLevel, final String goal, final String projectName) {
		StringBuilder ps = new StringBuilder();
		if (null != qualityGateName) {
			ps.append("sonar.qualityGateName=").append(qualityGateName).append("\n");
		}
		if (null != projectKey) {
			ps.append("sonar.projectKey=").append(projectKey).append("\n");
		}
		if (null != breakLevel) {
			ps.append("sonar.codeCoverageBreakLevel=").append(breakLevel).append("\n");
		}
		if (null != goal) {
			ps.append("sonar.codeCoverageGoal=").append(goal).append("\n");
		}
		if (null != projectName) {
			ps.append("sonar.projectName=").append(projectName).append("\n");
		}
		System.out.println("GATE KEEPER BUILDER : " + ps.toString());
		return new GateKeepahBuilder(null, ps.toString());
	}

	private String constructEmptyPropValue(final String key) {
		return "Property could not be set for " + key + " because it was missing its value";
	}

}
