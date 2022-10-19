package com.testlio.plugins.jenkins.helper;

import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.BrowsersDTO;
import com.testlio.plugins.jenkins.dto.DevicesDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class SearchDevicesHelper {
  public static BrowsersDTO searchBrowsers(RunConfigurationAction config, RestClient restClient) {
    BrowsersDTO browsersDTO;
    JSONObject request = new JSONObject();
    request.put("browserNames", config.getBrowserSelector().getBrowserNames());
    request.put("platformNames", config.getBrowserSelector().getPlatformNames());
    request.put("versions", config.getBrowserSelector().getBrowserVersions());
    browsersDTO = (BrowsersDTO) restClient.post("/automated-test-run/v1/browsers/search", request, BrowsersDTO.class);
    return browsersDTO;
  }

  public static DevicesDTO searchDevices(RunConfigurationAction config, RestClient restClient) {
    DevicesDTO devicesDTO;
    JSONObject searchDeviceRequest = new JSONObject();
    JSONArray manufactureList = new JSONArray();
    Arrays.stream(config.getDeviceSelector().getDeviceManufacturers().split(",")).forEach(manufactureList::put);
    searchDeviceRequest.put("manufacturers", manufactureList);
    JSONArray deviceOsList = new JSONArray();
    Arrays.stream(config.getDeviceSelector().getDeviceOses().split(",")).forEach(deviceOsList::put);
    searchDeviceRequest.put("oses", deviceOsList);
    JSONArray formFactorsList = new JSONArray();
    Arrays.stream(config.getDeviceSelector().getFormattedDeviceFormFactors().split(",")).forEach(formFactorsList::put);
    searchDeviceRequest.put("formFactors", formFactorsList);
    JSONArray list = new JSONArray();
    list.put("HIGHLY_AVAILABLE");
    list.put("AVAILABLE");
    searchDeviceRequest.put("availability", list);
    JSONArray platformList = new JSONArray();
    platformList.put(config.getDevicePlatformType().getName());
    searchDeviceRequest.put("platforms", platformList);

    devicesDTO = (DevicesDTO) restClient.post("/automated-test-run/v1/devices/search", searchDeviceRequest, DevicesDTO.class);
    return devicesDTO;
  }
}
