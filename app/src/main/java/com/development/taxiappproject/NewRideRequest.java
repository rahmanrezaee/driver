package com.development.taxiappproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.databinding.ActivityNewRideRequestBinding;
import com.development.taxiappproject.helper.FetchURL;
import com.development.taxiappproject.helper.TaskLoadedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Global.GlobalVal.GOOGLE_MAP_API_KEY;
import static com.development.taxiappproject.OTPScreen.MyPREFERENCES;

public class NewRideRequest extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, TaskLoadedCallback {

    private GoogleMap mMap;
    private ActivityNewRideRequestBinding requestBinding;
    private String TAG = "MAHDI";
    SharedPreferences sharedPreferences;
    ProgressDialog p;
    String userToken;
    String id;
    JSONObject notificationData;
    private FusedLocationProviderClient fusedLocationProviderClient;

    MarkerOptions place1, place2;

    //    Polyline driverPolyLine;
    Polyline currentPolyLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_new_ride_request);
        requestBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_ride_request);

        if (!MyCheckConnection.mCheckConnectivity(NewRideRequest.this)) {
            p.hide();
            return;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestBinding.newRideAcceptBtn.setOnClickListener(this);
        requestBinding.newRideDeclineBtn.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        id = extras.getString("rideId");
        Log.i(TAG, "onCreate: " + id + " : " + extras.getString("user"));
        try {
            notificationData = new JSONObject(extras.getString("user"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestBinding.newRideUserName.setText(notificationData.optString("username"));

        Log.i(TAG, "Mahdi: NewRideRequest: onCreate: 1 " + id + " : " + extras);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        getSingleRideItem(userToken, id);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        mMap = googleMap;
//
//        Dexter.withContext(NewRideRequest.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                        if (ActivityCompat.checkSelfPermission(NewRideRequest.this,
//                                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                                NewRideRequest.this, android.Manifest.permission.ACCESS_COARSE_LOCATION
//                        ) != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//                        mMap.setMyLocationEnabled(true);
//                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
//                            @Override
//                            public boolean onMyLocationButtonClick() {
//                                if (ActivityCompat.checkSelfPermission(NewRideRequest.this,
//                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                                        ActivityCompat.checkSelfPermission(NewRideRequest.this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
//                                                PackageManager.PERMISSION_GRANTED) {
//                                    // TODO: Consider calling
//                                    //    ActivityCompat#requestPermissions
//                                    // here to request the missing permissions, and then overriding
//                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                    //                                          int[] grantResults)
//                                    // to handle the case where the user grants the permission. See the documentation
//                                    // for ActivityCompat#requestPermissions for more details.
//                                    return false;
//                                }
//                                fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
////                                        Snackbar.make(requireView(), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT).show();
////                                        homeBinding.progressBar.setVisibility(View.GONE);
//                                    }
//                                }).addOnSuccessListener(new OnSuccessListener<Location>() {
//                                    @Override
//                                    public void onSuccess(Location location) {
//                                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                                        body.clear();
//                                        body.put("lat", location.getLatitude() + "");
//                                        body.put("lng", location.getLongitude() + "");
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10f));
////                                        homeBinding.progressBar.setVisibility(View.GONE);
//                                        getRideItem(userToken, userLatLng);
////                                        body["lat"] = "${location.latitude}";
////                                        body["lng"] = "${location.longitude}";
//                                    }
//                                });
//                                return true;
//                            }
////                            (mapFragment()!!.findViewById<View>("1".toInt())!!.parent!! as View)
////                                    .findViewById<View>("2".toInt());
//                        });
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
////                        Snackbar.make(requireView(), permissionDeniedResponse.
////                                getPermissionName() + "needed for run app", Snackbar.LENGTH_SHORT).show();
////                        homeBinding.progressBar.setVisibility(View.VISIBLE);
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
////                        homeBinding.progressBar.setVisibility(View.VISIBLE);
//                    }
//                }).check();
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//
//        // Add a marker in Sydney and move the camera
////        LatLng sydney = new LatLng(-34, 151);
////        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
////        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newRide_accept_btn:
                p = GlobalVal.mProgressDialog(NewRideRequest.this, p, "Please wait...");
                acceptRequest();
                break;

            case R.id.newRide_decline_btn:
                declineRequest();
                p = GlobalVal.mProgressDialog(NewRideRequest.this, p, "Please wait...");
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
                            intent.putExtra("Data", notificationData.toString());
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
        String mURL = baseUrl + "/ride/list/" + rideId;

        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: 10 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: res 0 " + response);
                    JSONObject data = response.optJSONObject("data");

//                        JSONObject singleRide = data.getJSONObject();

                    Log.i(TAG, "Mahdi: NewRideRequest:  getSingleRideItem: res 00 " + data);

                    requestBinding.newRidePrice.setText("$ " + data.optString("actualFareAmount"));
                    requestBinding.newRideDistance.setText(data.optString(("miles") + " Miles"));
                    requestBinding.newRideMinutes.setText(data.optString(("actualTimePassed") + " Mins"));
                    requestBinding.newRideTo.setText(data.optString("fromLabel"));
                    requestBinding.newRideFrom.setText(data.optString("toWhereLabel"));
//                        requestBinding.completeRidingTimeStartTxt.setText(singleRide.getString("paidIn"));
//                        ridingBinding.completeRidingTimeEndTxt.setText(singleRide.getString("updatedAt"));

                    //************************************************************

                    String fromJson = data.optString("from");
                    double fromLat = Double.parseDouble(fromJson.substring(0, fromJson.indexOf(",")));
                    double fromLng = Double.parseDouble(fromJson.substring(fromJson.indexOf(" ")));

                    String toWhereJson = data.optString("toWhere");
                    double toWhereLat = Double.parseDouble(toWhereJson.substring(0, toWhereJson.indexOf(",")));
                    double toWhereLng = Double.parseDouble(toWhereJson.substring(toWhereJson.indexOf(" ")));

                    place1 = new MarkerOptions().position(new LatLng(fromLat, fromLng)).title("Origin");
                    place2 = new MarkerOptions().position(new LatLng(toWhereLat, toWhereLng)).title("Destination");

                    final LocationListener mLocationListener = location -> {
                        MarkerOptions driverLocatin = new MarkerOptions().
                                position(new LatLng(location.getLatitude(), location.getLongitude()));
                        mMap.addMarker(new MarkerOptions().position(driverLocatin.getPosition()).title("Driver Location"));

                        new FetchURL(NewRideRequest.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                                location.getLatitude() + "," + location.getLongitude() + "&destination=" +
                                place1.getPosition().latitude + "," + place1.getPosition().longitude +
                                "&key=" + GOOGLE_MAP_API_KEY, "driving");
                    };

                    new FetchURL(NewRideRequest.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            place1.getPosition().latitude + "," + place1.getPosition().longitude
                            + "&destination=" + place2.getPosition().latitude + "," + place2.getPosition().longitude +
                            "&key=" + GOOGLE_MAP_API_KEY, "driving");

                    mMap.addMarker(new MarkerOptions().position(place1.getPosition()).title("Origin"));
                    mMap.addMarker(new MarkerOptions().position(place2.getPosition()).title("Destination"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), 14f));

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

    @Override
    public void onTaskDone(Object... values) {
//        if (driverPolyLine != null) {
//            driverPolyLine.remove();
//        }
//        driverPolyLine = mMap.addPolyline((PolylineOptions) values[0]);

        if (currentPolyLine != null) {
            currentPolyLine.remove();
        }
        currentPolyLine = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
