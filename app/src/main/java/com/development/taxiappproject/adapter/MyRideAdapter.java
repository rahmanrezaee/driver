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

public class MyRideAdapter extends  RecyclerView.Adapter<MyRideAdapter.MyViewHolder> {
    List<MyRideClass> myRideList ;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public MyViewHolder(View view) {
            super(view);
            //   imageView = (ImageView)view.findViewById(R.id.image);
//            cardView = (CardView)view.findViewById(R.id.cardView);
        }
    }

    public MyRideAdapter(Context context, List<MyRideClass> myRideList) {
        this.context = context;
        this.myRideList = myRideList;
    }

    @Override
    public MyRideAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_row, parent, false);

        return new MyRideAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyRideAdapter.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }
}
