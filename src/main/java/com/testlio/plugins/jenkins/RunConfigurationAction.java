package com.testlio.plugins.jenkins;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class RunConfigurationAction implements RunAction2 {

    private transient Run run;

    private final String testConfig;
    private final String projectConfig;
    private final String testArgs;

    public RunConfigurationAction(String testConfig, String projectConfig, String testArgs) {
        this.testConfig = testConfig;
        this.projectConfig = projectConfig;
        this.testArgs = testArgs;
    }

    public Run getRun() {
        return run;
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
    public String getIconFileName() {
        return "document.png";
    }

    @Override
    public String getDisplayName() {
        return "Run Configuration";
    }

    @Override
    public String getUrlName() {
        return "config";
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }
}
