package com.constantcontact.plugins.GateKeepah;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.constantcontact.plugins.GateKeepah.exceptionHandling.GateKeepahException;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateListCollection;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class GateKeepahBuilder extends Builder implements SimpleBuildStep {

	private final String propertiesFileName;
	private final String additionalProperties;
	private final String LOGGING_PREFIX = "GateKeepah:     ";

	private QualityGateClient qualityGateClient;
	private ProjectClient projectClient;

	@DataBoundConstructor
	public GateKeepahBuilder(final String propertiesFileName, final String additionalProperties) {
		this.propertiesFileName = propertiesFileName;
		this.additionalProperties = additionalProperties;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getPropertiesFileName() {
		return propertiesFileName;
	}

	public String getAdditionalProperties() {
		return additionalProperties;
	}

	public Properties readPropertiesFile(String filePath, String propertiesFileName, final String additionalProperties)
			throws GateKeepahException {
		Properties props;

		try {
			if (null == propertiesFileName) {
				throw new Exception("No file name found, creating our own");
			}
			InputStream input = new FileInputStream(filePath + File.separator + propertiesFileName);
			props = new Properties();
			props.load(input);

			if (null != additionalProperties && !additionalProperties.isEmpty()) {
				for (String line : additionalProperties.split(System.getProperty("line.separator"))) {
					String[] newProperty = line.split("=");
					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						throw new GateKeepahException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(filePath + File.separator + propertiesFileName);
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					throw new GateKeepahException(fnfe.getMessage());
				} catch (IOException ioe) {
					throw new GateKeepahException(ioe.getMessage());
				}
			}
		} catch (Exception e) {
			if (e instanceof GateKeepahException) {
				throw new GateKeepahException(e.getMessage());
			}

			props = new Properties();
			if (null != additionalProperties && !additionalProperties.isEmpty()) {
				for (String line : additionalProperties.split(System.getProperty("line.separator"))) {
					if (line.length() < 1) {
						throw new GateKeepahException(
								"A properties file must be in the right place or properties added to the text area");
					}
					String[] newProperty = line.split("=");

					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						throw new GateKeepahException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(filePath + File.separator + "gatekeepah.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					throw new GateKeepahException(fnfe.getMessage());
				} catch (IOException ioe) {
					throw new GateKeepahException(ioe.getMessage());
				}
			}

		}
		return props;
	}

	public List<Project> retrieveProjectsForKey(final String sonarResourceKey) throws Exception {
		List<Project> projects = getProjectClient().retrieveIndexOfProjects(sonarResourceKey);

		return projects;
	}

	public Project createProject(final String projectName, final String projectKey) throws Exception {
		Project project = new Project();
		project.setK(projectKey);
		project.setNm(projectName);
		return getProjectClient().createProject(project);
	}

	public QualityGate findQualityGate(final String gateName) throws Exception {
		QualityGateListCollection qualityGates = getQualityGateClient().retrieveQualityGateList();
		QualityGate qualityGateToUse = null;
		for (QualityGate qualityGate : qualityGates.getQualitygates()) {
			if (qualityGate.getName().equalsIgnoreCase(gateName)) {
				qualityGateToUse = qualityGate;
			}
		}
		return qualityGateToUse;
	}

	public QualityGateCondition retrieveQualityGateDetails(QualityGate qualityGateToUse) throws Exception {
		// Retrieve details on quality gate

		QualityGate qualityGate = getQualityGateClient().retrieveQualityGateDetails(qualityGateToUse.getId());
		QualityGateCondition conditionToUpdate = null;
		if (null != qualityGate.getConditions()) {
			for (QualityGateCondition condition : qualityGate.getConditions()) {
				if (condition.getMetric().equalsIgnoreCase("coverage")) {
					conditionToUpdate = condition;
					break;
				}
			}
		}
		return conditionToUpdate;
	}

	public QualityGateCondition retrieveConditionDetails(final QualityGate qualityGate) {
		QualityGateCondition conditionToUpdate = null;
		if (null != qualityGate.getConditions()) {
			for (QualityGateCondition condition : qualityGate.getConditions()) {
				if (condition.getMetric().equalsIgnoreCase("coverage")) {
					conditionToUpdate = condition;
					break;
				}
			}
		}
		return conditionToUpdate;
	}

	public QualityGateCondition updateQualityCondition(final QualityGateCondition conditionToUpdate,
			final String codeCoverageBreakLevel, final String codeCoverageGoal) throws Exception {
		conditionToUpdate.setError(Integer.parseInt(codeCoverageBreakLevel));
		conditionToUpdate.setWarning(Integer.parseInt(codeCoverageGoal));
		conditionToUpdate.setOp("LT");
		return getQualityGateClient().updateQualityGateCondition(conditionToUpdate);
	}

	public QualityGateCondition createQualityGateCondition(final String codeCoverageBreakLevel,
			final String codeCoverageGoal, final QualityGate qualityGateToUse) throws Exception {
		QualityGateCondition condition = new QualityGateCondition();
		condition.setError(Integer.parseInt(codeCoverageBreakLevel));
		condition.setWarning(Integer.parseInt(codeCoverageGoal));
		condition.setOp("LT");
		condition.setGateId(qualityGateToUse.getId());
		condition.setMetric("coverage");
		return getQualityGateClient().createQualityGateCondition(condition);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws GateKeepahException {
		try {

			checkGlobalPropertyValues(listener);
			setProjectClient(new ProjectClient(getDescriptor().getSonarHost(), getDescriptor().getSonarUserName(),
					getDescriptor().getSonarPassword()));
			setQualityGateClient(new QualityGateClient(getDescriptor().getSonarHost(),
					getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword()));

			Properties props = readPropertiesFile(workspace.absolutize().toString(), propertiesFileName,
					additionalProperties);

			// Ensure all required properties are set
			final String qualityGateName = props.getProperty("sonar.qualityGateName");
			final String sonarProjectName = props.getProperty("sonar.projectName");
			final String codeCoverageGoal = props.getProperty("sonar.codeCoverageGoal");
			final String codeCoverageBreakLevel = props.getProperty("sonar.codeCoverageBreakLevel");
			final String sonarProjectKey = props.getProperty("sonar.projectKey");

			if (null == getDescriptor().getDefaultQualityGateName()
					|| getDescriptor().getDefaultQualityGateName().isEmpty()) {
				checkRequiredPropertyValues(qualityGateName, sonarProjectName, codeCoverageGoal, codeCoverageBreakLevel,
						sonarProjectKey, listener);
			} else {
				setupDefaultQualityGate(sonarProjectKey, sonarProjectName, getDescriptor().getDefaultQualityGateName(),
						codeCoverageGoal, codeCoverageBreakLevel, listener);
			}

			setupQualityGate(qualityGateName, sonarProjectName, sonarProjectKey, codeCoverageGoal,
					codeCoverageBreakLevel, listener);
		} catch (Exception e) {
			listener.getLogger().println(e.getMessage());
			throw new GateKeepahException(e.getMessage());
		}

	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension // This indicates to Jenkins that this is an implementation of an
				// extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		private String sonarHost;
		private String sonarUserName;
		private String sonarPassword;
		private String defaultQualityGateName;

		public DescriptorImpl() {
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Gatekeepah";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			sonarHost = formData.getString("sonarHost");
			sonarUserName = formData.getString("sonarUserName");
			sonarPassword = formData.getString("sonarPassword");
			defaultQualityGateName = formData.getString("defaultQualityGateName");
			save();
			return super.configure(req, formData);
		}

		/**
		 * Create form validation
		 */

		public FormValidation doTestConnection(@QueryParameter("sonarHost") final String sonarHost,
				@QueryParameter("sonarUserName") final String sonarUserName,
				@QueryParameter("sonarPassword") final String sonarPassword) throws IOException, ServletException {
			try {
				doCheckSonarHost(sonarHost);
				doCheckSonarUserName(sonarUserName);
				doCheckSonarPassword(sonarPassword);
				QualityGateClient client = new QualityGateClient(this.sonarHost, this.sonarUserName,
						this.sonarPassword);
				client.retrieveQualityGateList();
				return FormValidation.ok("Success");
			} catch (Exception e) {
				return FormValidation.error("Client error : " + e.getMessage());
			}
		}

		public FormValidation doCheckSonarHost(@QueryParameter("sonarHost") String sonarHost)
				throws IOException, ServletException {
			try {
				if (sonarHost.contains("https")) {
					throw new FormException("Please use HTTP instead of HTTPS", "Sonar Host");
				}

				if (sonarHost.endsWith("/")) {
					throw new FormException("Please remove trailing /", "Sonar Host");
				}

				this.sonarHost = sonarHost;
				return FormValidation.ok();
			} catch (Exception e) {
				return FormValidation.error("Global config error : " + e.getMessage());
			}
		}

		public FormValidation doCheckSonarUserName(@QueryParameter("sonarUserName") final String sonarUserName)
				throws IOException, ServletException {
			try {

				if (null == sonarUserName || sonarUserName.isEmpty()) {
					throw new FormException("Sonar User Name must be set", "Sonar User Name");
				}

				this.sonarUserName = sonarUserName;

				return FormValidation.ok();
			} catch (Exception e) {
				return FormValidation.error("Global config error : " + e.getMessage());
			}
		}

		public FormValidation doCheckSonarPassword(@QueryParameter("sonarPassword") final String sonarPassword)
				throws IOException, ServletException {
			try {
				if (null == sonarPassword || sonarPassword.isEmpty()) {
					throw new FormException("Sonar Password must be set", "Sonar Password");
				}

				this.sonarPassword = sonarPassword;

				return FormValidation.ok();
			} catch (Exception e) {
				return FormValidation.error("Global config error : " + e.getMessage());
			}
		}

		public String getSonarHost() {
			return sonarHost;
		}

		public void setSonarHost(String sonarHost) {
			this.sonarHost = sonarHost;
		}

		public String getSonarUserName() {
			return sonarUserName;
		}

		public void setSonarUserName(String sonarUserName) {
			this.sonarUserName = sonarUserName;
		}

		public String getSonarPassword() {
			return sonarPassword;
		}

		public void setSonarPassword(String sonarPassword) {
			this.sonarPassword = sonarPassword;
		}

		public String getDefaultQualityGateName() {
			return defaultQualityGateName;
		}

		public void setDefaultQualityGateName(String defaultQualityGateName) {
			this.defaultQualityGateName = defaultQualityGateName;
		}

	}

	private void checkRequiredPropertyValues(final String qualityGateName, final String sonarProjectName,
			final String codeCoverageGoal, final String codeCoverageBreakLevel, final String sonarProjectKey,
			final TaskListener listener) throws GateKeepahException {

		boolean throwException = false;
		if (null == qualityGateName || qualityGateName.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + "sonar.qualityGateName is empty or null");
			throwException = true;
		}

		if (null == sonarProjectName || sonarProjectName.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + "sonar.projectName is empty or null");
			throwException = true;
		}

		if (null == codeCoverageGoal || codeCoverageGoal.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + "sonar.codeCoverageGoal is empty or null");
			throwException = true;
		}

		if (null == codeCoverageBreakLevel || codeCoverageBreakLevel.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + "sonar.codeCoverageBreakLevel is empty or null");
			throwException = true;
		}

		if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + "sonar.projectKey is empty or null");
			throwException = true;
		}

		if (throwException) {
			throw new GateKeepahException("Aborting the build, no properties were set to utilize quality gates");
		}
	}

	private void checkGlobalPropertyValues(final TaskListener listener) throws GateKeepahException {
		boolean failFast = false;
		if (null == getDescriptor().getSonarHost() || getDescriptor().getSonarHost().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + "Sonar host was not set in global configuration");
		}

		if (null == getDescriptor().getSonarUserName() || getDescriptor().getSonarUserName().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + "Sonar Username was not set in global configuration");
		}

		if (null == getDescriptor().getSonarPassword() || getDescriptor().getSonarPassword().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + "Sonar Password was not set in global configuration");
		}

		if (failFast) {
			throw new GateKeepahException("Global Sonar Configuration was not set correctly");
		}
	}

	private Project retrieveOrCreateProject(final String sonarProjectName, final String sonarProjectKey,
			final TaskListener listener) throws Exception {
		List<Project> projects = null;
		try {
			listener.getLogger().println(LOGGING_PREFIX + "Retrieving projects for key " + sonarProjectKey);
			projects = retrieveProjectsForKey(sonarProjectKey);
		} catch (Exception e) {
			projects = new ArrayList<Project>();
		}
		if (projects.size() > 1) {
			throw new GateKeepahException("Please be mores specific when defining sonar.projectKey");
		}

		if (projects.size() == 0) {
			if ((null != sonarProjectName && !sonarProjectName.isEmpty())
					&& (null != sonarProjectKey && !sonarProjectKey.isEmpty())) {
				listener.getLogger().println(LOGGING_PREFIX + "Creating a new project " + sonarProjectName);
				projects.add(createProject(sonarProjectName, sonarProjectKey));
			} else {
				throw new GateKeepahException(
						"Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName");
			}
		}

		return projects.get(0);
	}

	private QualityGateCondition createQualityGateCondtion(final String codeCoverageBreakLevel,
			final String codeCoverageGoal, final QualityGate qualityGate, final TaskListener listener)
					throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + "Creating Quality Gate Condition");
		return createQualityGateCondition(codeCoverageBreakLevel, codeCoverageGoal, qualityGate);
	}

	private QualityGateCondition updateQualityGateCondtion(final String codeCoverageBreakLevel,
			final String codeCoverageGoal, final QualityGateCondition conditionToUpdate, final TaskListener listener)
					throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + "Updating Quality Gate Condition");
		return updateQualityCondition(conditionToUpdate, codeCoverageBreakLevel, codeCoverageGoal);
	}

	private void associateProjectToQualityGate(final Project project, final QualityGate qualityGate,
			final TaskListener listener) throws NumberFormatException, Exception {
		listener.getLogger().println("Associating project " + project.getNm() + " to gate " + qualityGate.getName());
		getQualityGateClient().associateQualityGate(qualityGate.getId(), Integer.parseInt(project.getId()));
	}

	private QualityGateCondition updateOrCreateCondition(final QualityGateCondition conditionToUpdate,
			final String codeCoverageGoal, final String codeCoverageBreakLevel, final QualityGate qualityGate,
			TaskListener listener) throws Exception {
		QualityGateCondition condition = null;
		if (null != conditionToUpdate) {
			if (!(conditionToUpdate.getWarning() == Integer.parseInt(codeCoverageGoal)
					&& conditionToUpdate.getError() == Integer.parseInt(codeCoverageBreakLevel))) {
				condition = updateQualityGateCondtion(codeCoverageBreakLevel, codeCoverageGoal, conditionToUpdate,
						listener);
			}
		} else {
			condition = createQualityGateCondtion(codeCoverageBreakLevel, codeCoverageGoal, qualityGate, listener);
		}
		return condition;
	}

	private void setupQualityGate(final QualityGate qualityGateToUse, final String codeCoverageGoal,
			final String codeCoverageBreakLevel, final Project project, final TaskListener listener) throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + "Retrieving Quality Gate Details for Quality Gate Name: "
				+ qualityGateToUse.getName());
		QualityGate qualityGate = getQualityGateClient().retrieveQualityGateDetails(qualityGateToUse.getId());
		if (!qualityGate.getName().equalsIgnoreCase(getDescriptor().getDefaultQualityGateName())) {
			listener.getLogger().println(LOGGING_PREFIX
					+ "Retrieving Quality Gate Condition Details for Quality Gate Name: " + qualityGate.getName());
			QualityGateCondition conditionToUpdate = retrieveConditionDetails(qualityGate);
			listener.getLogger()
					.println(LOGGING_PREFIX + "Updating Quality Gate Condition Details for Quality Gate Condition ID: "
							+ conditionToUpdate.getId());
			listener.getLogger().println(LOGGING_PREFIX + "CODE COVERAGE GOAL = " + codeCoverageBreakLevel);
			listener.getLogger().println(LOGGING_PREFIX + "CODE COVERAGE BREAK LEVEL = " + codeCoverageGoal);
			updateOrCreateCondition(conditionToUpdate, codeCoverageGoal, codeCoverageBreakLevel, qualityGate, listener);
		}
		associateProjectToQualityGate(project, qualityGate, listener);
	}

	private void createAndSetupQualityGate(final String qualityGateName, final String codeCoverageGoal,
			final String codeCoverageBreakLevel, final Project project, final TaskListener listener) throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + "Creating Quality Gate");
		QualityGate qualityGateToUse = qualityGateClient.createQualityGate(qualityGateName);
		setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
	}

	private QualityGate findQualityGateToUse(final String qualityGateName, final TaskListener listener)
			throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + "Looking for matching quality gate to identify with project");
		return findQualityGate(qualityGateName);
	}

	private void setupDefaultQualityGate(final String sonarProjectKey, final String sonarProjectName,
			final String qualityGateName, final String codeCoverageGoal, final String codeCoverageBreakLevel,
			final TaskListener listener) throws Exception {
		if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
			throw new GateKeepahException(
					"Aborting the build, sonar.projectKey must be set to associate default quality gate");
		}
		try {
			Project project = retrieveOrCreateProject(sonarProjectName, sonarProjectKey, listener);
			QualityGate qualityGateToUse = findQualityGateToUse(qualityGateName, listener);

			if (null != qualityGateToUse) {
				setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
			} else {
				throw new GateKeepahException(
						"Encountered an issue locating quality gate details for Quality Gate Name:" + qualityGateName);
			}

		} catch (Exception e) {
			throw new GateKeepahException(e.getMessage());
		}
	}

	private void setupQualityGate(final String qualityGateName, final String sonarProjectName,
			final String sonarProjectKey, final String codeCoverageGoal, final String codeCoverageBreakLevel,
			final TaskListener listener) throws Exception {
		try {
			if (null != qualityGateName && null != sonarProjectName) {
				Project project = retrieveOrCreateProject(sonarProjectName, sonarProjectKey, listener);

				QualityGate qualityGateToUse = findQualityGateToUse(qualityGateName, listener);

				if (null != qualityGateToUse) {
					setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
				} else {
					createAndSetupQualityGate(qualityGateName, codeCoverageGoal, codeCoverageBreakLevel, project,
							listener);
				}

			}

		} catch (Exception e) {
			throw new GateKeepahException(e.getMessage());
		}
	}

	public QualityGateClient getQualityGateClient() {
		return qualityGateClient;
	}

	public void setQualityGateClient(QualityGateClient qualityGateClient) {
		this.qualityGateClient = qualityGateClient;
	}

	public ProjectClient getProjectClient() {
		return projectClient;
	}

	public void setProjectClient(ProjectClient projectClient) {
		this.projectClient = projectClient;
	}
}
