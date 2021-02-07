package com.development.taxiappproject;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

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
import com.development.taxiappproject.databinding.ActivityNewRideRequestBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class NewRideRequest extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityNewRideRequestBinding requestBinding;
    private String TAG = "MAHDI";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_new_ride_request);
        requestBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_ride_request);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestBinding.accept.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");

        Log.i(TAG, "Mahdi: NewRideRequest: onCreate: 1 " + id + " : " + extras);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        getSingleRideItem(userToken, id);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accept:
                startActivity(new Intent(NewRideRequest.this, TripTracking.class));
                break;
        }
    }

    public void getSingleRideItem(String userToken, String rideId) {
        RequestQueue requestQueue = Volley.newRequestQueue(NewRideRequest.this);
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

                        requestBinding.newRidePrice.setText("$ " + singleRide.getString("actualFareAmount"));
                        requestBinding.newRideDistance.setText(singleRide.getString("miles") + " Miles");
                        requestBinding.newRideMinutes.setText(singleRide.getString("actualTimePassed") + " Mins");
//                        requestBinding.completeRidingTimeStartTxt.setText(singleRide.getString("paidIn"));
//                        ridingBinding.completeRidingTimeEndTxt.setText(singleRide.getString("updatedAt"));
                        requestBinding.newRideTo.setText(singleRide.getString("toWhere"));
                        requestBinding.newRideFrom.setText(singleRide.getString("from"));


//                        ridingBinding.completeRidingRelativeLayout.setVisibility(View.VISIBLE);
//                        ridingBinding.completeRidingRelativeLayoutProgress.setVisibility(View.GONE);

                    } catch (JSONException e) {
//                        ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
                        Log.e("Mahdi", "Mahdi: CompleteRiding: getSingleRideItem: Error 0 " + e);
                        e.printStackTrace();
                    }
                }, error -> {
//            ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
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
}
