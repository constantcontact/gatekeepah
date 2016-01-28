package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;

public class ProjectClient extends Sonar {

	private HttpHelper httpHelper;

	public ProjectClient(final String host, final String username, final String password) throws Exception {
		super(host, username, password);
		this.setHttpHelper(new HttpHelper(host, username, password));
	}

	public List<Project> retrieveIndexOfProjects() throws Exception {
		return retrieveIndexOfProjects(null);
	}

	public List<Project> retrieveIndexOfProjects(final String key) throws Exception {
		String urlParams = "";
		if (null != key && !key.isEmpty()) {
			urlParams = "?key=" + key;
		}
		String responseBody = getHttpHelper().doGet(this.getHost() + "/api/projects/index" + urlParams);
		ObjectMapper mapper = new ObjectMapper();
		List<Project> collection = mapper.readValue(responseBody,
				mapper.getTypeFactory().constructCollectionType(List.class, Project.class));
		return collection;
	}

	public Project createProject(Project project) throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (null != project.getNm() && !project.getNm().isEmpty()) {
			formparams.add(new BasicNameValuePair("name", String.valueOf(project.getNm())));
		} else {
			throw new Exception("Name is a required field and must be set");
		}

		if (null != project.getK() && !project.getK().isEmpty()) {
			formparams.add(new BasicNameValuePair("key", String.valueOf(project.getK())));
		} else {
			throw new Exception("Key is a required field and must be set");
		}
		
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		String responseBody = getHttpHelper().doPost(this.getHost() + "/api/projects/create", payload);
		ObjectMapper mapper = new ObjectMapper();
		Project createdProject = mapper.readValue(responseBody, Project.class);
		return createdProject;
	}
	
	public void deleteProject(final String key) throws Exception {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (null != key && !key.isEmpty()) {
			formparams.add(new BasicNameValuePair("id", key));
		} else {
			throw new Exception("Key is a required field and must be set");
		}
		HttpEntity payload = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		getHttpHelper().doPost(this.getHost() + "/api/projects/destroy", payload);
	}

	public HttpHelper getHttpHelper() {
		return httpHelper;
	}

	public void setHttpHelper(HttpHelper httpHelper) {
		this.httpHelper = httpHelper;
	}
}
