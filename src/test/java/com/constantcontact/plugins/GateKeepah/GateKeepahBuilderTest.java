package com.constantcontact.plugins.GateKeepah;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	@Before
	public void testSetup() throws Exception {

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		Properties props = new Properties();
		props.load(is);

		HtmlPage configPage = jenkinsRule.createWebClient().goTo("configure");
		HtmlForm configForm = configPage.getFormByName("config");
		HtmlTextInput sonarHostInput = configPage.getElementByName("_.sonarHost");
		sonarHostInput.setValueAttribute(props.get("sonar.test.host").toString());
		HtmlTextInput sonarUserName = configPage.getElementByName("_.sonarUserName");
		sonarUserName.setValueAttribute(props.get("sonar.test.username").toString());
		HtmlPasswordInput sonarPassword = configPage.getElementByName("_.sonarPassword");
		sonarPassword.setValueAttribute(props.get("sonar.test.password").toString());
		jenkinsRule.submit(configForm);
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		runTest(new GateKeepahBuilder("",
				"sonar.team.name=autobots\nsonar.resource.key=20589\nsonar.codecoverage.breaklevel=50\nsonar.codecoverage.goal=80\nsonar.app.name=gatekeepah"),
				"+ echo hello");
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.team.name=autobots\nsonar.resource.key=20589\nsonar.codecoverage.breaklevel=50\nsonar.codecoverage.goal=80\nsonar.app.name=gatekeepah"),
				"+ echo hello");
	}

	@Test
	public void testGateKeepahEmptyTeam() throws Exception {
		runTest(buildGateKeepahProperties("", "20589", "50", "80", "gatekeepah"),
				constructEmptyPropValue("sonar.team.name="));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "", "50", "80", "gatekeepah"),
				constructEmptyPropValue("sonar.resource.key="));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "", "80", "gatekeepah"),
				constructEmptyPropValue("sonar.codecoverage.breaklevel="));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "", "gatekeepah"),
				constructEmptyPropValue("sonar.codecoverage.goal="));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "80", ""),
				constructEmptyPropValue("sonar.app.name="));
	}

	@Test
	public void testGateKeepahNullTeam() throws Exception {
		runTest(buildGateKeepahProperties(null, "20589", "50", "80", "gatekeepah"), "sonar.team.name is empty or null");
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", null, "50", "80", "gatekeepah"),
				"sonar.resource.key is empty or null");
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", null, "80", "gatekeepah"),
				"sonar.codecoverage.breaklevel is empty or null");
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", null, "gatekeepah"),
				"sonar.codecoverage.goal is empty or null");
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "80", null), "sonar.app.name is empty or null");
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
		runTest(new GateKeepahBuilder(null, ""),
				"A properties file must be in the right place or properties added to the text area");
	}

	@Test
	public void testAbilityToAddGateKeepahAllEmpty() throws Exception {
		runTest(new GateKeepahBuilder("", ""),
				"A properties file must be in the right place or properties added to the text area");
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

	private GateKeepahBuilder buildGateKeepahProperties(final String teamName, final String resourceKey,
			final String breakLevel, final String goal, final String appName) {
		StringBuilder ps = new StringBuilder();
		if (null != teamName) {
			ps.append("sonar.team.name=").append(teamName).append("\n");
		}
		if (null != resourceKey) {
			ps.append("sonar.resource.key=").append(resourceKey).append("\n");
		}
		if (null != breakLevel) {
			ps.append("sonar.codecoverage.breaklevel=").append(breakLevel).append("\n");
		}
		if (null != goal) {
			ps.append("sonar.codecoverage.goal=").append(goal).append("\n");
		}
		if (null != appName) {
			ps.append("sonar.app.name=").append(appName).append("\n");
		}
		System.out.println("GATE KEEPER BUILDER : " + ps.toString());
		return new GateKeepahBuilder(null, ps.toString());
	}

	private String constructEmptyPropValue(final String key) {
		return "Property could not be set for " + key + " because it was missing its value";
	}

}
