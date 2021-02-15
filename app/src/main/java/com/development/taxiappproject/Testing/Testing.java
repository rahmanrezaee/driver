package com.development.taxiappproject.Testing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.MyRideScreen;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.R;
import com.development.taxiappproject.adapter.MyRideAdapter;
import com.development.taxiappproject.databinding.ActivityMyRideScreenBinding;
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

public class Testing extends AppCompatActivity {
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    private MyRideAdapter rideAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ActivityMyRideScreenBinding screenBinding;
    List<MyRideClass> rideList = new ArrayList<>();
    private String TAG = "MAHDI";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        recyclerView = findViewById(R.id.testing_RecyclerView);
//        recyclerAdapter = new RecyclerView.Adapter();

        swipeRefreshLayout = findViewById(R.id.testing_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {

        });

        setRecyclerView();
        getRideItem(userToken);
    }

    private void setRecyclerView() {
        rideAdapter = new MyRideAdapter(Testing.this, rideList);
        mLayoutManager = new LinearLayoutManager(Testing.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rideAdapter);
    }

    public void getRideItem(String userToken) {
        RequestQueue requestQueue = Volley.newRequestQueue(Testing.this);
        String mURL = baseUrl + "/rides";

        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: 1 " + userToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 0 " + response);
                        JSONArray data = response.getJSONArray("data");

                        Log.i(TAG, "Mahdi: HomeScreen: getDashboard: res 1 " + data);

                        settestimonialList(data);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
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

    private void settestimonialList(JSONArray data) {

        rideList.clear();
        for (int i = 0; i < data.length(); i++) {
            MyRideClass ride = null;
            try {
                JSONObject myData = data.getJSONObject(i);
                ride = new MyRideClass(myData.getString("eta"), myData.getString("actualFareAmount"),
                        myData.getString("miles"), myData.getString("actualTimePassed"), myData.getString("from"),
                        myData.getString("toWhere"), myData.getString("_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            MyRideClass ride = new MyRideClass(dateRide, priceRide, distanceRide, timeRide, startLocationRide, endLocationRide);
            rideList.add(ride);
        }
        rideAdapter.notifyDataSetChanged();
    }

}

//********************************************************************************************************
//Upload image
//********************************************************************************************************

//package com.development.taxiappproject.Testing;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//import com.development.taxiappproject.Const.SharedPrefKey;
//import com.development.taxiappproject.OTPScreen;
//import com.development.taxiappproject.R;
//import com.development.taxiappproject.Retrofit.ApiConfig;
//import com.development.taxiappproject.Retrofit.RetrofitClient;
//import com.development.taxiappproject.Service.MyFirebaseMessagingService;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.util.Calendar;
//import java.util.Objects;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.functions.Consumer;
//import io.reactivex.schedulers.Schedulers;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//
//public class Testing extends AppCompatActivity {
//    private CircleImageView circleImageView;
//
//    private final int PERMISSION_REQUEST_CODE_CAMERA = 1;
//    private final int PERMISSION_REQUEST_CODE_GALLERY = 2;
//    private Uri profileUri;
//    private String TAG = "MAHDI";
//    SharedPreferences sharedPreferences;
//    String userToken;
//    File globalFileName;
//    byte[] globalByte;
//
//    ApiConfig apiConfig1;
//    CompositeDisposable compositeDisposable = new CompositeDisposable();
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        compositeDisposable.clear();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_testing);
//
//        isStoragePermissionGranted();
//
//        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
//        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");
//
//        Log.i(TAG, "Mahdi: Testing: 1 " + userToken);
//        Log.i(TAG, "Mahdi: Testing: 2 " + MyFirebaseMessagingService.getToken(getApplicationContext()));
//
//        circleImageView = findViewById(R.id.selectTestImage);
//
//        Retrofit retrofit = RetrofitClient.getInstance();
//        apiConfig1 = retrofit.create(ApiConfig.class);
//    }
//
//    public boolean isStoragePermissionGranted() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG, "Permission is granted");
//                return true;
//            } else {
//
//                Log.v(TAG, "Permission is revoked");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                return false;
//            }
//        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG, "Permission is granted");
//            return true;
//        }
//    }
//
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.selectTestImage:
//                selectImage(1);
//                break;
//
//            case R.id.click_me:
//                uploadMultipleFiles();
////                getCarList();
////                postData();
//                break;
//        }
//    }
//
//    private void uploadMultipleFiles() {
//
//        Log.i(TAG, "Mahdi: uploadMultipleFiles: 1 " + globalFileName);
//
//        RequestBody requestBody1 = RequestBody.create(MediaType.parse("multipart/form- data"), globalFileName);
//
//        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("uploadFile",
//                globalFileName.getName(), requestBody1);
//
//
//        RequestBody type = RequestBody.create(MediaType.parse("multipart/form-data"),
//                "documents");
//
//        RequestBody token = RequestBody.create(MediaType.parse("multipart/form-data"),
//                "7220A3B7F8D2FD2C236092E0918B4EA3");
//
//        Log.i(TAG, "Mahdi: uploadMultipleFiles: 1 " + userToken);
//        Log.i(TAG, "Mahdi: uploadMultipleFiles: 2 " + fileToUpload1);
//        Log.i(TAG, "Mahdi: uploadMultipleFiles: 3 " + type);
//        Log.i(TAG, "Mahdi: uploadMultipleFiles: 4 " + token);
//
//        compositeDisposable.add(apiConfig1.tempImageUpload(userToken, fileToUpload1, type, token)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Response<ResponseBody>>() {
//                    @Override
//                    public void accept(Response<ResponseBody> responseBodyResponse) throws Exception {
//
//                        Log.i(TAG, "Mahdi: accept: 3 " + responseBodyResponse);
//
//                        Log.i(TAG, "Mahdi: accept: 1 ");
//                        if (responseBodyResponse.isSuccessful()) {
//
//                            String remoteResponse = responseBodyResponse.body().string();
//                            try {
//                                Log.i(TAG, "Mahdi: accept: 2 ");
//                                JSONObject forecast = new JSONObject(remoteResponse);
//                                Log.i(TAG, "Mahdi: accept: 3 " + forecast);
//                            } catch (JSONException e) {
//                                Log.e(TAG, "Mahdi: accept: error 4 ", e);
//                                e.printStackTrace();
//                            }
//                        } else {
//                            Log.i(TAG, "Mahdi: accept: 5 " + responseBodyResponse.errorBody());
//                        }
//                    }
//                })
//        );
//    }
//
//    private boolean checkPermissionGallery() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            return false;
//        }
//        return true;
//    }
//
//    private void requestPermissionCamera() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.CAMERA},
//                PERMISSION_REQUEST_CODE_CAMERA);
//    }
//
//    private void requestPermissionGallery() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                PERMISSION_REQUEST_CODE_GALLERY);
//    }
//
//    private boolean checkPermissionCamera() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            return false;
//        }
//        return true;
//    }
//
//    private void selectImage(int countImage) {
//        String cameraNum = 1 + "" + countImage;
//        String galleryNum = 2 + "" + countImage;
//        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(Testing.this);
//        builder.setTitle("Add Photo!");
//        builder.setItems(options, (dialog, item) -> {
//            if (options[item].equals("Take Photo")) {
//                if (checkPermissionCamera()) {
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, Integer.parseInt(cameraNum));
//                } else {
//                    requestPermissionCamera();
//                }
//            } else if (options[item].equals("Choose from Gallery")) {
//                if (checkPermissionGallery()) {
//                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, Integer.parseInt(galleryNum));
//                } else {
//                    requestPermissionGallery();
//                }
//            } else if (options[item].equals("Cancel")) {
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.R)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            profileUri = data.getData();
//            if (requestCode == 11) {
//
//                Bitmap filePath = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
//                circleImageView.setImageBitmap(filePath);
//
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                filePath.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                globalByte = byteArrayOutputStream.toByteArray();
//
//                Uri tempUri = getImageUri(getApplicationContext(), filePath);
//
//                globalFileName = new File(getRealPathFromURI(tempUri));
//
//                Log.i(TAG, "Mahdi: onActivityResult: " + globalFileName);
//
//            } else if (requestCode == 21) {
//                Uri filePath = data.getData();
//
//                globalFileName = new File(getRealPathFromURI(filePath));
//
//                assert data != null;
//                circleImageView.setImageURI(profileUri);
//            }
//        }
//    }
//
//    public Uri getImageUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
//                "Title" + Calendar.getInstance().getTimeInMillis(), null);
//        return Uri.parse(path);
//    }
//
//    public String getRealPathFromURI(Uri uri) {
//        String path = "";
//        if (getContentResolver() != null) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//                path = cursor.getString(idx);
//                cursor.close();
//            }
//        }
//        return path;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE_CAMERA:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
//                    // main logic
//                } else {
//                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                                != PackageManager.PERMISSION_GRANTED) {
//                            showMessageOKCancel("You need to allow access permissions",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                requestPermissionCamera();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                }
//                break;
//
//            case PERMISSION_REQUEST_CODE_GALLERY:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                                != PackageManager.PERMISSION_GRANTED) {
//                            showMessageOKCancel("You need to allow access permissions",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                requestPermissionGallery();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                }
//        }
//    }
//
//    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
//        new AlertDialog.Builder(Testing.this)
//                .setMessage(message)
//                .setPositiveButton("OK", okListener)
//                .setNegativeButton("Cancel", null)
//                .create()
//                .show();
//    }
//
//    public JSONArray createDocument(String imageUrl) {
//        JSONObject dlObject = new JSONObject();
//        try {
//            dlObject.put("uriPath", imageUrl);
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        JSONArray dlArray = new JSONArray();
//        dlArray.put(dlObject);
//
//        return dlArray;
//    }
//}

