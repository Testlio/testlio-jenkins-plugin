package com.testlio.plugins.jenkins.enums;

public enum BrowserTestTypeEnum {
    SELENIUM_JAVA_TESTNG("Selenium Java TestNG"),
    SELENIUM_JAVA_JUNIT("Selenium Java JUnit"),
    SELENIUM_NODE("Selenium Node");

    private final String name;

    BrowserTestTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

