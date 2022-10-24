package com.testlio.plugins.jenkins.dto;

import lombok.Data;

import java.util.List;
@Data
public class RunConfigurationDTO {
  private String href;
  private List<RunConfigurationData> data;

  @Data
  public class RunConfigurationData {
    private String href;
    private String guid;
    private String runHref;
    private String name;
    private String type;
    private String provider;
    private String deviceAppType;
    private String deviceTimeoutInMinutes;
    private String testParameters;
    private Configuration configuration;
    private ExecutionConfiguration executionConfiguration;

    @Data
    public class Configuration {
      private String networkProfileName;
      private Radios radios;
      @Data
      public class Radios {
        private boolean wifi;
        private boolean bluetooth;
        private boolean gps;
        private boolean nfc;
      }
    }
    @Data
    public class ExecutionConfiguration {
      private boolean videoCapture;
      private int jobTimeoutMinutes;
    }
  }
}
