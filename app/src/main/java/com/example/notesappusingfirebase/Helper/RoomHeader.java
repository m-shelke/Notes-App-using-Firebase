package com.example.notesappusingfirebase.Helper;

public class RoomHeader extends SavedListItem {

    private String roomId;
    private String roomName;

    public RoomHeader(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }
}

