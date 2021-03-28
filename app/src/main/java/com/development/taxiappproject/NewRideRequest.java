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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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
import com.development.taxiappproject.model.CircleTransform;
import com.development.taxiappproject.model.PicassoMarker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Global.GlobalVal.GOOGLE_MAP_API_KEY;
import static com.development.taxiappproject.Global.GlobalVal.destinationIcon;
import static com.development.taxiappproject.Global.GlobalVal.drawCircle;
import static com.development.taxiappproject.Global.GlobalVal.originIcon;
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
    private boolean currentPolyLineFlag = false;

    HashMap<String, String> header = new HashMap();
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    MarkerOptions place1, place2;
    LatLng driverLatLng;

    //    Polyline driverPolyLine;
    Polyline currentPolyLine;
    LocationManager mLocationManager;

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

                    currentPolyLineFlag = true;

                    new FetchURL(NewRideRequest.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin="
                            + place1.getPosition().latitude + "," + place1.getPosition().longitude
                            + "&destination=" + place2.getPosition().latitude + "," + place2.getPosition().longitude +
                            "&key=" + GOOGLE_MAP_API_KEY, "driving");

                    mMap.addMarker(new MarkerOptions().position(place1.getPosition()).title("Origin")
                            .icon(BitmapDescriptorFactory.fromBitmap(originIcon(getApplicationContext()))));

                    mMap.addMarker(new MarkerOptions().position(place2.getPosition()).title("Destination")
                            .icon(BitmapDescriptorFactory.fromBitmap(destinationIcon())));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), 14f));

                    mMap.addGroundOverlay(new GroundOverlayOptions().
                            image(drawCircle(getApplicationContext())).
                            position(place1.getPosition(), 250 * 2, 250 * 2).
                            transparency(0.4f));

                }, error -> {
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
        //TODO What is it? I don't know
        if (currentPolyLineFlag) {
            Log.i(TAG, "onTaskDone: I did it!");

            init();
            currentPolyLineFlag = false;
        }
        currentPolyLine = mMap.addPolyline((PolylineOptions) values[0]);
    }


    private void init() {
        header.clear();
        header.put("Content-Type", "application/json");

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

                driverLatLng = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                new FetchURL(NewRideRequest.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin="
                        + driverLatLng.latitude + "," + driverLatLng.longitude + "&destination=" +
                        place1.getPosition().latitude + "," + place1.getPosition().longitude +
                        "&key=" + GOOGLE_MAP_API_KEY, "driving");

                MarkerOptions markerOption = new MarkerOptions().position(driverLatLng);

                Marker location_marker = mMap.addMarker(markerOption);

                Target target = new PicassoMarker(location_marker);

                Picasso.get().load(profilePath).resize(100, 100).transform(new CircleTransform())
                        .into(target);

                Log.i(TAG, "onTaskDone: I did it! 3");
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(NewRideRequest.this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Objects.requireNonNull(Looper.myLooper()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
