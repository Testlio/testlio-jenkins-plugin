package com.testlio.plugins.jenkins;

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
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class ScheduleAutomatedRunBuilder extends Builder implements SimpleBuildStep {

    private final String testConfig;
    private final String projectConfig;
    private final String testArgs;

    @DataBoundConstructor
    public ScheduleAutomatedRunBuilder(String testConfig, String projectConfig, String testArgs) {
        this.testConfig = testConfig;
        this.projectConfig = projectConfig;
        this.testArgs = testArgs;
    }

    public String getTestConfig() {
        return testConfig;
    }

    public String getProjectConfig() {
        return projectConfig;
    }

    public String getTestArgs() {
        return testArgs;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        run.addAction(new RunConfigurationAction(this.testConfig, this.projectConfig, this.testArgs));
        // TODO Implement automated run scheduling logic here
        listener.getLogger().println("Plugin works!");
    }

    @Symbol("schedule-automated-run")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckTestConfig(@QueryParameter String value)
                throws IOException, ServletException {
            // TODO Validate test config here
            return FormValidation.ok();
        }

        public FormValidation doCheckProjectConfig(@QueryParameter String value)
                throws IOException, ServletException {
            // TODO Validate project config here
            return FormValidation.ok();
        }

        public FormValidation doCheckTestArgs(@QueryParameter String value)
                throws IOException, ServletException {
            // TODO Validate test args here
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}