//    private void saveProfileAccount() {
//        JSONObject jsonBody = new JSONObject();
//
//        Log.i(TAG, "Mahdi: postData: " + globalFileName);
//        Log.i(TAG, "Mahdi: postData: " + userToken);
//
//        try {
//            jsonBody.put("uploadFile", globalFileName);
//            jsonBody.put("category", "license");
//            jsonBody.put("token", "7220A3B7F8D2FD2C236092E0918B4EA3");
//        } catch (JSONException e) {
//
//        }
//
//        final String requestBody = jsonBody.toString();
//
//        RequestQueue requestQueue = Volley.newRequestQueue(Testing.this);
//        String mURL;
//
//        mURL = baseUrl + "/upload";
//
//        Log.i(TAG, "Mahdi: Testing: signUp: 1 " + jsonBody);
//
//        // loading or check internet connection or something...
//        // ... then
//        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, mURL, new Response.Listener<NetworkResponse>() {
//            @Override
//            public void onResponse(NetworkResponse response) {
//                String resultResponse = new String(response.data);
//                try {
//                    JSONObject result = new JSONObject(resultResponse);
//                    String status = result.getString("status");
//                    String message = result.getString("message");
//
////                    if (status.equals(Constant.REQUEST_SUCCESS)) {
////                        // tell everybody you have succed upload image and post strings
////                        Log.i("Messsage", message);
////                    } else {
////                        Log.i("Unexpected", message);
////                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                NetworkResponse networkResponse = error.networkResponse;
//                String errorMessage = "Unknown error";
//                if (networkResponse == null) {
//                    if (error.getClass().equals(TimeoutError.class)) {
//                        errorMessage = "Request timeout";
//                    } else if (error.getClass().equals(NoConnectionError.class)) {
//                        errorMessage = "Failed to connect server";
//                    }
//                } else {
//                    String result = new String(networkResponse.data);
//                    try {
//                        JSONObject response = new JSONObject(result);
//                        String status = response.getString("status");
//                        String message = response.getString("message");
//
//                        Log.e("Error Status", status);
//                        Log.e("Error Message", message);
//
//                        if (networkResponse.statusCode == 404) {
//                            errorMessage = "Resource not found";
//                        } else if (networkResponse.statusCode == 401) {
//                            errorMessage = message + " Please login again";
//                        } else if (networkResponse.statusCode == 400) {
//                            errorMessage = message + " Check your inputs";
//                        } else if (networkResponse.statusCode == 500) {
//                            errorMessage = message + " Something is getting wrong";
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                Log.i("Error", errorMessage);
//                error.printStackTrace();
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//
//                params.put("category", "license");
//                params.put("token", "7220A3B7F8D2FD2C236092E0918B4EA3");
////                params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
////                params.put("name", mNameInput.getText().toString());
////                params.put("location", mLocationInput.getText().toString());
////                params.put("about", mAboutInput.getText().toString());
////                params.put("contact", mContactInput.getText().toString());
//                return params;
//            }
//
//            @Override
//            protected Map<String, DataPart> getByteData() {
//                Map<String, DataPart> params = new HashMap<>();
//                // file name could found file base or direct access from real path
//                // for now just get bitmap data from ImageView
//                params.put("avatar", new DataPart("file_avatar.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), circleImageView.getDrawable()), "image/jpeg"));
////                params.put("cover", new VolleyMultipartRequest.DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));
//                Log.i(TAG, "Mahdi: getByteData: " + params);
//
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> params = new HashMap<>();
//                params.put("firebaseToken", userToken);
//                return params;
//            }
//
//            @Override
//            public byte[] getBody() {
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
//                    Log.i(TAG, "Mahdi: Testing: signUp: res 1 " + response.data);
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    return Response.error(new ParseError(je));
//                }
//            }
//        };
//
//        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
//    }


