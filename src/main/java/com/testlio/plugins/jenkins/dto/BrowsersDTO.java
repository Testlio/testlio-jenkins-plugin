package com.testlio.plugins.jenkins.dto;

import lombok.Data;

import java.util.List;

@Data
public class BrowsersDTO {
  private String href;
  private List<BrowserData> data;

  @Data
  public class BrowserData {
    private String href;
    private String id;
    private String browserName;
    private String platform;
    private String platformName;
    private String version;
  }
}
