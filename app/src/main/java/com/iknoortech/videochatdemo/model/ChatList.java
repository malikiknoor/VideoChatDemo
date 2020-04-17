package com.iknoortech.videochatdemo.model;

public class ChatList {

    private String id;
    private String isGroup;
    private String time;
    private String imageUrl;

    public ChatList(String id, String isGroup, String time) {
        this.id = id;
        this.isGroup = isGroup;
        this.time = time;
    }

    public ChatList() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
