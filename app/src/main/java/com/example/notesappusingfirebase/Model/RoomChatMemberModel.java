package com.example.notesappusingfirebase.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class RoomChatMemberModel implements Parcelable {

    public static final Creator<RoomChatMemberModel> CREATOR =
            new Creator<RoomChatMemberModel>() {
                @Override
                public RoomChatMemberModel createFromParcel(Parcel in) {
                    return new RoomChatMemberModel(in);
                }

                @Override
                public RoomChatMemberModel[] newArray(int size) {
                    return new RoomChatMemberModel[size];
                }
            };
    private String messageId;
    private String senderId;
    private String type;          // TEXT, IMAGE, FILE
    private String note;          // text message
    private String senderName;    //sender name text
    private String imageUrl;      // for IMAGE
    private String fileUrl;       // for FILE
    private String fileName;
    private String title;
    private String fileSize;
    private String search;
    private long timestamp;
    // Reply
    private String replyToId;
    private String replyToText;
    private String replyToUser;
    private String replyTitle;
    // Reaction (single emoji)
    private String reaction;
    // Delete Status
    private boolean deletedForMe = false;
    private boolean deletedForEveryone = false;
    //Seen or not
    private Map<String, Boolean> seenBy;
    private boolean forwarded;
    private String forwardedFrom; // optional (room/user name)

    public RoomChatMemberModel() {
    }

    protected RoomChatMemberModel(Parcel in) {
        messageId = in.readString();
        senderId = in.readString();
        type = in.readString();
        note = in.readString();
        title = in.readString();
        senderName = in.readString();
        imageUrl = in.readString();
        fileUrl = in.readString();
        fileName = in.readString();
        fileSize = in.readString();
        timestamp = in.readLong();
        deletedForEveryone = in.readByte() != 0;
    }

    // Constructor
    public RoomChatMemberModel(String messageId, String senderId, String type, String note,
                               String imageUrl, String fileUrl, String fileName, String fileSize,
                               long timestamp, String senderName, Map<String, Boolean> seenBy, String search, String title) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.type = type;
        this.note = note;
        this.title = title;
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
    public String getMessageId() {
        return messageId;
    }

    // ------------------- SETTERS -------------------
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public String getReplyToText() {
        return replyToText;
    }

    public void setReplyToText(String replyToText) {
        this.replyToText = replyToText;
    }

    public String getReplyToUser() {
        return replyToUser;
    }

    public void setReplyToUser(String replyToUser) {
        this.replyToUser = replyToUser;
    }

    public String getReplyTitle() {
        return replyTitle;
    }

    public void setReplyTitle(String replyTitle) {
        this.replyTitle = replyTitle;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public boolean isDeletedForMe() {
        return deletedForMe;
    }

    public void setDeletedForMe(boolean deletedForMe) {
        this.deletedForMe = deletedForMe;
    }

    public boolean isDeletedForEveryone() {
        return deletedForEveryone;
    }

    public void setDeletedForEveryone(boolean deletedForEveryone) {
        this.deletedForEveryone = deletedForEveryone;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public String getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(String forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }

    public Map<String, Boolean> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(Map<String, Boolean> seenBy) {
        this.seenBy = seenBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageId);
        dest.writeString(senderId);
        dest.writeString(type);
        dest.writeString(note);
        dest.writeString(title);
        dest.writeString(senderName);
        dest.writeString(imageUrl);
        dest.writeString(fileUrl);
        dest.writeString(fileName);
        dest.writeString(fileSize);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (deletedForEveryone ? 1 : 0));
    }
}






















