package com.melardev.tutsnetlibrary.apis;

import com.melardev.tutsnetlibrary.model.ReqResUserCreation;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by melardev on 6/19/2017.
 */

public interface PostFormHttpBinApi {
    @FormUrlEncoded
    @POST("/post")
    Call<ReqResUserCreation> createUser(@Field(value = "username", encoded = false) String userName, @Field("password") String password);
    /*
    In retrofit 1.9 the equivalent would be
    Call<ReqResUserCreation> createUser(@Field("username") String userName, @Field("password") String password, Callback<ReqResUserCreation> cb);
    */
}
