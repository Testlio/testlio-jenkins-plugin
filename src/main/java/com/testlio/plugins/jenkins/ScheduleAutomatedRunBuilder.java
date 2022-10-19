package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.dto.BrowsersDTO;
import com.testlio.plugins.jenkins.dto.DevicesDTO;
import com.testlio.plugins.jenkins.dto.FileDTO;
import com.testlio.plugins.jenkins.dto.ResponseHrefDTO;
import com.testlio.plugins.jenkins.dto.RunDTO;
import com.testlio.plugins.jenkins.enums.AppTypeEnum;
import com.testlio.plugins.jenkins.enums.BrowserNameEnum;
import com.testlio.plugins.jenkins.enums.BrowserTestTypeEnum;
import com.testlio.plugins.jenkins.enums.DeviceFormFactorEnum;
import com.testlio.plugins.jenkins.enums.DevicePlatformEnum;
import com.testlio.plugins.jenkins.enums.DeviceTestTypeEnum;
import com.testlio.plugins.jenkins.enums.NetworkProfileEnum;
import com.testlio.plugins.jenkins.enums.PlatformNameEnum;
import com.testlio.plugins.jenkins.models.BrowserSelector;
import com.testlio.plugins.jenkins.models.DeviceSelector;
import com.testlio.plugins.jenkins.models.DeviceState;
import com.testlio.plugins.jenkins.utils.RestClient;
import com.testlio.plugins.jenkins.validation.FieldValidations;
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
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jenkinsci.Symbol;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.io.IOException;

import static com.testlio.plugins.jenkins.helper.ScheduleAutomatedRunHelper.attachDevices;
import static com.testlio.plugins.jenkins.helper.ScheduleAutomatedRunHelper.createAndUpdateAutomatedRunConfiguration;
import static com.testlio.plugins.jenkins.helper.ScheduleAutomatedRunHelper.createNewAutomatedRun;
import static com.testlio.plugins.jenkins.helper.ScheduleAutomatedRunHelper.scheduleAutomatedRun;
import static com.testlio.plugins.jenkins.helper.SearchDevicesHelper.searchBrowsers;
import static com.testlio.plugins.jenkins.helper.SearchDevicesHelper.searchDevices;
import static com.testlio.plugins.jenkins.helper.UploadHelper.uploadBuild;
import static com.testlio.plugins.jenkins.helper.UploadHelper.uploadTestPackage;

@Component
public class ScheduleAutomatedRunBuilder extends Builder implements SimpleBuildStep {


  public final Integer projectId;

  public final String testRunCollectionGuid;

  public final String automatedTestRunCollectionGuid;

  public final AppTypeEnum appType;
  public final DevicePlatformEnum devicePlatformType;

  public final String testPackageURI;

  public final Boolean videoCapture;

  public final Integer select;

  public final Boolean waitForResults;

  // Optional attributes
  @DataBoundSetter
  public String testArgs;

  // Browser-specific attributes
  @DataBoundSetter
  public BrowserTestTypeEnum browserTestType = null;

  @DataBoundSetter
  public BrowserSelector browserSelector = null;

  @DataBoundSetter
  public Integer timeoutBrowsers = null;

  // Device-specific attributes
  @DataBoundSetter
  public String appBuildURI = null;

  @DataBoundSetter
  public DeviceTestTypeEnum deviceTestType = null;

  @DataBoundSetter
  public DeviceSelector deviceSelector = null;

  @DataBoundSetter
  public DeviceState deviceState = null;

  @DataBoundSetter
  public Integer timeoutDevices = null;

