package com.development.taxiappproject.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.development.taxiappproject.HomeScreen;
import com.development.taxiappproject.NewRideRequest;
import com.development.taxiappproject.R;
import com.development.taxiappproject.Service.IResult;
import com.development.taxiappproject.Service.VolleyService;
import com.development.taxiappproject.data.model.NotificationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class MyNotificationAdapter extends  RecyclerView.Adapter<MyNotificationAdapter.MyViewHolder>  {
    List<NotificationModel> myNotificationList ;
    private Context context;

    IResult mResultCallback = null;
    VolleyService mVolleyService;
    String token;

    ProgressDialog progressDialog;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView body;
        public TextView date;
        public CardView cardView;




        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title_notification);
            body = view.findViewById(R.id.body_notification);
            date = view.findViewById(R.id.date_notification);
            cardView = view.findViewById(R.id.cardView);


        }
    }

    public MyNotificationAdapter(Context context, List<NotificationModel> myNotificationList, ProgressDialog progressDialog, String token) {
        this.context = context;
        this.myNotificationList = myNotificationList;
        this.progressDialog = progressDialog;
        this.token = token;
    }

    @Override
    public MyNotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_notification_row, parent, false);

        return new MyNotificationAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyNotificationAdapter.MyViewHolder holder, int position) {


        holder.title.setText(this.myNotificationList.get(position).getTitle());
        holder.body.setText(this.myNotificationList.get(position).getBody());


        String dateStr = this.myNotificationList.get(position).getDate();

        DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");

        // parse the date string into Date object
        Date date = null;
        try {
            date = srcDf.parse(dateStr);
        } catch (ParseException e) {
           date = new Date();
        }

        DateFormat destDf = new SimpleDateFormat("yyyy/MM/dd 'at' hh:mm a");

        // format the date into another format
        dateStr = destDf.format(date);



        holder.date.setText(dateStr);
        holder.cardView.setOnClickListener(v -> {

            progressDialog.show();
            initVolleyCallback();
            mVolleyService = new VolleyService(mResultCallback, this.context);

            try {
                String mURL =  "/ride/list/"+this.myNotificationList.get(position).getRideId();
                mVolleyService.getDataVolley( mURL, token,"Notification Requesst");
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    @Override
    public int getItemCount() {
        return myNotificationList.size();
    }

    void initVolleyCallback() {

        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d("Notification Ride", "Volley requester " + requestType);
                Log.d("Notification Ride", "Volley JSON post" + response);
                progressDialog.dismiss();


                try {
                    if (response.getBoolean("status")){
                        JSONObject data = response.optJSONObject("data");
                        if (data != null && data.has("status")){

                            if (data.getString("status").equalsIgnoreCase("active") ||  data.getString("status").equalsIgnoreCase("decline") ){

//                              Intent intent = new Intent(context, CompleteRides.class);
//                            intent.putExtra("AllData", data.toString());
//
//                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            Log.i("Noficaition", "Mahdi: Socket ride: completed" + data.toString());
//                            context.startActivity(intent);

                                JSONObject userData = data.getJSONObject("userId");

                                Intent intent = new Intent(context, NewRideRequest.class);
                                intent.putExtra("rideId", data.getString("_id"));
                                intent.putExtra("user",userData.toString());
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }else {
                                Toast.makeText(context, "Ride Already Accepted or Completed", Toast.LENGTH_SHORT).show();
                            }

                        }else {

                            Toast.makeText(context, "Ride Deleted", Toast.LENGTH_SHORT).show();
                        }
                      
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d("Notification Ride", "Volley requester " + requestType);
                Log.d("Notification Ride", "Volley JSON post" + "That didn't work!");
                Toast.makeText(context, "Error In Fetch Data", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };
    }

}
