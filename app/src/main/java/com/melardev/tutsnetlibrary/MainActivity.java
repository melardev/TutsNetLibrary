package com.melardev.tutsnetlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    /**
     * test web services
     * http://httpbin.org/
     * http://www.posttestserver.com/
     * http://putsreq.com/
     * https://resttesttest.com/
     * http://greensuisse.zzl.org/product/dump/dump.php
     * http://www.newburghschools.org/testfolder/dump.php
     * https://www.mockable.io/
     * http://requestb.in/
     * Repo for local server https://github.com/prabodhprakash/localTestingServer
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void okDemo(View view) {
        startDemo(ActivityOkHttpDemo_.class);
    }

    private void startDemo(Class activityOkHttpDemoClass) {
        startActivity(new Intent(this, activityOkHttpDemoClass));
    }

    public void retrofitDemo(View view) {
        startDemo(ActivityRetrofitDemo_.class);
    }

    public void retrofitOAuth(View view) {
        startDemo(ActivityRetroFitOAuth.class);
    }

    public void retrofitRxJava(View view) {
        startDemo(ActivityRxJava.class);
    }
}
