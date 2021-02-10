package com.development.taxiappproject;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
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
import org.json.JSONString;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.OTPScreen.MyPREFERENCES;

public class NewRideRequest extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityNewRideRequestBinding requestBinding;
    private String TAG = "MAHDI";
    SharedPreferences sharedPreferences;
    ProgressDialog p;
    String userToken;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_new_ride_request);
        requestBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_ride_request);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestBinding.newRideAcceptBtn.setOnClickListener(this);
        requestBinding.newRideDeclineBtn.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        id = extras.getString("rideId");

        Log.i(TAG, "Mahdi: NewRideRequest: onCreate: 1 " + id + " : " + extras);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

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
            case R.id.newRide_accept_btn:

                p = new ProgressDialog(NewRideRequest.this);
                p.setMessage("Please wait...");
                p.setIndeterminate(false);
                p.setCancelable(false);
                p.show();

                acceptRequest();
                break;

            case R.id.newRide_decline_btn:
                declineRequest();
                p = new ProgressDialog(NewRideRequest.this);
                p.setMessage("Please wait...");
                p.setIndeterminate(false);
                p.setCancelable(false);
                p.show();
                break;

            case R.id.newRequest_back_btn:
                finish();
                break;
        }
    }

    private void acceptRequest() {
        acceptOrDecline("accepted");
    }

    private void declineRequest() {
        acceptOrDecline("rejected");
    }

    public void acceptOrDecline(String status) {

        RequestQueue requestQueue = Volley.newRequestQueue(NewRideRequest.this);
        String mURL;

//        accepted
//        rejected
//        completed
//        paid

        if (status.equalsIgnoreCase("accepted")) {
            mURL = baseUrl + "/rides/" + id + "?status=" + status;
        } else {
            mURL = baseUrl + "/rides/" + id + "?status=" + status;
        }

        Log.i(TAG, "MahdiMahdi: NewRideRequest: sendRequest: 1 " + id);
        Log.i(TAG, "MahdiMahdi: NewRideRequest: sendRequest: 2 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mURL,
                null,
                response -> {
                    try {
                        p.hide();

                        Log.i(TAG, "Mahdi: NewRideRequest: sendRequest: res 0 " + response);

                        String message = response.getString("message");
                        boolean serverStatus = response.getBoolean("status");

                        if (serverStatus) {
                            Toast.makeText(NewRideRequest.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NewRideRequest.this, "Accepted by another driver", Toast.LENGTH_SHORT).show();
                        }

                        if (status.equalsIgnoreCase("accepted") && serverStatus) {
                            Intent intent = new Intent(NewRideRequest.this, TripTracking.class);
                            intent.putExtra("rideId", id);
                            startActivity(intent);
                        } else if (!status.equalsIgnoreCase("accepted") && serverStatus) {
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            p.hide();
            Log.e("Mahdi", "Mahdi: NewRideRequest: sendRequest: Error 0 " + error.getMessage());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("token", userToken);
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }

            @Override
            public byte[] getBody() {
                return null;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: NewRideRequest: sendRequest: res 1 " + response.data);
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    Log.e("Mahdi", "Mahdi: NewRideRequest: sendRequest: Error 1 " + je.getMessage());
                    return Response.error(new ParseError(je));
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void getSingleRideItem(String userToken, String rideId) {
        RequestQueue requestQueue = Volley.newRequestQueue(NewRideRequest.this);
        String mURL = baseUrl + "/rides/?id=" + rideId;

        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: 10 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        JSONObject singleRide = data.getJSONObject(0);

                        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: res 00 " + data);

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
                        Log.e("Mahdi", "Mahdi: NewRideRequest:  getSingleRideItem: Error 0 " + e);
                        e.printStackTrace();
                    }
                }, error -> {
//            ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Not connection!", Toast.LENGTH_SHORT).show();
            Log.e("Mahdi", "Mahdi: NewRideRequest:  getSingleRideItem: Error 1 " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: res 1 " + response.data);
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
