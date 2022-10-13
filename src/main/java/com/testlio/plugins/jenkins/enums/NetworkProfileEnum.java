package com.testlio.plugins.jenkins.enums;

public enum NetworkProfileEnum {
    NONE("None"),
    HALF("Half"),
    FULL("Full");

    private final String name;

    NetworkProfileEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
