package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.development.taxiappproject.adapter.CarAdapter;
import com.development.taxiappproject.databinding.ActivityMyProfileBinding;
import com.development.taxiappproject.model.MyRideClass;

import java.util.ArrayList;
import java.util.List;

public class MyProfile extends AppCompatActivity {
    private CarAdapter carAdapter;
    RecyclerView.LayoutManager mLayoutManager;
     ActivityMyProfileBinding profileBinding;
    List<MyRideClass> rideList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding  = DataBindingUtil.setContentView(this,R.layout.activity_my_profile);
        setRecyclerView();
        settestimonialList();
    }

    private void setRecyclerView() {
        carAdapter = new CarAdapter(MyProfile.this,  rideList);
        mLayoutManager = new LinearLayoutManager(MyProfile.this,LinearLayoutManager.HORIZONTAL,false);
        profileBinding.recyclerView.setLayoutManager(mLayoutManager);
        profileBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        profileBinding.recyclerView.setAdapter(carAdapter);

    }

    private void settestimonialList(){
        rideList.clear();
        for (int i=0;i<4;i++){
            MyRideClass ride = new MyRideClass("dateRide", "priceRide", "distanceRide", "timeRide", "startLocationRide", "endLocationRide");
            rideList.add(ride);
        }
        carAdapter.notifyDataSetChanged();
    }
}
