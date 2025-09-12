package com.example.notesappusingfirebase;

public class UserModel {

//    collecting Users information
    public String userId, name,profile,email;

//    empty constructor required
    public UserModel(){

    }

//    constructor with user info parameter
    public UserModel(String userId, String name, String profile, String email) {
        this.userId = userId;
        this.name = name;
        this.profile = profile;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
