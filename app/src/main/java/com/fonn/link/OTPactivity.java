package com.fonn.link;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OTPactivity extends AppCompatActivity {
    private EditText mEt1, mEt2, mEt3, mEt4;
    private Context mContext;
    public int count;
    public static final String finishotp = "finish";
    public static String MyPREFERENCES = "sharedprefs";
    public static boolean finish;
    public Button verify;
    Bundle b;
    public TextView countdown, resendotp;
    public String username = null, password = null;
    private static final String FORMAT = "%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        initialize();
        addTextWatcher(mEt1);
        addTextWatcher(mEt2);
        addTextWatcher(mEt3);
        addTextWatcher(mEt4);
        setCountdown();
        loadpref();
        Intent iin = getIntent();
        b = iin.getExtras();

        if (b != null) {
            username = b.getString("username");
            password = b.getString("password");
        }

        setResendotp();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadpref();

    }

    private void initialize() {
        mEt1 = findViewById(R.id.otp_edit_text1);
        mEt2 = findViewById(R.id.otp_edit_text2);
        mEt3 = findViewById(R.id.otp_edit_text3);
        mEt4 = findViewById(R.id.otp_edit_text4);
        resendotp = findViewById(R.id.tv_resend);




        verify = findViewById(R.id.btn_verify);
        countdown = findViewById(R.id.countdown);


        verify.setOnClickListener(view -> {
            String gettext = "" + mEt1.getText().toString() + mEt2.getText().toString() + mEt3.getText().toString() + mEt4.getText().toString();

                if (gettext.equals("1234")){
                    savepref();
                    FonnlinkService.getInstance().startActivity(getApplicationContext(), Dashboard.class);
                }
                else{
                    Snackbar.make(verify, "wrong otp", Snackbar.LENGTH_LONG).show();
                }
           // checkotp(gettext);
        });
        mContext = OTPactivity.this;


    }

    private void addTextWatcher(final EditText one) {
        one.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (one.getId()) {
                    case R.id.otp_edit_text1:
                        if (one.length() == 1) {
                            mEt2.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text2:
                        if (one.length() == 1) {
                            mEt3.requestFocus();
                        } else if (one.length() == 0) {
                            mEt1.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text3:
                        if (one.length() == 1) {
                            mEt4.requestFocus();
                        } else if (one.length() == 0) {
                            mEt2.requestFocus();
                        }
                        break;

                    case R.id.otp_edit_text4:
                        if (one.length() == 1) {
                            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(OTPactivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        } else if (one.length() == 0) {
                            mEt3.requestFocus();
                        }
                        break;
                }
            }
        });

    }


    public void savepref() {
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(finishotp, true);
        editor.apply();
    }

    public void loadpref() {
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        finish = sharedpreferences.getBoolean(finishotp, false);

        if (finish) {
            FonnlinkService.getInstance().startActivity(this, Dashboard.class);
        }


    }


    public void checkotp(String code) {

        Log.i("okhttp", "sending post");

        String url = "https://opis.link/api/check-otp";
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);

            data.put("otp", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json, data.toString());
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
                String responseCode2 = null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("status");
                    responseCode2 = object.getString("description");
                    if (responseCode.equals("SUCCESS")) {
                        Snackbar.make(verify, responseCode2, Snackbar.LENGTH_LONG).show();
                        FonnlinkService.getInstance().startActivity(getApplicationContext(), Dashboard.class);
                        savepref();
                    } else {
                        Log.i("okhttp", mMessage);

                        count += 1;
                        if (count == 3) {
                            finish();
                            FonnlinkService.getCore().clearProxyConfig();
                        }

                        Snackbar.make(verify, responseCode2, Snackbar.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("okhttp", mMessage);
            }


        });


    }

    public void setCountdown() {

        resendotp.setEnabled(false);
        //resendotp.setClickable(false);
        new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                countdown.setText("" + String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                countdown.setText("");
                resendotp.setEnabled(true);
            }

        }.start();
    }


    public void setResendotp() {

        Log.i("okhttp", "sending post");

        String url = "https://opis.link/api/resend-otp";
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("password", password);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json, data.toString());
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
                Log.i("okhttp", mMessage);
                JSONObject object = null;
                try {
                    object = new JSONObject(mMessage);
                    String responseCode2 = object.getString("description");
                    Snackbar.make(resendotp, responseCode2, Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        });

    }

    @Override
    protected void onDestroy() {
       // FonnlinkService.getCore().clearProxyConfig();
        super.onDestroy();
    }

    public void resend(View view) {

        setResendotp();
        setCountdown();
    }

    @Override
    public void onBackPressed() {
    // super.onBackPressed();
        //  disables back button in current screen.
    }
}