package com.example.notesappusingfirebase.Model;

public class SavedRow extends SavedListItem {

    private SavedItemModel item;

    public SavedRow(SavedItemModel item) {
        this.item = item;
    }

    public SavedItemModel getItem() {
        return item;
    }
}

