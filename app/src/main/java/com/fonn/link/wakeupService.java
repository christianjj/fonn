package com.fonn.link;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import static com.fonn.link.Applicationclass.CHANNEL_ID;

public class wakeupService extends Service  {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, LauncherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fonnlink")
                .setContentText("Fonnlink is running")
                .setSmallIcon(R.drawable.appicon,1)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build();
        startForeground(1,notification);
        Intent service = new Intent(this, FonnlinkService.class);
        startService(service);




        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
