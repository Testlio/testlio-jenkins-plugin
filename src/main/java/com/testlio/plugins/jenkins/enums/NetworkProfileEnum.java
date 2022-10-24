package com.testlio.plugins.jenkins.enums;

public enum NetworkProfileEnum {
    FULL("full"),
    HALF("half");
    private final String name;

    NetworkProfileEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
