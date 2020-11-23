package com.development.taxiappproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.development.taxiappproject.R;
import com.development.taxiappproject.model.MyRideClass;

import java.util.List;

public class EarningAdapter extends  RecyclerView.Adapter<EarningAdapter.MyViewHolder> {

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

    public EarningAdapter(Context context, List<MyRideClass> myRideList) {
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

    }

    @Override
    public int getItemCount() {
        return myRideList.size();
    }
}
