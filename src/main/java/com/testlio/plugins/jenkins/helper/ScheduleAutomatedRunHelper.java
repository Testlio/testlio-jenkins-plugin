package com.testlio.plugins.jenkins.helper;

import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.BrowsersDTO;
import com.testlio.plugins.jenkins.dto.DevicePoolDTO;
import com.testlio.plugins.jenkins.dto.DevicesDTO;
import com.testlio.plugins.jenkins.dto.ProjectDTO;
import com.testlio.plugins.jenkins.dto.ResponseHrefDTO;
import com.testlio.plugins.jenkins.dto.RunConfigurationDTO;
import com.testlio.plugins.jenkins.dto.RunDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Collections;

public class ScheduleAutomatedRunHelper {

  final static String APP_URL = "https://app.testlio.com/tmt/project";

  public static RunDTO createNewAutomatedRun(TaskListener listener, RestClient restClient, RunConfigurationAction config) {
    JSONObject createRunRequest = new JSONObject();
    createRunRequest.put("startAt", Instant.now().toString());
    createRunRequest.put("projectId", config.getProjectId());
    createRunRequest.put("type", "Automated");
    createRunRequest.put("sourceType", "testlio_cli");

    RunDTO automatedRun = (RunDTO) restClient.post("/test-run/v1/collections/" + config.getTestRunCollectionGuid() + "/runs", createRunRequest, RunDTO.class);
    listener.getLogger().println("Step 1.1: Update automated run name");
    JSONObject updateRunRequest = new JSONObject();
    updateRunRequest.put("name", "JENKINS:- " + automatedRun.getName());
    automatedRun = (RunDTO) restClient.put(automatedRun.getHref(), updateRunRequest, RunDTO.class, false);
    return automatedRun;
  }

  public static void createAndUpdateAutomatedRunConfiguration(TaskListener listener, RestClient restClient, RunDTO automatedRun, RunConfigurationAction config, boolean isBrowser) {
    listener.getLogger().println("Step 2.1: Search for automated run configuration");
    JSONObject searchRunConfigurationRequest = new JSONObject();
    searchRunConfigurationRequest.put("runHrefs", Collections.singletonList(automatedRun.getHref()));
    RunConfigurationDTO runConfigurationDTO = (RunConfigurationDTO) restClient.post("/automated-test-run/v1/collections/" + config.getAutomatedTestRunCollectionGuid() + "/search/run-configurations", searchRunConfigurationRequest, RunConfigurationDTO.class);


    JSONObject updateRunConfigRequest = new JSONObject();
    updateRunConfigRequest.put("type", isBrowser ? "BROWSER" : "DEVICE");
    if (isBrowser) {
      updateRunConfigRequest.put("deviceAppType", config.getAppType().getValue());
      updateRunConfigRequest.put("deviceTestType", config.getBrowserTestType());
      updateRunConfigRequest.put("deviceTimeoutInMinutes", config.getTimeoutBrowsers());
      if (StringUtils.isNotBlank(config.getTestArgs())) {
        updateRunConfigRequest.put("executionConfiguration", new JSONObject().put("testArgs", config.getTestArgs()));
      }
    }
    else {
      updateRunConfigRequest.put("deviceAppType", config.getDevicePlatformType().getName());
      updateRunConfigRequest.put("deviceTestType", config.getDeviceTestType().toString());

      JSONObject configuration = new JSONObject();
      JSONObject radioConfigs = new JSONObject();
      radioConfigs.put("bluetooth", config.getDeviceState().getDeviceRadios().getBluetooth());
      radioConfigs.put("gps", config.getDeviceState().getDeviceRadios().getGps());
      radioConfigs.put("nfc", config.getDeviceState().getDeviceRadios().getNfc());
      radioConfigs.put("wifi", config.getDeviceState().getDeviceRadios().getWifi());
      configuration.put("radios", radioConfigs);
      if (config.getDeviceState().getDeviceLocation().getLatitude() != null && config.getDeviceState().getDeviceLocation().getLongitude() != null) {
        JSONObject locationConfig = new JSONObject();
        locationConfig.put("latitude", config.getDeviceState().getDeviceLocation().getLatitude());
        locationConfig.put("longitude", config.getDeviceState().getDeviceLocation().getLongitude());
        configuration.put("location", locationConfig);
      }

      if (StringUtils.isNotBlank(config.getDeviceState().getDeviceLocale())) {
        configuration.put("locale", config.getDeviceState().getDeviceLocale());
      }
      configuration.put("networkProfileName", config.getDeviceState().getNetworkProfile().getName());

      updateRunConfigRequest.put("configuration", configuration);

      JSONObject executionConfiguration = new JSONObject();
      executionConfiguration.put("jobTimeoutMinutes", config.getTimeoutDevices());
      executionConfiguration.put("videoCapture", config.getVideoCapture());
      updateRunConfigRequest.put("executionConfiguration", executionConfiguration);
    }

    restClient.put(runConfigurationDTO.getData().get(0).getHref(), updateRunConfigRequest, null, false);
  }

