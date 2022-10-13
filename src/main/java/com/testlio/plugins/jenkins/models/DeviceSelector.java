package com.testlio.plugins.jenkins.models;

import org.kohsuke.stapler.DataBoundConstructor;

public class DeviceSelector {

    private final String deviceOses;

    private final String deviceManufacturers;

    private final String deviceNames;

    private final String deviceFormFactors;

    @DataBoundConstructor
    public DeviceSelector(String deviceOses, String deviceManufacturers, String deviceNames, String deviceFormFactors) {
        this.deviceOses = deviceOses;
        this.deviceManufacturers = deviceManufacturers;
        this.deviceNames = deviceNames;
        this.deviceFormFactors = deviceFormFactors;
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

    public String getDeviceFormFactors() {
        return deviceFormFactors;
    }

    @Override
    public String toString() {
        return "DeviceSelector{" +
                "deviceOses='" + deviceOses + '\'' +
                ", deviceManufacturers='" + deviceManufacturers + '\'' +
                ", deviceNames='" + deviceNames + '\'' +
                ", deviceFormFactors='" + deviceFormFactors + '\'' +
                '}';
    }
}
