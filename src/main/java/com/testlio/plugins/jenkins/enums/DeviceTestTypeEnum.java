package com.testlio.plugins.jenkins.enums;

public enum DeviceTestTypeEnum {
    APPIUM_JAVA_TESTNG("Appium Java TestNG"),
    APPIUM_JAVA_JUNIT("Appium Java JUnit"),
    APPIUM_NODE("Appium Node"),
    APPIUM_PYTHON("Appium Python"),
    APPIUM_RUBY("Appium Ruby"),
    UI_AUTOMATOR("UI Automator"),
    INSTRUMENTATION("Instrumentation"),
    CALABASH("Calabash");

    private final String name;

    DeviceTestTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
