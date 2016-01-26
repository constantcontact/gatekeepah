package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGateClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;

public class QualityGateClientTest {

	private Properties props;
	private static List<QualityGate> qualityGatesToDestroy;

	@Before
	public void testSetup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is);
		qualityGatesToDestroy = new ArrayList<QualityGate>();
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
	}

	@Test
	public void retrieveList() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveQualityGateList();
	}

	@Test
	public void associateQualityId() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.associateQualityGate(Integer.valueOf(props.getProperty("sonar.test.gate.id")),
				Integer.valueOf(props.getProperty("sonar.test.project.id")));
	}

	@Test
	public void qualityGateDetails() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.retrieveQualityGateDetails(Integer.valueOf(props.getProperty("sonar.test.gate.id")));
	}

	@Test
	public void updateQualityGateCondition() throws Exception {
		QualityGateCondition qualityGateCondition = new QualityGateCondition();
		qualityGateCondition.setError(25);
		qualityGateCondition.setId(Integer.valueOf(props.getProperty("sonar.test.condition.id")));
		qualityGateCondition.setMetric("coverage");
		qualityGateCondition.setOp("LT");
		qualityGateCondition.setWarning(50);
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		client.updateQualityGateCondition(qualityGateCondition);
	}

	@Test
	public void createQualityGate() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
		qualityGatesToDestroy.add(qualityGate);
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
		client.createQualityGateCondition(qualityGateCondition);
	}
	
	@Test
	public void createAndDestroy() throws Exception {
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		QualityGate qualityGate = client.createQualityGate("Ryans Test" + System.currentTimeMillis());
		client.destroyQualityGate(qualityGate.getId());

	}

}