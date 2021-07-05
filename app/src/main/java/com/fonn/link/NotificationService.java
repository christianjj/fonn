package com.fonn.link;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal.OSRemoteNotificationReceivedHandler;

import org.json.JSONObject;


public class NotificationService implements OSRemoteNotificationReceivedHandler {

    NotificationManager notificationManager = null;
    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        OSNotification notification = notificationReceivedEvent.getNotification();

        // Example of modifying the notification's accent color
        OSMutableNotification mutableNotification = notification.mutableCopy();
        mutableNotification.setExtender(builder -> builder.setColor(context.getResources().getColor(R.color.white)));

        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
        // To omit displaying a notifiation, pass `null` to complete()
        JSONObject data = notification.getAdditionalData();

        Log.d("OneSignalExample", "Received Notification Data: " + data);

      //  Looper.prepare();


       // Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
       // Looper.loop();
        work.oneoffRequest();
        //OneSignal.clearOneSignalNotifications();
        notificationReceivedEvent.complete(null);



    }






}
