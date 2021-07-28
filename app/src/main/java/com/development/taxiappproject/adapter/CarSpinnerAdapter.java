package com.development.taxiappproject.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.taxiappproject.R;
import com.development.taxiappproject.model.CarModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CarSpinnerAdapter extends BaseAdapter {
    Context context;
    List<CarModel> objects;
    public CarSpinnerAdapter(Context context, List<CarModel> list) {

        this.context = context;
        this.objects = list;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.car_drop_down, viewGroup, false);

        ImageView icon = (ImageView) itemView.findViewById(R.id.imageView);

        TextView names = (TextView) itemView.findViewById(R.id.textView);


//        if (i == 0) {
//            icon.setVisibility(View.GONE);
//            names.setText("Car Type");
//            names.setTextColor(Color.GRAY);
//        } else {
        Picasso.get().load(objects.get(i).getUrlIcon()).placeholder(R.drawable.cloud_download).into(icon);

        names.setText(objects.get(i).getCartName());
//        }



        return itemView;
    }


}