package com.development.taxiappproject.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.CompleteRiding;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.MyCheckConnection;
import com.development.taxiappproject.NewRideRequest;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.R;
import com.development.taxiappproject.adapter.DashboardAdapter;
import com.development.taxiappproject.databinding.FragmentHomeBinding;
import com.development.taxiappproject.model.MyRideClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MAHDI";

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    List<MyRideClass> rideList = new ArrayList<>();
    private DashboardAdapter dashboardAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    SharedPreferences sharedPreferences;
    SwipeRefreshLayout swipeRefreshLayout;

    private static String lastId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        sharedPreferences = getActivity().getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

//        fragmentHome_swipeRefreshLayout
//        swipeRefreshLayout = findViewById(R.id.testing_swipeRefreshLayout);
        binding.fragmentHomeSwipeRefreshLayout.setOnRefreshListener(() -> {
            getDashboardItem(userToken);
        });

        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
        binding.newRideRequest.setVisibility(View.GONE);

        if (!MyCheckConnection.mCheckConnectivity(getActivity())) {
            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
            binding.fragmentHomeSwipeRefreshLayout.setRefreshing(false);
            return binding.getRoot();
        }

        setRecyclerView();
        getDashboardItem(userToken);
        binding.newRideRequest.setOnClickListener(this);
//        binding.completedRequest.setOnClickListener(this);
        return binding.getRoot();
    }

    public void getDashboardItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String mURL = baseUrl + "/rides/dashboard";

        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 1 " + userToken);
        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 11 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    Log.i("Mahdi", "Mahdi: HomeFragment: getDashboardItem: response " + response);
                    try {
                        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");

                        boolean lastRideIsNull = data.get("lastRide").toString() == "null";

                        if (!lastRideIsNull) {
                            JSONObject lastRide = data.optJSONObject("lastRide");

                            lastId = lastRide.optString("_id");

                            double miles = lastRide.optDouble("miles");
                            double value = lastRide.optDouble("actualFareAmount");

                            binding.fragmentHomeDateLastTxt.setText(lastRide.optString("updatedAt"));
                            binding.fragmentHomePriceLastTxt.setText("$ " + new DecimalFormat("##.##").format(value));

                            String fromJson = lastRide.optString("from");
                            double fromLat = Double.parseDouble(fromJson.substring(0, fromJson.indexOf(",")));
                            double fromLng = Double.parseDouble(fromJson.substring(fromJson.indexOf(" ")));
                            String fromTxt = GlobalVal.convertLatLng(getActivity(), fromLat, fromLng);

                            binding.fragmentHomeFromTxt.setText(fromTxt);

                            String toWhereJson = lastRide.optString("toWhere");
                            double toWhereLat = Double.parseDouble(toWhereJson.substring(0, toWhereJson.indexOf(",")));
                            double toWhereLng = Double.parseDouble(toWhereJson.substring(toWhereJson.indexOf(" ")));
                            String destination = GlobalVal.convertLatLng(getActivity(), toWhereLat, toWhereLng);

                            binding.fragmentHomeToTxt.setText(destination);

                            binding.fragmentHomeMilesTxt.setText(new DecimalFormat("##.##").format(miles) + " Miles");
                            binding.fragmentHomeSwipeRefreshLayout.setRefreshing(false);
                        }
                        JSONArray todaySummery = data.optJSONArray("todaySummary");

                        assert todaySummery != null;
                        if (todaySummery.length() != 0) {
                            settestimonialList(todaySummery);
                        }

                        if (todaySummery.length() == 0 && lastRideIsNull) {
                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
                            binding.newRideRequest.setVisibility(View.GONE);
                        } else {
                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
                            binding.newRideRequest.setVisibility(View.VISIBLE);
                        }
                        binding.fragmentHomeSwipeRefreshLayout.setRefreshing(false);

                    } catch (JSONException | IOException e) {
                        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
                        binding.fragmentHomeSwipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    }

                }, error -> {
            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
            binding.fragmentHomeSwipeRefreshLayout.setRefreshing(false);
            Log.e("Mahdi", "Mahdi: HomeFragment: getDashboardItem: Error " + error.getMessage());
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
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Log.i(TAG, "Mahdi: HomeFragment: getDashboard: res 1 " + response.data);
                return super.parseNetworkResponse(response);
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void setRecyclerView() {
        dashboardAdapter = new DashboardAdapter(getActivity(), rideList);
        mLayoutManager = new LinearLayoutManager(getActivity());
        binding.fragmentHomeRecyclerView.setLayoutManager(mLayoutManager);
        binding.fragmentHomeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.fragmentHomeRecyclerView.setAdapter(dashboardAdapter);
    }


    private void settestimonialList(JSONArray data) {

        rideList.clear();
        for (int i = 0; i < data.length(); i++) {
            MyRideClass ride = null;
            JSONObject myData = data.optJSONObject(i);
            ride = new MyRideClass(myData.optString("updatedAt"),
                    "Total Fare:   $" + new DecimalFormat("##.##")
                            .format(myData.optDouble("actualFareAmount")),
                    "Total Miles:   " + new DecimalFormat("##.##")
                            .format(myData.optDouble("miles")) + " Miles",
                    myData.optString("actualTimePassed"), myData.optString("from"),
                    myData.optString("toWhere"), myData.optString("_id"));
            //            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        dashboardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_ride_request:
                Intent intent = new Intent(getActivity(), CompleteRiding.class);
                intent.putExtra("id", lastId);
                startActivity(intent);

//                startActivity(new Intent(getActivity(), NewRideRequest.class));
                break;

//            case R.id.dashboard_item_linear:
//                Intent intent = new Intent();
////                intent.putExtra("phone_number", email);
//                startActivity(new Intent(getActivity(), CompleteRide.class));
//                break;
        }
    }
}

