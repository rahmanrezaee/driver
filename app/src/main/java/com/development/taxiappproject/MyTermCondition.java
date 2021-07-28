package com.development.taxiappproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class MyTermCondition extends AppCompatActivity {
    private static final String TAG = "MAHDI";
    private TextView title, body;
    String type;
    ProgressDialog pr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_term_condition);
        title = findViewById(R.id.termScreen_title_txt);
        body = findViewById(R.id.termScreen_body_txt);
        pr = new ProgressDialog(this);
        pr.setMessage("Loading...");
        pr.show();


        Bundle extra = getIntent().getExtras();

        type = extra.getString("type");

        if(type.equalsIgnoreCase("terms")){
            title.setText("Term and Conditions");
        }else {
            title.setText("Privacy Policy");
        }

        if (!MyCheckConnection.mCheckConnectivity(MyTermCondition.this)) {
            return;
        }
        getTermsAndPrivacy();
    }

    private void getTermsAndPrivacy() {
        RequestQueue requestQueue = Volley.newRequestQueue(MyTermCondition.this);

        String mURL;
        if (type.equalsIgnoreCase("terms")) {
            mURL = baseUrl + "/public/pages/tandc";
        } else {
            mURL = baseUrl + "/public/pages/pandp";
        }
//public/pages/tandc


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    Log.i(TAG, "Mahdi: MyTermCondition: getTermsAndPrivacy: res 0 " + response);
                    JSONObject data = response.optJSONObject("data");

                    pr.dismiss();

//                    title.setText(myData.optString("title"));
                    body.setText(data.optString("body"));

                }, error -> {

            Toast.makeText(this, "Something went wrong! sorry )-:", Toast.LENGTH_SHORT).show();

            Log.e("Mahdi", "Mahdi: MyTermCondition: getTermsAndPrivacy: Error " + error.getMessage());
            pr.dismiss();
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();

                return params;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: MyTermCondition: getTermsAndPrivacy: res 1 " + response.data);


                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.termScreen_back:
                finish();
                break;
        }
    }
}
