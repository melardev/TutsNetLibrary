package com.melardev.tutsnetlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.melardev.tutsnetlibrary.apis.HttpBinApi;
import com.melardev.tutsnetlibrary.apis.ReqResApi;
import com.melardev.tutsnetlibrary.model.UserReqResResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

public class ActivityRxJava extends AppCompatActivity {

    CompositeDisposable disposables = new CompositeDisposable();
    private String TAG = getClass().getName();
    private AlertDialog dlg;
    private TextView txtRxResult;
    private rx.Subscription subscriptionRx1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        txtRxResult = (TextView) findViewById(R.id.txtRxResult);
        //this.subscription = observable.subscribe(this);//rx1
    }


    public void runSh() {
        //RxAndroid 2
        Observable<String> obsSearchEngine = Observable.defer(
                new Callable<ObservableSource<? extends String>>() {
                    @Override
                    public ObservableSource<? extends String> call() throws Exception {
                        return Observable.just("Google", "Bing", "Yahoo", "DuckDuckGo", "Ask", "Yandex", "IxQuick", "Dogpile");
                    }
                }
        );

        DisposableObserver<String> observable = obsSearchEngine
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(@NonNull String s) {
                        Log.d(TAG, "onComplete() -> " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onNext()");
                    }
                });
        disposables.add(observable);
    }

    public void retrofit() {
        /*
        Note also that subscribing an observer to the observable is what triggers the network request. For more information about how to attach multiple observers before dispatching the network requests, see this section.
         */
        //Retrofit 2 also supports RxJava extensions. You will need to create an RxJava Adapter. By default, all network calls are synchronous:
        //RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.create();
        //If you wish to default network calls to be asynchronous, you need to use createWithScheduler().
        //rxJava 1
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(rx.schedulers.Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build();

        ReqResApi service = retrofit.create(ReqResApi.class);
        rx.Observable<List<UserReqResResponse>> observable = service.getUsersInPageRx_1(String.valueOf(2));

        observable.observeOn(rx.android.schedulers.AndroidSchedulers.mainThread()).subscribeOn(rx.schedulers.Schedulers.io())
                .subscribe(new rx.Subscriber<List<UserReqResResponse>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<UserReqResResponse> userReqRes) {

                    }
                });
    }

    public void retrofit2RxJava1() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        HttpBinApi service = retrofit.create(HttpBinApi.class);

        rx.Observable<ResponseBody> observable = service.rxGet();//note that the request is fired on subscribe() and not here

        //hold this reference to unsubscribe, to prevent memory leaks
        subscriptionRx1 = observable
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribeOn(rx.schedulers.Schedulers.io())
                //.subscribe(responseBody -> updateTxt(responseBody), throwable -> txtRxResult.setText(throwable.getMessage()), () -> txtRxResult.setText("completed"))
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        txtRxResult.setText("completed");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        txtRxResult.setText(throwable.getMessage());
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        updateTxt(responseBody);
                    }
                });

        //You can implement a cache
        /*LruCache<Class<ResponseBody>, rx.Observable<ResponseBody>> apiObservables = new LruCache<>(10);
        if(apiObservables.get() == null)
            apiObservables.put()*/
    }

    private void updateTxt(ResponseBody responseBody) {
        try {
            txtRxResult.setText(responseBody.string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private rx.Subscription subscription;

    @Override
    protected void onDestroy() {
        //Caution with memory leaks
        disposables.clear();
        this.subscription.unsubscribe(); //rxJava 1

        if (subscriptionRx1 != null && subscriptionRx1.isUnsubscribed())
            subscriptionRx1.unsubscribe();
        super.onDestroy();
    }

    void retrofit2RxJAva2() {

        RxJava2CallAdapterFactory rxAdapter = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build();

        ReqResApi service = retrofit.create(ReqResApi.class);
        Observable<List<UserReqResResponse>> observable = service.getUsersInPageRx_2(String.valueOf(2));

        observable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(new DefaultObserver<List<UserReqResResponse>>() {
                    @Override
                    public void onNext(List<UserReqResResponse> userReqRes) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void simpleAsyncTaskToRxJava(String url) {
        Observable.generate(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return null;
            }
        }, new BiConsumer<Bitmap, Emitter<Bitmap>>() {
            @Override
            public void accept(Bitmap bitmap, Emitter<Bitmap> emitter) throws Exception {
                emitter.onNext(bitmap);
            }
        });


        Observable.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {

                Bitmap mIcon11 = null;
                try {
                    InputStream in = new URL(url).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                            showImage(bitmap);
                        },
                        throwable -> Toast.makeText(this, "Error retrieving the image", Toast.LENGTH_SHORT).show());

        rx.Observable<Bitmap> observableBmp = rx.Observable.create(new rx.Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new URL(url).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                subscriber.onNext(mIcon11);
                subscriber.onCompleted();
            }
        });

        Subscription mySubscription = observableBmp
                .subscribeOn(rx.schedulers.Schedulers.io()) //perform the work in the bacgrkound
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread()) //wait for results in main thread(onNext, onCompleted onError
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.d("", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        showImage(bitmap);
                    }
                });
    }


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
