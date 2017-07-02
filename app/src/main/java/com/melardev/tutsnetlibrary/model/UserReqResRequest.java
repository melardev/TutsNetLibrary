package com.melardev.tutsnetlibrary.model;

/**
 * Created by melardev on 6/26/2017.
 */

public class UserReqResRequest {
    public String name;
    public String job;

    public UserReqResRequest(String name, String job) {
        this.name = name;
        this.job = job;
    }

    @Override
    public String toString() {
        return "name: " + name + ", job: " + job;
    }
}
