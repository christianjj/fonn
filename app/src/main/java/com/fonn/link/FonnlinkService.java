package com.fonn.link;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fonn.link.fragments.HomeFragment;
import com.fonn.link.interfaces.activityListener;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import static com.fonn.link.fragments.HomeFragment.Mypref;
import static com.fonn.link.fragments.HomeFragment.callcpuntpref;
import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;

public class FonnlinkService extends Service implements SensorEventListener {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    // Keep a static reference to the Service so we can access it from anywhere in the app
    private static FonnlinkService sInstance;
    public activityListener activityListener;
    //Proximity
    private boolean mProximitySensingEnabled;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private PowerManager mPowerManager;
    private WakeLock mProximityWakelock;
    boolean willcount;
    int c;
    private Handler mHandler;
    private Timer mTimer;
    public static Call mcall;
    private Core mCore;
    public CoreListenerStub mCoreListener;
    NotificationManager notificationManager;

    public static boolean isReady() {
        return sInstance != null;
    }

    public static FonnlinkService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    public static Call getCall() {
        return mcall;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The first call to liblinphone SDK MUST BE to a Factory method
        // So let's enable the library debug logs & log collection
        String basePath = getFilesDir().getAbsolutePath();
       // Factory.instance().setLogCollectionPath(basePath);
       // Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));
        // Dump some useful information about the device we're running on
        //Log.i(START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();
        mPowerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mHandler = new Handler();


        // This will be our main Core listener, it will change activities depending on events
        mCoreListener = new CoreListenerStub() {
            @SuppressLint("WrongConstant")
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {

              //  Toast.makeText(FonnlinkService.this, message, Toast.LENGTH_SHORT).show();
                if (state == Call.State.IncomingReceived) {
                    // For this sample we will automatically answer incoming calls
                    sendNotification();
                    getInstance().activityListener.onIncomingActivity();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
//                        Intent intent = new Intent(FonnlinkService.this, Dashboard.class);
//                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
//                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
//                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
//                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//                        startActivity(intent);
                   }


                } else if (state == Call.State.Connected) {
                    // This stats means the call has been established, let's start the call activity

                    enableProximitySensing(true);
                    willcount = true;
                    notificationManager.cancel(10);
                    getInstance().activityListener.onCallActivity();

                } else if ((state == Call.State.End) || (state == Call.State.Released)) {
                    notificationManager.cancel(10);
                    getInstance().activityListener.onEndCall();
                    if(willcount) {
                        c = Integer.parseInt(HomeFragment.textCallCount.getText().toString());
                        c += 1;
                        HomeFragment.textCallCount.setText(String.valueOf(c));
                        savepref();
                        willcount= false;
                    }

                    if (call.getErrorInfo().getReason() == Reason.Declined) {
                        notificationManager.cancel(10);
                    }

                    stopService(
                            new Intent().setClass(getApplicationContext(), wakeupService.class));

                    enableProximitySensing(false);
                }


            }

            public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState state, String s) {

                if (state == RegistrationState.Ok) {
                    HomeFragment.status.setText(R.string.ready);
                } else if (state == RegistrationState.Cleared) {
                    HomeFragment.status.setText(R.string.disconnected);
                }  else if (state == RegistrationState.Progress) {
                    HomeFragment.status.setText(R.string.connecting);
                }
            }




        };

        try {
            // Let's copy some RAW resources to the device
            // The default config file must only be installed once (the first time)
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");

            // The factory config is used to override any other setting, let's copy it each time
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            //   Log.e(ioe);
        }


        // Create the Core and add our listener
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        // Core is ready to be configured

        mCore.ensureRegistered();
        mCore.keepAliveEnabled();
        mCore.setEnableSipUpdate(1);
        mCore.enableEchoCancellation(true);
        mCore.enableDnsSrv(true);


        configureCore();


    }

    /* Proximity sensor stuff */
    private void enableProximitySensing(boolean enable) {
        if (enable) {
            if (!mProximitySensingEnabled) {
                mSensorManager.registerListener(
                        this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
                mProximitySensingEnabled = true;
            }
        } else {
            if (mProximitySensingEnabled) {
                mSensorManager.unregisterListener(this);
                mProximitySensingEnabled = false;
                // Don't forgeting to release wakelock if held
                if (mProximityWakelock.isHeld()) {
                    mProximityWakelock.release();
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // If our Service is already running, no need to continue
        if (sInstance != null) {
            return START_STICKY;
        }



        // Our Service has been started, we can keep our reference on it
        // From now one the Launcher will be able to call onServiceReady()
        sInstance = this;


       // mCore.enterBackground();
        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(
                                () -> {
                                    if (mCore != null) {
                                        mCore.iterate();
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);

//
//        Core core = getCore();
//        Call call = core.getCurrentCall();
//        if (call == null) {
//
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // Do something after 5s = 5000ms
//                    stopService(
//                            new Intent().setClass(getApplicationContext(), wakeupService.class));
//                }
//            }, 3000);


        //     }


        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        mTimer.cancel();
        mCore.stop();
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;


        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }


    public void setStunServer(String stun) {
        if (getCore() == null) return;
        NatPolicy nat = getOrCreateNatPolicy();
        nat.setStunServer(stun);
        getCore().setNatPolicy(nat);
    }


    public NatPolicy getOrCreateNatPolicy() {
        if (getCore() == null) return null;
        NatPolicy nat = getCore().getNatPolicy();
        if (nat == null) {
            nat = getCore().createNatPolicy();
        }
        return nat;
    }

    public void setIceEnabled(boolean enabled) {
        if (getCore() == null) return;
        NatPolicy nat = getOrCreateNatPolicy();
        nat.enableIce(enabled);
        if (enabled) nat.enableStun(true);
        getCore().setNatPolicy(nat);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                //     Log.e(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);

        mProximityWakelock =
                mPowerManager.newWakeLock(
                        PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        getApplicationContext().getPackageName() + ";manager_proximity_sensor");
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        //  Log.i(sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            //  Log.e(nnfe);
        }

        if (info != null) {
//            Log.i(
//                    "[Service] Linphone version is ",
//                    info.versionName + " (" + info.versionCode + ")");
        } else {
//            Log.i("[Service] Linphone version is unknown");
        }
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public void startActivity(Context context, Class s) {
        Intent intent1 = new Intent(context, s);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

    }

    public String getAddressDisplayName(Address address) {
        if (address == null) return null;

        String displayName = address.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.getUsername();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = address.asStringUriOnly();
        }
        return displayName;
    }

    public String getAddressname() {
        String address = getAddressDisplayName(FonnlinkService.getCore().getCurrentCallRemoteAddress());
        return address;
    }

    public void answerCall() {

        Core core = FonnlinkService.getCore();
        CallParams params = core.createCallParams(mcall);
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                // Current call can be null if paused for example
                call = core.getCalls()[0];
            }
            call.acceptWithParams(params);
        }
    }

    public void lookupCurrentCall() {
        if (FonnlinkService.getCore() != null) {
            for (Call call : FonnlinkService.getCore().getCalls()) {
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()) {
                    mcall = call;
                    break;


                }
            }
        }
    }

    public void sendNotification() {
        lookupCurrentCall();
        String address = getAddressDisplayName(FonnlinkService.getCall().getCore().getCurrentCallRemoteAddress());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String Notification_Channel_ID = "christian";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(Notification_Channel_ID,
                    "My Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("app testing FCM");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, LauncherActivity.class);
//        Intent intentAccept = new Intent(getApplicationContext(), CallReceiver.class);
//        intentAccept.putExtra("action", "Accept");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 113, intent, PendingIntent.FLAG_UPDATE_CURRENT);
       // PendingIntent pIntentAccept = PendingIntent.getBroadcast(getApplicationContext(), 0, intentAccept, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Notification_Channel_ID);
        notificationBuilder.setAutoCancel(false)
                //   .setSound(null, null)

                .setWhen(System.currentTimeMillis())
                .setTicker("Hearty365")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(address)
                .setContentText("Incoming call")
                .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setSmallIcon(R.drawable.ic_baseline_call_24)
                .setFullScreenIntent(pendingIntent, true)
                .setOngoing(true)
                .setAutoCancel(true)

//                .addAction(R.drawable.ic_baseline_call_24, getString(R.string.AcceptCall),
//                        pIntentAccept)
//                .addAction(R.drawable.ic_baseline_call_end_24, getString(R.string.DeclineCall),
//                        pIntentDecline)
                .setContentInfo("info");
        notificationManager.notify(10, notificationBuilder.build());


    }

    public String getProfilename() {
        String profilename = getAddressDisplayName(FonnlinkService.getCore().getDefaultProxyConfig().getIdentityAddress());
        return profilename;

    }

    public String getProfilenameaddress() {
        String profileuseraddress = FonnlinkService.getCore().getDefaultProxyConfig().getIdentityAddress().asStringUriOnly();
        return profileuseraddress;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.timestamp == 0) return;
        if (isProximitySensorNearby(sensorEvent)) {
            if (!mProximityWakelock.isHeld()) {
                mProximityWakelock.acquire(10*60*1000L /*10 minutes*/);
            }
        } else {
            if (mProximityWakelock.isHeld()) {
                mProximityWakelock.release();
            }
        }

    }

    private Boolean isProximitySensorNearby(final SensorEvent event) {
        float threshold = 4.001f; // <= 4 cm is near

        final float distanceInCm = event.values[0];
        final float maxDistance = event.sensor.getMaximumRange();
//        Log.d(
//                "[Manager] Proximity sensor report ["
//                        + distanceInCm
//                        + "] , for max range ["
//                        + maxDistance
//                        + "]");

        if (maxDistance <= threshold) {
            // Case binary 0/1 and short sensors
            threshold = maxDistance;
        }
        return distanceInCm < threshold;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void savepref(){
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(Mypref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(callcpuntpref, HomeFragment.textCallCount.getText().toString());
        editor.apply();
    }



}

