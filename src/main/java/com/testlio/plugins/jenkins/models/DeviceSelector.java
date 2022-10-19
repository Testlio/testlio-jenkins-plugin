package com.testlio.plugins.jenkins.models;

import com.testlio.plugins.jenkins.enums.DeviceFormFactorEnum;
import com.testlio.plugins.jenkins.enums.PlatformNameEnum;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceSelector {

    private final String deviceOses;

    private final String deviceManufacturers;

    private final String deviceNames;

    private final List<DeviceFormFactorEnum> deviceFormFactors;

    @DataBoundConstructor
    public DeviceSelector(String deviceOses, String deviceManufacturers, String deviceNames, DeviceFormFactorEnum[] deviceFormFactors) {
        this.deviceOses = deviceOses;
        this.deviceManufacturers = deviceManufacturers;
        this.deviceNames = deviceNames;
        this.deviceFormFactors = Arrays.asList(deviceFormFactors);
    }

    public String getDeviceOses() {
        return deviceOses;
    }

    public String getDeviceManufacturers() {
        return deviceManufacturers;
    }

    public String getDeviceNames() {
        return deviceNames;
    }

    public List<DeviceFormFactorEnum> getDeviceFormFactors() {
        return deviceFormFactors;
    }

    public List<String> getDeviceFormFactorStrings() {
        return deviceFormFactors.stream().map(DeviceFormFactorEnum::toString).collect(Collectors.toList());
    }

    public String getFormattedDeviceFormFactors() {
        if (deviceFormFactors.isEmpty()) return "-";
        return deviceFormFactors.stream().map(DeviceFormFactorEnum::getName).map(String::toUpperCase).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return "DeviceSelector{" +
                "deviceOses='" + deviceOses + '\'' +
                ", deviceManufacturers='" + deviceManufacturers + '\'' +
                ", deviceNames='" + deviceNames + '\'' +
                ", deviceFormFactors='" + deviceFormFactors.toString() + '\'' +
                '}';
    }
}
