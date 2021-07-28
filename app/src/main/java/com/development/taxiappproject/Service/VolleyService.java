package com.development.taxiappproject.Service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class VolleyService {

    IResult mResultCallback = null;
    Context mContext;

    String TAG = "Mahdi";

    public VolleyService(IResult resultCallback, Context context) {
        mResultCallback = resultCallback;
        mContext = context;
    }


    public void postDataVolley(final String requestType, String url, JSONObject body, Map<String, String> header) {
        try {


            Log.i("Post body",body.toString());
            Log.i("Post header",header.toString());
            String mURL = baseUrl + url;
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.POST,mURL, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (mResultCallback != null)
                        mResultCallback.notifySuccess(requestType, response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String body = "";
                    //get status code here

                    error.printStackTrace();

                    if(error != null){
                        if (error.networkResponse != null){
                            if (error.networkResponse.data != null) {
                                try {
                                    body = new String(error.networkResponse.data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.e(TAG, "error response" + body);
                            Log.e(TAG, "error " + error);
                        }

                    }
                    if (mResultCallback != null)
                        mResultCallback.notifyError(requestType, error);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() {

                    return header;
                }


                @Override
                protected Response parseNetworkResponse(NetworkResponse response) {
                    try {
                        Log.i(TAG, "getDataVolley Response " + response.data);
                        String MyPREFERENCES = "User info";
                        SharedPreferences sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(SharedPrefKey.userToken, response.headers.get("refreshToken"));
                        editor.putString(SharedPrefKey.expireDate, SharedPrefKey.addHoursToJavaUtilDate());   String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

            queue.add(jsonObj);

        } catch (Exception e) {

        }
    }

    public void getDataVolley(final String URL, String requestType) {
        try {

            String mURL = baseUrl + URL;
            Log.i(TAG, "getDataVolley: Url" + mURL);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                    null,
                    response -> {
                        if (mResultCallback != null)
                            mResultCallback.notifySuccess(requestType, response);
                    }, error -> {
                if (mResultCallback != null)
                    mResultCallback.notifyError(requestType, error);
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }


                @Override
                protected Response parseNetworkResponse(NetworkResponse response) {
                    try {
                        Log.i(TAG, "getDataVolley Response " + response.data);
                        String MyPREFERENCES = "User info";
                        SharedPreferences sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(SharedPrefKey.userToken, response.headers.get("refreshToken"));
                        editor.putString(SharedPrefKey.expireDate, SharedPrefKey.addHoursToJavaUtilDate());     String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };
            queue.add(jsonObjectRequest);

        } catch (Exception e) {

        }
    }

    public void getDataVolley(final String URL, String userToken, String requestType) {
        try {

            String mURL = baseUrl + URL;
            RequestQueue queue = Volley.newRequestQueue(mContext);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                    null,
                    response -> {
                        if (mResultCallback != null)
                            mResultCallback.notifySuccess(requestType, response);
                    }, error -> {
                if (mResultCallback != null)
                    mResultCallback.notifyError(requestType, error);
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
                        Log.i(TAG, "Mahdi: RideBooked: getRideItem: res 1 " + response.data);
                        String MyPREFERENCES = "User info";
                        SharedPreferences sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(SharedPrefKey.userToken, response.headers.get("refreshToken"));
                        editor.putString(SharedPrefKey.expireDate, SharedPrefKey.addHoursToJavaUtilDate());    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };
            queue.add(jsonObjectRequest);

        } catch (Exception e) {

        }
    }

    public void deleteDataVolley(final String requestType, String url, JSONObject body, Map<String, String> header) {
        try {

            String mURL = baseUrl + url;
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.DELETE,mURL, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (mResultCallback != null)
                        mResultCallback.notifySuccess(requestType, response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    String body = "";
                    //get status code here

                    if(error != null){
                        if (error.networkResponse != null){
                            if (error.networkResponse.data != null) {
                                try {
                                    body = new String(error.networkResponse.data, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.e(TAG, "error response" + body);
                            Log.e(TAG, "error " + error);
                        }

                    }
                    if (mResultCallback != null)
                        mResultCallback.notifyError(requestType, error);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() {

                    return header;
                }


                @Override
                protected Response parseNetworkResponse(NetworkResponse response) {
                    try {
                        Log.i(TAG, "getDataVolley Response " + response.data);
                        String MyPREFERENCES = "User info";
                        SharedPreferences sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(SharedPrefKey.userToken, response.headers.get("refreshToken"));
                        editor.putString(SharedPrefKey.expireDate, SharedPrefKey.addHoursToJavaUtilDate());   String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

            queue.add(jsonObj);

        } catch (Exception e) {

        }
    }
}