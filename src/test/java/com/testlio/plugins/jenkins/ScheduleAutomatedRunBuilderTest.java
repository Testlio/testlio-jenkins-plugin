package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.enums.AppTypeEnum;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.apache.xpath.operations.Bool;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScheduleAutomatedRunBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final Integer projectId = 1234;
    final String testRunCollectionGuid = "test-run-collection-guid";
    final String automatedTestRunCollectionGuid = "automated-test-run-collection-guid";
    final AppTypeEnum appType = AppTypeEnum.DEVICE_APP;
    final String testPackageURI = "https://test.com/package";

    final Boolean videoCapture = true;
    final Integer select = 1;
    final Integer timeout = 900;
    final Boolean waitForResults = true;

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new ScheduleAutomatedRunBuilder(
                projectId, testRunCollectionGuid, automatedTestRunCollectionGuid,
                appType, testPackageURI, videoCapture, select, waitForResults
        ));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new ScheduleAutomatedRunBuilder(
                projectId, testRunCollectionGuid, automatedTestRunCollectionGuid,
                appType, testPackageURI, videoCapture, select, waitForResults
        ), project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScheduleAutomatedRunBuilder builder = new ScheduleAutomatedRunBuilder(
                projectId, testRunCollectionGuid, automatedTestRunCollectionGuid, appType,
                testPackageURI, videoCapture, select, waitForResults
        );
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
                + "  schedule-automated-run '%s' '%s' '%s' '%s' '%s' '%s' '%s' '%s'\n"
                + "}",
                projectId, testRunCollectionGuid, automatedTestRunCollectionGuid,
                appType, testPackageURI, videoCapture, select, waitForResults
        );
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "Script works!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}