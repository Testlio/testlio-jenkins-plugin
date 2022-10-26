package com.testlio.plugins.jenkins.dto;

import lombok.Data;

@Data
public class AutomatedRunResultsDTO {
  private Result results;

  @Data
  public static class Result {
    private String result;
  }
}
