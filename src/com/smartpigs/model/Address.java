package com.smartpigs.model;

public class Address {

    private final String ipAddress;
    private final String portNo;

    public Address(final String ipAddress, final String portNo) {
        this.ipAddress = ipAddress;
        this.portNo = portNo;
    }

    private String getIpAddress() {
        return ipAddress;
    }

    private String getPortNo() {
        return portNo;
    }
}
