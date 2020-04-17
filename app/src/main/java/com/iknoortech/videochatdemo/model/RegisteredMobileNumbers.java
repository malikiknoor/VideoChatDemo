package com.iknoortech.videochatdemo.model;

public class RegisteredMobileNumbers {
    String mobile;
    String status;
    String uId;

    public RegisteredMobileNumbers() {
    }

    public RegisteredMobileNumbers(String mobile, String status, String uId) {
        this.mobile = mobile;
        this.status = status;
        this.uId = uId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
