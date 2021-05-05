package com.fonn.link;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fonn.link.fragments.HomeFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LauncherActivity extends Activity {
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher);
        checkingUpdatedAds();
        mHandler = new Handler();
    }

    private void checkingUpdatedAds() {

        android.util.Log.i("okhttp", "sending post");

        String url = "https://opis.link/api/ads";
        OkHttpClient client = new OkHttpClient();

        //  RequestBody body  = RequestBody.create(json,data.toString());
        Request newreq = new Request.Builder().url(url).build();


        client.newCall(newreq).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                String mMessage = e.getMessage();
                android.util.Log.i("okhttp", mMessage);

            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                String mMessage = response.body().string();
                String responseCode = null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("path");
                    HomeFragment.urlads = "https://opis.link"+responseCode;
                    // Log.d("okhttpp","https://opis.link"+responseCode);


                } catch (JSONException e) {
                    e.printStackTrace();

                }
                android.util.Log.i("okhttp", mMessage);
            }

        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check whether the Service is already running
        if (FonnlinkService.isReady()) {
            onServiceReady();
        } else {
            // If it's not, let's start it
            startService(
                    new Intent().setClass(this, FonnlinkService.class));
            // And wait for it to be ready, so we can safely use it afterwards
            new ServiceWaitThread().start();
        }
    }

    private void onServiceReady() {
        // Once the service is ready, we can move on in the application
        // We'll forward the intent action, type and extras so it can be handled
        // by the next activity if needed, it's not the launcher job to do that
        Intent intent = new Intent();
        intent.setClass(LauncherActivity.this, Dashboard.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(getIntent().getAction());
        intent.setType(getIntent().getType());
        startActivity(intent);
    }

    // This thread will periodically check if the Service is ready, and then call onServiceReady
    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!FonnlinkService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            // As we're in a thread, we can't do UI stuff in it, must post a runnable in UI thread
            mHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            onServiceReady();
                        }
                    });
        }
    }
}
