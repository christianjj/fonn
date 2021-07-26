package com.fonn.link.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ebanx.swipebtn.SwipeButton;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.fonn.link.interfaces.activityListener;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;


import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.Reason;
import org.linphone.core.RegistrationState;


import java.util.Timer;
import java.util.TimerTask;

import static com.fonn.link.FonnlinkService.getCore;

public class HomeFragment extends Fragment implements activityListener {


    CoreListenerStub mCoreListener;
    private TextView
            CurrentcallerUsername,
            timertext,
            textcallstatus ;
    public static TextView IncomingcallerUsername;

    @SuppressLint("StaticFieldLeak")
    public static TextView status;
    @SuppressLint("StaticFieldLeak")
    public static TextView textCallCount;
    private LinearLayout defaultLayout,
            incomingLayout,
            callActivity;
    private ImageView   callAccept,
            callCancel,
            speakerToggle,
            muteToggle;
    @SuppressLint("StaticFieldLeak")
    public  ProgressBar progressBar;

    @SuppressLint("StaticFieldLeak")
    public static String total_counts;
    public static ImageView signal;
    public static String urlads;
    @SuppressLint("StaticFieldLeak")
    public static ImageView ads;
    public  Boolean oncall = false;
    //timer
    boolean timerStarted = false,
            speakerOn = false,
            willcount,
            muteon = false;


    Timer timer;
    TimerTask timerTask;
    private Handler handler;
    private Runnable handlerTask;
    Double time = 0.0;
    // count of calls
    int c;
    SwipeButton endCalltoggle;
    public static String Mypref = "myprefs";
    public  static  final String callcpuntpref = "callcount";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //layout in xml init
        defaultLayout = root.findViewById(R.id.defaultlayout);
        incomingLayout = root.findViewById(R.id.incoming);
        callActivity = root.findViewById(R.id.callActivity);

        //info textviews
        CurrentcallerUsername = root.findViewById(R.id.username2);
        IncomingcallerUsername = root.findViewById(R.id.username);
        textCallCount = root.findViewById(R.id.textCallCount);

        c = Integer.parseInt(textCallCount.getText().toString());
        ads = root.findViewById(R.id.adsimage);
        //signal and loading init
        progressBar = root.findViewById(R.id.progressBar);
        signal = root.findViewById(R.id.signal);

        //connection status
        status = root.findViewById(R.id.status);

        //buttons accept and end
        callAccept = root.findViewById(R.id.callAccept);
        endCalltoggle =  root.findViewById(R.id.swipe);
        speakerToggle = root.findViewById(R.id.speaker);
        muteToggle = root.findViewById(R.id.mute);
        callCancel = root.findViewById(R.id.callCancel);


        //status if ending call
        textcallstatus = root.findViewById(R.id.callstatus);

        //timer Init
       timertext = root.findViewById(R.id.timer);
        timer = new Timer();

        //interface init
        FonnlinkService.getInstance().activityListener = this;

        //get onesignal device info
        OSDeviceState device = OneSignal.getDeviceState();
        //Toast.makeText(getContext(), ""+device.getUserId(), Toast.LENGTH_SHORT).show();


        //button init
        callAccept.setOnClickListener(view -> callAccept());
        // callEnd.setOnClickListener(view -> endCall());


        endCalltoggle.setOnActiveListener(() -> endCall());
        callCancel.setOnClickListener(view -> callCancel());
        speakerToggle.setOnClickListener(view -> isSpeakerOn());
        muteToggle.setOnClickListener(view -> isMuteon());

