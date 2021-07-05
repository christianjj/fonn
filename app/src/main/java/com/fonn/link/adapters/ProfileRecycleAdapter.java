package com.fonn.link.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fonn.link.R;
import com.fonn.link.modal.profile_details;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ProfileRecycleAdapter extends RecyclerView.Adapter<ProfileRecycleAdapter.MyViewHolder> {


    private Context context;
    private ArrayList<profile_details> userList;

    public ProfileRecycleAdapter(Context context, ArrayList<profile_details> userList){
        this.context = context;
        this.userList = userList;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText, BalnceText,status;
        Button topup;
        ImageView copy;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameinfo);
            BalnceText = itemView.findViewById(R.id.balance);
            topup = itemView.findViewById(R.id.topup);
            copy = itemView.findViewById(R.id.copylink);
            //number = itemView.findViewById(R.id.number);
            status = itemView.findViewById(R.id.linkstatus);

        }

    }

    @NonNull
    @Override
    public ProfileRecycleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(context);
        itemView = inflater.inflate(R.layout.profile_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecycleAdapter.MyViewHolder holder, int position) {
        String newvalue;
        holder.nameText.setText(userList.get(position).getName());
        holder.BalnceText.setText(userList.get(position).getBalance());
        if (userList.get(position).getStatus().equals("false")){
             newvalue = "Legacy Off";
        }
        else {
             newvalue = "Legacy On";
        }

        holder.status.setText(newvalue);
      //  holder.number.setText(userList.get(position).getNumber());
        holder.topup.setOnClickListener(view -> {
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                            Uri.parse("https://test.opis.link/payment?url="+userList.get(position).getName()));
            context.startActivity(viewIntent);
        });
        holder.copy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", "https://test.opis.link/" +userList.get(position).getName());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Text Copied",Toast.LENGTH_SHORT).show();


        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface mOnClickListener{
        void mOnClickListener(int position);

    }
}
