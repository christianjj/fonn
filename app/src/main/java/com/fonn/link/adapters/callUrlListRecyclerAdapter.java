package com.fonn.link.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fonn.link.R;
import com.fonn.link.modal.callurl_details;
import com.fonn.link.modal.history_details;

import java.util.ArrayList;

public class callUrlListRecyclerAdapter extends RecyclerView.Adapter<callUrlListRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<callurl_details> callurlDetails;
    private int lastPosition = -1;
    private ImageRecycleAdapter.onclicklistener monclicklistener;
    public callUrlListRecyclerAdapter(Context context, ArrayList<callurl_details> callurlDetails,  ImageRecycleAdapter.onclicklistener monclicklistener) {
        this.context = context;
        this.callurlDetails = callurlDetails;
        this.monclicklistener = monclicklistener;
        }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameText;
        ImageRecycleAdapter.onclicklistener viewimage;

        public MyViewHolder(@NonNull View itemView, ImageRecycleAdapter.onclicklistener viewimage) {
            super(itemView);
            nameText = itemView.findViewById(R.id.linkname);
            this.viewimage = viewimage;
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            viewimage.dialogPosition(getAdapterPosition());
        }
    }
    @NonNull
    @Override
    public callUrlListRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(context);
        itemView = inflater.inflate(R.layout.callurl_list, parent, false);
        return new MyViewHolder(itemView,monclicklistener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull callUrlListRecyclerAdapter.MyViewHolder holder, int position) {
        callurl_details callurl = callurlDetails.get(position);
        holder.nameText.setText(callurl.getCallurl());
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
        return callurlDetails.size();
    }
}
