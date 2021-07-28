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

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.development.taxiappproject.adapter.EarningAdapter;
import com.development.taxiappproject.databinding.ActivityEarningScreenBinding;
import com.development.taxiappproject.model.MyEarningClass;
import com.development.taxiappproject.model.MyRideClass;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;

public class EarningScreen extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAHDI";
    private EarningAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityEarningScreenBinding screenBinding;
    List<MyEarningClass> rideList = new ArrayList<>();

    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    private SwipeRefreshLayout swipeContainer;

    private TextView profileTxt,navMenuGoTxt;
    private CircleImageView circleImageView;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createTable(JSONArray data) {
        TableLayout ll = findViewById(R.id.rateCar_tableLayout);

        ll.removeAllViews();



//        if (data.length() == 0) {
//            return;
//        }

        for (int i = 0; i < data.length() + 1; i++) {

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0);
            int dp = (int) EarningScreen.this.getResources().getDisplayMetrics().density;
            lp.setMargins(dp, dp, dp, dp);
            row.setWeightSum(1);
            row.setBackgroundColor(Color.parseColor("#C5C5C5"));

            row.setLayoutParams(lp);
            TextView times = new TextView(EarningScreen.this);
            TextView miles = new TextView(EarningScreen.this);
            TextView fare = new TextView(EarningScreen.this);
            TextView payforAdmin = new TextView(EarningScreen.this);

            TableRow.LayoutParams txtParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
            txtParams.setMargins(dp, dp, dp, dp);

            times.setBackgroundColor(Color.parseColor("#FFFFFF"));
            miles.setBackgroundColor(Color.parseColor("#FFFFFF"));
            fare.setBackgroundColor(Color.parseColor("#FFFFFF"));
            payforAdmin.setBackgroundColor(Color.parseColor("#FFFFFF"));

            times.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
            miles.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
            fare.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
            payforAdmin.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);

            times.setGravity(Gravity.CENTER);
            miles.setGravity(Gravity.CENTER);
            fare.setGravity(Gravity.CENTER);
            payforAdmin.setGravity(Gravity.CENTER);

            times.setTextAppearance(android.R.style.TextAppearance_Medium);
            miles.setTextAppearance(android.R.style.TextAppearance_Medium);
            fare.setTextAppearance(android.R.style.TextAppearance_Medium);
            payforAdmin.setTextAppearance(android.R.style.TextAppearance_Medium);

            times.setTypeface(null, Typeface.BOLD);
            miles.setTypeface(null, Typeface.BOLD);
            payforAdmin.setTypeface(null, Typeface.BOLD);
            fare.setTypeface(null, Typeface.BOLD);

            times.setLayoutParams(txtParams);
            miles.setLayoutParams(txtParams);
            payforAdmin.setLayoutParams(txtParams);
            fare.setLayoutParams(txtParams);

            if (i == 0) {
                times.setText("Time");
                miles.setText("Miles");
                fare.setText("Fare");
                payforAdmin.setText("Pay To Admin");

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
                    String milesS = myData.getString("miles") + "";
                    double milesTemp = Double.parseDouble(milesS);
                    double payToAdmin = Double.parseDouble(myData.getString("payToAdmin"));

                    String actualFareAmountS = myData.getString("actualFareAmount") + "";

                    double actualFareAmount = Double.parseDouble(actualFareAmountS);

                    times.setText(myData.getString("actualTimePassed") + " H");
                    miles.setText(String.format("%.1f", milesTemp));
                    payforAdmin.setText(String.format("%.1f", payToAdmin));
                    fare.setText(String.format("%.1f", actualFareAmount));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            row.addView(times);
            row.addView(miles);
            row.addView(fare);
            row.addView(payforAdmin);

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
    public void createTableTotal(JSONArray data) {
        TableLayout llHead = findViewById(R.id.totalEarning_tableLayout);

        llHead.removeAllViews();

        TableRow the_ligne_unidade = new TableRow(this);
//        the_ligne_unidade.setBackgroundColor(the_grey);

        TextView my_unidade = new TextView(this);
        my_unidade.setText("Totals");
        my_unidade.setTextSize(20);
        my_unidade.setTypeface(null, Typeface.BOLD);
        my_unidade.setVisibility(View.VISIBLE);

        // Put the TextView in the TableRow
        the_ligne_unidade.addView(my_unidade);

        // And now, we change the SPAN
        TableRow.LayoutParams the_param;
        the_param = (TableRow.LayoutParams)my_unidade.getLayoutParams();
        the_param.span = 3;
        my_unidade.setLayoutParams(the_param);

        llHead.addView(the_ligne_unidade,0);


        TableRow rowHead = new TableRow(this);
        TableRow.LayoutParams lpHead = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0);
        int dpHead = (int) EarningScreen.this.getResources().getDisplayMetrics().density;
        lpHead.setMargins(dpHead, dpHead, dpHead, dpHead);
        rowHead.setWeightSum(1);
        rowHead.setBackgroundColor(Color.parseColor("#C5C5C5"));

        rowHead.setLayoutParams(lpHead);
        TextView timesHead = new TextView(EarningScreen.this);
        TextView milesHead = new TextView(EarningScreen.this);
        TextView fareHead = new TextView(EarningScreen.this);
        TextView payforAdminHead = new TextView(EarningScreen.this);

        TableRow.LayoutParams txtParamsHead = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
        txtParamsHead.setMargins(dpHead, dpHead, dpHead, dpHead);

        timesHead.setBackgroundColor(Color.parseColor("#FFFFFF"));
        milesHead.setBackgroundColor(Color.parseColor("#FFFFFF"));
        fareHead.setBackgroundColor(Color.parseColor("#FFFFFF"));
        payforAdminHead.setBackgroundColor(Color.parseColor("#FFFFFF"));

        timesHead.setPadding(5 * dpHead, 5 * dpHead, 5 * dpHead, 5 * dpHead);
        milesHead.setPadding(5 * dpHead, 5 * dpHead, 5 * dpHead, 5 * dpHead);
        fareHead.setPadding(5 * dpHead, 5 * dpHead, 5 * dpHead, 5 * dpHead);
        payforAdminHead.setPadding(5 * dpHead, 5 * dpHead, 5 * dpHead, 5 * dpHead);

        timesHead.setGravity(Gravity.CENTER);
        milesHead.setGravity(Gravity.CENTER);
        fareHead.setGravity(Gravity.CENTER);
        payforAdminHead.setGravity(Gravity.CENTER);

        timesHead.setTextAppearance(android.R.style.TextAppearance_Medium);
        milesHead.setTextAppearance(android.R.style.TextAppearance_Medium);
        fareHead.setTextAppearance(android.R.style.TextAppearance_Medium);
        payforAdminHead.setTextAppearance(android.R.style.TextAppearance_Medium);

        timesHead.setTypeface(null, Typeface.BOLD);
        milesHead.setTypeface(null, Typeface.BOLD);
        payforAdminHead.setTypeface(null, Typeface.BOLD);
        fareHead.setTypeface(null, Typeface.BOLD);

        timesHead.setLayoutParams(txtParamsHead);
        milesHead.setLayoutParams(txtParamsHead);
        payforAdminHead.setLayoutParams(txtParamsHead);
        fareHead.setLayoutParams(txtParamsHead);

        timesHead.setText("Time");
        milesHead.setText("Miles");
        fareHead.setText("Fare");
        payforAdminHead.setText("Pay To Admin");

        rowHead.removeAllViewsInLayout();
        rowHead.removeAllViews();

        rowHead.addView(timesHead);
        rowHead.addView(milesHead);
        rowHead.addView(fareHead);
        rowHead.addView(payforAdminHead);
        llHead.addView(rowHead, 1);



        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0);
        int dp = (int) EarningScreen.this.getResources().getDisplayMetrics().density;
        lp.setMargins(dp, dp, dp, dp);
        row.setWeightSum(1);
        row.setBackgroundColor(Color.parseColor("#C5C5C5"));

        row.setLayoutParams(lp);
        TextView times = new TextView(EarningScreen.this);
        TextView miles = new TextView(EarningScreen.this);
        TextView fare = new TextView(EarningScreen.this);
        TextView payforAdmin = new TextView(EarningScreen.this);

        TableRow.LayoutParams txtParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
        txtParams.setMargins(dp, dp, dp, dp);

        times.setBackgroundColor(Color.parseColor("#FFFFFF"));
        miles.setBackgroundColor(Color.parseColor("#FFFFFF"));
        fare.setBackgroundColor(Color.parseColor("#FFFFFF"));
        payforAdmin.setBackgroundColor(Color.parseColor("#FFFFFF"));

        times.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
        miles.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
        fare.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);
        payforAdmin.setPadding(5 * dp, 5 * dp, 5 * dp, 5 * dp);

        times.setGravity(Gravity.CENTER);
        miles.setGravity(Gravity.CENTER);
        fare.setGravity(Gravity.CENTER);
        payforAdmin.setGravity(Gravity.CENTER);

        times.setTextAppearance(android.R.style.TextAppearance_Medium);
        miles.setTextAppearance(android.R.style.TextAppearance_Medium);
        fare.setTextAppearance(android.R.style.TextAppearance_Medium);
        payforAdmin.setTextAppearance(android.R.style.TextAppearance_Medium);

        times.setTypeface(null, Typeface.BOLD);
        miles.setTypeface(null, Typeface.BOLD);
        payforAdmin.setTypeface(null, Typeface.BOLD);
        fare.setTypeface(null, Typeface.BOLD);

        times.setLayoutParams(txtParams);
        miles.setLayoutParams(txtParams);
        payforAdmin.setLayoutParams(txtParams);
        fare.setLayoutParams(txtParams);


        JSONObject myData = null;
        try {

            if (data.length() == 0){


                myData = new JSONObject();

                myData.put("miles",0);
                myData.put("time",0);
                myData.put("fare",0);

            }else{

                    myData = data.getJSONObject(0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            assert myData != null;
            String milesS = myData.getString("miles") + "";
            double milesTemp = Double.parseDouble(milesS);

            String actualFareAmountS = myData.getString("fare") + "";

            double actualFareAmount = Double.parseDouble(actualFareAmountS);
            double payToAdmin = Double.parseDouble(myData.getString("totalPayToAdmin") + "");

            times.setText(myData.getString("time") + " H");
            miles.setText(String.format("%.1f", milesTemp));
            payforAdmin.setText(String.format("%.1f", payToAdmin));
            fare.setText(String.format("%.1f", actualFareAmount));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        row.addView(times);
        row.addView(miles);
        row.addView(fare);
        row.addView(payforAdmin);

        llHead.addView(row, 2);

//        ll.addView(rowTitle);
    }
    final Calendar myCalendar = Calendar.getInstance();
    String userToken;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenBinding = DataBindingUtil.setContentView(this, R.layout.activity_earning_screen);



        screenBinding.earningScreenMakePaidBtn.setVisibility(View.GONE);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");


        View headerLayout = getWindow().findViewById(R.id.header_layout_ride);

        profileTxt = headerLayout.findViewById(R.id.navHeader_email);
        circleImageView = findViewById(R.id.navHeader_profile_image);

        String userName = sharedPreferences.getString(SharedPrefKey.userName, "defaultValue");
        String profilePath = sharedPreferences.getString(SharedPrefKey.profilePath, "defaultValue");

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
        navMenuGoTxt.setVisibility(View.GONE);
        mSwitch.setVisibility(View.GONE);
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
        screenBinding.customNavigationDrawer.rateLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.ridesLayout.setOnClickListener(this);
        screenBinding.customNavigationDrawer.notification.setOnClickListener(this);


        screenBinding.customNavigationDrawer.navMenuLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                sendData(false);
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
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        screenBinding.timer.setOnClickListener(view1 -> {


            new DatePickerDialog(EarningScreen.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();

        });


        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.earningScreen_swipeRefreshLayout);
        progressBar = findViewById(R.id.earningScreen_progressBar);


        swipeContainer.setOnRefreshListener(() -> {
            getRideItem(userToken,getDateFormate());
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
        getRideItem(userToken,getDateFormate());



        screenBinding.earningScreenMakePaidBtn.setOnClickListener(v -> {

             loader = new ProgressDialog(this);
             loader.setMessage("Loading...");
             loader.show();

            initVolleyPayEarningCallback();
            mVolleyService = new VolleyService(mResultCallback, EarningScreen.this);
            HashMap<String, String> header = new HashMap<>();
            header.put("token", userToken);

            JSONObject jsonObject = new JSONObject();
            try {

                jsonObject.put("date",getDateFormate());

                Log.i(TAG, jsonObject +"");
                mVolleyService.postDataVolley("POSTCALL", "/rides/pay-to-admin", jsonObject, header);

            } catch (JSONException e) {
                e.printStackTrace();
            }



        });
    }
        ProgressDialog loader ;
    private void initVolleyPayEarningCallback() {

        mResultCallback = new IResult() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + response);

                try {                                                                getRideItem(userToken,getDateFormate());
                    Toast.makeText(EarningScreen.this, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finally {
                        loader.dismiss();
                }


            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");
//                Log.d(TAG, "Volley JSON post" + error.);

                error.printStackTrace();
                Toast.makeText(EarningScreen.this, "error: not payed", Toast.LENGTH_SHORT).show();
                  loader.dismiss();                   
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateLabel() {

        screenBinding.timer.setText(getDateFormate());

        getRideItem(userToken,getDateFormate());



    }


    IResult mResultCallback = null;
    VolleyService mVolleyService;



    private String getDateFormate(){

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        return sdf.format(myCalendar.getTime());
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

            case R.id.notification:
                startActivity(new Intent(this, NotificationScreen.class));
                finish();
                break;

            case R.id.rides_layout:
                startActivity(new Intent(this, MyRideScreen.class));
                finish();
                checkDrawer();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getRideItem(String userToken,String date) {
        progressBar.setVisibility(View.VISIBLE);

        RequestQueue requestQueue = Volley.newRequestQueue(EarningScreen.this);
        String mURL = baseUrl + "/rides?earnings=true&date="+date;

        Log.i(TAG, "getRideItem: Url of earning"+mURL);

        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        boolean isPaid = response.getJSONObject("data").getBoolean("isPaid");

                        if(isPaid){

                            screenBinding.earningScreenMakePaidBtn.setBackgroundColor(Color.GRAY);
                            screenBinding.earningScreenMakePaidBtn.setEnabled(false);
                            progressBar.setVisibility(View.GONE);
                            screenBinding.earningScreenMakePaidBtn.setVisibility(View.VISIBLE);
                            swipeContainer.setRefreshing(false);

                            Toast.makeText(this, "Already Paid", Toast.LENGTH_SHORT).show();


                        }else {
                            Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 0 " + response);
                            JSONArray earnings = response.getJSONObject("data").getJSONArray("earnings");
                            JSONArray total = response.getJSONObject("data").getJSONArray("total");

                            if (earnings.length()==0){
                                Toast.makeText(this, "Data Not Found", Toast.LENGTH_SHORT).show();
                            }

                            Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + earnings);
                            Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + total);

                            progressBar.setVisibility(View.GONE);
                            screenBinding.earningScreenMakePaidBtn.setVisibility(View.VISIBLE);
                            swipeContainer.setRefreshing(false);
                            createTable(earnings);
                            createTableTotal(total);


                        }

//                        settestimonialList(earnings);

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
//        screenBinding.earningRecyclerView.setLayoutManager(mLayoutManager);
//        screenBinding.earningRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        screenBinding.earningRecyclerView.setAdapter(rideAdapter);
    }

    private void settestimonialList(JSONArray data) {
        rideList.clear();
        for (int i = 0; i < data.length(); i++) {
            MyEarningClass ride = null;
            try {
                JSONObject myData = data.getJSONObject(i);
                ride = new MyEarningClass(myData.getString("actualTimePassed") + " hr", myData.getString("actualFareAmount"), myData.getString("miles"), myData.getString("actualFareAmount"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }
}
