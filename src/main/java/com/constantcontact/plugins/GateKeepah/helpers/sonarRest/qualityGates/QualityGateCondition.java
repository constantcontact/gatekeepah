package com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates;

public class QualityGateCondition {
	private int id;
	private int gateId;
	private int error;
	private String metric;
	private String op;
	private int warning;

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public int getWarning() {
		return warning;
	}

	public void setWarning(int warning) {
		this.warning = warning;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGateId() {
		return gateId;
	}

	public void setGateId(int gateId) {
		this.gateId = gateId;
	}
}
