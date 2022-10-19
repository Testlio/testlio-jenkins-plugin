package com.testlio.plugins.jenkins.enums;

public enum AppTypeEnum {
    DEVICE_APP("Mobile App"),
    DEVICE_WEB("Mobile Web"),
    BROWSER("Desktop Web");

    private final String name;

    AppTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

