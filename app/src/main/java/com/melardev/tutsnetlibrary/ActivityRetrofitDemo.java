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
import android.widget.Toast;

import com.melardev.tutsnetlibrary.apis.HttpBinApi;
import com.melardev.tutsnetlibrary.apis.ReqResApi;
import com.melardev.tutsnetlibrary.model.ReqResUnknown;
import com.melardev.tutsnetlibrary.model.ReqResUserCreation;
import com.melardev.tutsnetlibrary.model.UserReqResRequest;
import com.melardev.tutsnetlibrary.model.UserReqResResponse;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.melardev.tutsnetlibrary.ActivityOkHttpDemo.writeToFile;

@EActivity
public class ActivityRetrofitDemo extends AppCompatActivity {

    private static final String HTTPBIN_BASE_URL = "https://httpbin.org";
    private static final String REQRES_BASE_URL = "https://reqres.in";
    private TextView txtRetrofitResult;

    /**
     * Synchronous operations are performed in the UI thread by default, throwing hence an exception!
     * NetworkOnMainThreadException since API 4.0
     * Call objects are not reusable, you can not call execute() or enqueu multiple times, but you can clone() it
     * Retrofit automatically serialises the JSON response using a POJO(Plain Old Java Object) which must be defined in advanced for the JSON Structure.
     * To serialise JSON we need a converter to convert it into Gson first
     * When you define the interface to be used to perform network operations retrofit will map the response given from the remote server to the correspoding class
     * that you specify inside the Call<> return object, if the server does not return something or you just do not care then you specify Call<Void> as return type
     * Converters map the received response from the remote server to a Java object, and viceversa(converters are bidirectional)
     * Retrofit converters : https://github.com/square/retrofit/tree/master/retrofit-converters
     * only one single converter is used, even if you specify multiple converters through addConverterFactory(), they are queud so the first added converter will be used, if, he
     * can handle the response then that is it, otherwise the next in the queu will be asked to make the mapping over the result received from the server
     */

