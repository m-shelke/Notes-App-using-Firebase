package com.example.notesappusingfirebase.Model;

import java.util.Map;

public class RoomNoteMemberModel {

//    String senderName,senderUId,note,time,type,fileUrl,search,fileName,messageId,iamgeUrl,seen;
//
//
//    public String getSenderName() {
//        return senderName;
//    }
//
//    public void setSenderName(String senderName) {
//        this.senderName = senderName;
//    }
//
//    public String getSenderUId() {
//        return senderUId;
//    }
//
//    public void setSenderUId(String senderUId) {
//        this.senderUId = senderUId;
//    }
//
//    public String getNote() {
//        return note;
//    }
//
//    public void setNote(String note) {
//        this.note = note;
//    }
//
//    public String getTime() {
//        return time;
//    }
//
//    public void setTime(String time) {
//        this.time = time;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getFileUrl() {
//        return fileUrl;
//    }
//
//    public void setFileUrl(String fileUrl) {
//        this.fileUrl = fileUrl;
//    }
//
//    public String getSearch() {
//        return search;
//    }
//
//    public void setSearch(String search) {
//        this.search = search;
//    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public String getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(String messageId) {
//        this.messageId = messageId;
//    }
//
//    public String getIamgeUrl() {
//        return iamgeUrl;
//    }
//
//    public void setIamgeUrl(String iamgeUrl) {
//        this.iamgeUrl = iamgeUrl;
//    }
//
//    public String getSeen() {
//        return seen;
//    }
//
//    public void setSeen(String seen) {
//        this.seen = seen;
//    }





        private String messageId;
        private String senderId;

        private String type;          // TEXT, IMAGE, FILE
        private String note;          // text message
        private String senderName;    //sender name text


        private String imageUrl;      // for IMAGE
        private String fileUrl;       // for FILE
        private String fileName;
        private String fileSize;
        private String search;

        private long timestamp;

        // Reply
        private String replyToId;
        private String replyToText;
        private String replyToUser;

        // Reaction (single emoji)
        private String reaction;

        // Delete Status
        private boolean deletedForMe = false;
        private boolean deletedForEveryone = false;

        //Seen or not
        private Map<String, Boolean> seenBy;


        public RoomNoteMemberModel() {}

        // Constructor
        public RoomNoteMemberModel(String messageId, String senderId, String type, String note,
                                   String imageUrl, String fileUrl, String fileName, String fileSize,
                                   long timestamp,String senderName,Map<String, Boolean> seenBy,String search) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.type = type;
            this.note = note;
            this.imageUrl = imageUrl;
            this.fileUrl = fileUrl;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.timestamp = timestamp;
            this.senderName = senderName;
            this.seenBy = seenBy;
            this.search = search;
        }

        // ------------------- GETTERS -------------------
        public String getMessageId() { return messageId; }
        public String getSenderId() { return senderId; }

        public String getType() { return type; }
        public String getNote() { return note; }

        public String getImageUrl() { return imageUrl; }
        public String getFileUrl() { return fileUrl; }
        public String getFileName() { return fileName; }
        public String getFileSize() { return fileSize; }

        public long getTimestamp() { return timestamp; }

        public String getReplyToId() { return replyToId; }
        public String getReplyToText() { return replyToText; }
        public String getReplyToUser() { return replyToUser; }

        public String getReaction() { return reaction; }

        public boolean isDeletedForMe() { return deletedForMe; }
        public boolean isDeletedForEveryone() { return deletedForEveryone; }

        // ------------------- SETTERS -------------------
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public void setType(String type) { this.type = type; }
        public void setNote(String note) { this.note = note; }

        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

        public void setFileName(String fileName) { this.fileName = fileName; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }

        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
        public void setReplyToText(String replyToText) { this.replyToText = replyToText; }
        public void setReplyToUser(String replyToUser) { this.replyToUser = replyToUser; }

        public void setReaction(String reaction) { this.reaction = reaction; }

        public void setDeletedForMe(boolean deletedForMe) { this.deletedForMe = deletedForMe; }
        public void setDeletedForEveryone(boolean deletedForEveryone) { this.deletedForEveryone = deletedForEveryone; }

        public String getSenderName() {return senderName;}
        public void setSenderName(String senderName) {this.senderName = senderName;}

        public String getSearch() {
                return search;
        }

        public void setSearch(String search) {
                this.search = search;
        }

        public Map<String, Boolean> getSeenBy() {return seenBy;}

        public void setSeenBy(Map<String, Boolean> seenBy) {this.seenBy = seenBy;}

}
