package com.testlio.plugins.jenkins;

import com.testlio.plugins.jenkins.dto.AttachmentDTO;
import com.testlio.plugins.jenkins.dto.AutomatedRunPlanDTO;
import com.testlio.plugins.jenkins.dto.BrowsersDTO;
import com.testlio.plugins.jenkins.dto.DevicePoolDTO;
import com.testlio.plugins.jenkins.dto.RunConfigurationDTO;
import com.testlio.plugins.jenkins.dto.RunDTO;
import com.testlio.plugins.jenkins.dto.UploadDTO;
import com.testlio.plugins.jenkins.enums.AppTypeEnum;
import com.testlio.plugins.jenkins.enums.BrowserNameEnum;
import com.testlio.plugins.jenkins.enums.BrowserTestTypeEnum;
import com.testlio.plugins.jenkins.enums.DeviceFormFactorEnum;
import com.testlio.plugins.jenkins.enums.DeviceTestTypeEnum;
import com.testlio.plugins.jenkins.enums.NetworkProfileEnum;
import com.testlio.plugins.jenkins.enums.PlatformNameEnum;
import com.testlio.plugins.jenkins.models.BrowserSelector;
import com.testlio.plugins.jenkins.models.DeviceSelector;
import com.testlio.plugins.jenkins.models.DeviceState;
import com.testlio.plugins.jenkins.utils.RestClient;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Component
public class ScheduleAutomatedRunBuilder extends Builder implements SimpleBuildStep {


  public final Integer projectId;

  public final String testRunCollectionGuid;

  public final String automatedTestRunCollectionGuid;

  public final AppTypeEnum appType;

  public final String testPackageURI;

  public final Boolean videoCapture;

  public final Integer select;

  public final Boolean waitForResults;
  private static String BASE_URI = "http://local.testlio:8181";

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
  public ScheduleAutomatedRunBuilder(Integer projectId, String testRunCollectionGuid, String automatedTestRunCollectionGuid, AppTypeEnum appType, String testPackageURI, Boolean videoCapture, Integer select, Boolean waitForResults) {
    this.projectId = projectId;
    this.testRunCollectionGuid = testRunCollectionGuid;
    this.automatedTestRunCollectionGuid = automatedTestRunCollectionGuid;
    this.appType = appType;
    this.testPackageURI = testPackageURI;
    this.videoCapture = videoCapture;
    this.select = select;
    this.waitForResults = waitForResults;
  }


