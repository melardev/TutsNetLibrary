package com.melardev.tutsnetlibrary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.melardev.tutsnetlibrary.apis.GithubApi;
import com.melardev.tutsnetlibrary.model.pojo.GitAuthenticatedUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ActivityRetroFitOAuth extends AppCompatActivity {

    private static final String GIT_BASE_URL = "http://github.com";
    private static final String GIT_CLIENT_ID = "d1fbdd0d71c829bd82ad";
    private static final String GIT_CLIENT_SECRET = "22a337e90f16c950ff4d55871b040b25c51ed2f0";
    private final String TAG = getClass().getName();

    private final String CALLBACK_REDIRECT = "tutsnet://melardev.oauth.handler";

    private TextView txtGitCode;
    private TextView txtGitState;
    private TextView txtGitNameEmail;
    private TextView txtGitTokenType;
    private TextView txtGitAccessToken;
    private ImageView imgGitProfile;
    private SecureRandom random = new SecureRandom();
    private Button btnGetCode;
    private Button btnGetToken;
    private Interceptor mAuthInterceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retro_fit_oauth);

        txtGitCode = (TextView) findViewById(R.id.txtGitCode);
        txtGitState = (TextView) findViewById(R.id.txtGitState);
        txtGitAccessToken = (TextView) findViewById(R.id.txtGitAccessToken);
        txtGitTokenType = (TextView) findViewById(R.id.txtGitTokenType);
        txtGitNameEmail = (TextView) findViewById(R.id.txtGitNameEmail);

        btnGetCode = (Button) findViewById(R.id.btnGetCode);
        btnGetToken = (Button) findViewById(R.id.btnGetToken);

        txtGitNameEmail = (TextView) findViewById(R.id.txtGitNameEmail);
        imgGitProfile = (ImageView) findViewById(R.id.imgGitProfile);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().toString().startsWith(CALLBACK_REDIRECT)) {
            String code = intent.getData().getQueryParameter("code");
            String state = intent.getData().getQueryParameter("state");

            txtGitCode.setText(code);
            txtGitState.setText(state);

            btnGetCode.setEnabled(false);
            btnGetToken.setEnabled(true);
        }
    }

    public void getCode(View view) {
        //Step 1 : GET http://github.com/login/oauth/authorize
        btnGetCode.setEnabled(false);
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", GIT_CLIENT_ID)
                .addQueryParameter("redirect_uri", CALLBACK_REDIRECT)
                .addQueryParameter("state", getRandomString())
                .addQueryParameter("scope", "user:email")
                .build();

        Log.d(TAG, httpUrl.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()));
        startActivity(intent);
    }

    public void getToken(View view) {
        //Step 2 POST https://github.com/login/oauth/access_token

        btnGetToken.setEnabled(false);

        GithubApi service = ServiceGenerator.createService(GithubApi.class, GIT_BASE_URL, new Converter.Factory[]{ScalarsConverterFactory.create()});
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("client_id", GIT_CLIENT_ID);
        fieldMap.put("client_secret", GIT_CLIENT_SECRET);
        fieldMap.put("code", txtGitCode.getText().toString());
        fieldMap.put("redirect_uri", CALLBACK_REDIRECT);
        fieldMap.put("state", txtGitState.getText().toString());

        Call<String> response = service.getToken(fieldMap);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                //access_token=e72e16c7e42f292c6912e7710c838347ae178b4a&token_type=bearer
                String gitTokenResponse = response.body();
                String[] splitted = gitTokenResponse.split("=|&");
                txtGitTokenType.setText(splitted[1]);
                txtGitAccessToken.setText(splitted[3]);

                getUserInfo(splitted[1]);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                btnGetToken.setEnabled(true);
                t.printStackTrace();
                Toast.makeText(ActivityRetroFitOAuth.this, "onFailure: " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(String token) {
        mAuthInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder builder = original.newBuilder()
                        .header("Accept", "application/vnd.github.v3+json") //by default already is giving v3, but it is encouraged to request so explicitly
                        .header("Authorization", "token " + token);
            /*or you can pass the token as query HttpUrl newUrl = original.url().newBuilder()
                    .addQueryParameter("access_token", token)
                    .build();
            builder.url(newUrl);
            */
                Request request = builder.build();
                return chain.proceed(request);
            }
        };

        GithubApi service = ServiceGenerator.createService(GithubApi.class, GIT_BASE_URL, mAuthInterceptor);
        Call<GitAuthenticatedUser> response = service.getAuthenticatedUserInfo();
        response.enqueue(new Callback<GitAuthenticatedUser>() {
            @Override
            public void onResponse(Call<GitAuthenticatedUser> call, retrofit2.Response<GitAuthenticatedUser> response) {
                GitAuthenticatedUser userInfo = response.body();
                txtGitNameEmail.setText(userInfo.getName() + ",\n" + userInfo.getBlog());
                Picasso.with(ActivityRetroFitOAuth.this).load(userInfo.getAvatarUrl()).into(imgGitProfile);
            }

            @Override
            public void onFailure(Call<GitAuthenticatedUser> call, Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(ActivityRetroFitOAuth.this, "onFailure getUserInfo()", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRandomString() {
        return new BigInteger(130, random).toString(32);
    }
}
