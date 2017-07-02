package com.melardev.tutsnetlibrary.apis;

import com.melardev.tutsnetlibrary.model.GitTokenResponse;
import com.melardev.tutsnetlibrary.model.pojo.GitAuthenticatedUser;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by melardev on 7/1/2017.
 */

public interface GithubApi {

    @GET("http://github.com/login/oauth/authorize")
    Call<Void> getCode(@QueryMap Map<String, String> queryMap);

    @FormUrlEncoded
    @POST("https://github.com/login/oauth/access_token")
    Call<String> getToken(@FieldMap Map<String, String> fieldMap);

    @GET("https://api.github.com/user")
    Call<GitAuthenticatedUser> getAuthenticatedUserInfo();

}
