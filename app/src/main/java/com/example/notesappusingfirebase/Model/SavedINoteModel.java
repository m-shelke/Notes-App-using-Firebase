package com.example.notesappusingfirebase.Model;

public class SavedINoteModel {

    // State
    public boolean pinned;
    // Firebase keys
    private String savedId;     // key under SavedItems/{uid}
    private String messageId;
    // Room info (required for grouping)
    private String roomId;
    private String roomName;
    // Message content
    private String type;        // TEXT, IMAGE, FILE (PDF/DOC/PPT/IMAGE)
    private String note;
    private String title;
    private String imageUrl;
    private String fileName;
    // Meta
    private String senderName;
    private long timestamp;

    // 🔹 REQUIRED empty constructor for Firebase
    public SavedINoteModel() {
    }

    // 🔹 Optional constructor
    public SavedINoteModel(
            String savedId,
            String messageId,
            String roomId,
            String roomName,
            String type,
            String note,
            String title,
            String imageUrl,
            String fileName,
            String senderName,
            long timestamp,
            boolean pinned
    ) {
        this.savedId = savedId;
        this.messageId = messageId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.type = type;
        this.note = note;
        this.title = title;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.pinned = pinned;
    }

    // ---------------- GETTERS ----------------

    public String getSavedId() {
        return savedId;
    }

    public void setSavedId(String savedId) {
        this.savedId = savedId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    // ---------------- SETTERS ----------------

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}



