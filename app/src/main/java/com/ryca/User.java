package com.ryca;

import android.net.Uri;

public class User {

    private String id;
    private String username;
    private String imageurl;
    private String add;


    public User() {

    }

    public User(String id, String username, String imageurl, String add) {
        this.id = id;
        this.username = username;
        this.imageurl = imageurl;
        this.add = add;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public Uri setImageurl(String imageurl) {
        this.imageurl = imageurl;
        return null;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }
}