//    private JSONObject postData() {
//        Log.i("MAHDI", "Mahdi: Testing: Data: 0 " + profileUri);
//
////        try {
////            try {
////                jsonBody.put("username", "Hello");
////                jsonBody.put("email", "email");
////                jsonBody.put("phone", "contactNumber");
////                jsonBody.put("password", "password");
////                jsonBody.put("carType", "carType");
////                jsonBody.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
////                jsonBody.put("profilePhoto", imageData);
////            } catch (JSONException e) {
////                e.printStackTrace();
////                Log.e("MAHDI", "Mahdi: Testing: error create 0 ", e);
////            }
////
////            JSONObject document = new JSONObject();
////
////            try {
////                document.put("DL", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
////                document.put("Registration", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
////                document.put("Insurance", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
////                document.put("CarInside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
////                document.put("CarOutside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
////            } catch (JSONException e) {
////                e.printStackTrace();
////                Log.e("MAHDI", "Mahdi: Testing: error create 1 ", e);
////            }
////            jsonBody.put("documents", document.toString());
////
////            Log.i("MAHDI", "Mahdi: Testing: Data: 2 " + jsonBody.getJSONObject("documents").toString());
////        } catch (JSONException e) {
////            Log.e("MAHDI", "Mahdi: Testing: error create 2 ", e);
////            e.printStackTrace();
////        }
//
////        JSONObject jsonBody = new JSONObject();
//
//        Log.i(TAG, "Mahdi: postData: " + globalFileName);
//        Log.i(TAG, "Mahdi: postData: " + userToken);
//
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//
//        FileBody fileBody = new FileBody(new File(globalFileName.getPath()));
//
//        builder.addPart("uploadFile", fileBody);
//        builder.addTextBody("category", "license");
//        builder.addTextBody("token", "7220A3B7F8D2FD2C236092E0918B4EA3");
////        builder.addPart("category", "Test", new StringBody("test", ContentType.TEXT_PLAIN));
////        builder.addPart("my_file", fileBody);
//
//        HttpEntity entity = builder.build();
//
////        try {
////            jsonBody.put("uploadFile", globalFileName);
////            jsonBody.put("category", "license");
////            jsonBody.put("token", "7220A3B7F8D2FD2C236092E0918B4EA3");
////        } catch (JSONException e) {
////
////        }
////
////        final String requestBody = jsonBody.toString();
//
//        RequestQueue requestQueue = Volley.newRequestQueue(Testing.this);
//        String mURL;
//
//        mURL = baseUrl + "/upload";
//
////        Log.i(TAG, "Mahdi: Testing: signUp: 1 " + jsonBody);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mURL,
//                null,
//                response -> {
//                    Log.i(TAG, "Mahdi: Testing: " + response);
//                }, error -> {
//            Log.e("Mahdi", "Mahdi: Testing: error http 3 " + error.getMessage());
//        }) {
////            @Override
////            public String getBodyContentType() {
////                return "application/json; charset=utf-8";
////            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> params = new HashMap<>();
//                params.put("firebaseToken", userToken);
//                return params;
//            }
//
//            @Override
//            public byte[] getBody() {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                try {
//                    entity.writeTo(bos);
//                } catch (IOException e) {
//                    VolleyLog.e("IOException writing to ByteArrayOutputStream");
//                }
//                return bos.toByteArray();
////                try {
////                    return entity == null ? null : entity.getBytes("utf-8");
////                } catch (UnsupportedEncodingException uee) {
////                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
////                    return null;
////                }
//            }
//
//            @Override
//            protected Response parseNetworkResponse(NetworkResponse response) {
//                try {
//                    Log.i(TAG, "Mahdi: Testing: signUp: res 1 " + response.data);
//                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
//                } catch (UnsupportedEncodingException e) {
//                    return Response.error(new ParseError(e));
//                } catch (JSONException je) {
//                    return Response.error(new ParseError(je));
//                }
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//
//        return null;
//    }


