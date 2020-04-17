package com.iknoortech.videochatdemo.model;

public class Chat {

    private String messageId;
    private String sender;
    private String receiver;
    private String message;
    private String messageDate;
    private boolean isseen;
    private String time;
    private String deleteForMe;
    private String deleteForEveryone;
    private boolean isEdited;

    public Chat(String sender, String receiver, String message, String messageDate, boolean isseen, String time, String messageId,
                boolean isEdited, String deleteForMe, String deleteForEveryone) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageDate = messageDate;
        this.isseen = isseen;
        this.time = time;
        this.messageId = messageId;
        this.isEdited = isEdited;
        this.deleteForMe = deleteForMe;
        this.deleteForEveryone = deleteForEveryone;
    }

    public Chat() {
    }

    public String getDeleteForMe() {
        return deleteForMe;
    }

    public void setDeleteForMe(String deleteForMe) {
        this.deleteForMe = deleteForMe;
    }

    public String getDeleteForEveryone() {
        return deleteForEveryone;
    }

    public void setDeleteForEveryone(String deleteForEveryone) {
        this.deleteForEveryone = deleteForEveryone;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
