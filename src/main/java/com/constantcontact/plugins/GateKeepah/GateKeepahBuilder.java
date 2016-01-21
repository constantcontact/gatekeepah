package com.constantcontact.plugins.GateKeepah;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(GateKeepahBuilder.class);

	@DataBoundConstructor
	public GateKeepahBuilder(String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getPropertiesFileName() {
		return propertiesFileName;
	}

	public Properties readPropertiesFile(final FilePath workspace, final String propertiesFileName)
			throws InterruptedException {
		Properties props;
		try {
			InputStream input = new FileInputStream(workspace.absolutize() + "/" + propertiesFileName);
			props = new Properties();
			props.load(input);
		} catch (Exception e) {
			throw new InterruptedException("Could not read properties file, aborting job");
		}
		return props;
	}

	public List<Project> gatherProjectsForKey(final String sonarResourceKey) throws Exception {
		ProjectClient projectClient = new ProjectClient(getDescriptor().getSonarHost(),
				getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());
		List<Project> projects = projectClient.retrieveIndexOfProjects(sonarResourceKey);

		return projects;
	}

	public QualityGate findQualityGate(final QualityGateClient qualityGateClient, final String sonarTeamName,
			final String sonarAppName) throws Exception {
		QualityGateListCollection qualityGates = qualityGateClient.retrieveQualityGateList();
		QualityGate qualityGateToUse = null;
		for (QualityGate qualityGate : qualityGates.getQualitygates()) {
			if (qualityGate.getName().equalsIgnoreCase(sonarTeamName + "-" + sonarAppName)) {
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

		Properties props = readPropertiesFile(workspace, propertiesFileName);

		// Ensure all required properties are set
		final String sonarTeamName = props.getProperty("sonar.team.name");
		final String sonarAppName = props.getProperty("sonar.app.name");
		final String codeCoverageGoal = props.getProperty("sonar.codecoverage.goal");
		final String codeCoverageBreakLevel = props.getProperty("sonar.codecoverage.breaklevel");
		final String sonarResourceKey = props.getProperty("sonar.resource.key");

		try {
			listener.getLogger().println("Gathering Sonar Projects");
			List<Project> projects = gatherProjectsForKey(sonarResourceKey);
			if (projects.size() > 1) {
				listener.getLogger().println("Please be mores specific when defining sonar.resource.key");
				throw new InterruptedException("Please be mores specific when defining sonar.resource.key");
			}

			if (projects.size() == 0) {
				listener.getLogger().println("Did not find any projects for that resource key");
				throw new InterruptedException("Did not find any projects for that resource key");
			}

			listener.getLogger().println("");
			QualityGateClient qualityGateClient = new QualityGateClient(getDescriptor().getSonarHost(),
					getDescriptor().getSonarUserName(), getDescriptor().getSonarPassword());

			listener.getLogger().println("Looking for matching quality gate to identify with project");
			QualityGate qualityGateToUse = findQualityGate(qualityGateClient, sonarTeamName, sonarAppName);

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
				createQualityGateCondition(qualityGateClient, codeCoverageBreakLevel, codeCoverageGoal,
						qualityGate);
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

		public DescriptorImpl() {
			load();
		}

		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set a name");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
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
	}
}
