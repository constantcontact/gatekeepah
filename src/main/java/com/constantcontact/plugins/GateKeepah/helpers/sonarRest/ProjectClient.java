package com.constantcontact.plugins.GateKeepah.helpers.sonarRest;

import java.util.List;

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
		if (null != key) {
			urlParams = "?key=" + key;
		}
		String responseBody = getHttpHelper().doGet(this.getHost() + "/api/projects/index" + urlParams);
		ObjectMapper mapper = new ObjectMapper();
		List<Project> collection = mapper.readValue(responseBody,
				mapper.getTypeFactory().constructCollectionType(List.class, Project.class));
		return collection;
	}

	public HttpHelper getHttpHelper() {
		return httpHelper;
	}

	public void setHttpHelper(HttpHelper httpHelper) {
		this.httpHelper = httpHelper;
	}
}
