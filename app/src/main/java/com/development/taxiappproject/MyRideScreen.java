package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.development.taxiappproject.adapter.MyRideAdapter;
import com.development.taxiappproject.databinding.ActivityMyRideScreenBinding;
import com.development.taxiappproject.model.MyRideClass;

import java.util.ArrayList;
import java.util.List;

public class MyRideScreen extends AppCompatActivity {
    private MyRideAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityMyRideScreenBinding screenBinding;
    List<MyRideClass> rideList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_ride_screen);

        setRecyclerView();
        settestimonialList();
    }

    private void setRecyclerView() {
        rideAdapter = new MyRideAdapter(MyRideScreen.this, rideList);
        mLayoutManager = new LinearLayoutManager(MyRideScreen.this);
        screenBinding.recyclerView.setLayoutManager(mLayoutManager);
        screenBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        screenBinding.recyclerView.setAdapter(rideAdapter);

    }

    private void settestimonialList() {
        rideList.clear();
        for (int i = 0; i < 4; i++) {
            MyRideClass ride = new MyRideClass();
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
