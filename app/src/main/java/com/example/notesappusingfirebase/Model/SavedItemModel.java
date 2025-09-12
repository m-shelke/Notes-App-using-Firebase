package com.example.notesappusingfirebase.Model;

public class SavedItemModel {

//    public String savedId;
//
//    public String messageId;
//    public String roomId;
//
//    public String type;       // TEXT, IMAGE, FILE
//    public String note;
//    public String fileUrl;
//    public String fileName;
//
//    public String senderName;
//    public long timestamp;
//
//    public boolean pinned;
//
//    public SavedItemModel() {}




        // Firebase keys
        private String savedId;     // key under SavedItems/{uid}
        private String messageId;

        // Room info (required for grouping)
        private String roomId;
        private String roomName;

        // Message content
        private String type;        // TEXT, IMAGE, FILE (PDF/DOC/PPT/IMAGE)
        private String note;
        private String fileUrl;
        private String fileName;

        // Meta
        private String senderName;
        private long timestamp;

        // State
        public boolean pinned;

        // 🔹 REQUIRED empty constructor for Firebase
        public SavedItemModel() {}

        // 🔹 Optional constructor
        public SavedItemModel(
                String savedId,
                String messageId,
                String roomId,
                String roomName,
                String type,
                String note,
                String fileUrl,
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
            this.fileUrl = fileUrl;
            this.fileName = fileName;
            this.senderName = senderName;
            this.timestamp = timestamp;
            this.pinned = pinned;
        }

        // ---------------- GETTERS ----------------

        public String getSavedId() {
            return savedId;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getType() {
            return type;
        }

        public String getNote() {
            return note;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public String getSenderName() {
            return senderName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isPinned() {
            return pinned;
        }

        // ---------------- SETTERS ----------------

        public void setSavedId(String savedId) {
            this.savedId = savedId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setPinned(boolean pinned) {
            this.pinned = pinned;
        }
    }



