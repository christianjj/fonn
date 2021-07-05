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
import com.fonn.link.modal.history_details;

import java.util.ArrayList;

public class HistoryRecycleAdapter extends RecyclerView.Adapter<HistoryRecycleAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<history_details> historyList;
    private int lastPosition = -1;
    public HistoryRecycleAdapter(Context context, ArrayList<history_details> historyDetails) {
        this.context = context;
        this.historyList = historyDetails;
        }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, durationText, dateText;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tname);
            durationText = itemView.findViewById(R.id.tduration);
            dateText = itemView.findViewById(R.id.tdate);


        }

    }
    @NonNull
    @Override
    public HistoryRecycleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(context);
        itemView = inflater.inflate(R.layout.history_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryRecycleAdapter.MyViewHolder holder, int position) {
        history_details historyDetails = historyList.get(position);
        holder.nameText.setText("/"+historyDetails.getMyname());
        holder.dateText.setText("Date: "+historyDetails.getDate());
        holder.durationText.setText("Duration: "+ historyDetails.getDuration());
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
        return historyList.size();
    }
}
