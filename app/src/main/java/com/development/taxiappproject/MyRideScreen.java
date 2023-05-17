package com.development.taxiappproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
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
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.adapter.MyRideAdapter;
import com.development.taxiappproject.databinding.ActivityMyRideScreenBinding;
import com.development.taxiappproject.model.MyRideClass;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;

public class MyRideScreen extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAHDI";
    private MyRideAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityMyRideScreenBinding screenBinding;
    List<MyRideClass> rideList = new ArrayList<>();
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;

    private Switch mSwitch;
    private TextView profileTxt,navMenuGoTxt;
    private CircleImageView circleImageView;

//    myRideScreen_swipeRefreshLayout
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
    public void sendData(boolean status) {


        JSONObject jsonObject = new JSONObject();
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");


        Log.i(TAG, "Mahdi: MyProfile: userId: " + userId);

        try {
            jsonObject.put("driver", userId);
            jsonObject.put("status", status);
            mSocket.emit("online", jsonObject);

        }catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_ride_screen);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        View headerLayout = getWindow().findViewById(R.id.header_layout_ride);

        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        circleImageView = findViewById(R.id.navHeader_profile_image);

        String switchFlag = sharedPreferences.getString("isOnline", "null");
        assert switchFlag != null;
        boolean switchFlagBool = switchFlag.equalsIgnoreCase("value");

        mSwitch = findViewById(R.id.navMenu_switch);
        mSwitch.setChecked(switchFlagBool);


        navMenuGoTxt = findViewById(R.id.navMenu_go_txt);
        LinearLayout favourite_layout = findViewById(R.id.favourite_layout);

        favourite_layout.setVisibility(View.GONE);
        View favourite_layout_liner = findViewById(R.id.favourite_layout_liner);

        favourite_layout_liner.setVisibility(View.GONE);


        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

        profileTxt.setText(userName);
        try {
            JSONObject jsonProfile = new JSONObject(profilePath);
            Picasso.get().load(jsonProfile.getString("uriPath")).into(circleImageView);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, screenBinding.drawerLayout, screenBinding.headerLayoutAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        screenBinding.drawerLayout.addDrawerListener(toggle);
        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        screenBinding.customNavigationDrawer.testimonialLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.profileLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.earningLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.rateLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.notification.setOnClickListener(this);

        screenBinding.customNavigationDrawer.navMenuLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(false);
                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString(MyFirebaseMessagingService.fcmToken, "defaultValue");
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(MyFirebaseMessagingService.fcmToken, fcmToken);
                editor.apply();
                Intent intent = new Intent(MyRideScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

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

    private void checkDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.testimonial_layout:
                startActivity(new Intent(this, HomeScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.profile_layout:
                startActivity(new Intent(this, MyProfile.class));
                finish();
                checkDrawer();
                break;

            case R.id.earning_layout:
                startActivity(new Intent(this, EarningScreen.class));
                finish();
                checkDrawer();
                break;
            case R.id.notification:
                startActivity(new Intent(this, NotificationScreen.class));
                finish();
                break;


            case R.id.rate_layout:
                startActivity(new Intent(this, RateCardScreen.class));
                finish();
                checkDrawer();
                break;
        }
    }

    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(MyRideScreen.this);
        String mURL = baseUrl + "/rides";

        Log.i(TAG, "Mahdi: HomeScreen: My Ride: 1 " + userToken);

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


                String fromTxt = myData.optString("fromLabel");


                String destination = myData.optString("toWhereLabel");

                String value = new DecimalFormat("##.##").format(myData.optDouble("actualFareAmount"));
                String miles = new DecimalFormat("##.##").format(myData.optDouble("miles"));

                ride = new MyRideClass(myData.optString("completedTime"), "$ "+value, miles+" Miles", myData.getString("actualTimePassed")+" Hours",
                        fromTxt, destination, myData.getString("_id"));
            } catch (JSONException  e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
