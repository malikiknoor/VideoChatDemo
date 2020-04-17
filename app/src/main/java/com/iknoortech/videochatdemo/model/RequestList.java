package com.iknoortech.videochatdemo.model;

public class RequestList {

    private String request_type;
    private String send_time;

    public RequestList() {
    }

    public RequestList(String request_type, String send_time) {
        this.request_type = request_type;
        this.send_time = send_time;
    }

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
