package com.jkkc.travel.bean;

/**
 * Created by Xxyou on 2017/6/27.
 */

public class MacBean {
    private String userName;
    private String mac;

    public MacBean(String userName, String mac) {
        this.userName = userName;
        this.mac = mac;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
