package com.testlio.plugins.jenkins.enums;

import lombok.Getter;

@Getter
public enum AppTypeEnum {
    DEVICE_APP("Mobile App","OTHER"),
    DEVICE_WEB("Mobile Web", "WEB"),
    BROWSER("Desktop Web", "WEB");

    private final String name;
    private final String value;

    AppTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

