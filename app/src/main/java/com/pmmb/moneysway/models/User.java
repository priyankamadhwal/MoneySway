package com.pmmb.moneysway.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name;
    public String xattr;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String xattr) {
        this.name = name;
        this.xattr = xattr;
    }

}