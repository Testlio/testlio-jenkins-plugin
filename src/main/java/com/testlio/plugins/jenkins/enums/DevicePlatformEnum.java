package com.testlio.plugins.jenkins.enums;

public enum DevicePlatformEnum {
  ANDROID("ANDROID"),
  IOS("IOS");

  private final String name;

  DevicePlatformEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
