package com.melardev.tutsnetlibrary.apis;

import android.support.annotation.NonNull;

import com.melardev.tutsnetlibrary.model.ReqResUnknown;
import com.melardev.tutsnetlibrary.model.ReqResUserCreation;
import com.melardev.tutsnetlibrary.model.UserReqResRequest;
import com.melardev.tutsnetlibrary.model.UserReqResResponse;
import com.melardev.tutsnetlibrary.model.pojo.ReqResUsers;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by melardev on 6/24/2017.
 */

public interface ReqResApi {

    //use ResponseBody(or void) if you do not want to deal with responses
    @GET("https://reqres.in//api/users?page=2")
    Call<ResponseBody> simpleGet(@Body RequestBody body);

    //TODO:Work?
    @GET("/api/users")
    Call<ReqResUsers> getUsers(@Query("page") String page);

    /**
     * @param userId the id of the user to query, if you want to skip a query parameter for a get request
     *               you pass null, but ... null can only be passed for Java objects and not primitives (int,long ,etc)
     *               the solution is to replace the primitive by a java object equivalent long -> Long, int -> Integer, float-> Float, etc.
     *               if you are thinking to make that query parameter as optional
     *               <p>
     *               <p>
     *               the user id of the user to retrieve information from, it is optional, to skip this
     *               parameter pass an empty string. DO NOT PASS a null object!!!! otherwise an IllegalArgumentException
     * @return
     */
    @GET("/api/users/{userId}")
    Call<ReqResUsers.User> getUserById(@NonNull @Path("userId") long userId);

    @GET("/api/unknown")
    Call<ReqResUnknown> reqResdoGetUnknown();

    @POST("/api/users")
    Call<UserReqResResponse> reqRescreateUser(@Body UserReqResRequest user);


    @FormUrlEncoded //You are forced to use this annotation, otherwise crash
    @POST("/api/users")
    Call<ResponseBody> reqResdoCreateUserWithField(@Field("name") String name, @Field("job") String job);

    @POST("/api/users")
    Call<ResponseBody> createUser(@Body UserReqResRequest user);

    @FormUrlEncoded
    @POST("/api/users")
    Call<ReqResUserCreation> createUser(@Field(value = "userName", encoded = false) String userName);
    //You can use @FieldMap for multiple fields or multiple @Field parameter
    //FieldMap is for POST requests whereas @QueryMap is for GET requests

    @DELETE("/api/users/{id}")
    Call<Void> deleteUser(@Path("id") int id);


    @GET("/api/users")
    rx.Observable<List<UserReqResResponse>> getUsersInPageRx_1(@Query("page") String page);

    @GET("/api/users")
    Observable<List<UserReqResResponse>> getUsersInPageRx_2(@Query("page") String page);

    @GET("/api/users/{userId}")
    Observable<ReqResUsers.User> getUserByIdRx(@NonNull @Path("userId") long userId);

}
