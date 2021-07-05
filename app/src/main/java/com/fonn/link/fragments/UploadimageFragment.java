package com.fonn.link.fragments;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.fonn.link.adapters.ImageRecycleAdapter;
import com.fonn.link.adapters.ProfileRecycleAdapter;
import com.fonn.link.adapters.callUrlListRecyclerAdapter;
import com.fonn.link.interfaces.imagehandler;
import com.fonn.link.modal.callurl_details;
import com.fonn.link.modal.image_details;
import com.fonn.link.modal.profile_details;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadimageFragment extends Fragment implements ImageRecycleAdapter.onclicklistener {

    private  ImageRecycleAdapter imageRecycleAdapter;
    private com.fonn.link.adapters.callUrlListRecyclerAdapter callUrlListRecyclerAdapter;
    private RecyclerView recyclerView;
    static Button B_upload;
    ArrayList<image_details> imageDetails;
    ArrayList<callurl_details> callurlDetails;
    RecyclerView rvlist;
    Dialog dialog;
    Uri uri;
    Bitmap bitmap;
    ProgressDialog progressDialog;
    Cursor cursor;
    int finalposition;
    String callurl;
    public static String id;
    private boolean granted = false;
    private static final int READ_REQUEST_CODE = 99;
    private static final int READ_REQUEST_CODE_EDIT = 88;
    @SuppressLint("UseCompatLoadingForDrawables")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_uploadimage, container, false);
        Toolbar toolbar = root.findViewById(R.id.Itoolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_button));
        toolbar.setTitle("Upload Image");
        toolbar.setNavigationOnClickListener(v -> {
            FonnlinkService.getInstance().startActivity(getContext(), Dashboard.class);
        });

        recyclerView = root.findViewById(R.id.recycleimage);
        B_upload = root.findViewById(R.id.upload);
        imageDetails = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imageRecycleAdapter = new ImageRecycleAdapter(getContext(), imageDetails,this);

        parseJson();

        B_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imageDetails.add(new image_details("test", "tdate", "size"));
                //imageRecycleAdapter.notifyDataSetChanged();

                checkrequest2();
                dialogforAdd();
                //



            }
        });

        return root;
    }

    private void parseJson() {
        String urllink = getString(R.string.server_domain)+"/api/carousel/data/"+FonnlinkService.getInstance().getProfilename();;
        JSONObject jsonBody = new JSONObject();
        //jsonBody.put("username", FonnlinkService.getInstance().getProfilename());
        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, urllink, null, response -> {
            try {
                JSONObject object = null;
                JSONArray Jarray = response.getJSONArray("carousel");
                for (int i = 0; i < Jarray.length(); i++) {
                    object = Jarray.getJSONObject(i);
                    String imagename = object.getString("filename");
                    String date = object.getString("date_uploaded");
                    //String number = object.getString("mobileNumber");
                    String filesize = object.getString("size");
                    String id = object.getString("id");
                    String path = object.getString("path");
                    String callurl = object.getString("call_url");
                    imageDetails.add(new image_details(imagename, date, filesize, id, path, callurl));

                }
                //Log.d("volley", "" + object.getString("date_uploaded"));
                imageRecycleAdapter = new ImageRecycleAdapter(getContext(), imageDetails, this);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(imageRecycleAdapter);

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
    }
    public void checkrequest2(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                      //performFileSearch();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {

                        /* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                        /* ... */}
                }).check();
    }


    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the F to choose a file via the system's file
        // browser.

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
    public void performFileEdit(String idd) {

        // ACTION_OPEN_DOCUMENT is the F to choose a file via the system's file
        // browser.

        id = idd;
        Log.d("string", idd);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        //intent.putExtra("ID_STRING", id);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE_EDIT);

        Log.d("check","fromfilechose");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    uploadimage("null",callurl);
                    // convertimage();
                    //imagejson();
                    //saveImage(bitmap);
                    //uploadImage("https://test.opis.link/api/carousel/edit/"+FonnlinkService.getInstance().getProfilename(), saveImage(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }

             //   Uri selectedImageURI = data.getData();
//                File file = new File(uri.getPath());//create path from uri
//                final String[] split = file.getPath().split(":");//split the path.
//                String filePath = split[1];//assign it to a string(your choice).
//                File files = Environment.getExternalStorageDirectory();
//                String path = uri.getLastPathSegment(); // "/mnt/sdcard/FileName.mp3"
//                String Fullpath = files+filePath;
//                Log.i("image", "Uri: " +Fullpath );

                return;
            }
        }

         if (requestCode == READ_REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
             uri = null;

            if (data != null) {
                Log.d("check", "finish choosing");

                uri = data.getData();


                //String string = data.getStringExtra("ID_STRING");
                Log.d("string2", id);
                //Toast.makeText(getContext(), ""+string, Toast.LENGTH_SHORT).show();
                try {
                   bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    uploadimage(id,callurl);
                    // convertimage();
                    //imagejson();
                    //saveImage(bitmap);
                    //uploadImage("https://test.opis.link/api/carousel/edit/"+FonnlinkService.getInstance().getProfilename(), saveImage(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void uploadimage(String id, String callurl) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        //bitmap = Bitmap.createScaledBitmap(bitmap,600,600, true);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        Log.i("checkokhttp", "sending post");
        Log.i("checkstring", id);
        //Toast.makeText(getContext(), "sending post", Toast.LENGTH_SHORT).show();
        String urllink = getString(R.string.server_domain)+"/api/carousel/edit/"+FonnlinkService.getInstance().getProfilename();
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            data.put("carouselImg", imageString);
            data.put("call_url", callurl);
            data.put("existing_carousel_id", id);
            data.put("original_filename", ""+ Calendar.getInstance().getTimeInMillis()+".jpg");
            Log.i("checkokhttp", data.toString());
            //Toast.makeText(getContext(), ""+data.toString(), Toast.LENGTH_SHORT).show();



        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json, data.toString());
        Request newreq = new Request.Builder().url(urllink).post(body).build();


        client.newCall(newreq).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String mMessage = e.getMessage();
                Log.i("checkokhttp", mMessage);
                Snackbar.make(B_upload, mMessage, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


                progressDialog.dismiss();
                String mMessage = response.body().string();
                String responseDate = null;
                String responseSize = null;
                String responsePath = null;
                String responseFilename = null;
                String responseId = null;
                String responseDetails = null;
                String responseCallurl = null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    //responseCode = object.getString("status");
                    responseDetails = object.getString("description");
                    responseDate = object.getString("date_uploaded");
                    responseSize = object.getString("size");
                    responsePath = object.getString("path");
                    responseFilename = object.getString("filename");
                    responseId = object.getString("id");
                    responseCallurl = object.getString("call_url");
                    Log.d("response", responseDetails);
                    if (responseDetails.equals("Carousel img has been uploaded")) {
                        Handler refresh = new Handler(Looper.getMainLooper());
                        String finalResponseFilename = responseFilename;
                        String finalResponseDate = responseDate;
                        String finalResponseSize = responseSize;
                        String finalResponseId = responseId;
                        String finalResponsePath = responsePath;
                        String finalresponseCallurl = responseCallurl;


                        if (id.equals("null")) {
                            refresh.post(new Runnable() {
                                public void run() {
                                    Log.d("result", "added");
                                    imageDetails.add(new image_details(finalResponseFilename, finalResponseDate, finalResponseSize, finalResponseId, finalResponsePath, finalresponseCallurl));
                                    imageRecycleAdapter.notifyDataSetChanged();
                                }
                            });

                        } else {
                            refresh.post(new Runnable() {
                                public void run() {
                                    Log.d("result", "edit " + finalposition);
                                    //   imageDetails.add(new image_details(finalResponseFilename, finalResponseDate, finalResponseSize, finalResponseId, finalResponsePath, finalresponseCallurl));
                                    imageDetails.set(finalposition,new image_details(finalResponseFilename, finalResponseDate, finalResponseSize, finalResponseId, finalResponsePath, finalresponseCallurl));
                                    imageRecycleAdapter.notifyItemChanged(finalposition);


                                }
                            });
                        }
                    }



                    //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


                    Log.d("okhttp", mMessage);
                    Snackbar.make(B_upload, responseDetails, Snackbar.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("checkokhttp", mMessage);
            }


        });

    }

    @Override
    public void onClicklistener(int position) {
        //Toast.makeText(getContext(), ""+imageDetails.get(position).getId(), Toast.LENGTH_SHORT).show();
        dialog("https://test.opis.link"+imageDetails.get(position).getPath());
    }

    @Override
    public void imagePosition(int position) {
       // Toast.makeText(getContext(), ""+imageDetails.get(position).getId(), Toast.LENGTH_SHORT).show();
        performFileEdit(imageDetails.get(position).getId());
        finalposition = position;
        callurl = imageDetails.get(position).getCallurl();

    }

    @Override
    public void dialogPosition(int position) {
        callurl = callurlDetails.get(position).getCallurl();
        dialog.dismiss();
        performFileSearch();
    }


    public void dialog(String url){
         dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_dialog);
        Log.d("url", url);
        ImageView image =  dialog.findViewById(R.id.imge);

        dialog.setCancelable(true);

        Picasso.with(getContext()).load(url).resize(800,1080).into(image);
        dialog.show();
        //Glide.with(context).load("https://test.opis.link/assets/img/carousel/christian1/1623906767857-IMG_20210617_091216.jpg").into(image);

    }

    public void dialogforAdd(){
         dialog = new Dialog(getContext());
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setBackgroundDrawable(
//                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add);


        rvlist =  dialog.findViewById(R.id.rv_dialog);
        rvlist.setLayoutManager(new LinearLayoutManager(getContext()));
        callurlDetails = new ArrayList<>();
        parseCallUrl();
       // dialog.setCancelable(true);
        dialog.show();
        //Glide.with(context).load("https://test.opis.link/assets/img/carousel/christian1/1623906767857-IMG_20210617_091216.jpg").into(image);

    }

    private void parseCallUrl() {
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
                        callurlDetails.add(new callurl_details(name));

                    }
                    Log.d("volley", "" + object.getString("displayName"));
                    callUrlListRecyclerAdapter = new callUrlListRecyclerAdapter(getContext(), callurlDetails, this);
                    rvlist.setItemAnimator(new DefaultItemAnimator());
                    rvlist.setAdapter(callUrlListRecyclerAdapter);

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



}
