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
	public Properties props;

	@Before
	public void testSetup() throws Exception {

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
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
				"sonar.qualityGateName=autobots\nsonar.projectKey=" + props.get("sonar.test.project.id").toString()
						+ "\nsonar.codeCoverageBreakLevel=" + props.get("sonar.test.codecoverage.goal").toString()
						+ "\nsonar.codeCoverageGoal=" + props.get("sonar.test.codecoverage.breaklevel")
						+ "\nsonar.projectName=" + props.get("sonar.test.project.name")),
				"+ echo hello");
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.qualityGateName=autobots\nsonar.projectKey=" + props.get("sonar.test.project.id").toString()
						+ "\nsonar.codeCoverageBreakLevel=" + props.get("sonar.test.codecoverage.goal").toString()
						+ "\nsonar.codeCoverageGoal=" + props.get("sonar.test.codecoverage.breaklevel")
						+ "\nsonar.projectName=" + props.get("sonar.test.project.name")),
				"+ echo hello");
	}

	@Test
	public void testGateKeepahEmptyGateName() throws Exception {
		runTest(buildGateKeepahProperties("", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				constructEmptyPropValue("sonar.qualityGateName="));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "", props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				constructEmptyPropValue("sonar.projectKey="));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(), "",
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				constructEmptyPropValue("sonar.codeCoverageBreakLevel="));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(), "", "gatekeepah"),
				constructEmptyPropValue("sonar.codeCoverageGoal="));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), ""),
				constructEmptyPropValue("sonar.projectName="));
	}

	@Test
	public void testGateKeepahNullGateName() throws Exception {
		runTest(buildGateKeepahProperties(null, props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				"sonar.qualityGateName is empty or null");
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", null, props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				"sonar.projectKey is empty or null");
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(), null,
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"),
				"sonar.codeCoverageBreakLevel is empty or null");
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(), null, "gatekeepah"),
				"sonar.codeCoverageGoal is empty or null");
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), null),
				"sonar.projectName is empty or null");
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
