package com.constantcontact.plugins.GateKeepah;

import java.io.InputStream;
import java.util.Properties;

public class TestDataHelper {

	private String userName;
	private String password;
	private String host;
	private String gateId;
	private String conditionId;
	private String projectId;
	private String projectResource;
	private String gateName;
	private String codeCoverageGoal;
	private String codeCoverageBreakLevel;
	private String projectName;

	public TestDataHelper() throws Exception {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("config.properties");
		Properties props = new Properties();
		props.load(is);

		if (null == System.getProperty("username") || System.getProperty("username").isEmpty()) {
			setUserName(props.get("sonar.test.username").toString());
		} else {
			setUserName(System.getProperty("username"));
		}

		if (null == System.getProperty("password") || System.getProperty("password").isEmpty()) {
			setPassword(props.get("sonar.test.password").toString());
		} else {
			setPassword(System.getProperty("password"));
		}

		if (null == System.getProperty("host") || System.getProperty("host").isEmpty()) {
			setHost(props.get("sonar.test.host").toString());
		} else {
			setHost(System.getProperty("host"));
		}
		
		setGateId(props.get("sonar.test.gate.id").toString());
		setConditionId(props.get("sonar.test.condition.id").toString());
		setProjectId(props.get("sonar.test.project.id").toString());
		setProjectResource(props.get("sonar.test.project.resource").toString());
		setGateName(props.get("sonar.test.gate.name").toString());
		setCodeCoverageGoal(props.get("sonar.test.codecoverage.goal").toString());
		setCodeCoverageBreakLevel(props.get("sonar.test.codecoverage.breaklevel").toString());
		setProjectName(props.get("sonar.test.project.name").toString());
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getGateId() {
		return gateId;
	}

	public void setGateId(String gateId) {
		this.gateId = gateId;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void setConditionId(String conditionId) {
		this.conditionId = conditionId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectResource() {
		return projectResource;
	}

	public void setProjectResource(String projectResource) {
		this.projectResource = projectResource;
	}

	public String getGateName() {
		return gateName;
	}

	public void setGateName(String gateName) {
		this.gateName = gateName;
	}

	public String getCodeCoverageGoal() {
		return codeCoverageGoal;
	}

	public void setCodeCoverageGoal(String codeCoverageGoal) {
		this.codeCoverageGoal = codeCoverageGoal;
	}

	public String getCodeCoverageBreakLevel() {
		return codeCoverageBreakLevel;
	}

	public void setCodeCoverageBreakLevel(String codeCoverageBreakLevel) {
		this.codeCoverageBreakLevel = codeCoverageBreakLevel;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
