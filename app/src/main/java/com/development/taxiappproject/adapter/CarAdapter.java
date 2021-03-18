package com.development.taxiappproject.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.MyInterface.OnTextClickListener;
import com.development.taxiappproject.MyRideScreen;
import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyRideClass;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.MyViewHolder> {

    List<MyRideClass> myRideList;
    private Context context;
    OnTextClickListener listener;
    int row_index = -1;
    String carTypeId;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView carTitle, carQuantity;
        public ImageView carImage;
        public LinearLayout linearLayout_itemCar;

        public MyViewHolder(View view) {
            super(view);

            carImage = view.findViewById(R.id.itemCar_carIcon);
            carTitle = view.findViewById(R.id.itemCar_model_txt);
            carQuantity = view.findViewById(R.id.itemCar_carQuantity_txt);
            linearLayout_itemCar = view.findViewById(R.id.linearLayout_itemCar);
        }
    }

    public CarAdapter(Context context, List<MyRideClass> myRideList, OnTextClickListener listener, String carTypeId) {
        this.context = context;
        this.myRideList = myRideList;
        this.listener = listener;
        this.carTypeId = carTypeId;
    }

    @Override
    public CarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);

        return new CarAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CarAdapter.MyViewHolder holder, int position) {
        MyRideClass car = myRideList.get(position);
        holder.carTitle.setText(myRideList.get(position).getTimeRide());
        holder.carQuantity.setText(myRideList.get(position).getDistanceRide());
        Picasso.get().load(myRideList.get(position).getStartLocationRide()).into(holder.carImage);

        holder.linearLayout_itemCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTextClick(car.getId());
                Log.i("MAHDI", "CarAdapter: onClick: " + car.getId());
                row_index = position;
                carTypeId = car.getId();
                notifyDataSetChanged();
            }
        });
        if (carTypeId.equals(car.getId())) {
            holder.linearLayout_itemCar.setBackgroundColor(Color.parseColor("#c9c9c9"));
//            holder.tv1.setTextColor(Color.parseColor("#ffffff"));
        } else {
            holder.linearLayout_itemCar.setBackgroundColor(Color.TRANSPARENT);
//            holder.tv1.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }
}
