package com.fonn.link.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fonn.link.ConfigureAccountActivity;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OSDeviceState;
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

import static com.fonn.link.Dashboard.countDownTimer;
import static com.fonn.link.OTPactivity.MyPREFERENCES;
import static com.fonn.link.OTPactivity.finishotp;
import static com.fonn.link.fragments.HomeFragment.Mypref;
import static com.fonn.link.fragments.HomeFragment.callcpuntpref;

public class LogoutFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        sendpost();

        return inflater.inflate(R.layout.activity_logout_fragment, container, false);


    }

    public void sendpost(){
        OSDeviceState device = OneSignal.getDeviceState();
        Log.i("okhttp","sending post");;
        String url = getString(R.string.server_domain)+"/api/logout";
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            String s = FonnlinkService.getInstance().getAddressname();
            data.put("username", FonnlinkService.getInstance().getProfilename());
            data.put("player_id", device.getUserId());
            //   assert device != null;
            Log.d("onesignallog",data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Request.Builder builder = new Request.Builder()
//                .url(url)
//                .delete(RequestBody.create(
//                        (MediaType.parse("application/json; charset=utf-8"), json,data.toString()));
//        Request request = builder.build();
        RequestBody body  = RequestBody.create(json,data.toString());
        Request request = new Request.Builder().url(url).delete(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String mMessage = e.getMessage();
                Log.i("okhttp", mMessage);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String mMessage = Objects.requireNonNull(response.body()).string();
                String responseCode = null;
                String responseCode2 =null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("status");
                    responseCode2 = object.getString("description");
                    if(responseCode.equals("SUCCESS")) {
                        Log.i("okhttp",mMessage);
                        startActivity(new Intent(getContext(), ConfigureAccountActivity.class));
                        FonnlinkService.getCore().clearProxyConfig();
                        SharedPreferences sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(finishotp, false);
                        editor.apply();
                        SharedPreferences sharedpref = getContext().getSharedPreferences(Mypref, Context.MODE_PRIVATE);
                        SharedPreferences.Editor ceditor = sharedpref.edit();
                        ceditor.putString(callcpuntpref, "0");
                        ceditor.apply();
                        countDownTimer.cancel();


                      //  Toast.makeText(getContext(), ""+responseCode2, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        FonnlinkService.getInstance().startActivity(getContext(), Dashboard.class);
                       // Toast.makeText(getContext(), ""+responseCode2, Toast.LENGTH_SHORT).show();
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