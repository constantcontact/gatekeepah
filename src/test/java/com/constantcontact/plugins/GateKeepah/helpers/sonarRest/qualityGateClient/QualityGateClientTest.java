package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGateClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;

public class QualityGateClientTest {

	// Needs more negative testing and protection against empty or null values
	private Properties props;
	private static List<QualityGate> qualityGatesToDestroy;
	private static List<QualityGateCondition> qualityGateConditionsToDestroy;

	@Before
	public void testSetup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
		qualityGatesToDestroy = new ArrayList<QualityGate>();
		qualityGateConditionsToDestroy = new ArrayList<QualityGateCondition>();
	}

	@After
	public void testTeardown() throws Exception {
		if (null != qualityGatesToDestroy && qualityGatesToDestroy.size() > 1) {
			QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
					props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
			for (QualityGate qualityGate : qualityGatesToDestroy) {
				client.destroyQualityGate(qualityGate.getId());
			}
		}

		if (null != qualityGateConditionsToDestroy && qualityGateConditionsToDestroy.size() > 1) {
			QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
					props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
			for (QualityGateCondition qualityGateCondition : qualityGateConditionsToDestroy) {
				client.destroyQualityGateCondition(qualityGateCondition.getId());
			}
		}
	}

	@Test
	public void retrieveList() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.retrieveQualityGateList();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}
	}

	@Test
	public void associateQualityId() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.associateQualityGate(Integer.valueOf(props.getProperty("sonar.test.gate.id")),
					Integer.valueOf(props.getProperty("sonar.test.project.id")));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}
	}

	@Test
	public void qualityGateDetails() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.retrieveQualityGateDetails(Integer.valueOf(props.getProperty("sonar.test.gate.id")));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}
	}

	@Test
	public void updateQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		qualityGateCondition.setGateId(Integer.valueOf(props.getProperty("sonar.test.gate.id")));
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		
		QualityGateCondition createdCondition = client.createQualityGateCondition(qualityGateCondition);
		
		createdCondition.setError(0);
		createdCondition.setMetric("coverage");
		createdCondition.setOp("LT");
		createdCondition.setWarning(50);
		try {
			client.updateQualityGateCondition(createdCondition);
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
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.updateQualityGateCondition(qualityGateCondition);
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals("Quality Gate Condition Id can not be 0, it is a required field", e.getMessage());
		}
	}

	@Test
	public void createQualityGate() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
			qualityGatesToDestroy.add(qualityGate);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}
	}

	@Test
	public void createQualityGateEmptyName() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createQualityGate("");
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals("Name is a required field and needs to be set", e.getMessage());
		}
	}

	@Test
	public void createQualityGateNullName() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createQualityGate(null);
			Assert.fail("Should have thrown an Exception");
		} catch (Exception e) {
			Assert.assertEquals("Name is a required field and needs to be set", e.getMessage());
		}
	}

	@Test
	public void createQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		qualityGateCondition.setGateId(Integer.valueOf(props.getProperty("sonar.test.gate.id")));
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			client.createQualityGateCondition(qualityGateCondition);
			qualityGateConditionsToDestroy.add(qualityGateCondition);
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
		qualityGateCondition.setGateId(Integer.valueOf(props.getProperty("sonar.test.gate.id")));
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		try {
			QualityGateCondition createdQualityGateCondtion = client.createQualityGateCondition(qualityGateCondition);
			client.destroyQualityGateCondition(createdQualityGateCondtion.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}
	}

	@Test
	public void createAndDestroy() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
		try {
			client.destroyQualityGate(qualityGate.getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Assert.fail("Should not have thrown an Exception");		}

	}

}