package com.development.taxiappproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.Service.IResult;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.Service.VolleyService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class OTPScreen extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    EditText editText1, editText2, editText3, editText4, editText5, editText6;

    StringBuilder sb = new StringBuilder();

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String phone_number;
    ProgressDialog p;


    String type;
    JSONObject jsonObject1;
    public static String MyPREFERENCES = "User info";

    private TextView otp_timer;
    private TextView otp_resend;
    CountDownTimer countDownTimer;

    private String phone_code;


    IResult mResultCallback = null;
    VolleyService mVolleyService;

    void initVolleyCallbackVerfiyPhoneNumber() {


        mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                p.dismiss();


                Log.i(TAG, "initVolleyCallbackVerfiyPhoneNumber: response "+response.toString());
                String message = response.optString("message");
                Toast.makeText(OTPScreen.this, message, Toast.LENGTH_SHORT).show();
                startTimer();
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {

//                Toast.makeText(OTPScreen.this, "", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Volley requester " + requestType);
                Log.d(TAG, "Volley JSON post" + "That didn't work!");p.dismiss();
                Toast.makeText(OTPScreen.this, "Please try again", Toast.LENGTH_SHORT).show();
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_screen);

        editText1 = findViewById(R.id.edit1);
        editText2 = findViewById(R.id.edit2);
        editText3 = findViewById(R.id.edit3);
        editText4 = findViewById(R.id.edit4);
        editText5 = findViewById(R.id.edit5);
        editText6 = findViewById(R.id.edit6);
        otp_timer = findViewById(R.id.otp_timer);
        otp_resend = findViewById(R.id.otp_resend);
        editTextFocusListener();

        p = GlobalVal.mProgressDialog(OTPScreen.this, p, "Please wait...");


        if (!MyCheckConnection.mCheckConnectivity(OTPScreen.this)) {
            return;
        }

        type = getIntent().getStringExtra("type");
        assert type != null;
        if (type.equalsIgnoreCase("signUp")) {
            try {
                jsonObject1 = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("bodyData")));
//                jsonObject1.put("phone","+93772768220");
                phone_number = getIntent().getStringExtra("phone_number");
                Log.i(TAG, "Mahdi: OTP: " + jsonObject1);
            } catch (JSONException e) {
                Log.e(TAG, "Mahdi: OTP: Error ", e);
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("signIn")) {
            phone_number = getIntent().getStringExtra("phone_number");
        }

        Log.i(TAG, "Mahdi: OTP: " + phone_number);

        assert phone_number != null;
        otp_timer.setVisibility(View.GONE);
        otp_resend.setVisibility(View.VISIBLE);

