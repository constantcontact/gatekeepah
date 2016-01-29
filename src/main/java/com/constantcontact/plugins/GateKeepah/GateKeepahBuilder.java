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
			throws InterruptedException {
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
						throw new InterruptedException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(filePath + File.separator + propertiesFileName);
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
					throw new InterruptedException(fnfe.getMessage());
				} catch (IOException ioe) {
					ioe.printStackTrace();
					throw new InterruptedException(ioe.getMessage());
				}
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw new InterruptedException(e.getMessage());
			}

			props = new Properties();
			if (null != additionalProperties && !additionalProperties.isEmpty()) {
				for (String line : additionalProperties.split(System.getProperty("line.separator"))) {
					if (line.length() < 1) {
						throw new InterruptedException(
								"A properties file must be in the right place or properties added to the text area");
					}
					String[] newProperty = line.split("=");

					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						throw new InterruptedException(
								"Property could not be set for " + line + " because it was missing its value");
					}
				}
				try {
					File file = new File(filePath + File.separator + "gatekeepah.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
					throw new InterruptedException(fnfe.getMessage());
				} catch (IOException ioe) {
					ioe.printStackTrace();
					throw new InterruptedException(ioe.getMessage());
				}
			}

		}
		return props;
	}

	public List<Project> retrieveProjectsForKey(final ProjectClient projectClient, final String sonarResourceKey)
			throws Exception {
		List<Project> projects = projectClient.retrieveIndexOfProjects(sonarResourceKey);

		return projects;
	}

	public Project createProject(final ProjectClient projectClient, final String projectName, final String projectKey)
			throws Exception {
		Project project = new Project();
		project.setK(projectKey);
		project.setNm(projectName);
		return projectClient.createProject(project);
	}

	public QualityGate findQualityGate(final QualityGateClient qualityGateClient, final String gateName)
			throws Exception {
		QualityGateListCollection qualityGates = qualityGateClient.retrieveQualityGateList();
		QualityGate qualityGateToUse = null;
		for (QualityGate qualityGate : qualityGates.getQualitygates()) {
			if (qualityGate.getName().equalsIgnoreCase(gateName)) {
				qualityGateToUse = qualityGate;
			}
		}
		return qualityGateToUse;
	}

	public QualityGateCondition retrieveQualityGateDetails(final QualityGateClient qualityGateClient,
			QualityGate qualityGateToUse) throws Exception {
		// Retrieve details on quality gate

		QualityGate qualityGate = qualityGateClient.retrieveQualityGateDetails(qualityGateToUse.getId());
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

	public QualityGateCondition updateQualityCondition(final QualityGateClient qualityGateClient,
			final QualityGateCondition conditionToUpdate, final String codeCoverageBreakLevel,
			final String codeCoverageGoal) throws Exception {
		conditionToUpdate.setError(Integer.parseInt(codeCoverageBreakLevel));
		conditionToUpdate.setWarning(Integer.parseInt(codeCoverageGoal));
		conditionToUpdate.setOp("LT");
		return qualityGateClient.updateQualityGateCondition(conditionToUpdate);
	}

	public QualityGateCondition createQualityGateCondition(final QualityGateClient qualityGateClient,
			final String codeCoverageBreakLevel, final String codeCoverageGoal, final QualityGate qualityGateToUse)
					throws Exception {
		QualityGateCondition condition = new QualityGateCondition();
		condition.setError(Integer.parseInt(codeCoverageBreakLevel));
		condition.setWarning(Integer.parseInt(codeCoverageGoal));
		condition.setOp("LT");
		condition.setGateId(qualityGateToUse.getId());
		condition.setMetric("coverage");
		return qualityGateClient.createQualityGateCondition(condition);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException {
		try {

			boolean failFast = false;
			if (null == getDescriptor().getSonarHost() || getDescriptor().getSonarHost().isEmpty()) {
				failFast = true;
				listener.getLogger().println("Sonar host was not set in global configuration");
			}

			if (null == getDescriptor().getSonarUserName() || getDescriptor().getSonarUserName().isEmpty()) {
				failFast = true;
				listener.getLogger().println("Sonar Username was not set in global configuration");
			}

			if (null == getDescriptor().getSonarPassword() || getDescriptor().getSonarPassword().isEmpty()) {
				failFast = true;
				listener.getLogger().println("Sonar Password was not set in global configuration");
			}

			if (failFast) {
				throw new InterruptedException("Global Sonar Configuration was not set correctly");
			}

			Properties props = null;
			try {
				props = readPropertiesFile(workspace.absolutize().toString(), propertiesFileName, additionalProperties);
			} catch (Exception e) {
				throw new InterruptedException(e.getLocalizedMessage());
			}

			// Ensure all required properties are set
			final String qualityGateName = props.getProperty("sonar.qualityGateName");
			final String sonarProjectName = props.getProperty("sonar.projectName");
			final String codeCoverageGoal = props.getProperty("sonar.codeCoverageGoal");
			final String codeCoverageBreakLevel = props.getProperty("sonar.codeCoverageBreakLevel");
			final String sonarProjectKey = props.getProperty("sonar.projectKey");

			boolean throwException = false;
			if (null == getDescriptor().getDefaultQualityGateName()
					|| getDescriptor().getDefaultQualityGateName().isEmpty()) {
				if (null == qualityGateName || qualityGateName.isEmpty()) {
					listener.getLogger().println("sonar.qualityGateName is empty or null");
					throwException = true;
				}

				if (null == sonarProjectName || sonarProjectName.isEmpty()) {
					listener.getLogger().println("sonar.projectName is empty or null");
					throwException = true;
				}

				if (null == codeCoverageGoal || codeCoverageGoal.isEmpty()) {
					listener.getLogger().println("sonar.codeCoverageGoal is empty or null");
					throwException = true;
				}

				if (null == codeCoverageBreakLevel || codeCoverageBreakLevel.isEmpty()) {
					listener.getLogger().println("sonar.codeCoverageBreakLevel is empty or null");
					throwException = true;
				}

				if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
					listener.getLogger().println("sonar.projectKey is empty or null");
					throwException = true;
				}

				if (throwException) {
					throw new InterruptedException(
							"Aborting the build, no properties were set to utilize quality gates");
				}
			} else {
				if (null == sonarProjectKey || sonarProjectKey.isEmpty()) {
					throw new InterruptedException(
							"Aborting the build, sonar.projectKey must be set to associate default quality gate");
				}
				List<Project> projects;
				try {
					ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
					projects = retrieveProjectsForKey(projectClient, sonarProjectKey);
					if (projects.size() > 1) {
						throw new InterruptedException("Please be mores specific when defining sonar.projectKey");
					}

					if (projects.size() == 0) {
						if ((null != sonarProjectName && !sonarProjectName.isEmpty())
								&& (null != sonarProjectKey && !sonarProjectKey.isEmpty())) {
							projects = new ArrayList<Project>();
							listener.getLogger().println("Creating a new project " + sonarProjectName);
							projects.add(createProject(projectClient, sonarProjectName, sonarProjectKey));
						} else {
							throw new InterruptedException(
									"Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName");
						}
					}

				} catch (Exception e) {
					throw new InterruptedException(e.getLocalizedMessage());
				}
				try {
					QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

					listener.getLogger().println("Looking for matching quality gate to identify with project");
					QualityGate qualityGateToUse = findQualityGate(qualityGateClient,
							getDescriptor().getDefaultQualityGateName());

					if (null != qualityGateToUse) {
						listener.getLogger().println("Retrieving Quality Gate Details");
						QualityGate qualityGate = qualityGateClient
								.retrieveQualityGateDetails(qualityGateToUse.getId());
						listener.getLogger().println(
								"Associating project " + projects.get(0).getNm() + " to gate " + qualityGate.getName());
						qualityGateClient.associateQualityGate(qualityGate.getId(),
								Integer.parseInt(projects.get(0).getId()));

					} else {
						throw new InterruptedException("Encountered an issue locating quality gate details");
					}

				} catch (Exception e) {
					throw new InterruptedException(e.getLocalizedMessage());
				}
			}

			try {
				if (null != qualityGateName && null != sonarProjectName) {
					listener.getLogger().println("Gathering Sonar Projects");
					ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
					List<Project> projects = retrieveProjectsForKey(projectClient, sonarProjectKey);
					if (projects.size() > 1) {
						throw new InterruptedException("Please be mores specific when defining sonar.projectKey");
					}

					if (projects.size() == 0) {
						if ((null != sonarProjectName && !sonarProjectName.isEmpty())
								&& (null != sonarProjectKey && !sonarProjectKey.isEmpty())) {
							projects = new ArrayList<Project>();
							listener.getLogger().println("Creating a new project " + sonarProjectName);
							projects.add(createProject(projectClient, sonarProjectName, sonarProjectKey));
						} else {
							throw new InterruptedException(
									"Did not find the project and could not create one, please enter values for sonar.projectKey and sonar.projectName");
						}
					}

					QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

					listener.getLogger().println("Looking for matching quality gate to identify with project");
					QualityGate qualityGateToUse = findQualityGate(qualityGateClient, qualityGateName);

					QualityGate qualityGateToAssociate = null;
					if (null != qualityGateToUse) {
						listener.getLogger().println("Retrieving Quality Gate Details");
						QualityGate qualityGate = qualityGateClient
								.retrieveQualityGateDetails(qualityGateToUse.getId());

						listener.getLogger().println("Retrieving Details of Quality Gate Condition");
						QualityGateCondition conditionToUpdate = retrieveConditionDetails(qualityGate);

						if (null != conditionToUpdate) {
							if (!(conditionToUpdate.getWarning() == Integer.parseInt(codeCoverageGoal)
									&& conditionToUpdate.getError() == Integer.parseInt(codeCoverageBreakLevel))) {
								listener.getLogger().println("Updating Quality Gate Condition");
								updateQualityCondition(qualityGateClient, conditionToUpdate, codeCoverageBreakLevel,
										codeCoverageGoal);
							}
						} else {
							listener.getLogger().println("Creating new Quality Gate Condition");
							createQualityGateCondition(qualityGateClient, codeCoverageBreakLevel, codeCoverageGoal,
									qualityGateToUse);
						}

						qualityGateToAssociate = qualityGate;
					} else {
						listener.getLogger().println("Creating Quality Gate");
						QualityGate qualityGate = qualityGateClient.createQualityGate(qualityGateName);
						qualityGateToAssociate = qualityGate;

						listener.getLogger().println("Creating Quality Gate Condition");
						createQualityGateCondition(qualityGateClient, codeCoverageBreakLevel, codeCoverageGoal,
								qualityGate);
					}

					listener.getLogger().println("Associating project " + projects.get(0).getNm() + " to gate "
							+ qualityGateToAssociate.getName());
					qualityGateClient.associateQualityGate(qualityGateToAssociate.getId(),
							Integer.parseInt(projects.get(0).getId()));
				}

			} catch (Exception e) {
				throw new InterruptedException(e.getLocalizedMessage());
			}
		} catch (Exception e) {
			listener.getLogger().println(e.getLocalizedMessage());
			throw new InterruptedException(e.getLocalizedMessage());
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
				QualityGateClient client = new QualityGateClient(this.sonarHost, this.sonarUserName, this.sonarPassword);
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
}
