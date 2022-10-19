package com.testlio.plugins.jenkins.enums;

public enum NetworkProfileEnum {
    FULL("Full"),
    HALF("Half"),
    NONE("None");

    private final String name;

    NetworkProfileEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
