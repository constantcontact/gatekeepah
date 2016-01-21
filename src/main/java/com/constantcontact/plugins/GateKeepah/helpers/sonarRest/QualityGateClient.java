package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateListCollection;

public class QualityGateClient extends Sonar {

	private HttpHelper httpHelper;

	public QualityGateClient(final String host, final String username, final String password) throws Exception {
		super(host, username, password);
		this.httpHelper = new HttpHelper(host, username, password);
	}

	public QualityGateListCollection retrieveQualityGateList() throws Exception {
		String responseBody = getHttpHelper().doGet(this.getHost() + "/api/qualitygates/list");
		ObjectMapper mapper = new ObjectMapper();
		QualityGateListCollection collection = mapper.readValue(responseBody, QualityGateListCollection.class);
		return collection;
	}

	public QualityGateCondition updateQualityGateCondition(final QualityGateCondition qualityGateCondition)
			throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("error", String.valueOf(qualityGateCondition.getError())));
		formparams.add(new BasicNameValuePair("id", String.valueOf(qualityGateCondition.getId())));
		formparams.add(new BasicNameValuePair("metric", String.valueOf(qualityGateCondition.getMetric())));
		formparams.add(new BasicNameValuePair("op", String.valueOf(qualityGateCondition.getOp())));
		formparams.add(new BasicNameValuePair("warning", String.valueOf(qualityGateCondition.getWarning())));
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		String responseBody = getHttpHelper().doPost(this.getHost() + "/api/qualitygates/update_condition", payload);
		ObjectMapper mapper = new ObjectMapper();
		QualityGateCondition condition = mapper.readValue(responseBody, QualityGateCondition.class);
		return condition;
	}

	public QualityGateCondition createQualityGateCondition(final QualityGateCondition qualityGateCondition)
			throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("error", String.valueOf(qualityGateCondition.getError())));
		formparams.add(new BasicNameValuePair("gateId", String.valueOf(qualityGateCondition.getGateId())));
		formparams.add(new BasicNameValuePair("metric", String.valueOf(qualityGateCondition.getMetric())));
		formparams.add(new BasicNameValuePair("op", String.valueOf(qualityGateCondition.getOp())));
		formparams.add(new BasicNameValuePair("warning", String.valueOf(qualityGateCondition.getWarning())));
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		String responseBody = getHttpHelper().doPost(this.getHost() + "/api/qualitygates/create_condition", payload);
		ObjectMapper mapper = new ObjectMapper();
		QualityGateCondition condition = mapper.readValue(responseBody, QualityGateCondition.class);
		return condition;
	}

	public QualityGate retrieveQualityGateDetails(final int qualityGateId) throws Exception {
		String responseBody = getHttpHelper().doGet(this.getHost() + "/api/qualitygates/show?id=" + qualityGateId);
		ObjectMapper mapper = new ObjectMapper();
		QualityGate qualityGate = mapper.readValue(responseBody, QualityGate.class);
		return qualityGate;
	}

	public void associateQualityGate(final int gateId, final int projectId) throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("gateId", String.valueOf(gateId)));
		formparams.add(new BasicNameValuePair("projectId", String.valueOf(projectId)));
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		getHttpHelper().doPost(this.getHost() + "/api/qualitygates/select", payload);
	}

	public QualityGate createQualityGate(final String name) throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("name", name));
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		String responseBody = getHttpHelper().doPost(this.getHost() + "/api/qualitygates/create", payload);
		ObjectMapper mapper = new ObjectMapper();
		QualityGate qualityGate = mapper.readValue(responseBody, QualityGate.class);
		return qualityGate;
	}

	private HttpHelper getHttpHelper() {
		return httpHelper;
	}

}
