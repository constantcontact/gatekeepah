package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates;

import java.util.List;

public class QualityGate {
	private String name;
	private int id;
	private List<QualityGateCondition> conditions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<QualityGateCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<QualityGateCondition> conditions) {
		this.conditions = conditions;
	}
}
