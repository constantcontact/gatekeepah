package com.constantcontact.plugins.GateKeepah;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;


public class GateKeepahBuilderSonarLogicTest {
	private Properties props;

	@Before
	public void setup() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		props = new Properties();
		props.load(is); 
	}
	
	@Test
	public void retrieveProjectsForKey() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		ProjectClient client = new ProjectClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
		List<Project> projects = gateKeepahBuilder.retrieveProjectsForKey(client, props.get("sonar.test.project.id").toString());
		boolean foundProject = false;
		for(Project project: projects){
			if(project.getId().toString().equals(props.get("sonar.test.project.id").toString())){				
				foundProject = true;
			}
		}
		Assert.assertEquals(true, foundProject);
	}
	
	@Test
	public void findQualityGate() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
	
		QualityGate qualityGate = gateKeepahBuilder.findQualityGate(client, props.get("sonar.test.gate.name").toString());
		Assert.assertEquals(true, qualityGate.getName().equalsIgnoreCase(props.get("sonar.test.gate.name").toString()));
	}
	
	@Test
	public void retrieveQualityGateDetails() throws Exception {
		GateKeepahBuilder gateKeepahBuilder = new GateKeepahBuilder(null, "test3=test3");
		QualityGateClient client = new QualityGateClient(props.get("sonar.test.host").toString(),
				props.get("sonar.test.username").toString(), props.get("sonar.test.password").toString());
	
		QualityGate qualityGateToUse = gateKeepahBuilder.findQualityGate(client, props.get("sonar.test.gate.name").toString());
		QualityGateCondition conditionToUpdate = gateKeepahBuilder.retrieveQualityGateDetails(client, qualityGateToUse);
		Assert.assertNotEquals(null, conditionToUpdate);
	}
	
	@Test
	public void retrieveConditionDetails() throws Exception {
		
	}
	
	

}
