package com.testlio.plugins.jenkins.enums;

public enum BrowserNameEnum {
    CHROME("Google Chrome"),
    FIREFOX("Mozilla Firefox"),
    EDGE("Microsoft Edge");

    private final String name;

    BrowserNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
