package com.testlio.plugins.jenkins.models;

import jenkins.security.stapler.StaplerAccessibleType;
import org.kohsuke.stapler.DataBoundConstructor;

@StaplerAccessibleType
public class DeviceLocation {

    private final Float longitude;

    private final Float latitude;

    @DataBoundConstructor
    public DeviceLocation(Float longitude, Float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
