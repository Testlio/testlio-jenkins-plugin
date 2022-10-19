package com.testlio.plugins.jenkins.dto;

import lombok.Data;

import java.io.File;
@Data
public class FileDTO {
  private String fileName;
  private File file;
}
