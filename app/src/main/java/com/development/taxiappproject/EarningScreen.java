package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.development.taxiappproject.adapter.EarningAdapter;
import com.development.taxiappproject.adapter.MyRideAdapter;
import com.development.taxiappproject.databinding.ActivityEarningScreenBinding;
import com.development.taxiappproject.databinding.ActivityMyRideScreenBinding;
import com.development.taxiappproject.model.MyRideClass;

import java.util.ArrayList;
import java.util.List;

public class EarningScreen extends AppCompatActivity {
    private EarningAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityEarningScreenBinding screenBinding;
    List<MyRideClass> rideList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this,R.layout.activity_earning_screen);
        setRecyclerView();
        settestimonialList();
    }

    private void setRecyclerView() {
        rideAdapter = new  EarningAdapter(EarningScreen.this,  rideList);
        mLayoutManager = new LinearLayoutManager(EarningScreen.this);
        screenBinding.recyclerView.setLayoutManager(mLayoutManager);
        screenBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        screenBinding.recyclerView.setAdapter(rideAdapter);

    }

    private void settestimonialList(){
        rideList.clear();
        for (int i=0;i<4;i++){
            MyRideClass ride = new MyRideClass();
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
