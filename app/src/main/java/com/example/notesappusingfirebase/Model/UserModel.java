package com.example.notesappusingfirebase.Model;

public class UserModel {

    //    collecting Users information
    public String userId, name, search, profile, email;

    //    empty constructor required
    public UserModel() {

    }

    public UserModel(String userId, String name, String search, String profile, String email) {
        this.userId = userId;
        this.name = name;
        this.search = search;
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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
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




