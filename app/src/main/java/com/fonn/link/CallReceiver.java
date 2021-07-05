package com.fonn.link;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallReceiver extends BroadcastReceiver {
    NotificationManager notificationManager;
    Context c;
    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
//        String action=intent.getStringExtra("action");
//        if(action.equals("Accept")){
//           performAction1();
//           // Toast.makeText(context, "dsadsad", Toast.LENGTH_SHORT).show();
//        }
//        else if(action.equals("Decline")){
//            performAction2();
//
//        }
        //This is used to close the notification tray
        //notificationManager.cancel(10);
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

//    public void performAction1(){
//
//            //LinphoneService.getInstance().answerCall();
//            //HomeFragment.oncall = true;
//           // LinphoneService.getInstance().startActivity(c,Dashboard.class);
//
//
//    }

//    public void performAction2() {
//        Core core = LinphoneService.getCore();
//        if (core.getCallsNb() > 0) {
//            Call call = core.getCurrentCall();
//            if (call == null) {
//                // Current call can be null if paused for example
//                call = core.getCalls()[0];
//            }
//            call.terminate();
//        }
//
//
//    }
}

