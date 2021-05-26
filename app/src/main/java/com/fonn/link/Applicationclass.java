package com.fonn.link;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;
import org.json.JSONObject;

public class Applicationclass extends Application {

    private static final String ONESIGNAL_APP_ID = "9789faf0-7a32-4d38-8a4d-16ce13025528";
    public static final String CHANNEL_ID = "fonnlinkChannel";
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);




        OneSignal.setNotificationWillShowInForegroundHandler(notificationReceivedEvent -> {
            JSONObject data = notificationReceivedEvent.getNotification().getAdditionalData();
            OSNotification notification = notificationReceivedEvent.getNotification();

            notificationReceivedEvent.complete(null);





        });


    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel servicechannel = new NotificationChannel(
                    CHANNEL_ID,"fonnlinkChannel", NotificationManager.IMPORTANCE_DEFAULT

            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(servicechannel);
        }
    }
}
