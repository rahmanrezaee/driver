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
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
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
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class OTPScreen extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    EditText editText1, editText2, editText3, editText4, editText5, editText6;
    private FirebaseAuth mAuth;
    StringBuilder sb = new StringBuilder();

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String phone_number;
    ProgressDialog p;

    private long mLastClickTime = 0;

    String type;
    JSONObject jsonObject1;
    public static String MyPREFERENCES = "User info";

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
        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        editTextFocusListener();

        if (!MyCheckConnection.mCheckConnectivity(OTPScreen.this)) {
            return;
        }

        type = getIntent().getStringExtra("type");
        assert type != null;
        if (type.equalsIgnoreCase("signUp")) {
            try {
                jsonObject1 = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("bodyData")));
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
        initView();
        startPhoneNumberVerification(phone_number);
    }

//    private void pasteText() {
//        ClipboardManager clipboardManager = (ClipboardManager)
//                getSystemService(Context.CLIPBOARD_SERVICE);
//
//        if(clipboardManager.hasPrimaryClip()) {
//            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
//
//            CharSequence ptext = item.getText();
//            for(int i = 0 ; i <= ptext.length() ; i++){
//                editText3.setText(ptext);
//                // 4 cases and paste to 4 edittexts
//            }
//        }
//    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Tximeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void initView() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeAutoRetrievalTimeOut(@NotNull String verificationId) {
                mVerificationId = verificationId;
//                if (progressDialog != null) {
//                    dismissProgressDialog(progressDialog);
//                }
//                notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!");
            }

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                Log.d("onVerificationCompleted", "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
//                if (progressDialog != null) {
//                    dismissProgressDialog(progressDialog);
//                }
//                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Log.w("onVerificationFailed", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException" + e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.e("Exception:", "FirebaseTooManyRequestsException" + e);
                }
            }

            @Override
            public void onCodeSent(@NotNull String verificationId,
                                   @NotNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                Log.d("onCodeSent", "onCodeSent:" + verificationId);
                Log.i("Verification code:", verificationId);
            }
        };
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verify_button:
                verifyPhoneNumberWithCode(mVerificationId, sb.toString());
                break;
            case R.id.otp_resend:
                if (phone_number == null || mResendToken == null) {
                    return;
                }
                if (SystemClock.elapsedRealtime() - mLastClickTime < 60000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                resendVerificationCode(phone_number, mResendToken);
                break;

            case R.id.back:
                finish();
                break;
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        if (code.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your code", Toast.LENGTH_SHORT).show();
        } else {
            p = GlobalVal.mProgressDialog(OTPScreen.this, p, "Please wait...");
            try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential);
            } catch (Exception e) {
                p.hide();
                Toast toast = Toast.makeText(this, "Verification Code is wrong", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    public void signUpAndSignInToServer(String jsonBody, String idToken, String type) {

        final String requestBody = jsonBody;

        RequestQueue requestQueue = Volley.newRequestQueue(OTPScreen.this);
        String mURL;

        if (type.equalsIgnoreCase("signUp")) {
            mURL = baseUrl + "/driver/signupFirebase";
        } else {
            mURL = baseUrl + "/driver/signinFirebase";
        }

        Log.i(TAG, "Mahdi: OTPScreen: signUp: 1 " + jsonBody);
        Log.i(TAG, "Mahdi: OTPScreen: signUp: 2 " + idToken);

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

                    editor.putString(SharedPrefKey.userName, userName);
                    editor.putString(SharedPrefKey.profilePath, profilePath);

                    editor.apply();

                    if (type.equalsIgnoreCase("signUp")) {
                        Toast.makeText(getApplicationContext(), "Your profile successfully registered! and must be approved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                    }
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
                params.put("firebaseToken", idToken);
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

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                            assert user != null;
//                            user.getIdToken();

                            Log.i(TAG, "Hello: Mahdi: onComplete: User " + user);

                            user.getIdToken(true).addOnSuccessListener(result -> {
                                String idToken = result.getToken();

                                Log.i(TAG, "Hello: Mahdi: onComplete: firebaseToken " + idToken);

                                if (type.equalsIgnoreCase("signUp")) {
                                    final String requestBody = jsonObject1.toString();
                                    signUpAndSignInToServer(requestBody, idToken, type);
                                } else {
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    final String requestBody = jsonObject.toString();
                                    signUpAndSignInToServer(requestBody, idToken, type);
                                }
                                Log.d(TAG, "GetTokenResult result = " + idToken);
                            });
                        } else {
                            p.hide();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(), "Your code is not correct!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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

//                NetworkResponse responseString = null;
//                JSONObject jsonObject1;
//                if (response != null) {
//                    try {
//                        jsonObject1 = new JSONObject(Objects.requireNonNull(response).toString());
//                        Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + jsonObject1.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                            JSONArray array = new JSONArray(response);
//                            Log.i(TAG, "Mahdi: OTPScreen: signUp: res 0 " + array);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    responseString = response;
//                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 1 " + Arrays.toString(response.data));
//                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 2 " + response);
//
//                    // can get more details such as response.headers
//                }
//                assert response != null;
//                return Response.success(responseString.toString(), HttpHeaderParser.parseCacheHeaders(response));


//        StringRequest stringRequest = new StringRequest(Request.Method.POST, mURL,
//                response -> {
//                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res " + response);
//                    p.hide();
//
//                    startActivity(new Intent(getApplicationContext(), HomeScreen.class));
//                },
//                error -> {
//                    p.hide();
//                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e("Mahdi", "Mahdi: OTPScreen: signUp: Error " + error.getMessage());
//                }
//        ) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("Content-Type", "application/json");
//                params.put("firebaseToken", idToken);
//                return params;
//            }
//
//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                try {
//                    return requestBody == null ? null : requestBody.getBytes("utf-8");
//                } catch (UnsupportedEncodingException uee) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
//                    return null;
//                }
//            }
//
//            @Override
//            protected Response parseNetworkResponse(NetworkResponse response) {
//                try {
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    return Response.error(new ParseError(je));
//                }
////                NetworkResponse responseString = null;
////                JSONObject jsonObject1;
////                if (response != null) {
////                    try {
////                        jsonObject1 = new JSONObject(Objects.requireNonNull(response).toString());
////                        Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + jsonObject1.toString());
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                    try {
////                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
////                            JSONArray array = new JSONArray(response);
////                            Log.i(TAG, "Mahdi: OTPScreen: signUp: res 0 " + array);
////                        }
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                    responseString = response;
////                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 1 " + Arrays.toString(response.data));
////                    Log.i(TAG, "Mahdi: OTPScreen: signUp: res 2 " + response);
////
////                    // can get more details such as response.headers
////                }
////                assert response != null;
////                return Response.success(responseString.toString(), HttpHeaderParser.parseCacheHeaders(response));
//            }
//        };
//        requestQueue.add(stringRequest);
