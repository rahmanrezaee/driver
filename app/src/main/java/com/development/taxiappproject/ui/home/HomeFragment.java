package com.development.taxiappproject.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.development.taxiappproject.CompleteRide;
import com.development.taxiappproject.NewRideRequest;
import com.development.taxiappproject.R;
import com.development.taxiappproject.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        binding.newRideRequest.setOnClickListener(this);
        binding.completedRequest.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.new_ride_request:
                startActivity(new Intent(getActivity(), NewRideRequest.class));
                break;

            case R.id.completed_request:
                startActivity(new Intent(getActivity(), CompleteRide.class));
                break;
        }
    }
}
