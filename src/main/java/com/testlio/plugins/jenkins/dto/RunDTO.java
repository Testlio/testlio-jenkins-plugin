package com.testlio.plugins.jenkins.dto;

import lombok.Data;


@Data
public class RunDTO {
  private String href;
  private String guid;
  private String number;
  private String code;
  private String name;
  private String type;
  private Href plans;
  private Href configuration;

  @Data
  public class Href {
    private String href;
  }
}
