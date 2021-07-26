package com.fonn.link;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fonn.link.fragments.HomeFragment;
import com.fonn.link.interfaces.RegistrationListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.FirebaseApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.CoreListener;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.fonn.link.FonnlinkService.getCore;
import static com.fonn.link.OTPactivity.MyPREFERENCES;
import static com.fonn.link.OTPactivity.finish;
import static com.fonn.link.OTPactivity.finishotp;


import static com.fonn.link.fragments.HomeFragment.ads;
import static com.fonn.link.fragments.HomeFragment.urlads;
import static java.lang.Thread.sleep;
import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;

public class Dashboard extends AppCompatActivity implements RegistrationListener {

    private AppBarConfiguration mAppBarConfiguration;
    private PowerManager.WakeLock wakeLock;
    DrawerLayout drawer;
    private long timeofping;
    int pingvalue;
    int signal;
    private Handler mHandler;
    private CoreListener mCoreListener;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;
    private int totalvalue;
    public static CountDownTimer countDownTimer;
    @SuppressLint("HardwareIds")
    String mydeviceId;
    boolean countdown = false;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    AlertDialog alertDialog;

    @SuppressLint({"WakelockTimeout", "BatteryLife", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dashboard);
        checkAndRequestCallPermissions();

        PowerManager pwm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pwm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
        wakeLock.acquire();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);


        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        FonnlinkService.getInstance().registrationListener = this;
        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (!isNetworkConnected()) {
            HomeFragment.signal.setImageResource(R.drawable.signal_0);
        }
        mHandler = new Handler();

//        OSDeviceState device = OneSignal.getDeviceState();
//        assert device != null;
//        if (device.getUserId()==null){
//            Toast.makeText(this, "push disable", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(this, "push enable", Toast.LENGTH_SHORT).show();
//        }
        mydeviceId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        startService(
                new Intent().setClass(getApplicationContext(), wakeupService.class));

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home,R.id.nav_profile, R.id.nav_history, R.id.nav_setting, R.id.navLogout, R.id.nav_uploadimage)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);;

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {

            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_history){
                    toolbar.setVisibility(View.GONE);
                }
                if(destination.getId() == R.id.nav_profile){
                    toolbar.setVisibility(View.GONE);
                }
                if(destination.getId() == R.id.nav_setting){
                    toolbar.setVisibility(View.GONE);
                }
                if(destination.getId() == R.id.nav_uploadimage){
                    toolbar.setVisibility(View.GONE);
                }

            }

        });

        drawer.setScrimColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.ic_menuicon);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

    }


    @Override
    public void onResume() {



        super.onResume();

        UpdateApp();
            if (FonnlinkService.isReady()) {
                getCore().addListener(mCoreListener);
                //getCore().addListener(FonnlinkService.getInstance().mCoreListener);
                ProxyConfig proxyConfig = getCore().getDefaultProxyConfig();
                if (proxyConfig != null) {
                    updateLed(proxyConfig.getState());
                    if(!countdown) {
                        getLoginStatus();
                    }
                    //OneSignal.setEmail(LinphoneService.getInstance().getProfilename()+"@sysnet.com");
                } else {
                    // No account configured, we display the configuration activity
                    startActivity(new Intent(getContext(), ConfigureAccountActivity.class));

                }

                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                finish = sharedpreferences.getBoolean(finishotp, false);
                if (!finish) {
                    getCore().clearProxyConfig();
//            startActivity(new Intent(getContext(), ConfigureAccountActivity.class));
                    // Toast.makeText(this, "not finish", Toast.LENGTH_SHORT).show();
                }

                doConnectionScan();

                    if (urlads != null) {
                        Glide.with(this).load(urlads).into(ads);
                    } else {
                        FonnlinkService.getInstance().startActivity(this, LauncherActivity.class);
                    }

            } else {
                // If it's not, let's start it
                startService(
                        new Intent().setClass(this, FonnlinkService.class));
                // And wait for it to be ready, so we can safely use it afterwards
                new ServiceWaitThread().start();
                Log.d("fonn", "restarting");
            }
    }

    private void getLoginStatus() {
         countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {
                //Log.e("tick", "onTick: " +l/1000);
            }

            @Override
            public void onFinish() {
            countDownTimer.start();
                                    countdown = true;
                                    checklogin();
                                   // ConnectionQuality();

                            }
        }.start();
    }

    private void showAlert() {
         alertDialog = new AlertDialog.Builder(Dashboard.this).create();
        alertDialog.setMessage("You've been logged out");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> {

                    startActivity(new Intent(getContext(), ConfigureAccountActivity.class));

                    dialog.dismiss();
                });
        alertDialog.show();

    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NotNull int[] grantResults) {
        // Callback for when permissions are asked to the user
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            Log.i(
                    "[Permission] "
                            + permissions[i]
                            + " is "
                            + (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            ? "granted"
                            : "denied"));
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(
                new Intent().setClass(getApplicationContext(), wakeupService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Ask runtime permissions, such as record audio and camera
        // We don't need them here but once the user has granted them we won't have to ask again
        checkAndRequestCallPermissions();


    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();

        // Some required permissions needs to be validated manually by the user
        // Here we ask for record audio and camera to be able to make video calls with sound
        // Once granted we don't have to ask them again, but if denied we can
        int recordAudio = getPackageManager().checkPermission(android.Manifest.permission.RECORD_AUDIO, getPackageName());
        Log.i(
                "[Permission] Record audio permission is "
                        + (recordAudio == PackageManager.PERMISSION_GRANTED
                        ? "granted"
                        : "denied"));



        int request = getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName());

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (request != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        }


        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }






    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onPause() {
        super.onPause();
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        // Don't forget to free the singleton as well
        ProxyConfig proxyConfig = getCore().getDefaultProxyConfig();
        if (proxyConfig != null) {
            countDownTimer.cancel();
            countdown=false;
        }

        getCore().removeListener(mCoreListener);
        if (wakeLock.isHeld())
            wakeLock.release();



    }




    public void ConnectionQuality() {


        HomeFragment.signal.setVisibility(View.VISIBLE);
        NetworkInfo info = getInfo(getContext());
        if (info == null || !info.isConnected()) {
            HomeFragment.signal.setImageResource(R.drawable.signal_0);
        }

        long newvalue = ping(getString(R.string.server_domain));

        if (newvalue > 150)
            pingvalue = 1;
        else if (newvalue >= 80)
            pingvalue = 2;
        else if (newvalue >= 20)
            pingvalue = 3;
        else if (newvalue < 20)
            pingvalue = 3;
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 4;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            if (level == 1)
                signal = 1;
            else if (level == 2)
                signal  = 2;
            else if (level == 3)
                signal = 3;
            else if (level == 4)
                signal = 4;

        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkClass = getNetworkClass(getNetworkType(getContext()));

            if (networkClass == 1)
                signal = 2;
            else if (networkClass == 2)
                signal = 3;
            else if (networkClass == 3)
                signal = 4;
            else if (networkClass == 0)
                signal = 0;

      

        }

        int totalvalue = Math.round(signal + pingvalue) / 2;

        if (totalvalue == 1)
            HomeFragment.signal.setImageResource(R.drawable.signal_1);
        else if (totalvalue == 2)
            HomeFragment.signal.setImageResource(R.drawable.signal_2);
        else if (totalvalue == 3)
            HomeFragment.signal.setImageResource(R.drawable.signal_3);
        else if (totalvalue == 4)
            HomeFragment.signal.setImageResource(R.drawable.signal_4);
        else if (totalvalue == 0)
            HomeFragment.signal.setImageResource(R.drawable.signal_0);



    }


    public NetworkInfo getInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }


    public int getNetworkClass(int networkType) {
        try {
            return getNetworkClassReflect(networkType);
        } catch (Exception ignored) {
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case 16: // TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 1;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case 17: // TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return 2;
            case TelephonyManager.NETWORK_TYPE_LTE:

            case 18: // TelephonyManager.NETWORK_TYPE_IWLAN:
                return 3;
            default:
                return 0;
        }
    }

    private int getNetworkClassReflect(int networkType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getNetworkClass = TelephonyManager.class.getDeclaredMethod("getNetworkClass", int.class);
        if (!getNetworkClass.isAccessible()) {
            getNetworkClass.setAccessible(true);
        }
        return (Integer) getNetworkClass.invoke(null, networkType);
    }

    public int getNetworkType(Context context) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            checkAndRequestCallPermissions();
        }
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType();
    }

    public void doConnectionScan(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //your method here
                            Log.d("internet", "10secs");
                            ConnectionQuality();

                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        //  disables back button in current screen.

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            finishAffinity();
            System.exit(0);
            //super.onBackPressed();
            return;
        }
        else { Toast.makeText(getBaseContext(), "Tap back button in order to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    public long ping (String domain){
        Runtime runtime = Runtime.getRuntime();
        try {
            long a = (System.currentTimeMillis() % 100000);
            Process ipprocess = runtime.exec("/system/bin/ping -c 1 "+domain);
            ipprocess.waitFor();
            long b = (System.currentTimeMillis() % 100000);
            if (b <= a){
                timeofping = ((100000 - a) + b);
            }
            else {
                timeofping = (b - a );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeofping;
    }




    @Override
    public void onRegistrationComplete(int type) {


        switch (type) {
            case 1: // This state means you are connected, to can make and receive calls
                HomeFragment.status.setText(R.string.ready);
                break;

            case 2: // This one means an error happened, for example a bad password
                HomeFragment.status.setText(R.string.disconnected);
                break;

            case 3: // Connection is in progress, next state will be either Ok or Failed
                HomeFragment.status.setText(R.string.connecting);
                break;
        }
    }

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
                    () -> {
                        ProxyConfig proxyConfig = getCore().getDefaultProxyConfig();
                        if (proxyConfig != null) {
                            updateLed(proxyConfig.getState());
                            //OneSignal.setEmail(LinphoneService.getInstance().getProfilename()+"@sysnet.com");
                        } else {
                            // No account configured, we display the configuration activity
                            startActivity(new Intent(getContext(), ConfigureAccountActivity.class));

                        }

                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        finish = sharedpreferences.getBoolean(finishotp, false);
                        if (!finish) {
                            getCore().clearProxyConfig();
//            startActivity(new Intent(getContext(), ConfigureAccountActivity.class));
                            // Toast.makeText(this, "not finish", Toast.LENGTH_SHORT).show();
                        }


                    });
        }



    }
    private void updateLed(RegistrationState state) {
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                HomeFragment.status.setText(R.string.ready);
                break;
            case Cleared: // This state is when you disconnected
                HomeFragment.status.setText(R.string.disconnected);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                HomeFragment.status.setText(R.string.connecting);
                break;
        }
    }

    private void checklogin() {

        android.util.Log.i("okhttp", "sending post");

        String url = getString(R.string.server_domain)+"/api/check-deviceid/"+ FonnlinkService.getInstance().getProfilename();
        OkHttpClient client = new OkHttpClient();
        //  RequestBody body  = RequestBody.create(json,data.toString());
        Request newreq = new Request.Builder().url(url).get().build();


        client.newCall(newreq).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                String mMessage = e.getMessage();
           //     android.util.Log.i("okhttp", mMessage);

            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                String mMessage = response.body().string();
                String responseCode = null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("device_id");
                    if (!responseCode.equals(mydeviceId)){
                        Looper.prepare();
                        showAlert();
                        getCore().clearProxyConfig();
                        SharedPreferences sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(finishotp, false);
                        editor.apply();
                        countDownTimer.cancel();
                        Looper.loop();
                        //startActivity(new Intent(getContext(), ConfigureAccountActivity.class));



//                        SharedPreferences sharedpref = getContext().getSharedPreferences(Mypref, Context.MODE_PRIVATE);
//                        SharedPreferences.Editor ceditor = sharedpref.edit();
//                        ceditor.putString(callcpuntpref, "0");
//                        ceditor.apply();
                        //countDownTimer.cancel();

                    }

                   // android.util.Log.d("okhttpp",responseCode);
                } catch (JSONException e) {
                    e.printStackTrace();

                }
             //   android.util.Log.i("okhttp", mMessage);


            }

        });

    }

    public void UpdateApp(){
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(result -> {

            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
//                requestUpdate(result);
           //     android.view.ContextThemeWrapper ctw = new android.view.ContextThemeWrapper(this,R.style.Theme_AlertDialog);
                final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Update Fonn Link");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setIcon(R.mipmap.fonnicon);
                alertDialogBuilder.setMessage("Fonn Link recommends that you update to the latest version for a seamless & enhanced performance of the app.");
                alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id="+getPackageName())));
                        }
                        catch (ActivityNotFoundException e){
                            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
                        }
                    }
                });
                alertDialogBuilder.setNegativeButton("No Thanks",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                alertDialogBuilder.show();

            } else {

            }
        });
    }

}