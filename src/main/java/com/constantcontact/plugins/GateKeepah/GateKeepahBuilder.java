package com.constantcontact.plugins.GateKeepah;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.constantcontact.plugins.Messages;
import com.constantcontact.plugins.GateKeepah.exceptionHandling.GateKeepahException;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.ProjectClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.QualityGateClient;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.projects.Project;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGate;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateCondition;
import com.constantcontact.plugins.GateKeepah.helpers.sonarRest.qualityGates.QualityGateListCollection;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class GateKeepahBuilder extends Builder implements SimpleBuildStep {

	private String propertiesFileName;
	private String additionalProperties;
	private final String LOGGING_PREFIX = Messages.builder_logging_prefix();

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
		if (null != qualityGate.getConditions() && qualityGate.getConditions().size() != 0) {
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

			// GateKeepah text macro do not change spacing
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");
			listener.getLogger().println(
					LOGGING_PREFIX + "  ________        __          ____  __.                         .__     ");
			listener.getLogger().println(
					LOGGING_PREFIX + " /  _____/_____ _/  |_  ____ |    |/ _|____   ____ ___________  |  |__  ");
			listener.getLogger().println(LOGGING_PREFIX
					+ "/   \\  ___\\__  \\\\   __\\/ __ \\|      <_/ __ \\_/ __ \\\\____ \\__  \\ |  |  \\ ");
			listener.getLogger().println(LOGGING_PREFIX
					+ "\\    \\_\\  \\/ __ \\|  | \\  ___/|    |  \\  ___/\\  ___/|  |_> > __ \\|   Y  \\");
			listener.getLogger().println(
					LOGGING_PREFIX + " \\______  (____  /__|  \\___  >____|__ \\___  >\\___  >   __(____  /___|  /");
			listener.getLogger().println(LOGGING_PREFIX
					+ "        \\/     \\/          \\/        \\/   \\/     \\/|__|       \\/     \\/ ");
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");

			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_start());
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");

			checkGlobalPropertyValues(listener);
			setProjectClient(new ProjectClient(getDescriptor().getSonarHost(), getDescriptor().getSonarUserName(),
					getDescriptor().getSonarPassword()));
			setQualityGateClient(new QualityGateClient(getDescriptor().getSonarHost(),
					getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword()));

			if (null == propertiesFileName || propertiesFileName.isEmpty()) {
				propertiesFileName = "gatekeepah.properties";
			}
			FilePath filePath = workspace.child(propertiesFileName);
			HashMap<String, String> map = new HashMap<String, String>();

			EnvVars envVars = new EnvVars();
			envVars = build.getEnvironment(listener);

			String nodeName = envVars.get("NODE_NAME");
			if (nodeName.equalsIgnoreCase("master")) {
				map.putAll(Jenkins.getInstance().getRootPath()
						.act(new GateKeepahPropertiesHandler(filePath.toString(), additionalProperties)));
			} else {
				map.putAll(filePath.act(new GateKeepahPropertiesHandler(filePath.toString(), additionalProperties)));
			}
			Properties props = mapToProperties(map);

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
			} else if ((null == getDescriptor().getDefaultQualityGateName()
					|| getDescriptor().getDefaultQualityGateName().isEmpty())
					&& (null == qualityGateName || qualityGateName.isEmpty())) {
				throw new GateKeepahException(Messages.builder_qualitygate_notdefined());
			} else {
				boolean usedDefaultQualityGate = setupDefaultQualityGate(sonarProjectKey, sonarProjectName,
						getDescriptor().getDefaultQualityGateName(), codeCoverageGoal, codeCoverageBreakLevel,
						listener);
				if (!usedDefaultQualityGate && (null == qualityGateName || qualityGateName.isEmpty())) {
					throw new GateKeepahException(Messages.builder_qualitygate_notdefined());
				}
			}

			setupQualityGate(qualityGateName, sonarProjectName, sonarProjectKey, codeCoverageGoal,
					codeCoverageBreakLevel, listener);

			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");

			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_status_success());
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");
		} catch (Exception e) {
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");

			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_status_failed());
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_status_exception() + e.getMessage());
			listener.getLogger().println(
					LOGGING_PREFIX + "-----------------------------------------------------------------------------");
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
				return FormValidation.ok(Messages.builder_descriptor_formvalidation_success());
			} catch (Exception e) {
				return FormValidation.error("Client error : " + e.getMessage());
			}
		}

		public FormValidation doCheckSonarHost(@QueryParameter("sonarHost") String sonarHost)
				throws IOException, ServletException {
			try {
				if (null == sonarHost || sonarHost.isEmpty()) {
					throw new FormException(Messages.builder_descriptor_formvalidation_sonarhost_requried(),
							Messages.builder_descriptor_formvalidation_sonarhost());
				}
				if (sonarHost.contains("https")) {
					throw new FormException(Messages.builder_descriptor_formvalidation_http(),
							Messages.builder_descriptor_formvalidation_sonarhost());
				}

				if (sonarHost.endsWith("/")) {
					throw new FormException(Messages.builder_descriptor_formvalidation_backslash(),
							Messages.builder_descriptor_formvalidation_sonarhost());
				}

				this.sonarHost = sonarHost;
				return FormValidation.ok(Messages.builder_descriptor_formvalidation_success());
			} catch (Exception e) {
				return FormValidation.error(Messages.builder_sonar_globalconfig_exception() + e.getMessage());
			}
		}

		public FormValidation doCheckSonarUserName(@QueryParameter("sonarUserName") final String sonarUserName)
				throws IOException, ServletException {
			try {

				if (null == sonarUserName || sonarUserName.isEmpty()) {
					throw new FormException(Messages.builder_descriptor_formvalidation_username_required(),
							Messages.builder_descriptor_formvalidation_username());
				}

				this.sonarUserName = sonarUserName;

				return FormValidation.ok(Messages.builder_descriptor_formvalidation_success());
			} catch (Exception e) {
				return FormValidation.error(Messages.builder_sonar_globalconfig_exception() + e.getMessage());
			}
		}

		public FormValidation doCheckSonarPassword(@QueryParameter("sonarPassword") final String sonarPassword)
				throws IOException, ServletException {
			try {
				if (null == sonarPassword || sonarPassword.isEmpty()) {
					throw new FormException(Messages.builder_descriptor_formvalidation_password_required(),
							Messages.builder_descriptor_formvalidation_password());
				}

				this.sonarPassword = sonarPassword;

				return FormValidation.ok(Messages.builder_descriptor_formvalidation_success());
			} catch (Exception e) {
				return FormValidation.error(Messages.builder_sonar_globalconfig_exception() + e.getMessage());
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
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_qualitygatename_required());
			throwException = true;
		}

		if (null == sonarProjectName || sonarProjectName.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_projectname_required());
			throwException = true;
		}

		if (null == codeCoverageGoal || codeCoverageGoal.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_codecoveragegoal_required());
			throwException = true;
		}

		if (null == codeCoverageBreakLevel || codeCoverageBreakLevel.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_codecoveragebreaklevel_required());
			throwException = true;
		}

		if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_projectkey_required());
			throwException = true;
		}

		if (throwException) {
			throw new GateKeepahException(Messages.builder_abort_no_properties());
		}
	}

	private void checkGlobalPropertyValues(final TaskListener listener) throws GateKeepahException {
		boolean failFast = false;
		if (null == getDescriptor().getSonarHost() || getDescriptor().getSonarHost().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_sonar_host_required());
		}

		if (null == getDescriptor().getSonarUserName() || getDescriptor().getSonarUserName().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_sonar_username_required());
		}

		if (null == getDescriptor().getSonarPassword() || getDescriptor().getSonarPassword().isEmpty()) {
			failFast = true;
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_sonar_password_required());
		}

		if (failFast) {
			throw new GateKeepahException(Messages.builder_sonar_globalconfig_exception());
		}
	}

	private Project retrieveOrCreateProject(final String sonarProjectName, final String sonarProjectKey,
			final TaskListener listener) throws Exception {
		List<Project> projects = null;
		try {
			listener.getLogger().println(LOGGING_PREFIX + Messages.builder_retrieve_projects_key() + sonarProjectKey);
			projects = retrieveProjectsForKey(sonarProjectKey);
		} catch (Exception e) {
			projects = new ArrayList<Project>();
		}
		if (projects.size() > 1) {
			throw new GateKeepahException(Messages.builder_retrieve_projects_specific());
		}

		if (projects.size() == 0) {
			if ((null != sonarProjectName && !sonarProjectName.isEmpty())
					&& (null != sonarProjectKey && !sonarProjectKey.isEmpty())) {
				listener.getLogger()
						.println(LOGGING_PREFIX + Messages.builder_retrieve_projects_create() + sonarProjectName);
				projects.add(createProject(sonarProjectName, sonarProjectKey));
			} else {
				throw new GateKeepahException(Messages.builder_retrieve_projects_exception());
			}
		}

		return projects.get(0);
	}

	private QualityGateCondition createQualityGateCondtion(final String codeCoverageBreakLevel,
			final String codeCoverageGoal, final QualityGate qualityGate, final TaskListener listener)
					throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + Messages.builder_create_quality_gate());
		return createQualityGateCondition(codeCoverageBreakLevel, codeCoverageGoal, qualityGate);
	}

	private QualityGateCondition updateQualityGateCondtion(final String codeCoverageBreakLevel,
			final String codeCoverageGoal, final QualityGateCondition conditionToUpdate, final TaskListener listener)
					throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + Messages.builder_update_qualitygatecondition());
		return updateQualityCondition(conditionToUpdate, codeCoverageBreakLevel, codeCoverageGoal);
	}

	private void associateProjectToQualityGate(final Project project, final QualityGate qualityGate,
			final TaskListener listener) throws NumberFormatException, Exception {
		listener.getLogger().println(LOGGING_PREFIX + Messages.builder_associate_project() + project.getNm()
				+ Messages.builder_associate_project_to_gate() + qualityGate.getName());
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
		listener.getLogger()
				.println(LOGGING_PREFIX + Messages.builder_retrieve_qualitygate_details() + qualityGateToUse.getName());
		QualityGate qualityGate = getQualityGateClient().retrieveQualityGateDetails(qualityGateToUse.getId());
		if (!qualityGate.getName().equalsIgnoreCase(getDescriptor().getDefaultQualityGateName())) {
			listener.getLogger().println(LOGGING_PREFIX + Messages._builder_retrieve_qualitygate_condition_details()
					+ qualityGate.getName());
			QualityGateCondition conditionToUpdate = retrieveConditionDetails(qualityGate);
			if (null != conditionToUpdate) {
				listener.getLogger().println(LOGGING_PREFIX + Messages.builder_update_qualitygate_condition_details_id()
						+ conditionToUpdate.getId());
				listener.getLogger()
						.println(LOGGING_PREFIX + Messages.builder_code_coverage_goal() + codeCoverageBreakLevel);
				listener.getLogger()
						.println(LOGGING_PREFIX + Messages.builder_code_coverage_breaklevel() + codeCoverageGoal);
			}
			updateOrCreateCondition(conditionToUpdate, codeCoverageGoal, codeCoverageBreakLevel, qualityGate, listener);
		}
		associateProjectToQualityGate(project, qualityGate, listener);
	}

	private void createAndSetupQualityGate(final String qualityGateName, final String codeCoverageGoal,
			final String codeCoverageBreakLevel, final Project project, final TaskListener listener) throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + Messages.builder_create_quality_gate() + qualityGateName);
		QualityGate qualityGateToUse = qualityGateClient.createQualityGate(qualityGateName);
		setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
	}

	private QualityGate findQualityGateToUse(final String qualityGateName, final TaskListener listener)
			throws Exception {
		listener.getLogger().println(LOGGING_PREFIX + Messages.builder_matching_quality_gate());
		return findQualityGate(qualityGateName);
	}

	private boolean setupDefaultQualityGate(final String sonarProjectKey, final String sonarProjectName,
			final String qualityGateName, final String codeCoverageGoal, final String codeCoverageBreakLevel,
			final TaskListener listener) throws Exception {
		if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
			throw new GateKeepahException(Messages.builder_abort_projectkey());
		}
		try {
			Project project = retrieveOrCreateProject(sonarProjectName, sonarProjectKey, listener);
			QualityGate qualityGateToUse = findQualityGateToUse(qualityGateName, listener);

			if (null != qualityGateToUse) {
				setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
				return true;
			} else {
				listener.getLogger().println(LOGGING_PREFIX + Messages.builder_default_qualitygate() + qualityGateName
						+ Messages.builder_was_not_found());
				return false;
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

				if (null == codeCoverageBreakLevel || codeCoverageBreakLevel.isEmpty()) {
					throw new GateKeepahException(Messages.builder_quality_gate_codecoveragebreaklevel_required());
				}

				if (null == codeCoverageGoal || codeCoverageGoal.isEmpty()) {
					throw new GateKeepahException(Messages.builder_quality_gate_codecoveragegoal_required());
				}

				if (null != qualityGateToUse) {
					setupQualityGate(qualityGateToUse, codeCoverageGoal, codeCoverageBreakLevel, project, listener);
				} else {
					createAndSetupQualityGate(qualityGateName, codeCoverageGoal, codeCoverageBreakLevel, project,
							listener);
				}

			} else if (null == sonarProjectName || sonarProjectName.isEmpty()) {
				throw new GateKeepahException(Messages.builder_quality_gate_projectname_required());
			} else if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
				throw new GateKeepahException(Messages.builder_quality_gate_projectkey_required());
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

	private Properties mapToProperties(final Map<String, String> map) {
		Properties props = new Properties();
		props.putAll(map);
		return props;
	}
}
