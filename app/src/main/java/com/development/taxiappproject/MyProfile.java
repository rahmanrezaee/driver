package com.development.taxiappproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_profile);
        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.GONE);

        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);

//        progressBar = findViewById(R.id.myProfile_progressBar);
//        progressBar.setVisibility(View.VISIBLE);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");

        Log.i(TAG, "Mahdi: MyProfile: token: " + userToken);
        Log.i(TAG, "Mahdi: MyProfile: userId: " + userId);

        setRecyclerView();
        setTestimonialList();
        getProfileItem(userToken, userId);
    }

    public void getProfileItem(String userToken, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(MyProfile.this);
        String mURL = baseUrl + "/driver/profile/" + userId;

        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: 10 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");

                        profileBinding.myProfileRelativeLayoutItem.setVisibility(View.VISIBLE);
                        profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);

                        profileBinding.myProfileNameTxt.setText(data.getString("username"));
                        profileBinding.myProfileEmailTxt.setText(data.getString("email"));
//                        profileBinding.myProfilePlateNoTxt.setText(data.getString("email"));

                        Picasso.get().load(data.getString("profilePhoto")).into(profileBinding.myProfileCircleImage);

                        JSONObject document = data.getJSONObject("documents");

                        setImageView(document.getJSONArray("DL"), profileBinding.myProfileLicenseImage);
                        setImageView(document.getJSONArray("Registration"), profileBinding.myProfileRegistrationImage);
                        setImageView(document.getJSONArray("Insurance"), profileBinding.myProfileInsuranceImage);
                        setImageView(document.getJSONArray("CarInside"), profileBinding.myProfileCarInImage);
                        setImageView(document.getJSONArray("CarOutside"), profileBinding.myProfileCarOutImage);

                    } catch (JSONException e) {
                        profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }, error -> {
            profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
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

    public void setImageView(JSONArray jsonArray, ImageView imageView) {
        JSONObject mJsonObject = null;
        try {
            mJsonObject = jsonArray.getJSONObject(0);
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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.myProfile_back_btn:
                finish();
                break;

            case R.id.myProfile_edit_btn:
                break;
        }
    }

//    static class RetrieveFeedTask extends AsyncTask<String, Void, Drawable> {
//
//        protected Drawable doInBackground(String... urls) {
//            try {
//                try {
//                    Log.i(TAG, "Mahdi: doInBackground: " + urls[0]);
//                    InputStream is = (InputStream) new URL(urls[0]).getContent();
//                    Drawable d = Drawable.createFromStream(is, "src name");
//                    MyProfile.profileBinding.myProfileCircleImage.setImageDrawable(d);
//                    return Drawable.createFromStream(is, "src name");
//                } catch (Exception e) {
//                    Log.e(TAG, "LoadImageNet: ", e);
//                    return null;
//                }
//            } catch (Exception e) {
//
//                Log.e(TAG, "Exception: ", e);
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Drawable drawable) {
//            super.onPostExecute(drawable);
//        }
//    }
//
//
//    public static Drawable LoadImageNet(String url) {
//        return null;
//    }
}

//                        profileBinding.myProfileCircleImage.setImageDrawable(LoadImageNet(data.getString("profilePhoto")));
//                        createTable(data);
//                        settestimonialList(data);

//                        Log.i(TAG, "Mahdi: MyProfile: getProfileItem: res 2 " +
//                                new RetrieveFeedTask().execute(data.getString("profilePhoto")));
