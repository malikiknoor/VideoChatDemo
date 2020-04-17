package com.iknoortech.videochatdemo.model;

public class FriendList {

    private String friend_id;

    private boolean isFriend;

    public FriendList() {
    }

    public FriendList(String friend_id, boolean isFriend) {
        this.friend_id = friend_id;
        this.isFriend = isFriend;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
