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

	public Properties props;

	@Before
	public void testSetup() throws Exception {

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		runTest(new GateKeepahBuilder("",
				"sonar.qualityGateName=" + props.get("sonar.test.gate.name").toString() + "\nsonar.projectKey="
						+ props.get("sonar.test.project.id").toString() + "\nsonar.codeCoverageBreakLevel="
						+ props.get("sonar.test.codecoverage.breaklevel").toString() + "\nsonar.codeCoverageGoal="
						+ props.get("sonar.test.codecoverage.goal").toString() + "\nsonar.projectName="
						+ props.get("sonar.test.project.name")));
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.qualityGateName=" + props.get("sonar.test.gate.name").toString() + "\nsonar.projectKey="
						+ props.get("sonar.test.gate.name").toString() + "\nsonar.codeCoverageBreakLevel="
						+ props.get("sonar.test.codecoverage.breaklevel").toString() + "\nsonar.codeCoverageGoal="
						+ props.get("sonar.test.codecoverage.goal").toString() + "\nsonar.projectName="
						+ props.get("sonar.test.project.name")));
	}

	@Test
	public void testGateKeepahEmptyTeam() throws Exception {
		runTest(buildGateKeepahProperties("", props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(), "",
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), "",
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), props.get("sonar.test.codecoverage.goal").toString(), "",
				"gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), ""));
	}

	@Test
	public void testGateKeepahNullTeam() throws Exception {
		runTest(buildGateKeepahProperties(null, props.get("sonar.test.project.id").toString(),
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(), null,
				props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), null,
				props.get("sonar.test.codecoverage.breaklevel").toString(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), props.get("sonar.test.codecoverage.goal").toString(),
				null, "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties(props.get("sonar.test.gate.name").toString(),
				props.get("sonar.test.project.id").toString(), props.get("sonar.test.codecoverage.goal").toString(),
				props.get("sonar.test.codecoverage.breaklevel").toString(), null));
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

}
