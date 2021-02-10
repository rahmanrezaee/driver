package com.development.taxiappproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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
import com.development.taxiappproject.adapter.RateCardAdapter;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRateCardClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class RateCardScreen extends AppCompatActivity {
    private static final String TAG = "MAHDI";
//    private RateCardAdapter rideAdapter;
//    RecyclerView.LayoutManager mLayoutManager;
//    List<MyRateCardClass> rideList = new ArrayList<>();
//    RecyclerView recyclerView;

    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_card);
        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String firebaseToken = sharedPreferences.getString(SharedPrefKey.firebaseToken, "defaultValue");
//        recyclerView = findViewById(R.id.rateCard_recycler_view);

        progressBar = findViewById(R.id.rateCardScreen_progressBar);
        progressBar.setVisibility(View.VISIBLE);

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

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.rateCard_back_btn:
                finish();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createTable(JSONArray data) {
        TableLayout ll = findViewById(R.id.rateCar_tableLayout);

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

                        createTable(data);

//                        settestimonialList(data);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
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
