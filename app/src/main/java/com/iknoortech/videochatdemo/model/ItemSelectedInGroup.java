package com.iknoortech.videochatdemo.model;

public class ItemSelectedInGroup {

    private String Id;

    private boolean selected;

    public ItemSelectedInGroup(String id, boolean selected) {
        Id = id;
        this.selected = selected;
    }

    public ItemSelectedInGroup() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
