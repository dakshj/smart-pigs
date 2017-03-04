package com.smartpigs.model;

import java.io.Serializable;

public class Address implements Serializable {

    private final String host;
    private final int portNo;

    public Address(final String host, final int portNo) {
        this.host = host;
        this.portNo = portNo;
    }

    public String getHost() {
        return host;
    }

    public int getPortNo() {
        return portNo;
    }

    @Override
    public String toString() {
        return getHost() + ":" + getPortNo();
    }
}