        //Connection listener
        mCoreListener = new CoreListenerStub() {


            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                updateLed(state);
            }
        };
        //checkingcall();
        checkingcall();
        //Glide.with(this).load(urlads).into(ads);
        loadpref();
        return root;
    }




    @Override
    public void onResume() {

       // getCore().addListener(mCoreListener);
        //progressBar.setVisibility(View.VISIBLE);
        // Manually update the state, in case it has been registered before
        // we add a chance to register the above listener
        super.onResume();
        if (FonnlinkService.isReady()) {
            checkingcall();
        }

        loadpref();




    }



    public void callAccept() {
        FonnlinkService.getInstance().answerCall();

    }
    public void endCall() {
        Core core = FonnlinkService.getCore();
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                // Current call can be null if paused for example
                call = core.getCalls()[0];
            }
            call.terminate();

            //endCallUi();
        }
    }
    private void callCancel() {

        Core core = FonnlinkService.getCore();
        if (core.getCallsNb() > 0) {
            Call call = core.getCurrentCall();
            if (call == null) {
                // Current call can be null if paused for example
                call = core.getCalls()[0];
            }
            call.decline(Reason.Declined);
        }
        defauiltUi();



    }

    private void isSpeakerOn() {
        if (!speakerOn){
            speakerToggle.setImageResource(R.drawable.speaker_on);
            AudioManager mAudioMgr;
            mAudioMgr = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioMgr.setSpeakerphoneOn(true);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
            speakerOn = true;
        }
        else {
            speakerToggle.setImageResource(R.drawable.speaker_off);
            AudioManager mAudioMgr;
            mAudioMgr = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioMgr.setSpeakerphoneOn(false);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
            mAudioMgr.setMode(AudioManager.MODE_NORMAL);
            speakerOn = false;
        }

    }

    private void isMuteon(){


        Core core = FonnlinkService.getCore();
        if (!muteon){
            muteToggle.setImageResource(R.drawable.mic_on);


            if (core.getCallsNb() > 0) {
                Call call = core.getCurrentCall();
                if (call == null) {
                    // Current call can be null if paused for example
                    call = core.getCalls()[0];

                }
                call.setMicrophoneMuted(true);
            }
            muteon = true;
        }
        else{
            muteToggle.setImageResource(R.drawable.mic_off);

            if (core.getCallsNb() > 0) {
                Call call = core.getCurrentCall();
                if (call == null) {
                    // Current call can be null if paused for example
                    call = core.getCalls()[0];
                }
                call.setMicrophoneMuted(false);
            }
            muteon = false;
        }




    }

    public void incomingUi() {
        //  FonnlinkService.getInstance().sendNotification();
        defaultLayout.setVisibility(View.GONE);
        incomingLayout.setVisibility(View.VISIBLE);

        //String displayName = FonnlinkService.getInstance().getProfilename();
        //Call call = FonnlinkService.getCall();

    }

    public void defauiltUi() {
        defaultLayout.setVisibility(View.VISIBLE);
        incomingLayout.setVisibility(View.GONE);
        callActivity.setVisibility(View.GONE);
    }

    public void callUi() {
        textcallstatus.setText(R.string.connected);
        defaultLayout.setVisibility(View.GONE);
        incomingLayout.setVisibility(View.GONE);
        callActivity.setVisibility(View.VISIBLE);
        if(!timerStarted)
        {
            timerStarted = true;
            startTimer();
        }
        else
        {
            timerStarted = false;
            timerTask.cancel();

        }
        String displayName = FonnlinkService.getInstance().getAddressname();
       // CurrentcallerUsername.setText(displayName);

    }
    private void startTimer()
    {

//        timerTask = new TimerTask()
//        {
//
//            @Override
//            public void run()
//            {
//                time++;
//                timertext.setText(getTimerText());
//            }
//
//        };
//        timer.scheduleAtFixedRate(timerTask, 0 ,1000);

//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                timerTask = new TimerTask() {
//                    @Override
//                    public void run() {
//
//                        time++;
//                        timertext.setText(getTimerText());
//
//                    }
//                };
//            }
//        }, 0, 1000);

       timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        time++;
                        timertext.setText(getTimerText());
                    }
                });
            }
        }, 0, 1000);




    }

    private String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }

    public static void updateLed(RegistrationState state) {
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                status.setText(R.string.ready);
                break;
            case None: // This state is the default state
            case Cleared: // This state is when you disconnected
                status.setText(R.string.disconnected);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                status.setText(R.string.connecting);
                break;
        }
    }
    public void checkingcall(){

        Core core = getCore();
        Call call = core.getCurrentCall();
        if (call != null) {
            if (!oncall) {
                incomingUi();
                IncomingcallerUsername.setText(call.getRemoteParams().getCustomHeader("X-FonnLink-Loc"));
            }
            else {
                textcallstatus.setText(R.string.connected);
                defaultLayout.setVisibility(View.GONE);
                incomingLayout.setVisibility(View.GONE);
                callActivity.setVisibility(View.VISIBLE);
                CurrentcallerUsername.setText(call.getRemoteParams().getCustomHeader("X-FonnLink-Loc"));
            }

           // String displayName = FonnlinkService.getInstance().getAddressname();
          //  CurrentcallerUsername.setText(displayName);
            CurrentcallerUsername.setText(call.getRemoteParams().getCustomHeader("X-FonnLink-Loc"));
        }
    }

    @Override
    public void onCallActivity(String location) {
        callUi();
        oncall = true;
        CurrentcallerUsername.setText(location);
        //  willcount = true;
    }

    @Override
    public void onIncomingActivity(String location) {
        incomingUi();
        IncomingcallerUsername.setText(location);

    }


    @Override
    public void onEndCall() {
        defauiltUi();
//        if(willcount) {
//            c += 1;
//            Log.d("count", String.valueOf(c));
//            //textCallCount.setText(String.valueOf(c));
//            //savepref();
//            willcount= false;
//        }

        textcallstatus.setText(R.string.endingthecall);

        //timer.cancel();
        oncall = false;
        muteon = false;
        speakerOn = false;
        timer.cancel();
        time = 0.0;
        timerStarted = false;
        timertext.setText(formatTime(0,0,0));
        timer = new Timer();
        onesecdalay();

    }


    public void onesecdalay(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms

                defaultLayout.setVisibility(View.VISIBLE);
                incomingLayout.setVisibility(View.GONE);
                callActivity.setVisibility(View.GONE);

            }
        }, 1000);

    }

    public void savepref(){
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(Mypref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(callcpuntpref, textCallCount.getText().toString());
        editor.apply();
    }

    public void loadpref() {
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(Mypref, Context.MODE_PRIVATE);
        textCallCount.setText(sharedpreferences.getString(callcpuntpref, "0"));
    }



}