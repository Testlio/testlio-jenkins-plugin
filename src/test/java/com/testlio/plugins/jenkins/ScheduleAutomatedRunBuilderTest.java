package com.testlio.plugins.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScheduleAutomatedRunBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String testConfig = "{}";
    final String projectConfig = "{}";
    final String testArgs = "--spec";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ScheduleAutomatedRunBuilder(testConfig, projectConfig, testArgs));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new ScheduleAutomatedRunBuilder(testConfig, projectConfig, testArgs), project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScheduleAutomatedRunBuilder builder = new ScheduleAutomatedRunBuilder(testConfig, projectConfig, testArgs);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Plugin works!", build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = String.format("node {\n"
                + "  schedule-automated-run '%s' '%s' '%s'\n"
                + "}", testConfig, projectConfig, testArgs);
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Script works!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}