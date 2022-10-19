package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.enums.*;
import com.testlio.plugins.jenkins.models.*;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScheduleAutomatedRunBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    // Mandatory attributes
    final Integer projectId = 1234;
    final String testRunCollectionGuid = "test-run-collection-guid";
    final String automatedTestRunCollectionGuid = "automated-test-run-collection-guid";
    final AppTypeEnum appType = AppTypeEnum.DEVICE_APP;
    final String testPackageURI = "https://test.com/package";
    final Boolean videoCapture = true;
    final Integer select = 1;
    final Boolean waitForResults = true;

    // Optional attributes (default values)
    final BrowserSelector browserSelector = new BrowserSelector(
            new BrowserNameEnum[]{},
            new String[]{},
            new PlatformNameEnum[]{}
    );

    final DeviceSelector deviceSelector = new DeviceSelector(
            "",
            "",
            "",
            new DeviceFormFactorEnum[]{}
    );
    final DeviceState deviceState = new DeviceState(
            new DeviceRadios(false, false, false, false),
            new DeviceLocation(null, null),
            "",
            "",
            "",
        NetworkProfileEnum.FULL
    );
    final BrowserTestTypeEnum browserTestType = BrowserTestTypeEnum.SELENIUM_JAVA_TESTNG;
    final DeviceTestTypeEnum deviceTestType = DeviceTestTypeEnum.APPIUM_JAVA_TESTNG;
    final String appBuildURI = "";
    final String testArgs = "";

    final Integer timeoutBrowsers = 120;
    final Integer timeoutDevices = 15;

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(this.createBuilderMock());
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(this.createBuilderMock(), project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        ScheduleAutomatedRunBuilder builder = this.createBuilderMock();
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Plugin works!", build);
    }

    // TODO Commented out test for scripted pipeline until this functionality will be enabled
    /*@Test
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
    }*/

    private ScheduleAutomatedRunBuilder createBuilderMock() {
//        ScheduleAutomatedRunBuilder scheduleAutomatedRunBuilder = new ScheduleAutomatedRunBuilder(
//                projectId, testRunCollectionGuid, automatedTestRunCollectionGuid,
//                appType, testPackageURI, videoCapture, select, waitForResults
//        );
//        scheduleAutomatedRunBuilder.browserSelector = browserSelector;
//        scheduleAutomatedRunBuilder.deviceSelector = deviceSelector;
//        scheduleAutomatedRunBuilder.deviceState = deviceState;
//        scheduleAutomatedRunBuilder.browserTestType = browserTestType;
//        scheduleAutomatedRunBuilder.deviceTestType = deviceTestType;
//        scheduleAutomatedRunBuilder.appBuildURI = appBuildURI;
//        scheduleAutomatedRunBuilder.testArgs = testArgs;
//        scheduleAutomatedRunBuilder.timeoutBrowsers = timeoutBrowsers;
//        scheduleAutomatedRunBuilder.timeoutDevices = timeoutDevices;
//        return scheduleAutomatedRunBuilder;
        return null;
    }

}