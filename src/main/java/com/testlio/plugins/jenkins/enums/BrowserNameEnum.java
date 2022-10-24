package com.testlio.plugins.jenkins.enums;

import lombok.Getter;

@Getter
public enum BrowserNameEnum {
    CHROME("Google Chrome", "chrome"),
    FIREFOX("Mozilla Firefox","firefox"),
    EDGE("Microsoft Edge", "MicrosoftEdge");

    private final String name;
    private final String value;

    BrowserNameEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
