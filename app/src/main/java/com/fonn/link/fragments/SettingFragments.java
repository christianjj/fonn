package com.fonn.link.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


import com.fonn.link.ConfigureAccountActivity;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingFragments extends Fragment {
    EditText et_oldpass,
             et_retype,
             et_newpss;
    Button bt_changepass;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = root.findViewById(R.id.Stoolbar);

         et_newpss  = root.findViewById(R.id.et_newpass);
         et_retype = root.findViewById(R.id.et_retypepass);
         et_oldpass = root.findViewById(R.id.et_oldpass);
         bt_changepass = root.findViewById(R.id.bt_changepass);


        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_button));
        toolbar.setTitle("Settings");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                FonnlinkService.getInstance().startActivity(getContext(), Dashboard.class);
            }
        });

        bt_changepass.setOnClickListener(view -> changepass());



        return root;
    }

    private void changepass() {
        if (et_newpss.getText().toString().isEmpty()){
            et_newpss.setError("invalid input");
            return;}

          if  (et_oldpass.getText().toString().isEmpty()){
              et_oldpass.setError("invalid input");
            return;
        }
        if (!et_retype.getText().toString().equals(et_newpss.getText().toString())) {
            et_retype.setError("input not match");

        } else {
            Log.i("okhttp", "sending post");
            String url = "https://opis.link/api/changepassword";
            OkHttpClient client = new OkHttpClient();
            MediaType json = MediaType.parse("application/json;charset=utf-8");
            JSONObject data = new JSONObject();
            try {
                data.put("username", FonnlinkService.getInstance().getProfilename());
                data.put("password", et_oldpass.getText().toString());
                data.put("new_password", et_newpss.getText().toString());

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
                            Log.i("okhttp", mMessage);
                            Snackbar.make(bt_changepass, responseCode2, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(bt_changepass, responseCode2, Snackbar.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("okhttp", mMessage);
                }
            });

        }
    }
}