//        phone_number = "+93772768220";
        startPhoneNumberVerification();


    }


    private void startTimer() {

//        Log.i(TAG, "startTimer: Start Timer");
        otp_timer.setVisibility(View.VISIBLE);
        otp_resend.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(70000, 1000) {

            public void onTick(long millisUntilFinished) {
                otp_timer.setText("Active Resend: " + millisUntilFinished / 1000 + " Sec");
                Log.i(TAG, "onTick: Timer OTP : " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                otp_timer.setVisibility(View.GONE);
                otp_resend.setVisibility(View.VISIBLE);


            }

        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null)
            countDownTimer.cancel();

    }

    private void startPhoneNumberVerification() {
//
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phoneNumber)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // Activity (for callback binding)
//                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);


        p.show();
        initVolleyCallbackVerfiyPhoneNumber();
        mVolleyService = new VolleyService(mResultCallback, OTPScreen.this);


        try {
            JSONObject body = new JSONObject();
            HashMap<String, String> header = new HashMap<>();
            body.put("phoneNumber",this.phone_number);
            mVolleyService.postDataVolley("Verfiy Phone","/send-code",body,header);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verify_button:;

                if (type.equalsIgnoreCase("signUp")) {
                    try {
                        jsonObject1.put("code",sb.toString());
                        final String requestBody = jsonObject1.toString();
                        signUpAndSignInToServer(requestBody, type);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("code",sb.toString());
                        jsonObject.put("phone",phone_number);
                        jsonObject.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
                        final String requestBody = jsonObject.toString();
                        signUpAndSignInToServer(requestBody, type);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.otp_resend:
                if (phone_number == null) {
                    return;
                }

                Toast.makeText(this, "Resend clicked, please wait!", Toast.LENGTH_SHORT).show();
                startPhoneNumberVerification();

                break;

            case R.id.back:
                finish();
                break;
        }
    }



    public void signUpAndSignInToServer(String jsonBody, String type) {

        final String requestBody = jsonBody;

        RequestQueue requestQueue = Volley.newRequestQueue(OTPScreen.this);
        String mURL;

        if (type.equalsIgnoreCase("signUp")) {
            mURL = baseUrl + "/driver/signupFirebase";
        } else {
            mURL = baseUrl + "/driver/signinFirebase";
        }

        Log.i(TAG, "Mahdi: OTPScreen: signUp: 1 " + jsonBody);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mURL,
                null,
                response -> {
                    SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    String message = response.optString("message");

                    if (type.equalsIgnoreCase("signIn") && message.equalsIgnoreCase("New user")) {
                        Toast.makeText(getApplicationContext(), "Your account not found!", Toast.LENGTH_SHORT).show();
                        p.hide();
                        return;
                    }
                    if (type.equalsIgnoreCase("signIn") && message.equalsIgnoreCase("New user")) {
                        Toast.makeText(getApplicationContext(), "Your account not found!", Toast.LENGTH_SHORT).show();
                        p.hide();
                        return;
                    }

                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 0 " + response);
                    JSONObject data = response.optJSONObject("data");

                    JSONObject user = data.optJSONObject("user");
                    String userToken = data.optString("token");
                    String firebaseToken = data.optString("firebaseToken");

                    String userName = user.optString("username");
                    String profilePath = user.optString("profilePhoto");

                    String userId = user.optString("_id");
                    String isOnline = user.optString("isOnline");

                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + userId);
                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + isOnline);

                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 1 " + data);
                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 2 " + firebaseToken);

                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    editor.putString(SharedPrefKey.firebaseToken, firebaseToken);
                    editor.putString(SharedPrefKey.userToken, userToken);
                    editor.putString(SharedPrefKey.userId, userId);
                    editor.putString(SharedPrefKey.isOnline, isOnline);
                    editor.putString(SharedPrefKey.expireDate, SharedPrefKey.addHoursToJavaUtilDate());

                    editor.putString(SharedPrefKey.userName, userName);
                    editor.putString(SharedPrefKey.profilePath, profilePath);
                    Log.i(TAG, "Mahdi: OTPScreen: signUp: timer " + SharedPrefKey.addHoursToJavaUtilDate());
                    editor.apply();


                    startActivity(new Intent(getApplicationContext(), HomeScreen.class));

                    p.hide();
                }, error -> {
            p.hide();

            String responseBody = null;
            try {
                responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject data = new JSONObject(responseBody);

                Log.i(TAG, "Mahdi: OTPScreen: signUp: Error 2 " + data);
                Toast.makeText(getApplicationContext(), data.optString("message"), Toast.LENGTH_LONG).show();
                if (data.optString("message").equalsIgnoreCase("Pending For Admin Confirm")) {


                        finishAffinity();
                        startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                }

            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
            Log.e("Mahdi", "Mahdi: OTPScreen: signUp: Error 2 " + error.getMessage());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");

                return params;
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 1 " + response.data);
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


    public void editTextFocusListener() {
        editText1.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 0 & editText1.length() == 1) {
                    sb.append(s);
                    editText1.clearFocus();
                    editText2.requestFocus();
                    editText2.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 1) {
                    sb.deleteCharAt(0);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 0) {
                    editText1.requestFocus();
                }
            }
        });
        editText2.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 1 & editText2.length() == 1) {
                    sb.append(s);
                    editText2.clearFocus();
                    editText3.requestFocus();
                    editText3.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 2) {
                    sb.deleteCharAt(1);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 1) {
                    editText1.requestFocus();
                }
            }
        });
        editText3.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 2 & editText3.length() == 1) {
                    sb.append(s);
                    editText3.clearFocus();
                    editText4.requestFocus();
                    editText4.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 3) {
                    sb.deleteCharAt(2);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 2) {
                    editText2.requestFocus();
                }
            }
        });
        editText4.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 3 & editText4.length() == 1) {
                    sb.append(s);
                    editText4.clearFocus();
                    editText5.requestFocus();
                    editText5.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 4) {
                    sb.deleteCharAt(3);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 3) {
                    editText3.requestFocus();
                }
            }
        });
        editText5.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 4 & editText5.length() == 1) {
                    sb.append(s);
                    editText5.clearFocus();
                    editText6.requestFocus();
                    editText6.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 5) {
                    sb.deleteCharAt(4);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 4) {
                    editText4.requestFocus();
                }
            }
        });
        editText6.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (sb.length() == 5 & editText6.length() == 1) {
                    sb.append(s);
                    editText6.clearFocus();
                    editText6.requestFocus();
                    editText6.setCursorVisible(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                if (sb.length() == 6) {
                    sb.deleteCharAt(5);
                }
            }

            public void afterTextChanged(Editable s) {
                if (sb.length() == 5) {
                    editText5.requestFocus();
                }
            }
        });
    }
}
