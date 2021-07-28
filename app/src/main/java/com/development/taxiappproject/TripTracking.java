package com.development.taxiappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import info.hoang8f.android.segmented.SegmentedGroup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
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
import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
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
    private String status = "accepted";
    private String googleDirection = "http://maps.google.com/maps?";


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

            status = extras.getString("status","accepted");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");



        SegmentedGroup paidLayout = tripTrackingBinding.getRoot().findViewById(R.id.payid_layout);
        SegmentedGroup complateLayout =  tripTrackingBinding.getRoot().findViewById(R.id.complate_layout);


//        complateLayout.setTintColor(Color.DKGRAY);
//
//        complateLayout.setBackgroundColor(Color.GRAY);
//        complateLayout.setTintColor(Color.parseColor("#08C181FF"), Color.parseColor("#08C181FF"));
//        complateLayout.setTintColor(getResources().getColor(R.color.green));
//
//        paidLayout.setBackgroundColor(Color.GRAY);
//        paidLayout.setTintColor(Color.GRAY);
//        paidLayout.setTintColor(Color.parseColor("#08C181FF"), Color.parseColor("#08C181FF"));
        paidLayout.setTintColor(getResources().getColor(R.color.green));


        if (status.equalsIgnoreCase("paid")){
            paidLayout.setVisibility(View.GONE);
            complateLayout.setVisibility(View.VISIBLE);
        }



        tripTrackingBinding.paidCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {


            paidLayout.setVisibility(View.GONE);
            complateLayout.setVisibility(View.VISIBLE);
            p =   new ProgressDialog(TripTracking.this);
            p.setMessage("Loading...");
            p.setIndeterminate(false);
            p.setCancelable(false);

            paidAndComp("paid");



        });
        tripTrackingBinding.tripTrackingBackBtn.setOnClickListener(v -> {
            Intent intent = new  Intent(this,HomeScreen.class);
            startActivity(intent);
            finish();

        });
        tripTrackingBinding.googleDirection.setOnClickListener(v -> {


            if (!googleDirection.equalsIgnoreCase("")){

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(googleDirection));
            startActivity(intent);

            }


        });
        tripTrackingBinding.completeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {


            tripTrackingBinding.completeCheckbox.setChecked(true);

            p =   new ProgressDialog(TripTracking.this);
            p.setMessage("Loading...");
            p.setIndeterminate(false);
            p.setCancelable(false);
                  paidAndComp("completed");


        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        Log.i(TAG, "Mahdi: TripTracking: onCreate: 1 " + id + " : " + extras);
        getSingleRideItem(userToken, id);


    }

    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.tripTracking_back_btn:
                Toast.makeText(this, "Back click", Toast.LENGTH_SHORT).show();
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



                    double miles = data.optDouble("miles");
                    double showMiles = Double.parseDouble(new DecimalFormat("##.##").format(miles));


                    if (data.has("fareAmount") && Double.parseDouble(data.optString("fareAmount")) < Double.parseDouble(data.optString("actualFareAmount"))){


                        double value  = data.optDouble("fareAmount");
                        double showValue = Double.parseDouble(new DecimalFormat("##.##").format(value));
                        tripTrackingBinding.tripTrackingPriceTxt.setText("$ " + showValue);

                    }else{

                        double value  = data.optDouble("actualFareAmount");
                        double showValue = Double.parseDouble(new DecimalFormat("##.##").format(value));


                        tripTrackingBinding.tripTrackingPriceTxt.setText("$ " + showValue);


                    }

                    tripTrackingBinding.tripTrackingMilesTxt.setText("Miles "+ showMiles);
                    tripTrackingBinding.tripTrackingMinutesTxt.setText(data.optString("eta"));
                    tripTrackingBinding.tripTrackingUserTimePickedTxt.setText("ETA: "+data.optString("eta"));

                    tripTrackingBinding.tripTrackingPassengerNameTxt.setText("Username : " + data.optJSONObject("userId").optString("username"));


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


                    googleDirection +="saddr="+fromLat+","+fromLng+"&daddr="+toWhereLat+","+toWhereLng;


                    place1 = new MarkerOptions().position(new LatLng(fromLat, fromLng)).title("Origin");
                    place2 = new MarkerOptions().position(new LatLng(toWhereLat, toWhereLng)).title("Destination");

                    currentPolyLineFlag = true;

                    new FetchURL(TripTracking.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            place1.getPosition().latitude + "," + place1.getPosition().longitude
                            + "&destination=" + place2.getPosition().latitude + "," + place2.getPosition().longitude +
                            "&key=" + GOOGLE_MAP_API_KEY, "driving");

                    makeDirectionDriverToPassenger(new LatLng(fromLat, fromLng));


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


    private void makeDirectionDriverToPassenger(LatLng place1) {

        String driverName = sharedPreferences.getString(SharedPrefKey.userName, "No Name");
        String avatarObject = sharedPreferences.getString(SharedPrefKey.profilePath, "");
        String avatar = "";

        try {
            avatar = new JSONObject(avatarObject).optString("uriPath");
            Log.i(TAG, "makeDirectionDriverToPassenger: Driver name"+ driverName +" image "+ avatar );
        } catch (JSONException e) {
            e.printStackTrace();
            avatar = "https://image.flaticon.com/icons/png/512/1077/1077114.png";
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {

                makeDirection(lastKnownLocationGPS,place1,driverName,avatar);
//                return lastKnownLocationGPS;
            } else {
                Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                makeDirection(loc,place1,driverName,avatar);
            }
        } else {
            Toast.makeText(this, "Please Check you Location Permission", Toast.LENGTH_SHORT).show();
        }

    }
    private void makeDirection(
            Location distication,
            LatLng firstPlace,String driverName,String avatar){


        new FetchURL(TripTracking.this).execute("https://maps.googleapis.com/maps/api/directions/json?origin="
                + distication.getLatitude() + "," + distication.getLongitude()
                + "&destination=" + firstPlace.latitude + "," + firstPlace.longitude +
                "&key=" + GOOGLE_MAP_API_KEY, "driving");

        Picasso.get().load(avatar) .resize(80, 80)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        mMap.addMarker(new MarkerOptions().position(new LatLng(distication.getLatitude(),distication.getLongitude())).title(""+driverName)
                                .icon(BitmapDescriptorFactory.fromBitmap(getCircularBitmap(bitmap))));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }


                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });

    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
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



                        if (serverStatus && !status.equalsIgnoreCase("paid")) {

                            Intent i = new Intent(this,CompleteRiding.class);
                            i.putExtra("id", id);
                            // set the new task and clear flags
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);

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
