package com.example.notesappusingfirebase.Helper;

import com.example.notesappusingfirebase.Model.SavedINoteModel;

public abstract class SavedListItem {

    class RoomHeader extends SavedListItem {
        String roomId;
        String roomName;
    }

    class SavedRow extends SavedListItem {
        SavedINoteModel item;
    }

}
