package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGateClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.Messages;
import com.constantcontact.plugins.GateKeepah.TestDataHelper;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;

public class QualityGateClientTest {

	private TestDataHelper testHelper;

	@Before
	public void testSetup() throws Exception {
		testHelper = new TestDataHelper();
	}

	@Test
	public void retrieveList() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.retrieveQualityGateList();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void associateQualityId() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.associateQualityGate(Integer.valueOf(testHelper.getGateId()),
					Integer.valueOf(testHelper.getProjectId()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void qualityGateDetails() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.retrieveQualityGateDetails(Integer.valueOf(testHelper.getGateId()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void updateQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		qualityGateCondition.setGateId(Integer.valueOf(testHelper.getGateId()));
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());

		QualityGateCondition createdCondition = client.createQualityGateCondition(qualityGateCondition);

		createdCondition.setError(0);
		createdCondition.setMetric("coverage");
		createdCondition.setOp("LT");
		createdCondition.setWarning(50);
		try {
			client.updateQualityGateCondition(createdCondition);
			client.destroyQualityGateCondition(createdCondition.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void updateQualityGateConditionZERO() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setId(0);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.updateQualityGateCondition(qualityGateCondition);
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals(Messages.qualitygateclient_condition_id_required(), e.getMessage());
		}
	}

	@Test
	public void createQualityGate() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
			client.destroyQualityGate(qualityGate.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void createQualityGateEmptyName() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.createQualityGate("");
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals(Messages.qualitygateclient_name_required(), e.getMessage());
		}
	}

	@Test
	public void createQualityGateNullName() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			client.createQualityGate(null);
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals(Messages.qualitygateclient_name_required(), e.getMessage());
		}
	}

	@Test
	public void createQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		qualityGateCondition.setGateId(Integer.valueOf(testHelper.getGateId()));
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			QualityGateCondition createdQualityGateCondtion = client.createQualityGateCondition(qualityGateCondition);
			client.destroyQualityGateCondition(createdQualityGateCondtion.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void createAndDestroyQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		qualityGateCondition.setGateId(Integer.valueOf(testHelper.getGateId()));
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		try {
			QualityGateCondition createdQualityGateCondtion = client.createQualityGateCondition(qualityGateCondition);
			client.destroyQualityGateCondition(createdQualityGateCondtion.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}
	}

	@Test
	public void createAndDestroy() throws Exception {
		QualityGateClient client = new QualityGateClient(testHelper.getHost(), testHelper.getUserName(),
				testHelper.getPassword());
		QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
		try {
			client.destroyQualityGate(qualityGate.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");
		}

	}

}