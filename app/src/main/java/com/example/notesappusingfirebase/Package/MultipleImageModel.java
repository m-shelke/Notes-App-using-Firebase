package com.example.notesappusingfirebase.Package;


import android.net.Uri;

public class MultipleImageModel {
    public Uri uri;
    public String fileName;
    public long fileSize;
    public int progress;        // 0..100
    public boolean uploaded;
    public String downloadUrl;  // result from Firebase

    public MultipleImageModel(Uri uri, String fileName, long fileSize) {
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.progress = 0;
        this.uploaded = false;
        this.downloadUrl = null;
    }
}
