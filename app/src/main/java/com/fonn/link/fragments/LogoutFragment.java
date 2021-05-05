package com.fonn.link.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fonn.link.ConfigureAccountActivity;
import com.fonn.link.FonnlinkService;
import com.fonn.link.R;

import static com.fonn.link.OTPactivity.MyPREFERENCES;
import static com.fonn.link.OTPactivity.finishotp;

public class LogoutFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        startActivity(new Intent(getContext(), ConfigureAccountActivity.class));
        FonnlinkService.getCore().clearProxyConfig();
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(finishotp, false);
        editor.apply();

        return inflater.inflate(R.layout.activity_logout_fragment, container, false);


    }
}