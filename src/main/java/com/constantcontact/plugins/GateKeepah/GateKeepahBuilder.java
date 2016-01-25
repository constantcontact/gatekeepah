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

	public Properties readPropertiesFile(final FilePath workspace, String propertiesFileName,
			final String additionalProperties, final TaskListener listener) throws InterruptedException {
		Properties props;

		try {
			if (null == propertiesFileName) {
				throw new Exception("No file name found, creating our own");
			}
			InputStream input = new FileInputStream(workspace.absolutize() + File.separator + propertiesFileName);
			props = new Properties();
			props.load(input);
			if (null != additionalProperties) {
				for (String line : additionalProperties.split(System.getProperty("line.separator"))) {
					String[] newProperty = line.split("=");
					props.setProperty(newProperty[0], newProperty[1]);
				}
				try {
					File file = new File(workspace.absolutize() + File.separator + propertiesFileName);
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} catch (Exception e) {

			props = new Properties();
			if (null != additionalProperties) {
				for (String line : additionalProperties.split(System.getProperty("line.separator"))) {
					if (line.length() < 1) {
						listener.getLogger().println(
								"A properties file must be in the right place or properties added to the text area");
						throw new InterruptedException();
					}
					listener.getLogger().println("Creating Property for " + line);
					String[] newProperty = line.split("=");

					try {
						props.setProperty(newProperty[0], newProperty[1]);
					} catch (ArrayIndexOutOfBoundsException oobe) {
						listener.getLogger().println(
								"Property could not be set for " + line + " because it was missing its value");
						throw new InterruptedException();
					}
				}
				try {
					File file = new File(workspace.absolutize() + File.separator + "gatekeepah.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					props.store(fileOut, "GateKeepah Properties");
					fileOut.close();
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

		}
		return props;
	}

	public List<Project> gatherProjectsForKey(final String sonarResourceKey) throws Exception {
		ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
				getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
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

	public void updateQualityCondition(final QualityGateClient qualityGateClient,
			final QualityGateCondition conditionToUpdate, final String codeCoverageBreakLevel,
			final String codeCoverageGoal) throws Exception {
		conditionToUpdate.setError(Integer.parseInt(codeCoverageBreakLevel));
		conditionToUpdate.setWarning(Integer.parseInt(codeCoverageGoal));
		conditionToUpdate.setOp("LT");
		qualityGateClient.updateQualityGateCondition(conditionToUpdate);
	}

	public void createQualityGateCondition(final QualityGateClient qualityGateClient,
			final String codeCoverageBreakLevel, final String codeCoverageGoal, final QualityGate qualityGateToUse)
					throws Exception {
		QualityGateCondition condition = new QualityGateCondition();
		condition.setError(Integer.parseInt(codeCoverageBreakLevel));
		condition.setWarning(Integer.parseInt(codeCoverageGoal));
		condition.setOp("LT");
		condition.setGateId(qualityGateToUse.getId());
		condition.setMetric("coverage");
		qualityGateClient.createQualityGateCondition(condition);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException {

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
			throw new InterruptedException();
		}

		Properties props = readPropertiesFile(workspace, propertiesFileName, additionalProperties, listener);

		// Ensure all required properties are set
		final String sonarTeamName = props.getProperty("sonar.team.name");
		final String sonarAppName = props.getProperty("sonar.app.name");
		final String codeCoverageGoal = props.getProperty("sonar.codecoverage.goal");
		final String codeCoverageBreakLevel = props.getProperty("sonar.codecoverage.breaklevel");
		final String sonarResourceKey = props.getProperty("sonar.resource.key");
		final String useDefaultGateWay = props.getProperty("sonar.useDefault.qualitygate");

		boolean throwException = false;
		if (null == useDefaultGateWay || useDefaultGateWay.isEmpty()) {
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
				listener.getLogger().println("Aborting the build, no properties were set to utilize quality gates");
				throw new InterruptedException();
			}
		} else {
			if (null == sonarResourceKey || sonarResourceKey.isEmpty()) {
				listener.getLogger().println("sonar.resource.key is empty or null");
				throwException = true;
			}

			if (throwException) {
				listener.getLogger().println(
						"Aborting the build, sonar.resource.key must be set to associate default quality gate");
				throw new InterruptedException();
			}
			List<Project> projects;
			try {
				projects = gatherProjectsForKey(sonarResourceKey);
				if (projects.size() > 1) {
					listener.getLogger().println("Please be mores specific when defining sonar.resource.key");
					throw new InterruptedException();
				}

				if (projects.size() == 0) {
					listener.getLogger().println("Did not find any projects for that resource key");
					throw new InterruptedException();
				}

			} catch (Exception e) {
				listener.getLogger().println("Encountered an issue finding a project to associate");
				throw new InterruptedException();
			}
			try {
				QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
						getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

				listener.getLogger().println("Looking for matching quality gate to identify with project");
				QualityGate qualityGateToUse = findQualityGate(qualityGateClient,
						getDescriptor().getDefaultQualityGateName());

				if (null != qualityGateToUse) {
					listener.getLogger().println("Retrieving Quality Gate Details");
					QualityGate qualityGate = qualityGateClient.retrieveQualityGateDetails(qualityGateToUse.getId());
					qualityGateClient.associateQualityGate(qualityGate.getId(),
							Integer.parseInt(projects.get(0).getId()));

				} else {
					listener.getLogger().println("Encountered an issue locating quality gate details");
					throw new InterruptedException();
				}

			} catch (Exception e) {
				listener.getLogger().println("Encountered an issue locating quality gate details");
				throw new InterruptedException();
			}
			return;
		}

		try {
			listener.getLogger().println("Gathering Sonar Projects");
			List<Project> projects = gatherProjectsForKey(sonarResourceKey);
			if (projects.size() > 1) {
				listener.getLogger().println("Please be mores specific when defining sonar.resource.key");
				throw new InterruptedException();
			}

			if (projects.size() == 0) {
				listener.getLogger().println("Did not find any projects for that resource key");
				throw new InterruptedException();
			}

			QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
					getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

			listener.getLogger().println("Looking for matching quality gate to identify with project");
			QualityGate qualityGateToUse = findQualityGate(qualityGateClient, sonarTeamName + "-" + sonarAppName);

			QualityGate qualityGateToAssociate = null;
			if (null != qualityGateToUse) {
				listener.getLogger().println("Retrieving Quality Gate Details");
				QualityGate qualityGate = qualityGateClient.retrieveQualityGateDetails(qualityGateToUse.getId());

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
				QualityGate qualityGate = qualityGateClient.createQualityGate(sonarTeamName + "-" + sonarAppName);
				qualityGateToAssociate = qualityGate;

				listener.getLogger().println("Creating Quality Gate Condition");
				createQualityGateCondition(qualityGateClient, codeCoverageBreakLevel, codeCoverageGoal, qualityGate);
			}

			listener.getLogger().println("Associate Quality Gate to Project");
			qualityGateClient.associateQualityGate(qualityGateToAssociate.getId(),
					Integer.parseInt(projects.get(0).getId()));

		} catch (Exception e) {
			listener.getLogger().println(e.getMessage());
			listener.getLogger().println(e.getLocalizedMessage());
			throw new InterruptedException(
					"Could not update quality gate please check your properties file for errors ");
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
