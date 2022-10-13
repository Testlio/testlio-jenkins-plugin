package com.testlio.plugins.jenkins.enums;

public enum PlatformNameEnum {
    WINDOWS("Microsoft Windows");

    private final String name;

    PlatformNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