    Handler mainThreadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            txtRetrofitResult.setText(msg.obj.toString());
        }
    };
    private AlertDialog dlg;
    private OkHttpClient httpClient = new OkHttpClient();

    private void updateResult(String myResponse) {
        Message msg = Message.obtain();
        msg.obj = myResponse;
        mainThreadHandler.sendMessage(msg);
        //Message msg = Message.obtain(mainThreadHandler);
        //msg.sendToTarget();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrofit_demo);
        txtRetrofitResult = (TextView) findViewById(R.id.txtRetrofitResult);
    }

    @Background
    public void retrofitSynchronous(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);
        Call<ResponseBody> callableResponse = service.get();
        try {
            Response<ResponseBody> response = callableResponse.execute(); //execute() for synchronous operations
            updateResult(getStrFromResponseBody(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> String getStrFromResponseBody(Response<T> response) throws IOException {
        String body = "";
        if (response.body() instanceof ResponseBody)
            body = ((ResponseBody) response.body()).string();
        else if (response.body() == null)
            body = "";
        else
            body = response.body().toString();
        return String.format(Locale.US, "headers:\n %s, \nmessage \n: %s\nbody:\n %s\n", response.headers(), response.message(), body);
    }

    public void retrofitAsynchronous(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);
        Call<ResponseBody> callableResponse = service.get();
        callableResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        updateResult(getStrFromResponseBody(response));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    txtRetrofitResult.setText("get() not succesful");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ActivityRetrofitDemo.this, "onFailure called", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void retroDoGetAndSerialize(View view) {
        ReqResApi service = ServiceGenerator.createService(ReqResApi.class, REQRES_BASE_URL);
        Call<ReqResUnknown> callable = service.reqResdoGetUnknown();
        dumpCallableResponse(callable);
    }

    public void retroReqResCreateUser(View view) {
        ReqResApi service = ServiceGenerator.createService(ReqResApi.class, REQRES_BASE_URL);
        UserReqResRequest userReqRes = new UserReqResRequest("Melardev", "youtuber");
        Call<UserReqResResponse> callable = service.reqRescreateUser(userReqRes);
        dumpCallableResponse(callable);
    }

    public void retroReqResdoCreateUserWithField(View view) {
        ReqResApi service = ServiceGenerator.createService(ReqResApi.class, REQRES_BASE_URL);
        Call<ResponseBody> callable = service.reqResdoCreateUserWithField("Melardev", "youtuber");
        dumpCallableResponse(callable);
    }

    public void retroSendSimplePostNoConverters(View view) {

        //Not using converters means that you have to implement by yourself the MIME type, and parse
        //by yourself the response, if you do not want to deal with that, then you can use converters

        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(HTTPBIN_BASE_URL);
        builder.client(httpClient);
        Retrofit retrofit = builder.build();
        HttpBinApi service = retrofit.create(HttpBinApi.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), "your text");
        Call<ResponseBody> callable = service.post(requestBody);
        //Call<ResponseBody> callable = service.post("some text"); IllegalArgumentException Could not locate converter for String
        dumpCallableResponse(callable);
    }

    public void retroSendScalar(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL,
                new Converter.Factory[]{ScalarsConverterFactory.create(), GsonConverterFactory.create()});
        Call<String> callable = service.sendScalar("basic text");
        dumpCallableResponse(callable);
    }

    public void retroBasicAuth(View view) {
        //Approach 1 : Remember that you can use Authenticator class for your OkHttpClient
        //Aproach 2 : Using Interceptors as follows
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(HTTPBIN_BASE_URL);
        ServiceGenerator.AuthenticationInterceptor interceptor = new ServiceGenerator.AuthenticationInterceptor(
                Credentials.basic("user", "passwd")
        );

        if (!httpClient.interceptors().contains(interceptor)) {
            httpClient.addInterceptor(interceptor);

            builder.client(httpClient.build());
            Retrofit retrofit = builder.build();
            HttpBinApi service = retrofit.create(HttpBinApi.class);
            Call<ResponseBody> response = service.basicAuth();
            dumpCallableResponse(response);
        }
    }


    public void retroGetWithParams(View view) {
        ReqResApi service = ServiceGenerator.createService(ReqResApi.class, REQRES_BASE_URL);
        dumpCallableResponse(service.getUsers("2"));
        //dumpCallableResponse(service.getUserById(2));
    }

    public void retroFormWithFields(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);
        Map<String, String> map = new HashMap<>();
        map.put("name", "Melardev");
        map.put("job", "student");
        dumpCallableResponse(service.post(map));
    }

    public void retroSendObjAsPost(View view) {
        ReqResApi service = ServiceGenerator.createService(ReqResApi.class, REQRES_BASE_URL);
        UserReqResRequest user = new UserReqResRequest("Melardev", "thisismypassword");
        Call<ResponseBody> callableResponse = service.createUser(user);
        dumpCallableResponse(callableResponse);
    }

    public void retroTestHeaders(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);
        //Call<Void> callableResponse = service.testStaticHeaders();
        //Call<Void> callableResponse = service.testDynamicHeaders("some/porno-type");
        Map<String, String> map = new HashMap<>();
        map.put("Content-type", "some/porno-type");
        map.put("x-powered-by", "Melardev");
        map.put("server", "Melardev-nginx");
        Call<Void> callableResponse = service.testDynamicMapHeaders(map);
        dumpCallableResponse(callableResponse);

        //In Retrofit 1.9 You had to use interceptors to override the headers. RequestInterceptor
    }

    @Background
    public void retroGetImage(View view) {
        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);
        Call<ResponseBody> callable = service.getImage("https://httpbin.org/image/png");
        ; //Note that even if you specifie a relative url ,this still works because the library will concatenate the baseUrl with the relative
        try {
            Response<ResponseBody> response = callable.execute();
            InputStream is = response.body().byteStream();
            Bitmap bmp = BitmapFactory.decodeStream(is);
            showImage(bmp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void retroUploadFile(View view) {
        uploadFile();
    }

    public void uploadFile() {
        File file = new File(getCacheDir(), "fileToUpload.txt");
        if (!file.exists())
            writeToFile(file);

        HttpBinApi service = ServiceGenerator.createService(HttpBinApi.class, HTTPBIN_BASE_URL);

        Uri uri = Uri.fromFile(file);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("myfile", file.getName(), requestBody);

        String descriptionString = "description yuhu";
        RequestBody description = RequestBody.create(MultipartBody.FORM, descriptionString);
        Call<ResponseBody> callable = service.uploadFile(description, body);
        dumpCallableResponse(callable);
    }

    private <T> void dumpCallableResponse(Call<T> callableResponse) {
        //TODO: request() has to be called in a background thread, since it makes some heavy work
        Request request = callableResponse.request();
        try {
            Buffer buffer = new Buffer();
            String show = request.toString() + "headers: " + request.headers() + "\n";
            if (request.body() != null) {
                request.body().writeTo(buffer);
                show += "Body : " + buffer.readString(Charset.defaultCharset());
            }

            updateResult(show + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        callableResponse.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                try {
                    updateResult(txtRetrofitResult.getText() + ("\nResponse : " + getStrFromResponseBody(response)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                String str = "onFailure";
                if (t instanceof Exception)
                    str += ((Exception) t).getMessage();
                Toast.makeText(ActivityRetrofitDemo.this, str, Toast.LENGTH_SHORT).show();
            }
        });
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

}
