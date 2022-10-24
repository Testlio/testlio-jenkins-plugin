package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.enums.AppTypeEnum;
import com.testlio.plugins.jenkins.enums.BrowserTestTypeEnum;
import com.testlio.plugins.jenkins.enums.DevicePlatformEnum;
import com.testlio.plugins.jenkins.enums.DeviceTestTypeEnum;
import com.testlio.plugins.jenkins.models.BrowserSelector;
import com.testlio.plugins.jenkins.models.DeviceSelector;
import com.testlio.plugins.jenkins.models.DeviceState;
import hudson.model.Run;
import jenkins.model.RunAction2;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunConfigurationAction implements RunAction2 {

  private transient Run run;

  private final Integer projectId;

  private final String testRunCollectionGuid;

  private final String automatedTestRunCollectionGuid;

  private final AppTypeEnum appType;

  private final DevicePlatformEnum devicePlatformType;

  private final String testPackageURI;

  private final Boolean videoCapture;

  private final Integer select;

  private final Boolean waitForResults;

  private BrowserTestTypeEnum browserTestType = null;

  private DeviceTestTypeEnum deviceTestType = null;

  private DeviceSelector deviceSelector = null;

  private BrowserSelector browserSelector = null;

  private DeviceState deviceState = null;

  private String appBuildURI = null;

  private String testArgs = null;

  public Integer timeoutBrowsers = null;

  public Integer timeoutDevices = null;

  public RunConfigurationAction(Integer projectId, String testRunCollectionGuid, String automatedTestRunCollectionGuid, AppTypeEnum appType, String testPackageURI, Boolean videoCapture, Integer select, Boolean waitForResults, DevicePlatformEnum devicePlatformType) {
    this.projectId = projectId;
    this.testRunCollectionGuid = testRunCollectionGuid;
    this.automatedTestRunCollectionGuid = automatedTestRunCollectionGuid;
    this.appType = appType;
    this.testPackageURI = testPackageURI;
    this.videoCapture = videoCapture;
    this.select = select;
    this.waitForResults = waitForResults;
    this.devicePlatformType = devicePlatformType;
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
