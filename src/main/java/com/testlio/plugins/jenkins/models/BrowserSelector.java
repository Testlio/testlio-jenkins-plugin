package com.testlio.plugins.jenkins.models;

import com.testlio.plugins.jenkins.enums.BrowserNameEnum;
import com.testlio.plugins.jenkins.enums.PlatformNameEnum;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BrowserSelector {
    private final List<BrowserNameEnum> browserNames;

    private final List<String> browserVersions;

    private final List<PlatformNameEnum> platformNames;

    @DataBoundConstructor
    public BrowserSelector(BrowserNameEnum[] browserNames, String[] browserVersions, PlatformNameEnum[] platformNames) {
        this.browserNames = Arrays.asList(browserNames);
        this.platformNames = Arrays.asList(platformNames);
        this.browserVersions = Arrays.asList(browserVersions);
    }

    public List<BrowserNameEnum> getBrowserNames() {
        return browserNames;
    }

    public List<String> getBrowserNameStrings() {
        return browserNames.stream().map(BrowserNameEnum::toString).collect(Collectors.toList());
    }

    public String getFormattedBrowserNames() {
        return browserNames.stream().map(BrowserNameEnum::getName).collect(Collectors.joining(", "));
    }

    public List<PlatformNameEnum> getPlatformNames() {
        return platformNames;
    }

    public List<String> getPlatformNameStrings() {
        return platformNames.stream().map(PlatformNameEnum::toString).collect(Collectors.toList());
    }

    public String getFormattedPlatformNames() {
        return platformNames.stream().map(PlatformNameEnum::getName).collect(Collectors.joining(", "));
    }

    public List<String> getBrowserVersions() {
        return browserVersions;
    }

    public String getFormattedBrowserVersions() {
        return String.join(", ", browserVersions);
    }

    @Override
    public String toString() {
        return "BrowserSelector{" +
                "browserNames=" + browserNames.toString() +
                ", browserVersions=" + browserVersions.toString() +
                ", platformNames=" + platformNames.toString() +
                '}';
    }
}
