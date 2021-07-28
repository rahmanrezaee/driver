package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.databinding.ActivityCompleteRidingBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class CompleteRiding extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    ActivityCompleteRidingBinding ridingBinding;
    SharedPreferences sharedPreferences;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ridingBinding = DataBindingUtil.setContentView(this, R.layout.activity_complete_riding);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        Log.i(TAG, "Mahdi: CompleteRiding: token: " + userToken);

        ridingBinding.completeRidingRelativeLayout.setVisibility(View.GONE);
        ridingBinding.completeRidingRelativeLayoutProgress.setVisibility(View.VISIBLE);
        ridingBinding.back.setOnClickListener(v -> {
            Log.i(TAG, "onCreate: Back press");
            Intent intent = new  Intent(this,HomeScreen.class);
            startActivity(intent);
            finish();

            super.onBackPressed();

        });

        String sessionId = getIntent().getStringExtra("id");

        getSingleRideItem(userToken, sessionId);
    }

    public void getSingleRideItem(String userToken, String rideId) {
        RequestQueue requestQueue = Volley.newRequestQueue(CompleteRiding.this);
        String mURL = baseUrl + "/rides/?id=" + rideId;

        Log.i(TAG, "Mahdi: CompleteRiding: getSingleRideItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: CompleteRiding: getSingleRideItem: 10 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: CompleteRiding: getSingleRideItem: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        JSONObject singleRide = data.getJSONObject(0);

                        Log.i(TAG, "Mahdi: CompleteRiding: getSingleRideItem: res 00 " + data);

                        if (singleRide.has("fareAmount") && Double.parseDouble(singleRide.optString("fareAmount")) < Double.parseDouble(singleRide.optString("actualFareAmount"))){


                            double value  = singleRide.optDouble("fareAmount");
                            double showValue = Double.parseDouble(new DecimalFormat("##.##").format(value));

                            ridingBinding.completeRidingPriceTxt.setText("$ " + showValue);
                        }else{

                            double value  = singleRide.optDouble("actualFareAmount");
                            double showValue = Double.parseDouble(new DecimalFormat("##.##").format(value));



                            ridingBinding.completeRidingPriceTxt.setText("$ " + showValue);

                        }

                        double miles = singleRide.optDouble("miles");
                        double showMiles = Double.parseDouble(new DecimalFormat("##.##").format(miles));




                        ridingBinding.completeRidingMilesTxt.setText( showMiles+" Miles");
                        ridingBinding.completeRidingTimeTxt.setText(singleRide.optString("eta"));


                        try {
                            Date startTime =new SimpleDateFormat().parse(singleRide.getString("paidIn"));

                            Date endTime =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'").parse(singleRide.getString("updatedAt"));
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
                            Calendar calendarStart = GregorianCalendar.getInstance(); // creates a new calendar instance
                            calendarStart.setTime(startTime);
                            Calendar calendarEnd = GregorianCalendar.getInstance(); // creates a new calendar instance
                            calendarStart.setTime(endTime);

                            ridingBinding.completeRidingTimeStartTxt.setText("Time " +dateFormat.format(calendarStart.getTime()) );
                            ridingBinding.completeRidingTimeEndTxt.setText("Time " +dateFormat.format(calendarEnd.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        ridingBinding.completeRidingEndLocationTxt.setText(singleRide.getString("toWhereLabel"));
                        ridingBinding.completeRidingStartLocationTxt.setText(singleRide.getString("fromLabel"));


                        ridingBinding.completeRidingRelativeLayout.setVisibility(View.VISIBLE);
                        ridingBinding.completeRidingRelativeLayoutProgress.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
                        Log.e("Mahdi", "Mahdi: CompleteRiding: getSingleRideItem: Error 0 " + e);
                        e.printStackTrace();
                    }
                }, error -> {
            ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Not connection!", Toast.LENGTH_SHORT).show();
            Log.e("Mahdi", "Mahdi: CompleteRiding: getSingleRideItem: Error 1 " + error.getMessage());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("token", userToken);
                return params;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: CompleteRiding: getSingleRideItem: res 1 " + response.data);
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new  Intent(this,HomeScreen.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }
}