package com.development.taxiappproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.CompleteRiding;
import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyRideClass;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.MyViewHolder> {
    List<MyRideClass> myDashboardList;
    private Context context;

    public DashboardAdapter(Context context, List<MyRideClass> myRideList) {
        this.context = context;
        this.myDashboardList = myRideList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_item_row, parent, false);

        return new DashboardAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.dateTxt.setText(myDashboardList.get(position).getDateRide());
        holder.priceTxt.setText(myDashboardList.get(position).getPriceRide());
        holder.distanceTxt.setText(myDashboardList.get(position).getDistanceRide());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CompleteRiding.class);
                intent.putExtra("id", myDashboardList.get(position).getId());
                context.startActivity(intent);

//                Intent intent = new Intent(, CompleteMyRide.class);
//                intent.putExtra("id", myDashboardList.get(position).getId());
//                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myDashboardList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTxt, priceTxt, distanceTxt;
        public LinearLayout linearLayout;

        public MyViewHolder(View view) {
            super(view);
            dateTxt = view.findViewById(R.id.dashboard_item_date);
            priceTxt = view.findViewById(R.id.dashboard_item_fare);
            distanceTxt = view.findViewById(R.id.dashboard_item_miles);
            linearLayout = view.findViewById(R.id.dashboard_item_linear);
        }
    }
}
