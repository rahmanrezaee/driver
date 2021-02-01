package com.development.taxiappproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRideClass;

import java.util.List;

public class EarningAdapter extends RecyclerView.Adapter<EarningAdapter.MyViewHolder> {

    List<MyEarningClass> myRideList;
    private Context context;

    public EarningAdapter(Context context, List<MyEarningClass> myRideList) {
        this.context = context;
        this.myRideList = myRideList;
    }

    @Override
    public EarningAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.earning_row, parent, false);

        return new EarningAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EarningAdapter.MyViewHolder holder, int position) {
        holder.timeTxt.setText(myRideList.get(position).getTime());
        holder.fareTxt.setText(myRideList.get(position).getFare());
        holder.milesTxt.setText(myRideList.get(position).getMiles());
        holder.paidTxt.setText(myRideList.get(position).getPaidTo());
    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTxt, fareTxt, milesTxt, paidTxt;

        public MyViewHolder(View view) {
            super(view);

            timeTxt = view.findViewById(R.id.earningItem_time_txt);
            fareTxt = view.findViewById(R.id.earningItem_fare_txt);
            milesTxt = view.findViewById(R.id.earningItem_miles_txt);
            paidTxt = view.findViewById(R.id.earningItem_payToAdmin_txt);
            //   imageView = (ImageView)view.findViewById(R.id.image);
//            cardView = (CardView)view.findViewById(R.id.cardView);
        }
    }
}
