package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
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

                        ridingBinding.completeRidingPriceTxt.setText(singleRide.getString("actualFareAmount"));
                        ridingBinding.completeRidingMilesTxt.setText(singleRide.getString("miles") + " Miles");
                        ridingBinding.completeRidingTimeTxt.setText(singleRide.getString("actualTimePassed") + " Mins");
                        ridingBinding.completeRidingTimeStartTxt.setText(singleRide.getString("paidIn"));
                        ridingBinding.completeRidingTimeEndTxt.setText(singleRide.getString("updatedAt"));
                        ridingBinding.completeRidingEndLocationTxt.setText(singleRide.getString("toWhere"));
                        ridingBinding.completeRidingStartLocationTxt.setText(singleRide.getString("from"));


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

}