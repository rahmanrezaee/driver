package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.adapter.MyRideAdapter;
import com.development.taxiappproject.databinding.ActivityMyRideScreenBinding;
import com.development.taxiappproject.model.MyRideClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class MyRideScreen extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    private MyRideAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityMyRideScreenBinding screenBinding;
    List<MyRideClass> rideList = new ArrayList<>();
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;

//    myRideScreen_swipeRefreshLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_ride_screen);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String firebaseToken = sharedPreferences.getString(SharedPrefKey.firebaseToken, "defaultValue");

        progressBar = findViewById(R.id.rideScreen_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        swipeRefreshLayout = findViewById(R.id.myRideScreen_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getRideItem(userToken);
        });

        if (!MyCheckConnection.mCheckConnectivity(MyRideScreen.this)) {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            return;
        }

        setRecyclerView();
        getRideItem(userToken);
    }

    private void setRecyclerView() {
        rideAdapter = new MyRideAdapter(MyRideScreen.this, rideList);
        mLayoutManager = new LinearLayoutManager(MyRideScreen.this);
        screenBinding.recyclerView.setLayoutManager(mLayoutManager);
        screenBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        screenBinding.recyclerView.setAdapter(rideAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.myRideScreen_back_btn:
                finish();
                break;
        }
    }

    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(MyRideScreen.this);
        String mURL = baseUrl + "/rides";

        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + data);

                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);

                        settestimonialList(data);

                    } catch (JSONException e) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                }, error -> {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            Log.e("Mahdi", "Mahdi: HomeScreen: getDashboard: Error " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + response.data);
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

    private void settestimonialList(JSONArray data) {

        rideList.clear();
        for (int i = 0; i < data.length(); i++) {
            MyRideClass ride = null;

            try {
                JSONObject myData = data.getJSONObject(i);

                String fromJson = myData.optString("from");
                double fromLat = Double.parseDouble(fromJson.substring(0, fromJson.indexOf(",")));
                double fromLng = Double.parseDouble(fromJson.substring(fromJson.indexOf(" ")));
                String fromTxt = GlobalVal.convertLatLng(MyRideScreen.this, fromLat, fromLng);

                String toWhereJson = myData.optString("toWhere");
                double toWhereLat = Double.parseDouble(toWhereJson.substring(0, toWhereJson.indexOf(",")));
                double toWhereLng = Double.parseDouble(toWhereJson.substring(toWhereJson.indexOf(" ")));
                String destination = GlobalVal.convertLatLng(MyRideScreen.this, toWhereLat, toWhereLng);

                String value = new DecimalFormat("##.##").format(myData.optDouble("actualFareAmount"));
                String miles = new DecimalFormat("##.##").format(myData.optDouble("miles"));

                ride = new MyRideClass(myData.getString("eta"), value, miles, myData.getString("actualTimePassed"),
                        fromTxt, destination, myData.getString("_id"));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
