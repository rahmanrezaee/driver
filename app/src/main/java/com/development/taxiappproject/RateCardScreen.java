package com.development.taxiappproject;

import androidx.annotation.RequiresApi;
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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.development.taxiappproject.adapter.RateCardAdapter;
import com.development.taxiappproject.databinding.ActivityRateCardBinding;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRateCardClass;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class RateCardScreen extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAHDI";
    ActivityRateCardBinding rateCardBinding;
//    private RateCardAdapter rideAdapter;
//    RecyclerView.LayoutManager mLayoutManager;
//    List<MyRateCardClass> rideList = new ArrayList<>();
//    RecyclerView recyclerView;

    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;
    private TextView profileTxt;
    private CircleImageView circleImageView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rateCardBinding = DataBindingUtil.setContentView(this, R.layout.activity_rate_card);
        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String firebaseToken = sharedPreferences.getString(SharedPrefKey.firebaseToken, "defaultValue");
//        recyclerView = findViewById(R.id.rateCard_recycler_view);

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
                this, rateCardBinding.drawerLayout, rateCardBinding.headerLayoutAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        rateCardBinding.drawerLayout.addDrawerListener(toggle);
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

        rateCardBinding.customNavigationDrawer.testimonialLayout.setOnClickListener(this);
        rateCardBinding.customNavigationDrawer.profileLayout.setOnClickListener(this);
        rateCardBinding.customNavigationDrawer.earningLayout.setOnClickListener(this);
        rateCardBinding.customNavigationDrawer.ridesLayout.setOnClickListener(this);

        rateCardBinding.customNavigationDrawer.navMenuLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString(MyFirebaseMessagingService.fcmToken, "defaultValue");
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(MyFirebaseMessagingService.fcmToken, fcmToken);
                editor.apply();
                Intent intent = new Intent(RateCardScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        progressBar = findViewById(R.id.rateCardScreen_progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rateCard_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getRideItem(userToken);
        });

        progressBar.setVisibility(View.VISIBLE);

//        rateCard_swipeRefreshLayout

        if (!MyCheckConnection.mCheckConnectivity(RateCardScreen.this)) {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

//        setRecyclerView();
        getRideItem(userToken);
    }

//    private void setRecyclerView() {
//        rideAdapter = new RateCardAdapter(RateCardScreen.this, rideList);
//        mLayoutManager = new LinearLayoutManager(RateCardScreen.this);
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(rideAdapter);
//    }

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

            case R.id.rides_layout:
                startActivity(new Intent(this, MyRideScreen.class));
                finish();
                checkDrawer();
                break;

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createTable(JSONArray data) {
        TableLayout ll = findViewById(R.id.rateCar_tableLayout);


//        if (data.length() == 0) {
//            return;
//        }

        for (int i = 0; i < data.length() + 1; i++) {

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0);
            int dp = (int) RateCardScreen.this.getResources().getDisplayMetrics().density;
            lp.setMargins(dp, dp, dp, dp);
            row.setWeightSum(1);
            row.setBackgroundColor(Color.parseColor("#C5C5C5"));

            row.setLayoutParams(lp);
            TextView zip = new TextView(RateCardScreen.this);
            TextView rate = new TextView(RateCardScreen.this);

            TableRow.LayoutParams txtParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
            txtParams.setMargins(dp, dp, dp, dp);

            zip.setBackgroundColor(Color.parseColor("#FFFFFF"));
            rate.setBackgroundColor(Color.parseColor("#FFFFFF"));

            zip.setPadding(10 * dp, 10 * dp, 10 * dp, 10 * dp);
            rate.setPadding(10 * dp, 10 * dp, 10 * dp, 10 * dp);

            zip.setGravity(Gravity.CENTER);
            rate.setGravity(Gravity.CENTER);

            zip.setTextAppearance(android.R.style.TextAppearance_Medium);
            rate.setTextAppearance(android.R.style.TextAppearance_Medium);

            zip.setTypeface(null, Typeface.BOLD);

            zip.setLayoutParams(txtParams);
            rate.setLayoutParams(txtParams);

            if (i == 0) {
                zip.setText("Zip/City");
                rate.setText("Rate/Miles");

                row.removeAllViewsInLayout();
                row.removeAllViews();
            } else {

                JSONObject myData = null;
                try {
                    myData = data.getJSONObject(i - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    assert myData != null;
                    zip.setText(myData.getString("zipCode"));
                    rate.setText(myData.getString("rate"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            row.addView(zip);
            row.addView(rate);

//            checkBox = new CheckBox(this);
//            tv = new TextView(this);
//            addBtn = new ImageButton(this);
//            addBtn.setImageResource(R.drawable.add);
//            minusBtn = new ImageButton(this);
//            minusBtn.setImageResource(R.drawable.minus);
//            qty = new TextView(this);
//            checkBox.setText("hello");
//            qty.setText("10");
//            row.addView(checkBox);
//            row.addView(minusBtn);
//            row.addView(qty);
//            row.addView(addBtn);
            ll.addView(row, i);
        }

//        ll.addView(rowTitle);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(RateCardScreen.this);
        String mURL = baseUrl + "/rides/ratecard";

        Log.i(TAG, "Mahdi: RateCardScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: RateCardScreen: getDashboard: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        Log.i(TAG, "Mahdi: RateCardScreen: getDashboard: res 1 " + data);

                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);

                        createTable(data);

//                        settestimonialList(data);

                    } catch (JSONException e) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                }, error -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
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
                    Log.i(TAG, "Mahdi: RateCardScreen: getDashboard: res 1 " + response.data);
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

//    private void settestimonialList(JSONArray data) {
//        rideList.clear();
//        MyRateCardClass rateCardTitle = new MyRateCardClass("Zip/City", "Rate/Miles");
//        rideList.add(rateCardTitle);
//        for (int i = 0; i < data.length(); i++) {
//            MyRateCardClass rateCardClass = null;
//            try {
//                JSONObject myData = data.getJSONObject(i);
//                rateCardClass = new MyRateCardClass(myData.getString("zipCode"), myData.getString("rate"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            rideList.add(rateCardClass);
//        }
//        rideAdapter.notifyDataSetChanged();
//    }
}
