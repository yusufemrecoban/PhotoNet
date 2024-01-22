package com.example.afinal.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.afinal.Activity.SplashActivity;
import com.example.afinal.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogOutFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(requireContext(), SplashActivity.class));

        if(getActivity()!=null){
            getActivity().finish();
        }

        View root=inflater.inflate(R.layout.fragment_log_out,container,false);
        return root;
    }
}