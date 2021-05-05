package com.fonn.link;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfigureAccountActivity extends Activity {
    private EditText mUsername, mPassword, mDomain;
    private RadioGroup mTransport;
    private Button mConnect;
    ProgressDialog pd;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.configure_account);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        CheckBox ch1=(CheckBox)findViewById(R.id.checkBox);
        TextView textTAC = (TextView)findViewById(R.id.texttac);
        textTAC.setText(Html.fromHtml("I agree to the " +
                "<a href=https://fonn.link/terms-and-conditions/> terms & conditions</a>"));
        textTAC.setClickable(true);
        textTAC.setMovementMethod(LinkMovementMethod.getInstance());
        //  checkingUpdatedAds();

        // Account creator can help you create/config accounts, even not sip.linphone.org ones
        // As we only want to configure an existing account, no need for server URL to make requests
        // to know whether or not account exists, etc...
        mAccountCreator = FonnlinkService.getCore().createAccountCreator(null);

        mUsername = findViewById(R.id.usernamewww);
        mPassword = findViewById(R.id.password);


        mConnect = findViewById(R.id.configure);
        mConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ch1.isChecked()) {
                            sendpost();

                        }
                        else {
                            Snackbar.make(mConnect, "Must have Accept Terms and Condition to continue", Snackbar.LENGTH_LONG).show();
                        }


                        // configureAccount();
                    }
                });

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    pd.dismiss();
                    //finish();
                    FonnlinkService.getInstance().startActivity(getApplicationContext(),OTPactivity.class);
                } else if (state == RegistrationState.Failed) {

                    FonnlinkService.getCore().clearProxyConfig();
                    Toast.makeText(getApplicationContext(), "Failure: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        FonnlinkService.getCore().addListener(mCoreListener);
    }

    @Override
    protected void onPause() {
        FonnlinkService.getCore().removeListener(mCoreListener);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void configureAccount() {




        // At least the 3 below values are required
        mAccountCreator.setUsername(mUsername.getText().toString());
        mAccountCreator.setDomain("asteriskcloudworks.sysnetph.com:5090");
        mAccountCreator.setPassword(mPassword.getText().toString());
        mAccountCreator.setTransport(TransportType.Udp);
        // By default it will be UDP if not set, but TLS is strongly recommended
//        switch (mTransport.getCheckedRadioButtonId()) {
//            case R.id.transport_udp:
//                mAccountCreator.setTransport(TransportType.Udp);
//                break;
//            case R.id.transport_tcp:
//                mAccountCreator.setTransport(TransportType.Tcp);
//                break;
//            case R.id.transport_tls:
//                mAccountCreator.setTransport(TransportType.Tls);
//                break;
//        }

        // This will automatically create the proxy config and auth info and add them to the Core
        ProxyConfig cfg = mAccountCreator.createProxyConfig();
        FonnlinkService.getCore().setStunServer("stun1.l.google.com:19302");
        FonnlinkService.getInstance().setIceEnabled(true);
        // Make sure the newly created one is the default
        FonnlinkService.getCore().setDefaultProxyConfig(cfg);

    }





    public void sendpost(){

        Log.i("okhttp","sending post");
        pd = new ProgressDialog(ConfigureAccountActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);
        pd.show();
        String url = "https://opis.link/api/login";
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            data.put("username",mUsername.getText().toString() );
            data.put("password", mPassword.getText().toString());
            data.put("player_id", ""+Objects.requireNonNull(OneSignal.getDeviceState()).getUserId());
            data.put("device_type", "android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body  = RequestBody.create(json,data.toString());
        Request newreq = new Request.Builder().url(url).post(body).build();


        client.newCall(newreq).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String mMessage = e.getMessage();
                Log.i("okhttp", mMessage);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String mMessage = response.body().string();
                String responseCode = null;
                String responseCode2 =null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("status");
                    responseCode2 = object.getString("description");
                    if(responseCode.equals("ERROR")) {
                        Snackbar.make(mConnect, responseCode2, Snackbar.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                    else {
                        Log.i("okhttp",mMessage);
                        configureAccount();
                        Snackbar.make(mConnect, responseCode2, Snackbar.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("okhttp",  mMessage);
            }


        });


    }


}

