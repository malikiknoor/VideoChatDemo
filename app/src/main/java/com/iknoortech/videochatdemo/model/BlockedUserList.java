package com.iknoortech.videochatdemo.model;

public class BlockedUserList {
    private String key;
    private String block_type;

    public BlockedUserList() {
    }

    public BlockedUserList(String block_type, String key) {
        this.block_type = block_type;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBlock_type() {
        return block_type;
    }

    public void setBlock_type(String block_type) {
        this.block_type = block_type;
    }
}
