package com.example.notesappusingfirebase.Helper;

import com.example.notesappusingfirebase.Model.SavedINoteModel;

public class SavedRow extends SavedListItem {

    private SavedINoteModel item;

    public SavedRow(SavedINoteModel item) {
        this.item = item;
    }

    public SavedINoteModel getItem() {
        return item;
    }
}

