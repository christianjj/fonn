package com.fonn.link.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.fonn.link.modal.profile_details;
import com.fonn.link.adapters.ProfileRecycleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment implements ProfileRecycleAdapter.mOnClickListener {

    TextView tv_displayname,
             tv_topup,
             tv_addnew;


    private ProfileRecycleAdapter profileRecycleAdapter;
    ArrayList<profile_details> userList;
    private RecyclerView recyclerView;


    @SuppressLint("UseCompatLoadingForDrawables")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        Toolbar toolbar = root.findViewById(R.id.Ptoolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_button));
        toolbar.setTitle("Profile");
        toolbar.setNavigationOnClickListener(v -> {
            FonnlinkService.getInstance().startActivity(getContext(), Dashboard.class);
        });
        //countDownTimer.cancel();
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        parseJson();
        tv_displayname = root.findViewById(R.id.usernamewww);
        tv_displayname.setText(FonnlinkService.getInstance().getProfilename());

       // tv_addnew.setClickable(true);
     //  tv_addnew.setOnClickListener(view -> {
           // Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
            //requestapi();

          //  throw new RuntimeException();
       // });



        return root;
    }

    private void parseJson() {
        String urllink = getString(R.string.server_domain)+"/api/profile";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", FonnlinkService.getInstance().getProfilename());
            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, urllink, jsonBody, response -> {
                try {
                    JSONObject object = null;
                    JSONArray Jarray = response.getJSONArray("callURLs");
                    for (int i = 0; i < Jarray.length(); i++) {
                        object = Jarray.getJSONObject(i);
                        String name = object.getString("callURL");
                        String balance = object.getString("totalBalance");
                        //String number = object.getString("mobileNumber");
                        String status = object.getString("status");
                        userList.add(new profile_details(name, balance, status));

                    }
                    Log.d("volley", "" + object.getString("displayName"));
                    profileRecycleAdapter = new ProfileRecycleAdapter(getContext(), userList);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(profileRecycleAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> error.printStackTrace()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", "radd");
                    return params;
                }

                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(getContext()).add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mOnClickListener(int position) {
        userList.get(position);
    }
}