package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

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
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.databinding.ActivityTripTrackingBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.OTPScreen.MyPREFERENCES;

public class TripTracking extends AppCompatActivity {
    private ActivityTripTrackingBinding tripTrackingBinding;
    private String TAG = "MAHDI";
    String userToken;
    String id;
    SharedPreferences sharedPreferences;
    ProgressDialog p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripTrackingBinding = DataBindingUtil.setContentView(this, R.layout.activity_trip_tracking);
//        setContentView(R.layout.activity_trip_tracking);

        if (!MyCheckConnection.mCheckConnectivity(TripTracking.this)) {
            return;
        }

        Bundle extras = getIntent().getExtras();
        id = extras.getString("rideId");

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        Log.i(TAG, "Mahdi: TripTracking: onCreate: 1 " + id + " : " + extras);
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tripTracking_paidAndComp_btn:
                p = new ProgressDialog(TripTracking.this);
                p.setMessage("Please wait...");
                p.setIndeterminate(false);
                p.setCancelable(false);
                p.show();

                if (tripTrackingBinding.tripTrackingPaidAndCompBtn.getText().toString().equalsIgnoreCase("Paid and Start Ride")) {
                    paidAndComp("paid");
                } else {
                    paidAndComp("completed");
                }
                break;

            case R.id.tripTracking_back_btn:
                finish();
                break;
        }
    }

    public void paidAndComp(String status) {

        RequestQueue requestQueue = Volley.newRequestQueue(TripTracking.this);
        String mURL;

//        accepted
//        rejected
//        completed
//        paid

        if (status.equalsIgnoreCase("paid")) {
            mURL = baseUrl + "/rides/" + id + "?status=" + status;
        } else {
            mURL = baseUrl + "/rides/" + id + "?status=" + status;
        }

        Log.i(TAG, "MahdiMahdi: TripTracking: sendRequest: 1 " + id);
        Log.i(TAG, "MahdiMahdi: TripTracking: sendRequest: 2 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mURL,
                null,
                response -> {
                    try {
                        p.hide();

                        Log.i(TAG, "Mahdi: TripTracking: sendRequest: res 0 " + response);

                        String message = response.getString("message");
                        boolean serverStatus = response.getBoolean("status");

                        Toast.makeText(TripTracking.this, message, Toast.LENGTH_SHORT).show();

                        if (status.equalsIgnoreCase("paid") && serverStatus) {
                            tripTrackingBinding.tripTrackingPaidAndCompBtn.setText("Complete Trip");
                        } else if (!status.equalsIgnoreCase("paid") && serverStatus) {
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            p.hide();
            Log.e("Mahdi", "Mahdi: TripTracking: sendRequest: Error 0 " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: TripTracking: sendRequest: res 1 " + response.data);
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    Log.e("Mahdi", "Mahdi: TripTracking: sendRequest: Error 1 " + je.getMessage());
                    return Response.error(new ParseError(je));
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}