  @DataBoundConstructor
  public ScheduleAutomatedRunBuilder(Integer projectId, String testRunCollectionGuid, String automatedTestRunCollectionGuid, AppTypeEnum appType, String testPackageURI, Boolean videoCapture, Integer select, Boolean waitForResults, DevicePlatformEnum devicePlatformType) {
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

  private boolean isIOS() {
    return devicePlatformType != null && devicePlatformType.equals(DevicePlatformEnum.IOS);
  }

  private String getAppType() {
    return appType.equals(AppTypeEnum.BROWSER) ? "BROWSER" : "DEVICE";
  }

  @SneakyThrows
  @Override
  public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
          throws InterruptedException, IOException {
    listener.getLogger().println(this);

    RunConfigurationAction config = new RunConfigurationAction(
            projectId,
            testRunCollectionGuid,
            automatedTestRunCollectionGuid,
            appType,
            testPackageURI,
            videoCapture,
            select,
            waitForResults,
            devicePlatformType
    );

    final String accessToken = env.get("accessToken");

    FieldValidations.validateAccessToken(accessToken);
    final RestClient restClient = new RestClient(accessToken, listener);

    final FileDTO testPackageFile = FieldValidations.downloadFileFromURL(testPackageURI);

    FileDTO buildFile = null;
    if (isTestTypeDevice()) {
      buildFile = FieldValidations.downloadFileFromURL(appBuildURI);
    }

    if (isBrowser()) {
      config.setBrowserTestType(browserTestType);
      config.setBrowserSelector(browserSelector);
      config.setTimeoutBrowsers(timeoutBrowsers);
    }
    else {
      config.setAppBuildURI(appBuildURI);
      config.setDeviceTestType(deviceTestType);
      config.setDeviceSelector(deviceSelector);
      config.setDeviceState(deviceState);
      config.setTimeoutDevices(timeoutDevices);
    }

    if (testArgs != null) {
      config.setTestArgs(testArgs);
    }

    run.addAction(config);

    listener.getLogger().println("Starting process to create an automation run...");
    BrowsersDTO browsersDTO = null;
    DevicesDTO devicesDTO = null;
    if (isBrowser()) {
      listener.getLogger().println("Step 0: Search browsers");
      browsersDTO = searchBrowsers(config, restClient);
    }
    else {
      listener.getLogger().println("Step 0: Search devices");
      devicesDTO = searchDevices(config, restClient);
    }

    listener.getLogger().println("Step 1: Create a new automated run");
    RunDTO automatedRun = createNewAutomatedRun(listener, restClient, config);

    listener.getLogger().println("Step 2: Create and Update automated run configuration");
    createAndUpdateAutomatedRunConfiguration(listener, restClient, automatedRun, config, isBrowser());

    listener.getLogger().println("Step 3: Create automated run plan");
    ResponseHrefDTO automatedTestPlan = (ResponseHrefDTO) restClient.post(automatedRun.getPlans().getHref(), new JSONObject(), ResponseHrefDTO.class);

    listener.getLogger().println("Step 4: Upload test build");
    uploadBuild(listener, restClient, buildFile, automatedTestPlan, isTestTypeDevice(), config, isIOS());

    listener.getLogger().println("Step 5: Upload test package");
    uploadTestPackage(listener, restClient, testPackageFile, automatedTestPlan, config);

    listener.getLogger().println("Step 6: Devices");
    attachDevices(listener, restClient, browsersDTO, devicesDTO, automatedTestPlan, config, isBrowser());

    listener.getLogger().println("Step 7. Schedule run! 🚀");
    scheduleAutomatedRun(listener, restClient, automatedRun, config);

    listener.getLogger().println("Step 8. Wait for results");
  }

  private boolean isTestTypeDevice() {
    return StringUtils.equals(getAppType(), "DEVICE") && !StringUtils.equals(appType.getValue(), "WEB");
  }

  private boolean isBrowser() {
    return appType.equals(AppTypeEnum.BROWSER);
  }

  @Symbol("schedule-automated-run")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    public ListBoxModel getBrowserNames() {
      ListBoxModel items = new ListBoxModel();
      for (BrowserNameEnum browserNameEnum : BrowserNameEnum.values()) {
        items.add(browserNameEnum.getName(), browserNameEnum.toString());
      }
      return items;
    }

    public ListBoxModel getBrowserVersions() {
      ListBoxModel items = new ListBoxModel();
      items.add("latest", "latest");
      items.add("latest-1", "latest-1");
      items.add("latest-2", "latest-2");
      return items;
    }

    public ListBoxModel getPlatformNames() {
      ListBoxModel items = new ListBoxModel();
      for (PlatformNameEnum platformNameEnum : PlatformNameEnum.values()) {
        items.add(platformNameEnum.getName(), platformNameEnum.toString());
      }
      return items;
    }

    public ListBoxModel getDeviceFormFactors() {
      ListBoxModel items = new ListBoxModel();
      for (DeviceFormFactorEnum deviceFormFactorEnum : DeviceFormFactorEnum.values()) {
        items.add(deviceFormFactorEnum.getName(), deviceFormFactorEnum.toString());
      }
      return items;
    }

    public ListBoxModel getAppTypes() {
      ListBoxModel items = new ListBoxModel();
      for (AppTypeEnum appTypeEnum : AppTypeEnum.values()) {
        items.add(appTypeEnum.getName(), appTypeEnum.toString());
      }
      return items;
    }

    public ListBoxModel getDevicePlatformTypes() {
      ListBoxModel items = new ListBoxModel();
      for (DevicePlatformEnum devicePlatformEnum : DevicePlatformEnum.values()) {
        items.add(devicePlatformEnum.getName(), devicePlatformEnum.toString());
      }
      return items;
    }

    public ListBoxModel doFillBrowserTestTypeItems() {
      ListBoxModel items = new ListBoxModel();
      for (BrowserTestTypeEnum browserTestTypeEnum : BrowserTestTypeEnum.values()) {
        items.add(browserTestTypeEnum.getName(), browserTestTypeEnum.toString());
      }
      return items;
    }

    public ListBoxModel doFillDeviceTestTypeItems() {
      ListBoxModel items = new ListBoxModel();
      for (DeviceTestTypeEnum deviceTestTypeEnum : DeviceTestTypeEnum.values()) {
        items.add(deviceTestTypeEnum.getName(), deviceTestTypeEnum.toString());
      }
      return items;
    }

    public ListBoxModel doFillNetworkProfileItems() {
      ListBoxModel items = new ListBoxModel();
      for (NetworkProfileEnum networkProfileEnum : NetworkProfileEnum.values()) {
        items.add(networkProfileEnum.getName(), networkProfileEnum.toString());
      }
      return items;
    }

    public FormValidation doCheckProjectId(@QueryParameter Integer value)
            throws IOException, ServletException {
      if (value == null) {
        return FormValidation.error("Workspace ID cannot be empty");
      }
      if (value <= 0) {
        return FormValidation.error("Workspace ID cannot be negative or 0");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckTestRunCollectionGuid(@QueryParameter String value)
            throws IOException, ServletException {
      if (value.isEmpty()) {
        return FormValidation.error("Test run collection GUID should be provided");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckAutomatedTestRunCollectionGuid(@QueryParameter String value)
            throws IOException, ServletException {
      if (value.isEmpty()) {
        return FormValidation.error("Automated test run collection GUID should be provided");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckTimeout(@QueryParameter Integer value)
            throws IOException, ServletException {
      if (value == null) {
        return FormValidation.error("Timeout cannot be empty");
      }
      if (value < 0) {
        return FormValidation.error("Timeout cannot be negative");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckSelect(@QueryParameter Integer value)
            throws IOException, ServletException {
      if (value == null) {
        return FormValidation.error("Number of devices cannot be empty");
      }
      if (value < 1) {
        return FormValidation.error("Number of devices cannot be less than 1");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckAppBuildURI(@QueryParameter String value, @QueryParameter String appType)
            throws IOException, ServletException {
      if (appType == null || !AppTypeEnum.valueOf(appType).equals(AppTypeEnum.DEVICE_APP)) {
        return FormValidation.ok();
      }

      if (value.isEmpty()) {
        return FormValidation.error("App build URL should be provided");
      }

      String[] schemes = {"http", "https"};
      UrlValidator urlValidator = new UrlValidator(schemes);
      if (!urlValidator.isValid(value)) {
        return FormValidation.error("Please provide URL in a correct format");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckTestPackageURI(@QueryParameter String value)
            throws IOException, ServletException {
      if (value.isEmpty()) {
        return FormValidation.error("Test package URL should be provided");
      }

      String[] schemes = {"http", "https"};
      UrlValidator urlValidator = new UrlValidator(schemes);
      if (!urlValidator.isValid(value)) {
        return FormValidation.error("Please provide URL in a correct format");
      }

      return FormValidation.ok();
    }

    public FormValidation doCheckTestArgs(@QueryParameter String value)
            throws IOException, ServletException {
      if (value.isEmpty()) {
        return FormValidation.ok();
      }

      if (!value.matches("^[\\w*'\"\\s/.-]+$")) {
        return FormValidation.error("Test arguments contain restricted symbols");
      }

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

  @Override
  public String toString() {
    return "ScheduleAutomatedRunBuilder{" +
            "projectId=" + projectId +
            ", testRunCollectionGuid='" + testRunCollectionGuid + '\'' +
            ", automatedTestRunCollectionGuid='" + automatedTestRunCollectionGuid + '\'' +
            ", appType=" + appType +
            ", testPackageURI='" + testPackageURI + '\'' +
            ", videoCapture=" + videoCapture +
            ", select=" + select +
            ", waitForResults=" + waitForResults +
            ", testArgs='" + testArgs + '\'' +
            ", browserTestType=" + browserTestType +
            ", browserSelector=" + browserSelector +
            ", timeoutBrowsers=" + timeoutBrowsers +
            ", appBuildURI='" + appBuildURI + '\'' +
            ", deviceTestType=" + deviceTestType +
            ", deviceSelector=" + deviceSelector +
            ", deviceState=" + deviceState +
            ", timeoutDevices=" + timeoutDevices +
            '}';
  }
}
