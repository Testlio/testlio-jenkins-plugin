package com.testlio.plugins.jenkins.dto;

import lombok.Data;

import java.net.URL;

@Data
public class UploadDTO {
  private Href put;
  private Href get;
  @Data
  public static class Href {
    private URL href;
  }
}
