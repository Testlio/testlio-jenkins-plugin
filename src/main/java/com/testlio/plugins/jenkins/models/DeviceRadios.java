package com.testlio.plugins.jenkins.models;

import jenkins.security.stapler.StaplerAccessibleType;
import org.kohsuke.stapler.DataBoundConstructor;

@StaplerAccessibleType
public class DeviceRadios {
    private final Boolean wifi;

    private final Boolean bluetooth;

    private final Boolean gps;

    private final Boolean nfc;

    public Boolean getWifi() {
        return wifi;
    }

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public Boolean getGps() {
        return gps;
    }

    public Boolean getNfc() {
        return nfc;
    }

    @DataBoundConstructor
    public DeviceRadios(Boolean wifi, Boolean bluetooth, Boolean gps, Boolean nfc) {
        this.wifi = wifi;
        this.bluetooth = bluetooth;
        this.gps = gps;
        this.nfc = nfc;
    }

    @Override
    public String toString() {
        return "DeviceRadios{" +
                "wifi=" + wifi +
                ", bluetooth=" + bluetooth +
                ", gps=" + gps +
                ", nfc=" + nfc +
                '}';
    }
}