//********************************************************************************************************
//Background location
//********************************************************************************************************

//package com.development.taxiappproject.Testing;
//
//import android.Manifest;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.PersistableBundle;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.development.taxiappproject.R;
//import com.development.taxiappproject.Service.MyBackgroundLocationService;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.karumi.dexter.Dexter;
//import com.karumi.dexter.PermissionToken;
//import com.karumi.dexter.listener.PermissionDeniedResponse;
//import com.karumi.dexter.listener.PermissionGrantedResponse;
//import com.karumi.dexter.listener.PermissionRequest;
//import com.karumi.dexter.listener.single.PermissionListener;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//
//public class Testing extends AppCompatActivity {
//    Button btn_start;
//    private final String TAG = "TESTING";
//    private static final int REQUEST_PERMISSIONS = 100;
//    boolean boolean_permission;
//    TextView txt_location, txt_address;
//    SharedPreferences mPref;
//    SharedPreferences.Editor medit;
//    Double latitude, longitude;
//    Geocoder geocoder;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_testing);
//
//        Toast.makeText(getApplicationContext(), "Hello World", Toast.LENGTH_SHORT).show();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        txt_location = findViewById(R.id.txt_location);
//        txt_address = findViewById(R.id.txt_address);
//        btn_start = findViewById(R.id.test_button);
//
//        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
//        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        medit = mPref.edit();
//
//        btn_start.setOnClickListener(v -> {
//            if (boolean_permission) {
////                if (mPref.getString("service", "").matches("")) {
//                    medit.putString("service", "service").commit();
//                    Toast.makeText(getApplicationContext(), "Service run", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(), MyBackgroundLocationService.class);
//                    startService(intent);
////                } else {
////                    Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
////                }
//            } else {
//                Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fn_permission();
//    }
//
//    private void fn_permission() {
//        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//            if ((ActivityCompat.shouldShowRequestPermissionRationale(Testing.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {
//            } else {
//                ActivityCompat.requestPermissions(Testing.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
//                        },
//                        REQUEST_PERMISSIONS);
//            }
//        } else {
//            boolean_permission = true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        switch (requestCode) {
//            case REQUEST_PERMISSIONS: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    boolean_permission = true;
//                } else {
//                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }
//
//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            latitude = Double.valueOf(intent.getStringExtra("latutide"));
//            longitude = Double.valueOf(intent.getStringExtra("longitude"));
//
//            List<Address> addresses = null;
//
//            try {
//                addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                String cityName = addresses.get(0).getAddressLine(0);
//                String stateName = addresses.get(0).getAddressLine(1);
//                String countryName = addresses.get(0).getAddressLine(2);
//
//                txt_address.setText(cityName + "/" + stateName + "/" + countryName);
//
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            txt_location.setText(latitude + "/" + longitude);
//        }
//    };
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        registerReceiver(broadcastReceiver, new IntentFilter(MyBackgroundLocationService.str_receiver));
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(broadcastReceiver);
//    }
//
////    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
////        @Override
////        public void onReceive(Context context, Intent intent) {
////
////            latitude = Double.valueOf(intent.getStringExtra("latutide"));
////            longitude = Double.valueOf(intent.getStringExtra("longitude"));
////
////            List<Address> addresses = null;
////
////            try {
////                addresses = geocoder.getFromLocation(latitude, longitude, 1);
////                String cityName = addresses.get(0).getAddressLine(0);
////                String stateName = addresses.get(0).getAddressLine(1);
////                String countryName = addresses.get(0).getAddressLine(2);
////
////                txt_address.setText(countryName);
////
////            } catch (IOException e1) {
////                e1.printStackTrace();
////            }
////            txt_location.setText(latitude+"");
////        }
////    };
////
////    @Override
////    protected void onResume() {
////        super.onResume();
////        registerReceiver(broadcastReceiver, new IntentFilter(MyBackgroundLocationService.str_receiver));
////
////    }
////
////    @Override
////    protected void onPause() {
////        super.onPause();
////        unregisterReceiver(broadcastReceiver);
////    }
//
////        instance = this;
////
////        txt_location = findViewById(R.id.txt_location);
////
////        Log.i(TAG, "Testing: onCreate: 1111");
////
////        Dexter.withContext(Testing.this)
////                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
////                .withListener(new PermissionListener() {
////                    @Override
////                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
////                        updateLocation();
////                    }
////
////                    @Override
////                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
////                        Toast.makeText(Testing.this, "You must accept this location", Toast.LENGTH_SHORT).show();
////                    }
////
////                    @Override
////                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
////
////                    }
////                }).check();
//
////    @Override
////    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_testing);
////
////        instance = this;
////
////        txt_location = findViewById(R.id.txt_location);
////
////        Log.i(TAG, "Testing: onCreate: 1111");
////
////        Dexter.withContext(Testing.this)
////                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
////                .withListener(new PermissionListener() {
////                    @Override
////                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
////                        updateLocation();
////                    }
////
////                    @Override
////                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
////                        Toast.makeText(Testing.this, "You must accept this location", Toast.LENGTH_SHORT).show();
////                    }
////
////                    @Override
////                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
////
////                    }
////                }).check();
////    }
//
////    private void updateLocation() {
////        buildLocationRequest();
////
////        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
////        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////            return;
////        }
////        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
////    }
////
////    private PendingIntent getPendingIntent() {
////        Intent intent = new Intent(this, MyLocationService.class);
////        intent.setAction(MyLocationService.ACTION_PROGRESS_UPDATE);
////        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
////    }
////
////    private void buildLocationRequest() {
////        locationRequest = new LocationRequest();
////        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////        locationRequest.setInterval(5000);
////        locationRequest.setFastestInterval(3000);
////        locationRequest.setSmallestDisplacement(10f);
////    }
////
////    public void updateTextView(String value) {
////        Testing.this.runOnUiThread(new Runnable() {
////            @Override
////            public void run() {
////                txt_location.setText(value);
////            }
////        });
////    }
//}