  @SneakyThrows
  @Override
  public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
          throws InterruptedException, IOException {
    listener.getLogger().println(this);

    if(StringUtils.isBlank(env.get("accessToken"))){
      throw new Error("Access token is missing, please configure it in your global configuration");
    }

    RestClient restClient = new RestClient(env.get("accessToken"), listener);

    RunConfigurationAction config = new RunConfigurationAction(
            projectId,
            testRunCollectionGuid,
            automatedTestRunCollectionGuid,
            appType,
            testPackageURI,
            videoCapture,
            select,
            waitForResults
    );

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
    if (isBrowser()) {
      JSONObject request = new JSONObject();
      request.put("browserNames", config.getBrowserSelector().getBrowserNames());
      request.put("platformNames", config.getBrowserSelector().getPlatformNames());
      request.put("versions", config.getBrowserSelector().getBrowserVersions());

      browsersDTO = (BrowsersDTO) restClient.post("/automated-test-run/v1/browsers/search", request, BrowsersDTO.class);
    }

    listener.getLogger().println("Step 1: Create a new automated run");
    JSONObject createRunRequest = new JSONObject();
    createRunRequest.put("startAt", Instant.now().toString());
    createRunRequest.put("projectId", projectId);
    createRunRequest.put("type", "Automated");
    createRunRequest.put("sourceType", "testlio_cli");

    RunDTO automatedRun = (RunDTO) restClient.post("/test-run/v1/collections/" + testRunCollectionGuid + "/runs", createRunRequest, RunDTO.class);
    listener.getLogger().println("Step 1.1: Update automated run name");
    JSONObject updateRunRequest = new JSONObject();
    updateRunRequest.put("name", "JENKINS : " + automatedRun.getName());
    automatedRun = (RunDTO) restClient.put(automatedRun.getHref(), updateRunRequest, RunDTO.class);


    listener.getLogger().println("Step 2: Create and Update automated run configuration");
    listener.getLogger().println("Step 2.1: Search for automated run configuration");
    JSONObject searchRunConfigurationRequest = new JSONObject();
    searchRunConfigurationRequest.put("runHrefs", Collections.singletonList(automatedRun.getHref()));
    RunConfigurationDTO runConfigurationDTO = (RunConfigurationDTO) restClient.post("/automated-test-run/v1/collections/" + automatedTestRunCollectionGuid + "/search/run-configurations", searchRunConfigurationRequest, RunConfigurationDTO.class);


    JSONObject updateRunConfigRequest = new JSONObject();
    updateRunConfigRequest.put("deviceTimeoutInMinutes", timeoutBrowsers);
    updateRunConfigRequest.put("deviceTestType", browserTestType);
    if (StringUtils.isNotBlank(testArgs)) {
      updateRunConfigRequest.put("executionConfiguration", new JSONObject().put("testArgs", testArgs));
    }
    updateRunConfigRequest.put("deviceAppType", appType.getValue());
    updateRunConfigRequest.put("type", isBrowser() ? "BROWSER" : "DEVICE");
    restClient.put(runConfigurationDTO.getData().get(0).getHref(), updateRunConfigRequest, null);

    listener.getLogger().println("Step 3: Create automated run plan");
    AutomatedRunPlanDTO automatedRunPlanDTO = (AutomatedRunPlanDTO) restClient.post(automatedRun.getPlans().getHref(), new JSONObject(), AutomatedRunPlanDTO.class);

    listener.getLogger().println("Skipping the step as no build is required for Web testing");

    listener.getLogger().println("Step 5: Upload test package");
    listener.getLogger().println("Step 5.1: Get upload URL");
    JSONObject uploadServiceRequest = new JSONObject();
    uploadServiceRequest.put("prefix", "automated-run-attachment");
    UploadDTO uploadDTO = (UploadDTO) restClient.post("/upload/v1/files", uploadServiceRequest, UploadDTO.class);

    listener.getLogger().println("Step 5.2: Upload test package");
    listener.getLogger().println("Uploading test package to AWS");


    RestTemplate restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    restTemplate.setRequestFactory(requestFactory);

    RequestCallback requestCallback = request -> request
            .getHeaders()
            .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));


    final String[] filename = new String[1];
    File file = restTemplate.execute(testPackageURI, HttpMethod.GET, requestCallback, clientHttpResponse -> {
      listener.getLogger().println(clientHttpResponse.getHeaders());
      filename[0] = clientHttpResponse.getHeaders().getContentDisposition().getFilename();
      File ret = File.createTempFile("testPackage", "tmp");
      StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
      return ret;
    });


    file.deleteOnExit();

    HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(new FileSystemResource(file));
    ResponseEntity e = restTemplate.exchange(uploadDTO.getPut().getHref().toURI(), HttpMethod.PUT, requestEntity, Map.class);


    JSONObject createAttachmentRequest = new JSONObject();
    createAttachmentRequest.put("name", filename[0] != null ? filename[0] : "testPackage.zip");
    createAttachmentRequest.put("fileType", "application/zip");
    createAttachmentRequest.put("attachmentType", "TestPackage");
    createAttachmentRequest.put("url", uploadDTO.getGet().getHref().toURI().toString());
    createAttachmentRequest.put("size", file.length());
    AttachmentDTO attachmentDTO = (AttachmentDTO) restClient.post("/automated-test-run/v1/collections/" + automatedTestRunCollectionGuid + "/attachments", createAttachmentRequest, AttachmentDTO.class);


    listener.getLogger().println("Step 5.4: Attach test package attachment to plan");
    JSONObject attachRequest = new JSONObject();
    attachRequest.put("attachmentHref", attachmentDTO.getHref());
    restClient.post(automatedRunPlanDTO.getHref() + "/attachments", attachRequest, null);


    listener.getLogger().println("Step 6: Devices");
    listener.getLogger().println("Step 6.1: Create Device pool");
    JSONObject createDevicePool = new JSONObject();
    createDevicePool.put("name", "Automated device");
    createDevicePool.put("hidden", true);
    createDevicePool.put("withoutDeviceAssignment", true);
    createDevicePool.put("isGroup", false);
    createDevicePool.put("isMultipleDevices", false);
    DevicePoolDTO devicePoolDTO = (DevicePoolDTO) restClient.post("/test-run/v1/collections/" + testRunCollectionGuid + "/device-pools", createDevicePool, DevicePoolDTO.class);

    final String devicePoolFilterHref = devicePoolDTO.getHref() + "/filters";
    browsersDTO.getData().stream().limit(select).forEach(b -> {
      JSONObject request = new JSONObject();
      request.put("automatedBrowsers", Collections.singletonList(new JSONObject().put("automatedBrowserGuid", b.getId())));
      restClient.post(devicePoolFilterHref, request, null);
    });

    listener.getLogger().println("Step 6.3: Attaching device pool filter to automated run plan");
    JSONObject request = new JSONObject();
    request.put("jobsCount", 1);
    request.put("devicePoolGuid", devicePoolDTO.getGuid());
    restClient.post(automatedRunPlanDTO.getHref() + "/plan-device-pools", request, null);

    listener.getLogger().println("Step 7. Schedule run! 🚀");
    JSONObject scheduleRunRequest = new JSONObject();
    scheduleRunRequest.put("status", "inProgress");
    scheduleRunRequest.put("projectId", projectId);
    restClient.put(automatedRun.getHref(), scheduleRunRequest, null);

    // TODO Implement automated run scheduling logic here
    listener.getLogger().println("Plugin works!");
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
