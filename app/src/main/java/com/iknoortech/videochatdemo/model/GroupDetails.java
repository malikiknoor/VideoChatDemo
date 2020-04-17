package com.iknoortech.videochatdemo.model;

public class GroupDetails {

    private boolean admin;
    private String joinDate;
    private String memberId;

    public GroupDetails() {
    }

    public GroupDetails(boolean admin, String joinDate, String memberId) {
        this.admin = admin;
        this.joinDate = joinDate;
        this.memberId = memberId;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
