package com.fonn.link.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fonn.link.ConfigureAccountActivity;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;

import com.fonn.link.R;
import com.fonn.link.adapters.HistoryRecycleAdapter;
import com.fonn.link.adapters.ImageRecycleAdapter;
import com.fonn.link.modal.history_details;
import com.fonn.link.modal.image_details;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.fonn.link.Dashboard.countDownTimer;
import static com.fonn.link.OTPactivity.MyPREFERENCES;
import static com.fonn.link.OTPactivity.finishotp;
import static com.fonn.link.fragments.HomeFragment.Mypref;
import static com.fonn.link.fragments.HomeFragment.callcpuntpref;

public class SettingFragments extends Fragment {
    EditText et_oldpass,
             et_retype,
             et_newpss;
    Button bt_changepass;

    Uri uri;
    Bitmap bitmap;
    Cursor cursor;
    private boolean granted = false;
    File f;
    String URL ="https://test.opis.link/api/carousel/edit/christian1";
    ProgressDialog progressDialog;
    private static final String IMAGE_DIRECTORY = "/images/";
    //private  ImageRecycleAdapter imageRecycleAdapter;
     ArrayList<image_details> imageDetails;
    private RecyclerView recyclerView;
    private static final int READ_REQUEST_CODE = 99;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = root.findViewById(R.id.Stoolbar);


         et_newpss  = root.findViewById(R.id.et_newpass);
         et_retype = root.findViewById(R.id.et_retypepass);
         et_oldpass = root.findViewById(R.id.et_oldpass);
         bt_changepass = root.findViewById(R.id.bt_changepass);
       // recyclerView = root.findViewById(R.id.recycleimage);
      //  imageDetails = new ArrayList<>();
       // recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
       // imageRecycleAdapter = new ImageRecycleAdapter(getContext(), imageDetails, this);


        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
       // parseJson();
        //okhttps();
        //parseJson2();
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

        bt_changepass.setOnClickListener(view ->

               changepass()
              //  test()
        //throw new RuntimeException("Test Crash"); // Force a crash
        );



        return root;
    }

    public void test(){
        throw new RuntimeException("Test Crash"); // Force a crash
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
            String url = getString(R.string.server_domain) +"/api/changepassword";
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
                            sendpost();

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
