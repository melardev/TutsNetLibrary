package com.melardev.tutsnetlibrary.apis;

import com.melardev.tutsnetlibrary.model.ReqResUserCreation;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by melardev on 6/24/2017.
 */

public interface HttpBinApi {
    @GET("get")
    Call<ResponseBody> get();

    //use ResponseBody(or void) if you do not want to deal with responses
    @GET("get")
    Call<ResponseBody> get(@Query("id") String requestBody);

    @GET("/basic-auth/user/passwd")
    Call<ResponseBody> basicAuth();

    /*@Headers({"Accept: text/xml",
            "User-Agent: something"})*/
    @Headers("Cache-Control: max-age=640000")
    @HEAD("/headers")
//HEAD must be in a method that returns Call<Void>
    Call<Void> testStaticHeaders();

    @HEAD("/headers")
    Call<Void> testDynamicHeaders(@Header("Content-Type") String contentRange);

    @GET("/headers")
    Call<Void> testDynamicMapHeaders(@HeaderMap Map<String, String> headers);

    @POST("/post")
    Call<ResponseBody> postField(@Field("key") String value, @Field(value = "default Value", encoded = true) String n);

    @FormUrlEncoded //FieldMap can only be used if you specify @FormUlrEncoded, otherwise crash!!
    @POST("/post")
    Call<ResponseBody> post(@FieldMap Map<String, String> map);

    @GET
    Call<ResponseBody> getImage(@Url String url);

    @Multipart
    @POST("/post")
    Call<ResponseBody> uploadFile(@Part("description") RequestBody description, @Part MultipartBody.Part file);

    //@FormUrlEncoded Cannot be used in @Body, otherwise crash!!
    @POST("/post")
    Call<String> sendScalar(@Body String body);

    @POST("/post")
    Call<ResponseBody> post(@Body RequestBody requestBody);

    @POST("/post")
    Call<ResponseBody> post(@Body String stringBody);

    @GET("")
    Observable<ResponseBody> rxGet();
}
