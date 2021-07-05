package com.fonn.link.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fonn.link.ConfigureAccountActivity;
import com.fonn.link.Dashboard;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;
import com.fonn.link.fragments.UploadimageFragment;
import com.fonn.link.interfaces.imagehandler;
import com.fonn.link.modal.image_details;
import com.fonn.link.modal.profile_details;
import com.google.android.material.snackbar.Snackbar;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.provider.Settings.System.getString;
import static com.fonn.link.Dashboard.countDownTimer;
import static com.fonn.link.OTPactivity.MyPREFERENCES;
import static com.fonn.link.OTPactivity.finishotp;
import static com.fonn.link.fragments.HomeFragment.Mypref;
import static com.fonn.link.fragments.HomeFragment.ads;
import static com.fonn.link.fragments.HomeFragment.callcpuntpref;
import static com.fonn.link.fragments.HomeFragment.urlads;


public class ImageRecycleAdapter extends RecyclerView.Adapter<ImageRecycleAdapter.MyViewHolder> {


    private Context context;
    private Activity activity;
    private ArrayList<image_details> Imagelist;
    image_details imageDetails;
    private onclicklistener monclicklistener;
    private int lastPosition = -1;
    ImageView image;

    UploadimageFragment uploadimageFragment;



    public ImageRecycleAdapter(Context context, ArrayList<image_details> Imagelist, onclicklistener monclicklistener){
        this.context = context;
        this.Imagelist = Imagelist;
        this.monclicklistener = monclicklistener;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView image, dateuploaded, filesize, callurl;
        ImageView view, delete;
        onclicklistener viewimage;

        private imagehandler imageHandler;
        public MyViewHolder(@NonNull View itemView, onclicklistener viewimage) {
            super(itemView);

            image = itemView.findViewById(R.id.tv_image);
            dateuploaded = itemView.findViewById(R.id.tv_date);
            filesize = itemView.findViewById(R.id.tv_filesize);
            delete = itemView.findViewById(R.id.deleteImage);
            view = itemView.findViewById(R.id.viewImage);
            callurl = itemView.findViewById(R.id.tv_callUrl);

            this.viewimage = viewimage;
            itemView.setOnClickListener(this);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewimage.imagePosition(getAdapterPosition());
                }
            });

         // viewimage.onClicklistener(getAdapterPosition());

        }



        @Override
        public void onClick(View view) {
        viewimage.onClicklistener(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public ImageRecycleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(context);
        itemView = inflater.inflate(R.layout.image_list, parent, false);
       // itemView.setOnClickListener(mOnClickListener);
        return new ImageRecycleAdapter.MyViewHolder(itemView,monclicklistener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ImageRecycleAdapter.MyViewHolder holder, int position) {
        String newvalue;
        onclicklistener editimage;
        holder.image.setText(Imagelist.get(position).getImage());
        holder.dateuploaded.setText(Imagelist.get(position).getDate());
        holder.filesize.setText(Imagelist.get(position).getFilesize());
        holder.callurl.setText(Imagelist.get(position).getCallurl());
//        holder.view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//              //  dialog("https://test.opis.link"+Imagelist.get(position).getPath());
//                //Toast.makeText(context, ""+Imagelist.get(position).getPath(), Toast.LENGTH_SHORT).show();
//               // oonclicklistener.onedit(Integer.parseInt(Imagelist.get(position).getId()));
//              // uploadimageFragment.performFileEdit("137");
//
//             //UploadimageFragment.test(""+Imagelist.get(position).getId(), activity);
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
//             //   intent.putExtra("ID_STRING", id);
//                intent.setType("image/*");
//                activity.startActivityForResult(intent, 88);
//            // imageHandler.imagePosition(Imagelist.get(position).getId());
//            }
//        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, ""+Imagelist.get(position).getId(), Toast.LENGTH_SHORT).show();

               confirmation(position);


            }
        });
        setAnimation(holder.itemView, position);
    }




    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return Imagelist.size();
    }

    public interface mOnClickListener{
        void mOnClickListener(int position);
    }

    public void deleteimage(String id, int position){

        Log.i("okhttp","sending post");;
        String url = "https://test.opis.link/api/carousel/delete/"+ FonnlinkService.getInstance().getProfilename();
        OkHttpClient client = new OkHttpClient();
        MediaType json = MediaType.parse("application/json;charset=utf-8");
        JSONObject data = new JSONObject();
        try {
            String s = FonnlinkService.getInstance().getAddressname();
            data.put("call_url", FonnlinkService.getInstance().getProfilename());
            data.put("carousel_id", id);
            //   assert device != null;
            Log.d("deletelog",data.toString() + url);
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
                ImageView delete = null;
                try {
                    JSONObject object = new JSONObject(mMessage);
                    responseCode = object.getString("status");
                    responseCode2 = object.getString("description");
                    if (responseCode.equals("SUCCESS")) {
                        Log.i("okhttp", mMessage);
                        Handler refresh = new Handler(Looper.getMainLooper());
                        refresh.post(() -> removeAt(position));
                    }
                    Looper.prepare();
                    Toast.makeText(context, responseCode2, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("okhttp",  mMessage);
            }
        });
    }

    public void removeAt(int position) {
        Imagelist.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, Imagelist.size());
    }


    public void confirmation (int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Delete");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", (dialog, which) -> {
            // Do nothing but close the dialog
            deleteimage(Imagelist.get(position).getId(),position);
            dialog.dismiss();
        });

        builder.setNegativeButton("NO", (dialog, which) -> {
            // Do nothing
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public interface  onclicklistener{
        void onClicklistener(int position);
        void imagePosition(int position);
        void dialogPosition(int position);
    }











}
