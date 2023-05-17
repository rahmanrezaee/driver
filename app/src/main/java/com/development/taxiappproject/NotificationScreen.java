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
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.Service.IResult;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.Service.VolleyService;
import com.development.taxiappproject.adapter.MyNotificationAdapter;
import com.development.taxiappproject.data.model.NotificationModel;
import com.development.taxiappproject.databinding.ActivityNotificationScreenBinding;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;


public class NotificationScreen extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MAHDI";
    private MyNotificationAdapter myNotificationAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityNotificationScreenBinding screenBinding;
    List<NotificationModel> notificationModelList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    ActionBarDrawerToggle toggle;

    private TextView profileTxt;
    private CircleImageView circleImageView;

    ProgressDialog p;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        p.dismiss();
    }

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
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification_screen);


        p = GlobalVal.mProgressDialog(this,p,"Loading...");
        p.hide();
        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        screenBinding.rideScreenProgressBar.setVisibility(View.VISIBLE);

        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

        View headerLayout = getWindow().findViewById(R.id.header_layout_ride);

        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        circleImageView = findViewById(R.id.navHeader_profile_image);

        screenBinding.deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                p.show();
                initVolleyCallback(userToken);
                mVolleyService = new VolleyService(mResultCallback, NotificationScreen.this);
                HashMap<String, String> header = new HashMap<>();
                header.put("token", userToken);
                try {

                    mVolleyService.deleteDataVolley("DELETE_NOTIFICATION", "/notification", null, header);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        profileTxt.setText(userName);
        Picasso.get().load(profilePath).into(circleImageView);

        Toolbar toolbar = findViewById(R.id.header_layout_appBar_notification);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, screenBinding.drawerLayout, screenBinding.headerLayoutAppBarNotification,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        screenBinding.drawerLayout.addDrawerListener(toggle);
        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        screenBinding.drawerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Drawer Layout onClick: ");
            }
        });

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                Log.i(TAG, "Toggle Layout onClick: ");
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
        screenBinding.customNavigationDrawer.notification.setOnClickListener(this);
        screenBinding.customNavigationDrawer.earningLayout.setOnClickListener(this);


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


                Intent intent = new Intent(NotificationScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });


//        screenBinding.customNavigationDrawer..setOnClickListener(this);
//        screenBinding.customNavigationDrawer.settings.setOnClickListener(this);
//        screenBinding.customNavigationDrawer.invite.setOnClickListener(this);
//        screenBinding.customNavigationDrawer.myProfile.setOnClickListener(this);
//        screenBinding.customNavigationDrawer.favouriteLayout.setOnClickListener(this);
//
//        screenBinding.customNavigationDrawer.logoutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
//                String fcmToken = sharedPreferences.getString(SessionManager.FCM_TOKEN, "defaultValue");
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.clear();
//                editor.putString(SessionManager.FCM_TOKEN, fcmToken);
//                editor.apply();
//                Intent intent = new Intent(NotificationScreen.this, LoginScreen.class);
//                startActivity(intent);
//                finish();
//            }
//        });


        LinearLayout favourite_layout = findViewById(R.id.favourite_layout);

        favourite_layout.setVisibility(View.GONE);


        View favourite_layout_liner = findViewById(R.id.favourite_layout_liner);

        favourite_layout_liner.setVisibility(View.GONE);


        screenBinding.myRideScreenSwipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getApplicationContext(), "Refreshed!", Toast.LENGTH_SHORT).show();
            getRideItem(userToken);
        });

        if (!MyCheckConnection.mCheckConnectivity(NotificationScreen.this)) {
            screenBinding.myRideScreenSwipeRefreshLayout.setRefreshing(false);
            screenBinding.rideScreenProgressBar.setVisibility(View.GONE);
            return;
        }

        setRecyclerView(userToken);
        getRideItem(userToken);
    }



    IResult mResultCallback = null;
    VolleyService mVolleyService;

    void initVolleyCallback(String userToken) {

        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + response);
                getRideItem(userToken);
                p.dismiss();
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");p.dismiss();
            }
        };
    }


    private void setRecyclerView(String token) {
        myNotificationAdapter = new MyNotificationAdapter(NotificationScreen.this, notificationModelList,p,token);
        mLayoutManager = new LinearLayoutManager(NotificationScreen.this);
        screenBinding.recyclerView.setLayoutManager(mLayoutManager);
        screenBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        screenBinding.recyclerView.setAdapter(myNotificationAdapter);
    }

    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(NotificationScreen.this);
        String mURL = baseUrl + "/notification";

        Log.i(TAG, "Mahdi: MyRideScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: MyRideScreen: getDashboard: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        Log.i(TAG, "Mahdi: MyRideScreen: getDashboard: res 1 " + data);

                        screenBinding.rideScreenProgressBar.setVisibility(View.GONE);
                        screenBinding.myRideScreenSwipeRefreshLayout.setRefreshing(false);

                        if (data.length() == 0){

                            screenBinding.emptyView.setVisibility(View.VISIBLE);
                        }else {
                            screenBinding.emptyView.setVisibility(View.GONE);
                        }

                        setTestimonialList(data);

                    } catch (JSONException e) {
                        screenBinding.rideScreenProgressBar.setVisibility(View.GONE);
                        screenBinding.myRideScreenSwipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }
                }, error -> {
            screenBinding.myRideScreenSwipeRefreshLayout.setRefreshing(false);
            screenBinding.rideScreenProgressBar.setVisibility(View.GONE);
            Log.e("Mahdi", "Mahdi: MyRideScreen: getDashboard: Error " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: MyRideScreen: getDashboard: res 1 " + response.data);
                    String MyPREFERENCES = "User info";
                    SharedPreferences sharedpreferences = getApplication().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(SharedPrefKey.userToken, response.headers.get("refreshToken"));
                    editor.putString(SharedPrefKey.userToken, SharedPrefKey.addHoursToJavaUtilDate());   String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
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

    private void setTestimonialList(JSONArray data) {
        notificationModelList.clear();
        for (int i = 0; i < data.length(); i++) {
            NotificationModel notification = null;
            try {

                JSONObject myData = data.getJSONObject(i);
                notification = new NotificationModel(
                        myData.getString("title"),
                        myData.getString("body"),
                        myData.getString("updatedAt"),
                        myData.getString("rideId")

                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            notificationModelList.add(notification);
        }
        myNotificationAdapter.notifyDataSetChanged();
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


            case R.id.rate_layout:
                startActivity(new Intent(this, RateCardScreen.class));
                finish();
                checkDrawer();
                break;
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