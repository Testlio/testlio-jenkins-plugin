package com.testlio.plugins.jenkins.dto;

import lombok.Data;

@Data
public class AutomatedRunResultsDTO {
  private Result results;

  @Data
  public class Result {
    private String result;
  }
}
