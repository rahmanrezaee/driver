package com.development.taxiappproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyRideClass;

import java.util.List;

public class MyRideAdapter extends RecyclerView.Adapter<MyRideAdapter.MyViewHolder> {
    List<MyRideClass> myRideList;
    private Context context;

    public MyRideAdapter(Context context, List<MyRideClass> myRideList) {
        this.context = context;
        this.myRideList = myRideList;
    }

    @Override
    public MyRideAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyRideAdapter.MyViewHolder holder, int position) {
        holder.dateTxt.setText(myRideList.get(position).getDateRide());
        holder.priceTxt.setText(myRideList.get(position).getPriceRide());
        holder.distanceTxt.setText(myRideList.get(position).getDistanceRide());
        holder.timeTxt.setText(myRideList.get(position).getTimeRide());
        holder.startLocationTxt.setText(myRideList.get(position).getStartLocationRide());
        holder.endLocationTxt.setText(myRideList.get(position).getEndLocationRide());
    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTxt, priceTxt, distanceTxt, timeTxt, startLocationTxt, endLocationTxt;

        public MyViewHolder(View view) {
            super(view);
            dateTxt = view.findViewById(R.id.ride_item_date);
            priceTxt = view.findViewById(R.id.ride_item_price);
            distanceTxt = view.findViewById(R.id.ride_item_distance);
            timeTxt = view.findViewById(R.id.ride_item_time);
            startLocationTxt = view.findViewById(R.id.ride_item_start_location);
            endLocationTxt = view.findViewById(R.id.ride_item_end_location);
//            cardView = (CardView)view.findViewById(R.id.cardView);
        }
    }
}
