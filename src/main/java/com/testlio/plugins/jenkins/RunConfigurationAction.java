package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.enums.AppTypeEnum;
import com.testlio.plugins.jenkins.enums.BrowserTestTypeEnum;
import com.testlio.plugins.jenkins.enums.DeviceTestTypeEnum;
import com.testlio.plugins.jenkins.models.BrowserSelector;
import com.testlio.plugins.jenkins.models.DeviceSelector;
import com.testlio.plugins.jenkins.models.DeviceState;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class RunConfigurationAction implements RunAction2 {

    private transient Run run;

    private final Integer projectId;

    private final AppTypeEnum appType;

    private final String testPackageURI;

    private final Boolean videoCapture;

    private final Integer select;

    private final Integer timeout;

    private final Boolean waitForResults;

    private BrowserTestTypeEnum browserTestType = null;

    private DeviceTestTypeEnum deviceTestType = null;

    private DeviceSelector deviceSelector = null;

    private BrowserSelector browserSelector = null;

    private DeviceState deviceState = null;

    private String appBuildURI = null;

    private String testArgs = null;

    public RunConfigurationAction(Integer projectId, AppTypeEnum appType, String testPackageURI, Boolean videoCapture, Integer select, Integer timeout, Boolean waitForResults) {
        this.projectId = projectId;
        this.appType = appType;
        this.testPackageURI = testPackageURI;
        this.videoCapture = videoCapture;
        this.select = select;
        this.timeout = timeout;
        this.waitForResults = waitForResults;
    }

    public Run getRun() {
        return run;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public AppTypeEnum getAppType() {
        return appType;
    }

    public String getTestPackageURI() {
        return testPackageURI;
    }

    public Boolean getVideoCapture() {
        return videoCapture;
    }

    public Integer getSelect() {
        return select;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Boolean getWaitForResults() {
        return waitForResults;
    }

    public BrowserTestTypeEnum getBrowserTestType() {
        return browserTestType;
    }

    public void setBrowserTestType(BrowserTestTypeEnum browserTestType) {
        this.browserTestType = browserTestType;
    }

    public DeviceTestTypeEnum getDeviceTestType() {
        return deviceTestType;
    }

    public void setDeviceTestType(DeviceTestTypeEnum deviceTestType) {
        this.deviceTestType = deviceTestType;
    }

    public DeviceSelector getDeviceSelector() {
        return deviceSelector;
    }

    public void setDeviceSelector(DeviceSelector deviceSelector) {
        this.deviceSelector = deviceSelector;
    }

    public BrowserSelector getBrowserSelector() {
        return browserSelector;
    }

    public void setBrowserSelector(BrowserSelector browserSelector) {
        this.browserSelector = browserSelector;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public String getAppBuildURI() {
        return appBuildURI;
    }

    public void setTestArgs(String testArgs) {
        this.testArgs = testArgs;
    }

    public String getTestArgs() {
        return testArgs;
    }

    public void setAppBuildURI(String appBuildURI) {
        this.appBuildURI = appBuildURI;
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