//    public void getDashboardItem(String userToken) {
//        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//        String mURL = baseUrl + "/rides/dashboard";
//
//        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 1 " + userToken);
//        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 11 " + mURL);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
//                null,
//                response -> {
//                    Log.e("Mahdi", "Mahdi: HomeFragment: getDashboardItem: response " + response);
////                    try {
////                        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: res 0 " + response);
////                        JSONObject data = response.getJSONObject("data");
////
////                        boolean lastRideIsNull = data.get("lastRide").toString() == "null";
////
////                        if (!lastRideIsNull) {
////                            JSONObject lastRide = data.getJSONObject("lastRide");
////
////                            lastId = lastRide.getString("_id");
////
////                            binding.fragmentHomeDateLastTxt.setText(lastRide.getString("route"));
////                            binding.fragmentHomePriceLastTxt.setText("$ " + lastRide.getString("actualFareAmount"));
////                            binding.fragmentHomeFromTxt.setText(lastRide.getString("from"));
////                            binding.fragmentHomeToTxt.setText(lastRide.getString("toWhere"));
////                            binding.fragmentHomeMilesTxt.setText(lastRide.getString("miles") + " Miles");
////                        }
////                        JSONArray todaySummery = data.getJSONArray("todaySummary");
////
////                        if (todaySummery.length() != 0) {
////                            settestimonialList(todaySummery);
////                        }
////
////                        if (todaySummery.length() == 0 && lastRideIsNull) {
////                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
////                            binding.newRideRequest.setVisibility(View.GONE);
////                        } else {
////                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
////                            binding.newRideRequest.setVisibility(View.VISIBLE);
////                        }
////
////                    } catch (JSONException e) {
////                        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
////                        e.printStackTrace();
////                    }
//                }, error -> {
//            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
//            Log.e("Mahdi", "Mahdi: HomeFragment: getDashboardItem: Error " + error.getMessage());
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
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: res 1 " + new JSONObject(String.valueOf(response.data)));
//
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: Error 1 " + e);
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: Error 2 " + je);
//                    return Response.error(new ParseError(je));
//                }
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//    }

//    public void getDashboardItem(String userToken) {
//        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
//        String mURL = baseUrl + "/rides/dashboard";
//
//        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 1 " + userToken);
//        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: 11 " + mURL);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
//                null,
//                response -> {
////                    try {
////                        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: res 0 " + response);
////                        JSONObject data = response.getJSONObject("data");
////
////                        boolean lastRideIsNull = data.get("lastRide").toString() == "null";
////
////                        if (!lastRideIsNull) {
////                            JSONObject lastRide = data.getJSONObject("lastRide");
////
////                            lastId = lastRide.getString("_id");
////
////                            binding.fragmentHomeDateLastTxt.setText(lastRide.getString("route"));
////                            binding.fragmentHomePriceLastTxt.setText("$ " + lastRide.getString("actualFareAmount"));
////                            binding.fragmentHomeFromTxt.setText(lastRide.getString("from"));
////                            binding.fragmentHomeToTxt.setText(lastRide.getString("toWhere"));
////                            binding.fragmentHomeMilesTxt.setText(lastRide.getString("miles") + " Miles");
////                        }
////                        JSONArray todaySummery = data.getJSONArray("todaySummary");
////
////                        if (todaySummery.length() != 0) {
////                            settestimonialList(todaySummery);
////                        }
////
////                        if (todaySummery.length() == 0 && lastRideIsNull) {
////                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
////                            binding.newRideRequest.setVisibility(View.GONE);
////                        } else {
////                            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
////                            binding.newRideRequest.setVisibility(View.VISIBLE);
////                        }
////                    } catch (JSONException e) {
////                        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
////                        e.printStackTrace();
////                    }
//                }, error -> {
//            binding.fragmentHomeProgressBar.setVisibility(View.GONE);
//            Log.e("Mahdi", "Mahdi: HomeFragment: getDashboardItem: Error " + error.getMessage());
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
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: parseNetworkResponse 1 " + response.data);
//
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//
////                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
////                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
////                    return Response.success(new JSONObject(String.valueOf(response.data)), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: parseNetworkResponse error 1 " + e);
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: parseNetworkResponse error 2 " + je);
//                    return Response.error(new ParseError(je));
//                }
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//    }
