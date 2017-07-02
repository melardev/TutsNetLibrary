package com.melardev.tutsnetlibrary.model;

/**
 * Created by melardev on 6/19/2017.
 */

public class ReqResUserCreation {
    public String userName;
    public String password;

    public ReqResUserCreation(String username, String password) {
        this.userName = username;
        this.password = password;
    }
}
