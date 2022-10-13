package com.testlio.plugins.jenkins.models;

import com.testlio.plugins.jenkins.enums.NetworkProfileEnum;
import org.kohsuke.stapler.DataBoundConstructor;

public class DeviceState {

    private final DeviceRadios deviceRadios;

    private final DeviceLocation deviceLocation;

    private final String hostMachinePaths;

    private final String androidPaths;

    private final String deviceLocale;

    private final NetworkProfileEnum networkProfile;

    @DataBoundConstructor
    public DeviceState(DeviceRadios deviceRadios, DeviceLocation deviceLocation, String hostMachinePaths, String androidPaths, String deviceLocale, NetworkProfileEnum networkProfile) {
        this.deviceRadios = deviceRadios;
        this.deviceLocation = deviceLocation;
        this.hostMachinePaths = hostMachinePaths;
        this.androidPaths = androidPaths;
        this.deviceLocale = deviceLocale;
        this.networkProfile = networkProfile;
    }

    public DeviceRadios getDeviceRadios() {
        return deviceRadios;
    }

    public DeviceLocation getDeviceLocation() {
        return deviceLocation;
    }

    public String getHostMachinePaths() {
        return hostMachinePaths;
    }

    public String getAndroidPaths() {
        return androidPaths;
    }

    public String getDeviceLocale() {
        return deviceLocale;
    }

    public NetworkProfileEnum getNetworkProfile() {
        return networkProfile;
    }

    @Override
    public String toString() {
        return "DeviceState{" +
                "deviceRadios=" + deviceRadios +
                ", deviceLocation=" + deviceLocation +
                ", hostMachinePaths='" + hostMachinePaths + '\'' +
                ", androidPaths='" + androidPaths + '\'' +
                ", deviceLocale='" + deviceLocale + '\'' +
                ", networkProfile='" + networkProfile + '\'' +
                '}';
    }
}
