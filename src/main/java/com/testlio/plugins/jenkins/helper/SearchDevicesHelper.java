package com.testlio.plugins.jenkins.helper;

import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.BrowsersDTO;
import com.testlio.plugins.jenkins.dto.DevicesDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
    if(CollectionUtils.isEmpty(browsersDTO.getData())){
      throw new IllegalArgumentException("No browsers found, please check your device params");
    }
    return browsersDTO;
  }

  public static DevicesDTO searchDevices(RunConfigurationAction config, RestClient restClient) {
    DevicesDTO devicesDTO;
    JSONObject searchDeviceRequest = new JSONObject();
    if(StringUtils.isNotBlank(config.getDeviceSelector().getDeviceManufacturers())){
      JSONArray manufactureList = new JSONArray();
      Arrays.stream(config.getDeviceSelector().getDeviceManufacturers().split(",")).forEach(manufactureList::put);
      searchDeviceRequest.put("manufacturers", manufactureList);
    }

    if(StringUtils.isNotBlank(config.getDeviceSelector().getDeviceOses())){
      JSONArray deviceOsList = new JSONArray();
      Arrays.stream(config.getDeviceSelector().getDeviceOses().split(",")).forEach(deviceOsList::put);
      searchDeviceRequest.put("oses", deviceOsList);
    }

    if(StringUtils.isNotBlank(config.getDeviceSelector().getDeviceNames())){
      JSONArray deviceNameList = new JSONArray();
      Arrays.stream(config.getDeviceSelector().getDeviceNames().split(",")).forEach(deviceNameList::put);
      searchDeviceRequest.put("names", deviceNameList);
    }

    if(StringUtils.isNotBlank(config.getDeviceSelector().getFormattedDeviceFormFactors())){
      JSONArray formFactorsList = new JSONArray();
      Arrays.stream(config.getDeviceSelector().getFormattedDeviceFormFactors().split(",")).forEach(formFactorsList::put);
      searchDeviceRequest.put("formFactors", formFactorsList);
    }

    JSONArray list = new JSONArray();
    list.put("HIGHLY_AVAILABLE");
    list.put("AVAILABLE");
    searchDeviceRequest.put("availability", list);
    JSONArray platformList = new JSONArray();
    platformList.put(config.getDevicePlatformType().getName());
    searchDeviceRequest.put("platforms", platformList);

    devicesDTO = (DevicesDTO) restClient.post("/automated-test-run/v1/devices/search", searchDeviceRequest, DevicesDTO.class);

    if(CollectionUtils.isEmpty(devicesDTO.getData())){
     throw new IllegalArgumentException("No devices found, please check your device params");
    }
    return devicesDTO;
  }
}
