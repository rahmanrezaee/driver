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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.development.taxiappproject.adapter.CarAdapter;
import com.development.taxiappproject.databinding.ActivityMyProfileBinding;
import com.development.taxiappproject.model.MyRideClass;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;

public class MyProfile extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAHDI";

    JSONObject userInfo = new JSONObject();
    public static ActivityMyProfileBinding profileBinding;

    SharedPreferences sharedPreferences;
    private TextView profileTxt;
    private TextView navMenuGoTxt;
    private CircleImageView circleImageView;
    ProgressDialog p;
    private Switch mSwitch;
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
        p = GlobalVal.mProgressDialog(this,null,"Loading...");
        p.dismiss();

        profileBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile);

//        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.GONE);
        profileBinding.myProfileProgressBar.setVisibility(View.VISIBLE);
        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);

        View headerLayout = getWindow().findViewById(R.id.header_layout_ride);
        String switchFlag = sharedPreferences.getString("isOnline", "null");
        assert switchFlag != null;
        boolean switchFlagBool = switchFlag.equalsIgnoreCase("value");


        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        circleImageView = findViewById(R.id.navHeader_profile_image);
        mSwitch = findViewById(R.id.navMenu_switch);
        mSwitch.setChecked(switchFlagBool);

        navMenuGoTxt = findViewById(R.id.navMenu_go_txt);

        navMenuGoTxt.setVisibility(View.GONE);
        mSwitch.setVisibility(View.GONE);

        LinearLayout favourite_layout = findViewById(R.id.favourite_layout);

        favourite_layout.setVisibility(View.GONE);


        View favourite_layout_liner = findViewById(R.id.favourite_layout_liner);

        favourite_layout_liner.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, profileBinding.drawerLayout, profileBinding.headerLayoutAppBar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        profileBinding.drawerLayout.addDrawerListener(toggle);
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

        profileBinding.customNavigationDrawer.testimonialLayout.setOnClickListener(this);
        profileBinding.customNavigationDrawer.ridesLayout.setOnClickListener(this);
        profileBinding.customNavigationDrawer.earningLayout.setOnClickListener(this);
        profileBinding.customNavigationDrawer.rateLayout.setOnClickListener(this);
        profileBinding.customNavigationDrawer.notification.setOnClickListener(this);

        profileBinding.myProfileCircleImage.setImageResource(R.drawable.profile);

        profileBinding.customNavigationDrawer.navMenuLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(false);
                SharedPreferences preferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
                String fcmToken = sharedPreferences.getString(MyFirebaseMessagingService.fcmToken, "defaultValue");
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(MyFirebaseMessagingService.fcmToken, fcmToken);
                editor.apply();

                Intent intent = new Intent(MyProfile.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

//        progressBar = findViewById(R.id.myProfile_progressBar);
//        progressBar.setVisibility(View.VISIBLE);

        if (!MyCheckConnection.mCheckConnectivity(MyProfile.this)) {
//            profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);
            profileBinding.myProfileProgressBar.setVisibility(View.GONE);
            profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);
            return;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");

        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

        profileTxt.setText(userName);
        try {
            JSONObject jsonProfile = new JSONObject(profilePath);
            Picasso.get().load(jsonProfile.getString("uriPath")).into(circleImageView);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        profileBinding.myProfileSwipeRefreshLayout.setOnRefreshListener(() -> {
            getProfileItem(userToken, userId);
        });

        getProfileItem(userToken, userId);
    }

    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.myProfile_edit_btn:
                Intent intent = new Intent(MyProfile.this, EditProfile.class);
                intent.putExtra("userInfo", userInfo.toString());
                startActivity(intent);
                break;

            case R.id.testimonial_layout:
                startActivity(new Intent(this, HomeScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.notification:
                startActivity(new Intent(this, NotificationScreen.class));
                finish();
                break;

            case R.id.rides_layout:
                startActivity(new Intent(this, MyRideScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.earning_layout:
                startActivity(new Intent(this, EarningScreen.class));
                finish();
                checkDrawer();
                break;

            case R.id.rate_layout:
                startActivity(new Intent(this, RateCardScreen.class));
                finish();
                checkDrawer();
                break;

//            case R.id.myProfile_edit_btn:

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getProfileItem(String userToken, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(MyProfile.this);
        String mURL = baseUrl + "/driver/profile/" + userId;
        p.show();
        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 10 " + mURL);

        profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");

                        userInfo.put("carType", data.optJSONObject("carType").optString("_id"));

                        Picasso.get().load(data.optJSONObject("carType").optString("carIcon"))
                                .into(profileBinding.myProfileCarIcon);
//                        profileBinding.myProfileCarQuantityTxt.setText(data.optJSONObject("carType").optString("quantityCars"));
                        profileBinding.myProfileModelTxt.setText(data.optJSONObject("carType").optString("carTypeName"));

//                        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);
                        profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
                        profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);

                        String userName = data.optString("username");
                        String carPlateNumber = data.optString("carPlateNumber");
                        String email = data.optString("email");
                        String profilePhoto = data.optJSONObject("profilePhoto").getString("uriPath");
                        String _id = data.optString("_id");

                        profileBinding.myProfileNameTxt.setText(userName);
                        profileBinding.myProfilePlateNoTxt.setText("PlateNo: " + carPlateNumber);
                        profileBinding.myProfileEmailTxt.setText(email);

                        userInfo.put("username", userName);
                        userInfo.put("carPlateNumber", carPlateNumber);
                        userInfo.put("email", email);
                        userInfo.put("profilePhoto", data.optJSONObject("profilePhoto"));
                        userInfo.put("_id", _id);

                        JSONObject dl = data.optJSONObject("DL");
                        JSONObject registration = data.optJSONObject("Registration");
                        JSONObject insurance = data.optJSONObject("Insurance");

                        JSONArray carInside = data.optJSONArray("CarInside");
                        JSONArray carOutside = data.optJSONArray("CarOutside");

                        userInfo.put("DL", dl);
                        userInfo.put("Registration", registration);
                        userInfo.put("Insurance", insurance);

                        userInfo.put("CarInside", carInside);
                        userInfo.put("CarOutside", carOutside);

//                        profileBinding.myProfilePlateNoTxt.setText(data.getString("email"));

                        Picasso.get().load(profilePhoto).into(profileBinding.myProfileCircleImage);

                        setImageView(dl, profileBinding.myProfileLicenseImage);
                        setImageView(registration, profileBinding.myProfileRegistrationImage);
                        setImageView(insurance, profileBinding.myProfileInsuranceImage);

//                        ImageView[] carInsideViews = {profileBinding.myProfileCarInImage1, profileBinding.myProfileCarInImage2,
//                                profileBinding.myProfileCarInImage3, profileBinding.myProfileCarInImage4};

//                        ImageView[] carOutsideViews = {profileBinding.myProfileCarOutImage1, profileBinding.myProfileCarOutImage2,
//                                profileBinding.myProfileCarOutImage3, profileBinding.myProfileCarOutImage4};

                        List<ImageView> carInsideViews = new ArrayList<>();
                        carInsideViews.add(profileBinding.myProfileCarInImage1);
                        carInsideViews.add(profileBinding.myProfileCarInImage2);
                        carInsideViews.add(profileBinding.myProfileCarInImage3);
                        carInsideViews.add(profileBinding.myProfileCarInImage4);

                        List<ImageView> carOutsideViews = new ArrayList<>();
                        carOutsideViews.add(profileBinding.myProfileCarOutImage1);
                        carOutsideViews.add(profileBinding.myProfileCarOutImage2);
                        carOutsideViews.add(profileBinding.myProfileCarOutImage3);
                        carOutsideViews.add(profileBinding.myProfileCarOutImage4);

                        setMultiImageView(carInside, carInsideViews);
                        setMultiImageView(carOutside, carOutsideViews);
//                        setImageView(data.getJSONArray("CarInside"), profileBinding.myProfileCarInImage);
//                        setImageView(data.getJSONArray("CarOutside"), profileBinding.myProfileCarOutImage);

                    } catch (JSONException e) {
                        profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
                        profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }finally {
                        p.dismiss();
                    }
                }, error -> {
            p.dismiss();
            profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
            profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);
            Log.e("Mahdi", "Mahdi: MyProfile: getProfileItem: Error " + error.getMessage());
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
                    Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 1 " + response.data);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setMultiImageView(JSONArray mJsonObject, List<ImageView> imageViews) {
        Log.i(TAG, "setMultiImageView: Mahdi: " + mJsonObject);
        try {
            Log.i(TAG, "setMultiImageView: Mahdi: " + mJsonObject);

            if(mJsonObject != null){
                for (int i = 0; i < mJsonObject.length(); i++) {
                    Picasso.get().load(mJsonObject.getString(i)).into(imageViews.get(i));
                }
            }

        } catch (JSONException e) {
            Log.i(TAG, "setMultiImageView: Mahdi: Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setImageView(JSONObject mJsonObject, ImageView imageView) {
        try {
            Picasso.get().load(mJsonObject.getString("uriPath")).into(imageView);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

//                        profileBinding.myProfileCircleImage.setImageDrawable(LoadImageNet(data.getString("profilePhoto")));
//                        createTable(data);
//                        settestimonialList(data);

//                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 2 " +
//                                new RetrieveFeedTask().execute(data.getString("profilePhoto")));
