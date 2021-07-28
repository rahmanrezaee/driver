package com.development.taxiappproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Service.IResult;
import com.development.taxiappproject.Service.VolleyService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import static com.development.taxiappproject.Service.MyFirebaseMessagingService.fcmToken;

public class LoginScreen extends AppCompatActivity {
    EditText loginEdt;
    Button btnLogin;
    SharedPreferences sharedPreferences;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 999;

    FirebaseAuth auth;
    IResult mResultCallbackCheckPhoneNumber = null;
    VolleyService mVolleyService;
    ProgressDialog p;

    String TAG = "Requests";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null){
            auth.signOut();
        }
        checkLocationPermission();


        p = new ProgressDialog(LoginScreen.this);
        p.setMessage("Loading...");
        p.dismiss();


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(LoginScreen.this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        LoginScreen.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
        String expireDate = sharedPreferences.getString(SharedPrefKey.expireDate, "defaultValue");
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String newToken = instanceIdResult.getToken();
            sharedPreferences.edit().putString(fcmToken, newToken).apply();
        });

        assert userToken != null;
        if (!userToken.equalsIgnoreCase("defaultValue")) {

            Date d = SharedPrefKey.convertStringToDate(expireDate);

            if (SharedPrefKey.isAfterNow(d)){


                startActivity(new Intent(this,HomeScreen.class));
                finish();

            }else {
                String getFcmToken = sharedPreferences.getString(fcmToken, "defaultValue");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString(fcmToken, getFcmToken);

                if (auth.getCurrentUser() != null)
                    auth.signOut();

                editor.apply();


            }


        }


        loginEdt = findViewById(R.id.phone_number);

        loginEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                if (!s.toString().startsWith("+1")) {
                    if(!s.toString().equalsIgnoreCase("+"))
                        loginEdt.setText("+1"+s);
                    else
                        loginEdt.setText("+1");
                    Selection.setSelection(loginEdt.getText(), loginEdt
                            .getText().length());


                }

            }

        });


        btnLogin = findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(view -> {
            if (validate()) {
                try {
                    String email = loginEdt.getText().toString();


                    p.show();
                    initVolleyCallbackCheckPhoneNumber();

                    mVolleyService = new VolleyService(mResultCallbackCheckPhoneNumber, LoginScreen.this);
                    HashMap<String, String> header = new HashMap<>();


                    JSONObject body = new JSONObject();
                    body.put("phone",email);
                    body.put("type","driver");
                    try {

                        mVolleyService.postDataVolley("POSTCALL", "/check-phone-number", body, header);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }




                } catch (Exception ex) {
                    Log.e("Mahdi: Login error: 1 ", String.valueOf(ex));
                }
            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission!")
                        .setMessage("For the driver to find your position")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(LoginScreen.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, you must enable your location!", Toast.LENGTH_SHORT).show();
                    System.exit(0);
                }
                return;
            }

        }
    }

    void initVolleyCallbackCheckPhoneNumber() {

        mResultCallbackCheckPhoneNumber = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {

                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + response);
                p.dismiss();

                try {
                    if (response.getBoolean("exit")){
                        String phone = loginEdt.getText().toString();
                        Log.i("MAHDI: phone number",phone);
                        Intent intent = new Intent(getBaseContext(), OTPScreen.class);
                        intent.putExtra("type", "signIn");
                        intent.putExtra("phone_number", phone);
                        startActivity(intent);

                    }else{
                        Toast.makeText(LoginScreen.this, "User Not Exits", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Volley JSON post" + "That didn't work!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginScreen.this, "User Not Exits", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");
                Log.d(TAG, "Volley JSON post" + error);
                p.dismiss();
            }
        };
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup:
                startActivity(new Intent(getApplicationContext(), SignUpScreen.class));
                break;
        }
    }

    public boolean validate() {
        boolean valid = true;

        String login = loginEdt.getText().toString();

        if (login.isEmpty()) {
            loginEdt.setError("enter a valid email address");
            valid = false;
        } else {
            loginEdt.setError(null);
        }
        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "Thank you!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        System.exit(0);
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}

//class RetrieveFeedTask extends AsyncTask<String, Void, Void> {
//    private Exception exception;
//    private String phone;
//    private String password;
//
//    RetrieveFeedTask(String phone, String password) {
//        this.phone = phone;
//        this.password = password;
//    }
//
//    protected Void doInBackground(String... urls) {
//        try {
//            loginMethod();
//        } catch (UnsupportedEncodingException e) {
//            Log.e("Mahdi: Login: ", "doInBackground: " + e);
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    public void loginMethod() throws UnsupportedEncodingException {
//        String data = URLEncoder.encode("phone", "UTF-8")
//                + "=" + URLEncoder.encode(phone, "UTF-8");
//
//
//        String text = "";
//        BufferedReader reader = null;
//
//        try {
//            // Defined URL  where to send data
//            URL url = new URL("https://taxiappapi.webfumeprojects.online/driver/login");
//
//            // Send POST data request
//
//            URLConnection conn = url.openConnection();
//            conn.setDoOutput(true);
//            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//            wr.write(data);
//            wr.flush();
//
//            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//
//            Log.i("Mahdi: Login: Server", sb.toString());
//
//            while ((line = reader.readLine()) != null) {
//                // Append server response in string
//                sb.append(line).append("\n");
//            }
//
//            text = sb.toString();
//
//        } catch (IOException e) {
//            Log.e("Mahdi: Login error: 2 ", String.valueOf(e));
//        } finally {
//            try {
//                assert reader != null;
//                if (reader != null)
//                    reader.close();
//            } catch (Exception ex) {
//                Log.e("Mahdi: Login error: 3 ", String.valueOf(ex));
//
//            }
//        }
//    }
//}
