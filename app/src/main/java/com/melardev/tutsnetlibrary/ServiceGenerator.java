package com.melardev.tutsnetlibrary;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.melardev.tutsnetlibrary.apis.RetrofitBasicService;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by melardev on 6/18/2017.
 */

public class ServiceGenerator {

    //TODO : convert OkHttpClient and Retrofit objects to private static fields so they are not recreated each time

    private static OkHttpClient okHttpClient;

    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        return createService(serviceClass, baseUrl, (Interceptor) null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, Interceptor interceptor) {
        return createService(serviceClass, baseUrl, interceptor, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, Converter.Factory[] factories) {
        return createService(serviceClass, baseUrl, null, factories);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, Interceptor interceptor, Converter.Factory[] factories) {
        // In retrofit 1.9 Retrofit class is RestAdapter, baseUrl(String) is setEndpoint(String); addInterceptor(Interceptor) is setRequestInterceptor(RequestInterceptor)
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder = new Retrofit.Builder();

        //check if contains default converter(JSON)
        boolean containsDefaultConverter = false;
        if (factories != null && factories.length > 0) {
            for (Converter.Factory factory : factories) {
                builder.addConverterFactory(factory); //The factory who takes care for serialization and deserialization of objects
                if (factory instanceof GsonConverterFactory)
                    containsDefaultConverter = true;
            }
        }

        //if it does not contain default converter, then add it
        if (!containsDefaultConverter)
            builder.addConverterFactory(GsonConverterFactory.create());

        if (!TextUtils.isEmpty(baseUrl))
            builder.baseUrl(baseUrl);

        if (interceptor != null && !httpClient.interceptors().contains(interceptor))
            httpClient.addInterceptor(interceptor);

        builder.client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

    public static class AuthenticationInterceptor implements Interceptor {

        private String authToken;

        public AuthenticationInterceptor(String token) {
            this.authToken = token;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .header("Authorization", authToken);//Remember header() vs addHeader

            //further customizations if you want, example, overwriting urls
            /*HttpUrl newUrl = original.url().newBuilder()
                    .addQueryParameter("key", "your_api_key")
                    .build();
            builder.url(newUrl);
            */
            Request request = builder.build();
            return chain.proceed(request);
        }
    }

    static void logThreadInfo(String methodName) {
        //Looper.getMainLooper().getThread() == Thread.currentThread()
        //Looper.getMainLooper().isCurrentThread() Requires API 23 (M)

        Log.d(methodName + "()", String.format(Locale.ENGLISH, "Thread Name : %s; is Main Thread ? %s", Thread.currentThread().getName(),
                Looper.getMainLooper() == Looper.myLooper() ? "yes" : "no"));
    }

    /*
    public static <S> S createService(Class<S> serviceClass, String baseUrl, final AccessToken accessToken) {

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient());

        if (accessToken != null) {

        }
    }*/
}
