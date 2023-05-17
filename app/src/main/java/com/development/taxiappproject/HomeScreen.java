package com.development.taxiappproject;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Service.LocationService;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.databinding.ActivityHomeScreenBinding;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;
import static com.development.taxiappproject.Service.MyFirebaseMessagingService.fcmToken;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {
    private TextView navMenuGoTxt, profileTxt;
    private CircleImageView imageProfile;
    private Switch mSwitch;

    private static final String TAG = "MAHDI";
    private AppBarConfiguration mAppBarConfiguration;
    ActivityHomeScreenBinding screenBinding;
    SharedPreferences sharedPreferences;

    LocationService mYourService;
    Intent mServiceIntent;

    private Socket mSocket;

    {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket(socketBaseUrl, options);
            //mSocket = IO.socket("http://chat.socket.io");
        } catch (URISyntaxException e) {
            Log.e("abc", "index=" + e);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }
    IntentFilter  filter;


    @Override
    protected void onDestroy() {

        Toast.makeText(this, "On Destory Was Called", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();




    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i(TAG, "Mahdi: Socket Connected: ");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i(TAG, "Mahdi: Socket DISCONNECT: ");
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Mahdi: Socket EVENT_CONNECT_ERROR: ");
            }
        }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Mahdi: Socket EVENT_CONNECT_TIMEOUT: ");
            }
        }).on("ride", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Mahdi: Socket ride: " + args[0]);
            }
        });

        mSocket.connect();

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String firebaseToken = sharedPreferences.getString(SharedPrefKey.firebaseToken, "defaultValue");
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");
        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

        String switchFlag = sharedPreferences.getString("isOnline", "null");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        assert switchFlag != null;
        boolean switchFlagBool = switchFlag.equalsIgnoreCase("value");

        Log.i(TAG, "Mahdi: HomeScreen: 4 " + switchFlagBool + switchFlag);

        if (switchFlagBool){
            try {

                registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }
        }

        Log.i(TAG, "Mahdi: HomeScreen: 0 " + userToken);
        Log.i(TAG, "Mahdi: HomeScreen: 1 " + MyFirebaseMessagingService.getToken(getApplicationContext()));
        Log.i(TAG, "Mahdi: HomeScreen: 2 " + firebaseToken);
        Log.i(TAG, "Mahdi: HomeScreen: 3 " + userId);

//        profileTxt = findViewById(R.id.navHeader_email);

//        getDashboardItem(userToken);

        ///////////////////////////////////////////////////////////////////////

//        mYourService = new TestService();
//        mServiceIntent = new Intent(this, mYourService.getClass());
//        if (!isMyServiceRunning(mYourService.getClass())) {
//            startService(mServiceIntent);
//        }

        ///////////////////////////////////////////////////////////////////////

        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen);
        navMenuGoTxt = findViewById(R.id.navMenu_go_txt);
        mSwitch = findViewById(R.id.navMenu_switch);
        mSwitch.setChecked(switchFlagBool);

        View headerLayout = getWindow().findViewById(R.id.header_layout);

        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        imageProfile = findViewById(R.id.navHeader_profile_image);


        profileTxt.setText(userName);
        try {
            JSONObject jsonProfile = new JSONObject(profilePath);
            Picasso.get().load(jsonProfile.getString("uriPath")).into(imageProfile);


        } catch (JSONException e) {
            e.printStackTrace();
        }



        if (switchFlagBool) {
            navMenuGoTxt.setText("Go Offline");
        } else {
            navMenuGoTxt.setText("Go Online");
        }

        mSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isOnline", "value");
                editor.commit();
                navMenuGoTxt.setText("Go Offline");
                sendData(true);
                try {

                    registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
                } catch(IllegalArgumentException e) {

                    e.printStackTrace();
                }


            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isOnline", "null");
                editor.commit();
                sendData(false);
                navMenuGoTxt.setText("Go Online");
                try {

                    unregisterReceiver(broadcastReceiver);
                } catch(IllegalArgumentException e) {

                    e.printStackTrace();
                }

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, screenBinding.drawerLayout, screenBinding.appBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        screenBinding.drawerLayout.addDrawerListener(toggle);
        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        toggle.setToolbarNavigationClickListener(view -> {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        screenBinding.customNavigationDrawer.profileLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.ridesLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.earningLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.rateLayout.setOnClickListener(this);

        screenBinding.customNavigationDrawer.notification.setOnClickListener(this);
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    private FusedLocationProviderClient fusedLocationClient;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String switchFlag = sharedPreferences.getString("isOnline", "null");
            boolean switchFlagBool = switchFlag.equalsIgnoreCase("null") ? false : true;

            if (switchFlagBool){

                sendData(true);
            }

        }

    };





    public void sendData(boolean status) {


        JSONObject jsonObject = new JSONObject();
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");


        Log.i(TAG, "Mahdi: MyProfile: userId: " + userId);

        try {
            jsonObject.put("driver", userId);
            jsonObject.put("status", status);
            mSocket.emit("online", jsonObject);



            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                JSONObject jsonMyLocationObject = new JSONObject();
                                try {
                                    jsonMyLocationObject.put("driver", userId);
                                    jsonMyLocationObject.put("lat",location.getLatitude() );
                                    jsonMyLocationObject.put("lng", location.getLongitude());

                                    Log.i(TAG, "My Location Request socket" + jsonMyLocationObject.toString());
                                    mSocket.emit("MyLocation", jsonMyLocationObject);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



//    public void getDashboardItem(String userToken) {
//        RequestQueue requestQueue = Volley.newRequestQueue(HomeScreen.this);
//        String mURL = baseUrl + "/rides/dashboard";
//
//        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: 1 " + userToken);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
//                null,
//                response -> {
//                    try {
//                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 0 " + response);
//                        JSONObject data = response.getJSONObject("data");
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//            Log.e("Mahdi", "Mahdi: HomeScreen: getDashboard: Error " + error.getMessage());
//        }) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> params = new HashMap<>();
//                params.put("token", userToken);
//                return params;
//            }
//
//            @Override
//            protected Response parseNetworkResponse(NetworkResponse response) {
//                try {
//                    Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + response.data);
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    return Response.error(new ParseError(je));
//                }
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.profile_layout:
                startActivity(new Intent(HomeScreen.this, MyProfile.class));
                finish();
                checkDrawer();
                break;

            case R.id.rides_layout:
                startActivity(new Intent(HomeScreen.this, MyRideScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.earning_layout:
                startActivity(new Intent(HomeScreen.this, EarningScreen.class));
                finish();
                checkDrawer();
                break;
            case R.id.notification:
                startActivity(new Intent(this, NotificationScreen.class));
                finish();
                break;
            case R.id.rate_layout:
                startActivity(new Intent(HomeScreen.this, RateCardScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.navMenu_logOut_btn:
                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
                String getFcmToken = sharedPreferences.getString(fcmToken, "defaultValue");
                Log.i(TAG, "HomeScreen: LogOut btn: " + getFcmToken);

                sendData(false);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(fcmToken, getFcmToken);
                editor.apply();
                Intent intent = new Intent(HomeScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();

                break;

//            case R.id.navMenu_switch:
//                sendNewMessage();
//                break;

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
