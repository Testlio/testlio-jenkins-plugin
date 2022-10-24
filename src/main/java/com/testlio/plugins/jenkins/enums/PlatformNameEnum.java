package com.testlio.plugins.jenkins.enums;

public enum PlatformNameEnum {
    WINDOWS("Microsoft Windows", "windows");

    private final String name;
    private final String value;

    PlatformNameEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
