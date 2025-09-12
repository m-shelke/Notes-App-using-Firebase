package com.example.notesappusingfirebase.Model;

public abstract class SavedListItem {

    class RoomHeader extends SavedListItem {
        String roomId;
        String roomName;
    }

    class SavedRow extends SavedListItem {
        SavedItemModel item;
    }

}
