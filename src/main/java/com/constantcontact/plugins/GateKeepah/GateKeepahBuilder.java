package com.constantcontact.plugins.GateKeepah;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.kohsuke.stapler.DataBoundConstructor;
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
			final String sonarTeamName = props.getProperty("sonar.team.name");
			final String sonarAppName = props.getProperty("sonar.app.name");
			final String codeCoverageGoal = props.getProperty("sonar.codecoverage.goal");
			final String codeCoverageBreakLevel = props.getProperty("sonar.codecoverage.breaklevel");
			final String sonarResourceKey = props.getProperty("sonar.resource.key");

			boolean throwException = false;
			if (null == getDescriptor().getDefaultQualityGateName()
					|| getDescriptor().getDefaultQualityGateName().isEmpty()) {
				if (null == sonarTeamName || sonarTeamName.isEmpty()) {
					listener.getLogger().println("sonar.team.name is empty or null");
					throwException = true;
				}

				if (null == sonarAppName || sonarAppName.isEmpty()) {
					listener.getLogger().println("sonar.app.name is empty or null");
					throwException = true;
				}

				if (null == codeCoverageGoal || codeCoverageGoal.isEmpty()) {
					listener.getLogger().println("sonar.codecoverage.goal is empty or null");
					throwException = true;
				}

				if (null == codeCoverageBreakLevel || codeCoverageBreakLevel.isEmpty()) {
					listener.getLogger().println("sonar.codecoverage.breaklevel is empty or null");
					throwException = true;
				}

				if (null == sonarResourceKey || sonarResourceKey.isEmpty()) {
					listener.getLogger().println("sonar.resource.key is empty or null");
					throwException = true;
				}

				if (throwException) {
					throw new InterruptedException(
							"Aborting the build, no properties were set to utilize quality gates");
				}
			} else {
				if (null == sonarResourceKey || sonarResourceKey.isEmpty()) {
					throw new InterruptedException(
							"Aborting the build, sonar.resource.key must be set to associate default quality gate");
				}
				List<Project> projects;
				try {
					ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
					projects = retrieveProjectsForKey(projectClient, sonarResourceKey);
					if (projects.size() > 1) {
						throw new InterruptedException("Please be mores specific when defining sonar.resource.key");
					}

					if (projects.size() == 0) {
						throw new InterruptedException("Did not find any projects for that resource key");
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
				if (null != sonarTeamName && null != sonarAppName) {
					listener.getLogger().println("Gathering Sonar Projects");
					ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
					List<Project> projects = retrieveProjectsForKey(projectClient, sonarResourceKey);
					if (projects.size() > 1) {
						throw new InterruptedException("Please be mores specific when defining sonar.resource.key");
					}

					if (projects.size() == 0) {
						throw new InterruptedException("Did not find any projects for that resource key");
					}

					QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
							getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

					listener.getLogger().println("Looking for matching quality gate to identify with project");
					QualityGate qualityGateToUse = findQualityGate(qualityGateClient,
							sonarTeamName + "-" + sonarAppName);

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
						QualityGate qualityGate = qualityGateClient
								.createQualityGate(sonarTeamName + "-" + sonarAppName);
						qualityGateToAssociate = qualityGate;

						listener.getLogger().println("Creating Quality Gate Condition");
						createQualityGateCondition(qualityGateClient, codeCoverageBreakLevel, codeCoverageGoal,
								qualityGate);
					}

					listener.getLogger().println("Associate Quality Gate to Project");
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

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
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
			return "Gate Keepah";
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
