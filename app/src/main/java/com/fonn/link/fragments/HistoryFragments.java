package com.fonn.link.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.adapters.HistoryRecycleAdapter;
import com.fonn.link.R;
import com.fonn.link.modal.history_details;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryFragments extends Fragment {

    Toolbar toolbar;

    private HistoryRecycleAdapter historyRecycleAdapter;
    ArrayList<history_details> historyDetails = new ArrayList<>();
    private RecyclerView recyclerView;
    String responseCode = null;
    public int intstart = 0 ,intsize = 10;
    private Parcelable recyclerViewState;
    private ProgressBar loadingPB;
    boolean loading = false;

    @SuppressLint("UseCompatLoadingForDrawables")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_history, container, false);
        toolbar = root.findViewById(R.id.Htoolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setTitle("History");
        //for recycleview
        NestedScrollView nestedSV = root.findViewById(R.id.scroll_view);
        historyDetails = new ArrayList<>();
        toolbar.setNavigationOnClickListener(v -> FonnlinkService.getInstance().startActivity(getContext(), Dashboard.class));
        recyclerView = root.findViewById(R.id.recyclerView2);
        historyRecycleAdapter = new HistoryRecycleAdapter(getContext(), historyDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyRecycleAdapter);
        loadingPB = root.findViewById(R.id.idPBLoading);
        loadingPB.setVisibility(View.GONE);
        parseJson(""+intstart,""+intsize,responseCode);
        //countDownTimer.cancel();

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (! recyclerView.canScrollVertically(1)){ //1 for down
//
//                    intsize += 5;
//                    intstart += 5;
//                   parseJson(""+intstart,""+intsize,responseCode);
//
//                   Toast.makeText(getContext(), ""+intstart+"/"+intsize, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        nestedSV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY==v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()){

                    if (!loading) {
                        loading = true;
                        intsize += 10;
                        intstart += 10;
                        loadingPB.setVisibility(View.VISIBLE);
                        parseJson("" + intstart, "" + intsize, responseCode);
                       // Toast.makeText(getContext(), "" + intstart + "/" + intsize, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



        return root;


    }


    private void parseJson(String start, String size, String Date) {
        String urllink = getString(R.string.server_domain)+"/api/call-history";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", FonnlinkService.getInstance().getProfilename());
            jsonBody.put("start", start);
            jsonBody.put("size", size);
            if (responseCode != null) {
                jsonBody.put("requestDatetime", Date);
            }
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            //jsonBody.put("username", "jaybee");
            JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.POST, urllink, jsonBody, response -> {

                try {
                    JSONObject object = null;
                    JSONObject objects = new JSONObject();

                    JSONArray Jarray = response.getJSONArray("callHistory");
                    for (int i = 0; i < Jarray.length(); i++) {
                        object = Jarray.getJSONObject(i);

                        history_details history = new history_details();
//                        String name = object.getString("name");
//                        String date = object.getString("date");
//                        String duration = object.getString("duration");

                        history.setName(object.getString("name"));
                        history.setDate(object.getString("date"));
                        history.setDuration(object.getString("duration"));
                        historyDetails.add(history);
                        // historyRecycleAdapter = new HistoryRecycleAdapter(getContext(), historyDetails);
                        // recyclerView.setAdapter(historyRecycleAdapter);

                    }
                    //responseCode = objects.getString("requestDatetime");
                   // Log.d("volley", "" + objects.getString("requestDatetime"));
                    //historyRecycleAdapter = new HistoryRecycleAdapter(getContext(), historyDetails);
                    loadingPB.setVisibility(View.GONE);
                    historyRecycleAdapter = new HistoryRecycleAdapter(getContext(), historyDetails);
                    //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(historyRecycleAdapter);
                    //recyclerView.getAdapter().notifyDataSetChanged();
                    loading = false;



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> error.printStackTrace()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", "");
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
}