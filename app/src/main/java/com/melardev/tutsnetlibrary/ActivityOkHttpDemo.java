package com.melardev.tutsnetlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;

import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.melardev.tutsnetlibrary.model.pojo.ReqResUsers;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

import static com.melardev.tutsnetlibrary.ServiceGenerator.logThreadInfo;

@EActivity
public class ActivityOkHttpDemo extends AppCompatActivity {

    private TextView txtResult;
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();
    /**
     * Advantages : caching, connection pools, redirects, (a)synchronous
     * Client instances can only be executed once ... but we can clone() them
     * Callback objects will be executed in the main thread, so it is safe to update the UI
     * from its methods, BUT if you use this library in Java, the Callbacks are executed in same thread!!!
     * Resources https://github.com/square/okhttp/wiki/Recipes
     */
    Handler mainThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            txtResult.setText(msg.obj.toString());
        }
    };
    private AlertDialog dlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http_demo);

        txtResult = (TextView) findViewById(R.id.txtResult);
    }

    public void doSimpleSynchronous(View view) {
        logThreadInfo("doSimpleSynchronous");
        doSimpleGet(false);
    }

    public void doSimpleAsync(View view) {
        logThreadInfo("doSimpleAsync");
        doSimpleGet(true);
    }

    @Background
    public void doSimpleGet(boolean asynchronous) {
        updateResult("");
        logThreadInfo("doSimpleGet(" + asynchronous + ")");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://reqres.in/api/users?page=2")
                .build();

        if (asynchronous) {
            //Asynchronous
            Call myCall = client.newCall(request);
            myCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    logThreadInfo("onResponse");
                    Gson gson = new Gson();
                    //ReqResUsers rrU = gson.fromJson(response.body().charStream(), ReqResUsers.class); response.body().close();
                    final String myResponse = response.body().string(); //string() read the stream, and close it

                    //Response cachedResponse = response.cacheResponse();
                    //JSONObject jsonObj = new JSONObject(response.body().string());
                    //JSONArray jsonArr = new JSONObject(response.body().string()).getJSONArray("data");
                    //JSONArray jsonArr = new JSONArray(new JSONObject(response.body().string()).getString("data"));

                    updateResult(myResponse);
                    /*
                    ActivityOkHttpDemo.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtResult.setText(myResponse);
                        }
                    });
                    */
                }
            });
        } else {
            //Synchronous
            try {
                Response response = client.newCall(request).execute();
                updateResult(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Background
    public void doPostText(View view) {
        OkHttpClient client = new OkHttpClient();

        String postBody = "{\n" +
                "    \"email\": \"melar@dev.com\",\n" +
                "    \"password\": \"melardev\"\n" +
                "}";

        Request request = new Request.Builder()
                .url("https://reqres.in/api/register")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8")/*MediaType.parse("text/x-markdown; charset=utf-8")*/, postBody))
                .build();

        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            String responseStr = response.body().string();  //json2pojo already explained
            updateResult(responseStr);
            //For authentication tutorial
            String token = null;
            try {
                //token = new JSONObject(response.body().string()).getString("token"); IllegalStateException: closed!!
                token = new JSONObject(responseStr).getString("token");
                /*Request requestForAuthorizedUsers = new Request.Builder()
                        .url("toRestrictedUrl")
                        .addHeader("Authorization", token
                        //Credentials.basic("thisismy@email.com", "thisismypassword")
                        )
                        .build();
                        */
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Background
    public void doPostJson(View view) {
        String jsonStr = "{\n" +
                "    \"name\": \"Melardev\",\n" +
                "    \"job\": \"Student\"\n" +
                "}";

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);

        Request request = new Request.Builder()
                .url("https://reqres.in/api/users")
                .post(body)
                .build();


        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            updateResult(response.body().string()); //json2pojo already explained
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void doGetWithParams(View view) {
        String url = HttpUrl.parse("https://httpbin.org/get").newBuilder()
                .addEncodedQueryParameter("author", "Melar Dev") //for GET requests
                .addQueryParameter("category", "android") //for GET requests
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();


        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            updateResult(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void doFormPostWithParams(View view) {

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("httpbin.org")
                .addPathSegment("post")
                .build();

        FormBody form = new FormBody.Builder()
                .add("email", "thisismy@email.com")
                .add("password", "andthisismypassword")
                .build();


        try {
            updateResult(doSyncPost(okHttpClient, httpUrl, form));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void doPostWithParamsUseless(View view) {
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("email", "thisismy@email.com")
                .addFormDataPart("password", "andthismypassword")
                .build();

        Request request = new Request.Builder()
                .url("http://httpbin.org/post")
                .post(body)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            updateResult(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void doUploadFile(View view) {

        File file = new File(getCacheDir(), "fileToUpload.txt");
        if (!file.exists())
            writeToFile(file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputTextName", "inputTextValue")
                .addFormDataPart("file", "file.txt", RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                .url("http://httpbin.org/post")
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            updateResult(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }


        //MediaType.parse(getContentResolver().getType(Uri.fromFile(file)));
        //MediaType.parse("image/png");
        //http://www.freeformatter.com/mime-types-list.html
    }


    @Background
    public void doBasicAuth(View view) {
        //https://github.com/square/okhttp/wiki/Recipes#handling-authentication
        logThreadInfo("doBasicAuth");
        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        updateResult(response.headers().toString());
                        //String credential = Credentials.basic("jesse", "password1");
                        String credential = Credentials.basic("user", "passwd");
                        if (credential.equals(response.request().header("Authorization")))
                            return null; //do not retry again if already failed
                        return response.request().newBuilder()
                                .header("Authorization", credential) //vs addHeader
                                .build();
                    }
                })
                .build();
        Request request = new Request.Builder()
                //.url("http://publicobject.com/secrets/hellosecret.txt")
                .url("https://httpbin.org/basic-auth/user/passwd")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            Request requestPrev = response.request();
            updateResult("Response1:\n" + txtResult.getText() + "\n\nRequest2:\n" + requestPrev.headers().toString() + "\n\nResponse2:\n" + response.body().string())
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void doGetImage(View view) {
        try {
            OkHttpClient okhttp = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://httpbin.org/image/png")
                    .build();
            Response response = okhttp.newCall(request).execute();
            InputStream is = response.body().byteStream();

            Bitmap bmp = BitmapFactory.decodeStream(is);
            showImage(bmp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void showImage(Bitmap bmp) {
        ImageView imgView = new ImageView(this);
        imgView.setImageBitmap(bmp);
        dlg = new AlertDialog.Builder(this)
                .setView(imgView)
                .setTitle("Downloaded Image")
                .setCancelable(true)
                .show();
    }

    //************************** Settings Methods *************************//
    public void enableLogging() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //.networkInterceptors().add(httpLoggingInterceptor)
                .addNetworkInterceptor(httpLoggingInterceptor).build();
    }

    public void caching() {
        OkHttpClient client = new OkHttpClient();
        Cache cache = new Cache(new File(getCacheDir(), "okHttpCache"), 10 * 1024 * 1024);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Request request = new Request.Builder()
                .url("https://reqres.in/api/users")
                .cacheControl(new CacheControl.Builder().maxStale(365, TimeUnit.DAYS).noCache().build())
                .build();
    }

    public void settings() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        OkHttpClient.Builder copy = okHttpClient.newBuilder().writeTimeout(2, TimeUnit.SECONDS); //change configuration for one call
    }


    //************************** Helper Methods *************************//
    private void updateResult(String myResponse) {
        Message msg = Message.obtain();
        msg.obj = myResponse;
        mainThreadHandler.sendMessage(msg);

        //Message msg = Message.obtain(mainThreadHandler);
        //msg.sendToTarget();
    }

    public static String doSyncGet(OkHttpClient client, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public static String doSyncPost(OkHttpClient client, HttpUrl url, RequestBody body) throws IOException {
        return doSyncPost(client, url.toString(), body);
    }

    public static String doSyncPost(OkHttpClient client, String url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                //.method("POST", body) already done in post()
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void writeToFile(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write("This is the string contained in the fiel to be uploaded, Yiiiiiihaaaaaa!!!".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dlg != null && dlg.isShowing())
            dlg.dismiss();
    }
}
