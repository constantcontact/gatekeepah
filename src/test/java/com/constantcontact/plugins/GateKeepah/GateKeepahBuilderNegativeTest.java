package com.constantcontact.plugins.GateKeepah;

import java.util.ArrayList;
import java.util.List;

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

	public TestDataHelper testHelper;

	@Before
	public void testSetup() throws Exception {
		testHelper = new TestDataHelper();
	}

	@Test
	public void testAbilityToAddGateKeepah() throws Exception {
		runTest(new GateKeepahBuilder("",
				"sonar.qualityGateName=" + testHelper.getGateName() + "\nsonar.projectKey="
						+ testHelper.getProjectId() + "\nsonar.codeCoverageBreakLevel="
						+ testHelper.getCodeCoverageBreakLevel() + "\nsonar.codeCoverageGoal="
						+ testHelper.getCodeCoverageGoal() + "\nsonar.projectName="
						+ testHelper.getProjectName()));
	}

	@Test
	public void testAbilityToAddGateKeepahNullPropertyName() throws Exception {
		runTest(new GateKeepahBuilder(null,
				"sonar.qualityGateName=" + testHelper.getGateName() + "\nsonar.projectKey="
						+ testHelper.getGateName() + "\nsonar.codeCoverageBreakLevel="
						+ testHelper.getCodeCoverageBreakLevel() + "\nsonar.codeCoverageGoal="
						+ testHelper.getCodeCoverageGoal() + "\nsonar.projectName="
						+ testHelper.getProjectName()));
	}

	@Test
	public void testGateKeepahEmptyTeam() throws Exception {
		runTest(buildGateKeepahProperties("", testHelper.getProjectId(),
				testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyResourceKey() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(), "",
				testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), "",
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), testHelper.getCodeCoverageGoal(), "",
				"gatekeepah"));
	}

	@Test
	public void testGateKeepahEmptyAppName() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), ""));
	}

	@Test
	public void testGateKeepahNullTeam() throws Exception {
		runTest(buildGateKeepahProperties(null, testHelper.getProjectId(),
				testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullResourceKey() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(), null,
				testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullBreakLevel() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), null,
				testHelper.getCodeCoverageBreakLevel(), "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullCoverageGoal() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				null, "gatekeepah"));
	}

	@Test
	public void testGateKeepahNullAppName() throws Exception {
		runTest(buildGateKeepahProperties(testHelper.getGateName(),
				testHelper.getProjectId(), testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel(), null));
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
