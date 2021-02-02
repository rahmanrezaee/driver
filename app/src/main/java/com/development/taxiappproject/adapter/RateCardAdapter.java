package com.development.taxiappproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRateCardClass;

import java.util.List;

public class RateCardAdapter extends RecyclerView.Adapter<RateCardAdapter.MyViewHolder> {

    List<MyRateCardClass> myRideList;
    private Context context;


    public RateCardAdapter(Context context, List<MyRateCardClass> myRideList) {
        this.context = context;
        this.myRideList = myRideList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.table_rate_card_item, parent, false);

        return new RateCardAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.zipTxt.setText(myRideList.get(position).getZip());
        holder.rateTxt.setText(myRideList.get(position).getRate());
    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView zipTxt, rateTxt;

        public MyViewHolder(View view) {
            super(view);
            zipTxt = view.findViewById(R.id.rateCard_zip_txt);
            rateTxt = view.findViewById(R.id.rateCard_rate_txt);
        }
    }
}
