package com.fonn.link;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class OTPactivity extends AppCompatActivity {
    private EditText mEt1, mEt2, mEt3, mEt4;
    private Context mContext;
    public int count;
    public  static  final String finishotp = "finish";
    public static String MyPREFERENCES = "sharedprefs";
    public static boolean finish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        initialize();



        addTextWatcher(mEt1);
        addTextWatcher(mEt2);
        addTextWatcher(mEt3);
        addTextWatcher(mEt4);
        loadpref();

    }

    private void initialize() {
        mEt1 = findViewById(R.id.otp_edit_text1);
        mEt2 = findViewById(R.id.otp_edit_text2);
        mEt3 = findViewById(R.id.otp_edit_text3);
        mEt4 = findViewById(R.id.otp_edit_text4);

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

    public void next(View view) {
        String gettext = "" + mEt1.getText().toString() + mEt2.getText().toString() + mEt3.getText().toString() + mEt4.getText().toString();

        if(gettext.equals("1234")){
            FonnlinkService.getInstance().startActivity(this, Dashboard.class);
            savepref();
        }
        else {
            count += 1;
            if (count == 3){

             finish();
             FonnlinkService.getCore().clearProxyConfig();
            }

            Snackbar.make(view, "You entered a wrong OTP Code, please enter again", Snackbar.LENGTH_LONG).show();
        }


    }

    public void savepref(){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(finishotp, true);
        editor.apply();
    }
    public void loadpref(){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        finish = sharedpreferences.getBoolean(finishotp,false);

        if (finish){
            FonnlinkService.getInstance().startActivity(this, Dashboard.class);
        }



    }

}