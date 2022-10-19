package com.testlio.plugins.jenkins.dto;

import lombok.Data;

import java.util.List;

@Data
public class DevicesDTO {
  private String href;
  private List<DeviceData> data;

  @Data
  public class DeviceData {
    private String href;
    private String id;
    private String model;
    private String name;
    private String os;
    private String platform;
  }
}
