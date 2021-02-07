package com.development.taxiappproject.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.development.taxiappproject.NewRideRequest;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.R;
import com.development.taxiappproject.adapter.DashboardAdapter;
import com.development.taxiappproject.databinding.FragmentHomeBinding;
import com.development.taxiappproject.model.MyRideClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

    private static String lastId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        sharedPreferences = getActivity().getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
        binding.newRideRequest.setVisibility(View.GONE);

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
                    try {
                        Log.i(TAG, "Mahdi: HomeFragment: getDashboardItem: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");

                        boolean lastRideIsNull = data.get("lastRide").toString() == "null";

                        if (!lastRideIsNull) {
                            JSONObject lastRide = data.getJSONObject("lastRide");

                            lastId = lastRide.getString("_id");

                            binding.fragmentHomeDateLastTxt.setText(lastRide.getString("route"));
                            binding.fragmentHomePriceLastTxt.setText("$ " + lastRide.getString("actualFareAmount"));
                            binding.fragmentHomeFromTxt.setText(lastRide.getString("from"));
                            binding.fragmentHomeToTxt.setText(lastRide.getString("toWhere"));
                            binding.fragmentHomeMilesTxt.setText(lastRide.getString("miles") + " Miles");
                        }
                        JSONArray todaySummery = data.getJSONArray("todaySummary");

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

                    } catch (JSONException e) {
                        binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                }, error -> {
            binding.fragmentHomeProgressBar.setVisibility(View.VISIBLE);
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
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: HomeFragment: getDashboard: res 1 " + response.data);
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
            try {
                JSONObject myData = data.getJSONObject(i);
                ride = new MyRideClass(myData.getString("updatedAt"),
                        "Total Fare:   $" + myData.getString("actualFareAmount"),
                        "Total Miles:   " + myData.getString("miles") + " Miles",
                        myData.getString("actualTimePassed"), myData.getString("from"),
                        myData.getString("toWhere"), myData.getString("_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
