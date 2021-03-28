package com.development.taxiappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.databinding.ActivityTripTrackingBinding;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Global.GlobalVal.GOOGLE_MAP_API_KEY;
import static com.development.taxiappproject.Global.GlobalVal.destinationIcon;
import static com.development.taxiappproject.Global.GlobalVal.drawCircle;
import static com.development.taxiappproject.Global.GlobalVal.originIcon;
import static com.development.taxiappproject.OTPScreen.MyPREFERENCES;

public class TripTracking extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 99;
    private ActivityTripTrackingBinding tripTrackingBinding;
    private String TAG = "MAHDI";
    String userToken;
    String id;
    SharedPreferences sharedPreferences;
    ProgressDialog p;
    private GoogleMap mMap;

    MarkerOptions place1, place2;

    //    Polyline driverPolyLine;
    Polyline currentPolyLine;
    private boolean currentPolyLineFlag = false;
    JSONObject notificationData;

    private FusedLocationProviderClient fusedLocationProviderClient;
    HashMap<String, String> header = new HashMap();
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private boolean paidClicked = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

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
        try {
            notificationData = new JSONObject(extras.getString("Data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tripTrackingBinding.tripTrackingPassengerNameTxt.setText(notificationData.optString("Data"));

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.i(TAG, "Mahdi: TripTracking: onCreate: 1 " + id + " : " + extras);
        getSingleRideItem(userToken, id);
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tripTracking_paidAndComp_btn:
                p = GlobalVal.mProgressDialog(TripTracking.this, p, "Please wait...");

                if (tripTrackingBinding.tripTrackingPaidAndCompBtn.getText().toString().equalsIgnoreCase("Paid and Start Ride")) {
                    paidAndComp("paid");
                } else {
                    paidAndComp("completed");
                }
                break;

            case R.id.tripTracking_back_btn:
                finish();
                break;

            case R.id.tripTracking_call_btn:
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + notificationData.optString("phone")));
                    if (ContextCompat.checkSelfPermission(TripTracking.this,
                            Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(TripTracking.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);

                        // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    } else {
                        //You already have permission
                        try {
                            startActivity(callIntent);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Calling a Phone Number", "Call failed", activityException);
                    Toast.makeText(getApplicationContext(), "Call failed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + notificationData.optString("phone")));
                    startActivity(callIntent);
                    // permission was granted, yay! Do the phone call

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void getSingleRideItem(String userToken, String rideId) {
        RequestQueue requestQueue = Volley.newRequestQueue(TripTracking.this);
        String mURL = baseUrl + "/ride/list/" + rideId;

        Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: 10 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: res 0 " + response);
                    JSONObject data = response.optJSONObject("data");

//                        JSONObject singleRide = data.getJSONObject();

                    Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: res 00 " + data);
                    Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: res 01 " + data.optString("actualFareAmount")
                            + " : ");

                    assert data != null;
                    tripTrackingBinding.tripTrackingPriceTxt.setText("$ " + data.optString("actualFareAmount"));
                    tripTrackingBinding.tripTrackingMilesTxt.setText(data.optString("miles") + " Miles");
                    tripTrackingBinding.tripTrackingMinutesTxt.setText(data.optString("actualTimePassed") + " Mins");
                    tripTrackingBinding.tripTrackingFromTxt.setText(data.optString("fromLabel"));
                    tripTrackingBinding.tripTrackingToTxt.setText(data.optString("toWhereLabel"));
                    tripTrackingBinding.tripTrackingUserTimePickedTxt.setText(data.optString("eta"));
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

                    new FetchURL(TripTracking.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            place1.getPosition().latitude + "," + place1.getPosition().longitude
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
//            ridingBinding.completeRidingProgressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Not connection!", Toast.LENGTH_SHORT).show();
            Log.e("Mahdi", "Mahdi: TripTracking:  getSingleRideItem: Error 1 " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: TripTracking:  getSingleRideItem: res 1 " + response.data);
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

                        paidClicked = true;

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

    @Override
    public void onTaskDone(Object... values) {
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

                LatLng driverLatLng = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                new FetchURL(TripTracking.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin="
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(TripTracking.this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Objects.requireNonNull(Looper.myLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
