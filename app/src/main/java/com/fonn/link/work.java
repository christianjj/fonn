package com.fonn.link;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.linphone.core.AccountCreator;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;

import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class work extends Worker {

    ProxyConfig cfg;
    AccountCreator mAccountCreator;
    Context mcontext;
    public work(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
        this.mcontext = context;
    }

    @SuppressLint("WrongConstant")
    @NonNull
    @Override
    public Result doWork() {
//        Intent service = new Intent(this, MainActivity.class);
//        startService(service);

        //Intent service = new Intent(getApplicationContext(), LinphoneService.class);




        getApplicationContext().startService(
                new Intent().setClass(getApplicationContext(), wakeupService.class));
            // And wait for it to be ready, so we can safely use it afterwards

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void run() {
//                Intent service = new Intent(mcontext, wakeupService.class);
//                service.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                mcontext.startForegroundService(service);
//
//
//
//            }
//        });


        return Result.success(); //true - success / false - failure

    }


    public static void oneoffRequest(){
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(work.class)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setConstraints(setcons())
                .build();
        WorkManager.getInstance().enqueue(oneTimeWorkRequest);
    }

    public static Constraints setcons(){
        return new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();
    }





}