  public static void scheduleAutomatedRun(TaskListener listener, RestClient restClient, RunDTO automatedRun, RunConfigurationAction config) {
    JSONObject scheduleRunRequest = new JSONObject();
    scheduleRunRequest.put("status", "inProgress");
    scheduleRunRequest.put("projectId", config.getProjectId());
    restClient.put(automatedRun.getHref(), scheduleRunRequest, null, true);

    ProjectDTO projectResponse = (ProjectDTO) restClient.get("/project/v1/projects/" + config.getProjectId(), ProjectDTO.class);
    listener.getLogger().println("Run is successfully scheduled, find automated run href: " + APP_URL + "/" + projectResponse.getUrlCode() + "/runs/" + automatedRun.getNumber());
  }

  public static void attachDevices(TaskListener listener, RestClient restClient, BrowsersDTO browsersDTO, DevicesDTO devicesDTO,
                                   ResponseHrefDTO responseHrefDTO, RunConfigurationAction config, boolean isBrowser) {
    listener.getLogger().println("Step 6.1: Create Device pool");
    JSONObject createDevicePool = new JSONObject();
    createDevicePool.put("name", "Automated device");
    createDevicePool.put("hidden", true);
    createDevicePool.put("withoutDeviceAssignment", true);
    createDevicePool.put("isGroup", false);
    createDevicePool.put("isMultipleDevices", false);
    DevicePoolDTO devicePoolDTO = (DevicePoolDTO) restClient.post("/test-run/v1/collections/" + config.getTestRunCollectionGuid() + "/device-pools", createDevicePool, DevicePoolDTO.class);

    listener.getLogger().println("Step 6.2: Create device pool filters");
    final String devicePoolFilterHref = devicePoolDTO.getHref() + "/filters";
    createDevicePoolFilter(restClient, browsersDTO, devicesDTO, config, isBrowser, devicePoolFilterHref);

    listener.getLogger().println("Step 6.3: Attaching device pool filter to automated run plan");
    JSONObject request = new JSONObject();
    request.put("jobsCount", 1);
    request.put("devicePoolGuid", devicePoolDTO.getGuid());
    restClient.post(responseHrefDTO.getHref() + "/plan-device-pools", request, null);
  }

  private static void createDevicePoolFilter(RestClient restClient, BrowsersDTO browsersDTO, DevicesDTO devicesDTO, RunConfigurationAction config, boolean isBrowser, String devicePoolFilterHref) {
    if (isBrowser) {
      browsersDTO.getData().stream().limit(config.getSelect()).forEach(b -> {
        JSONObject request = new JSONObject();
        request.put("automatedBrowsers", Collections.singletonList(new JSONObject().put("automatedBrowserGuid", b.getId())));
        request.put("isGroup", false);
        restClient.post(devicePoolFilterHref, request, null);
      });
    }
    else {
      devicesDTO.getData().stream().limit(config.getSelect()).forEach(d -> {
        JSONObject request = new JSONObject();
        request.put("automatedDevices", Collections.singletonList(new JSONObject().put("automatedDeviceGuid", d.getId())));
        request.put("isGroup", false);
        restClient.post(devicePoolFilterHref, request, null);
      });
    }
  }
}
