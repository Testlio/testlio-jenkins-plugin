package com.testlio.plugins.jenkins.enums;

public enum DeviceFormFactorEnum {
    PHONE("Phone"),
    TABLET("Tablet");
    private final String name;

    DeviceFormFactorEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
