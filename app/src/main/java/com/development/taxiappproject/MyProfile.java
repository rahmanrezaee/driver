package com.development.taxiappproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.development.taxiappproject.adapter.CarAdapter;
import com.development.taxiappproject.databinding.ActivityMyProfileBinding;
import com.development.taxiappproject.model.MyRideClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class MyProfile extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    private CarAdapter carAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    public static ActivityMyProfileBinding profileBinding;
    List<MyRideClass> rideList = new ArrayList<>();

    JSONObject userInfo = new JSONObject();

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile);

        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.GONE);
        profileBinding.myProfileProgressBar.setVisibility(View.VISIBLE);

//        progressBar = findViewById(R.id.myProfile_progressBar);
//        progressBar.setVisibility(View.VISIBLE);

        if (!MyCheckConnection.mCheckConnectivity(MyProfile.this)) {
            profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);
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

        profileBinding.myProfileSwipeRefreshLayout.setOnRefreshListener(() -> {
            getProfileItem(userToken, userId);
        });

        setRecyclerView();
        setTestimonialList();
        getProfileItem(userToken, userId);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.myProfile_back_btn:
                finish();
                break;

            case R.id.myProfile_edit_btn:
                Intent intent = new Intent(MyProfile.this, EditProfile.class);
                intent.putExtra("userInfo", userInfo.toString());
                startActivity(intent);
                break;

//            case R.id.myProfile_edit_btn:

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getProfileItem(String userToken, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(MyProfile.this);
        String mURL = baseUrl + "/driver/profile/" + userId;

        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 10 " + mURL);

        profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");

                        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);
                        profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
                        profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);

                        String userName = data.optString("username");
                        String carPlateNumber = data.optString("carPlateNumber");
                        String email = data.optString("email");
                        String profilePhoto = data.optString("profilePhoto");
                        String _id = data.optString("_id");

                        profileBinding.myProfileNameTxt.setText(userName);
                        profileBinding.myProfilePlateNoTxt.setText("PlateNo: " + carPlateNumber);
                        profileBinding.myProfileEmailTxt.setText(email);

                        userInfo.put("username", userName);
                        userInfo.put("carPlateNumber", carPlateNumber);
                        userInfo.put("email", email);
                        userInfo.put("profilePhoto", profilePhoto);
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
                    }
                }, error -> {
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
            for (int i = 0; i < mJsonObject.length(); i++) {
                Picasso.get().load(mJsonObject.getString(i)).into(imageViews.get(i));
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

    private void setRecyclerView() {
        carAdapter = new CarAdapter(MyProfile.this, rideList);
        mLayoutManager = new LinearLayoutManager(MyProfile.this, LinearLayoutManager.HORIZONTAL, false);
        profileBinding.recyclerView.setLayoutManager(mLayoutManager);
        profileBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        profileBinding.recyclerView.setAdapter(carAdapter);

    }

    private void setTestimonialList() {
        rideList.clear();
        for (int i = 0; i < 4; i++) {
            MyRideClass ride = new MyRideClass("dateRide", "priceRide", "distanceRide", "timeRide",
                    "startLocationRide", "endLocationRide", "id");
            rideList.add(ride);
        }
        carAdapter.notifyDataSetChanged();
    }
}

//                        profileBinding.myProfileCircleImage.setImageDrawable(LoadImageNet(data.getString("profilePhoto")));
//                        createTable(data);
//                        settestimonialList(data);

//                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 2 " +
//                                new RetrieveFeedTask().execute(data.getString("profilePhoto")));
