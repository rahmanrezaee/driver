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
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.adapter.EarningAdapter;
import com.development.taxiappproject.databinding.ActivityEarningScreenBinding;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRideClass;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class EarningScreen extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAHDI";
    private EarningAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityEarningScreenBinding screenBinding;
    List<MyEarningClass> rideList = new ArrayList<>();

    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    private SwipeRefreshLayout swipeContainer;

    private TextView profileTxt;
    private CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_earning_screen);

        screenBinding.earningScreenMakePaidBtn.setVisibility(View.GONE);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");


        View headerLayout = getWindow().findViewById(R.id.header_layout_ride);

        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        circleImageView = findViewById(R.id.navHeader_profile_image);

        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

        profileTxt.setText(userName);
        Picasso.get().load(profilePath).into(circleImageView);

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
        screenBinding.customNavigationDrawer.rateLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.ridesLayout.setOnClickListener(this);

        screenBinding.customNavigationDrawer.navMenuLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString(MyFirebaseMessagingService.fcmToken, "defaultValue");
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(MyFirebaseMessagingService.fcmToken, fcmToken);
                editor.apply();
                Intent intent = new Intent(EarningScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.earningScreen_swipeRefreshLayout);
        progressBar = findViewById(R.id.earningScreen_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        swipeContainer.setOnRefreshListener(() -> {
            getRideItem(userToken);
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (!MyCheckConnection.mCheckConnectivity(EarningScreen.this)) {
            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
            return;
        }

        setRecyclerView();
        getRideItem(userToken);
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

            case R.id.rate_layout:
                startActivity(new Intent(this, RateCardScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.rides_layout:
                startActivity(new Intent(this, MyRideScreen.class));
                finish();
                checkDrawer();
                break;
        }
    }

    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(EarningScreen.this);
        String mURL = baseUrl + "/rides?earnings=true";

        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + data);

                        progressBar.setVisibility(View.GONE);
                        screenBinding.earningScreenMakePaidBtn.setVisibility(View.VISIBLE);
                        swipeContainer.setRefreshing(false);

                        settestimonialList(data);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
            screenBinding.earningScreenMakePaidBtn.setVisibility(View.GONE);
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

    private void setRecyclerView() {
        rideAdapter = new EarningAdapter(EarningScreen.this, rideList);
        mLayoutManager = new LinearLayoutManager(EarningScreen.this);
        screenBinding.earningRecyclerView.setLayoutManager(mLayoutManager);
        screenBinding.earningRecyclerView.setItemAnimator(new DefaultItemAnimator());
        screenBinding.earningRecyclerView.setAdapter(rideAdapter);
    }

    private void settestimonialList(JSONArray data) {
        rideList.clear();
        for (int i = 0; i < data.length(); i++) {
            MyEarningClass ride = null;
            try {
                JSONObject myData = data.getJSONObject(i);
                ride = new MyEarningClass(myData.getString("eta") + " hr", myData.getString("actualFareAmount"), myData.getString("miles"), myData.getString("actualFareAmount"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
