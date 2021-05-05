package com.fonn.link.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fonn.link.FonnlinkService;
import com.fonn.link.R;

public class ProfileFragment extends Fragment {

    TextView displayname;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        displayname = root.findViewById(R.id.usernamewww);
        displayname.setText(FonnlinkService.getInstance().getProfilename());
        return root;
    }
}