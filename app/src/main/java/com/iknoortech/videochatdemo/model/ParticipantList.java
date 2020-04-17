package com.iknoortech.videochatdemo.model;

import java.io.Serializable;

public class ParticipantList implements Serializable {

    private String friend_id;
    private String name;
    private String image;
    private boolean selected;

    public ParticipantList() {
    }

    public ParticipantList(String friend_id, String name, String image, boolean selected) {
        this.friend_id = friend_id;
        this.name = name;
        this.image = image;
        this.selected = selected;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
