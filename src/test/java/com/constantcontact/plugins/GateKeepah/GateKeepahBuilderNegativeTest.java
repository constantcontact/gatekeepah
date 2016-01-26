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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;

public class GateKeepahBuilderNegativeTest {

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	public List<FreeStyleProject> projectsToDestroy = new ArrayList<FreeStyleProject>();
	public String assertion1 = "Sonar host was not set in global configuration";
	public String assertion2 = "Sonar Username was not set in global configuration";
	public String assertion3 = "Sonar Password was not set in global configuration";

	@Before
	public void testSetup() throws Exception {

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		Properties props = new Properties();
		props.load(is);
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		runTest(new GateKeepahBuilder("",
				"sonar.team.name=autobots\nsonar.resource.key=20589\nsonar.codecoverage.breaklevel=50\nsonar.codecoverage.goal=80\nsonar.app.name=gatekeepah"));
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.team.name=autobots\nsonar.resource.key=20589\nsonar.codecoverage.breaklevel=50\nsonar.codecoverage.goal=80\nsonar.app.name=gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyTeam() throws Exception {
		runTest(buildGateKeepahProperties("", "20589", "50", "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "", "50", "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "", "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "", "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "80", ""));
	}

	@Test
	public void testGateKeepahNullTeam() throws Exception {
		runTest(buildGateKeepahProperties(null, "20589", "50", "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties("autobots", null, "50", "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", null, "80", "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", null, "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties("autobots", "20589", "50", "80", null));
	}

	@Test
	public void testAbilityToAddGateKeepahBadFilePath() throws Exception {
		runTest(new GateKeepahBuilder("file.properties", null));
	}

	@Test
	public void testAbilityToAddGateKeepahNoData() throws Exception {
		runTest(new GateKeepahBuilder(null, null));
	}

	@Test
	public void testAbilityToAddGateKeepahEmptyPropertiesFile() throws Exception {
		runTest(new GateKeepahBuilder("", null));
	}

	@Test
	public void testAbilityToAddGateKeepahEmptyProps() throws Exception {
		runTest(new GateKeepahBuilder(null, ""));
	}

	@Test
	public void testAbilityToAddGateKeepahAllEmpty() throws Exception {
		runTest(new GateKeepahBuilder("", ""));
	}

	private void runTest(final GateKeepahBuilder gateKeepahBuilder) throws Exception {

		FreeStyleProject project = jenkinsRule.createFreeStyleProject();

		project.getBuildersList().add(gateKeepahBuilder);
		project.getBuildersList().add(new Shell("echo hello"));
		project.save();

		FreeStyleBuild build = project.scheduleBuild2(0).get();
		System.out.println(build.getDisplayName() + " completed");
		String s = FileUtils.readFileToString(build.getLogFile());
		System.out.println(s);
		Assert.assertEquals(true, s.contains(assertion1));
		Assert.assertEquals(true, s.contains(assertion2));
		Assert.assertEquals(true, s.contains(assertion3));
		
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

}
