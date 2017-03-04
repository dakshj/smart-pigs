package com.smartpigs.model;

public class Address {

    private final String ipAddress;
    private final int portNo;

    public Address(final String ipAddress, final int portNo) {
        this.ipAddress = ipAddress;
        this.portNo = portNo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPortNo() {
        return portNo;
    }

    @Override
    public String toString() {
        return getIpAddress() + ":" + getPortNo();
    }
}
