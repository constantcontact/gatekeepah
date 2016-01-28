package com.constantcontact.plugins.GateKeepah;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;

public class GateKeepahBuilderSonarLogicTest {
	private TestDataHelper testHelper;

	@Before
	public void setup() throws Exception {
		testHelper = new TestDataHelper();
	}

	@Test
	public void retrieveProjectsForKey() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		ProjectClient client = new ProjectClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());
		List<Project> projects = gateKeepahBuilder.retrieveProjectsForKey(client,
				testHelper.getProjectId());
		boolean foundProject = false;
		for (Project project : projects) {
			if (project.getId().toString().equals(testHelper.getProjectId())) {
				foundProject = true;
			}
		}
		Assert.assertEquals(true, foundProject);
	}

	@Test
	public void findQualityGate() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());

		QualityGate qualityGate = gateKeepahBuilder.findQualityGate(client,
				testHelper.getGateName());
		Assert.assertEquals(true, qualityGate.getName().equalsIgnoreCase(testHelper.getGateName()));
	}

	@Test
	public void retrieveQualityGateDetails() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());

		QualityGate qualityGateToUse = gateKeepahBuilder.findQualityGate(client,
				testHelper.getGateName());
		QualityGateCondition conditionToUpdate = gateKeepahBuilder.retrieveQualityGateDetails(client, qualityGateToUse);
		Assert.assertNotEquals(null, conditionToUpdate);
	}

	@Test
	public void retrieveConditionDetails() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());

		QualityGate qualityGateToUse = gateKeepahBuilder.findQualityGate(client,
				testHelper.getGateName());
		QualityGate qualityGate = client.retrieveQualityGateDetails(qualityGateToUse.getId());
		QualityGateCondition conditionToUpdate = gateKeepahBuilder.retrieveConditionDetails(qualityGate);
		Assert.assertNotEquals(null, conditionToUpdate);
	}

	@Test
	public void updateQualityCondition() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());

		QualityGate qualityGateToUse = gateKeepahBuilder.findQualityGate(client,
				testHelper.getGateName());
		QualityGate qualityGate = client.retrieveQualityGateDetails(qualityGateToUse.getId());
		QualityGateCondition conditionToUpdate = gateKeepahBuilder.retrieveConditionDetails(qualityGate);
		QualityGateCondition updatedQualityCondition = gateKeepahBuilder.updateQualityCondition(client,
				conditionToUpdate, testHelper.getCodeCoverageGoal(),
				testHelper.getCodeCoverageBreakLevel());
		Assert.assertNotEquals(null, updatedQualityCondition);
	}

	@Test
	public void createQualityGateCondition() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(testHelper.getHost(),
				testHelper.getUserName(), testHelper.getPassword());

		QualityGate qualityGateToUse = gateKeepahBuilder.findQualityGate(client,
				testHelper.getGateName());
		QualityGate qualityGate = client.retrieveQualityGateDetails(qualityGateToUse.getId());
		QualityGateCondition newlyCreatedCondition = gateKeepahBuilder.createQualityGateCondition(client,
				testHelper.getCodeCoverageBreakLevel(),
				testHelper.getCodeCoverageGoal(), qualityGate);
		Assert.assertNotEquals(null, newlyCreatedCondition);
	}

}
