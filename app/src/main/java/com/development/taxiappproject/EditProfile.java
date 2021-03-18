package com.development.taxiappproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.MyInterface.OnTextClickListener;
import com.development.taxiappproject.Retrofit.MyApiConfig;
import com.development.taxiappproject.Retrofit.RetrofitClient;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.adapter.CarAdapter;
import com.development.taxiappproject.databinding.ActivityEditProfileBinding;
import com.development.taxiappproject.model.MyRideClass;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.util.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.MyProfile.setImageView;

public class EditProfile extends AppCompatActivity {

    private static final String TAG = "MAHDI";
    JSONObject userInfo;
    ActivityEditProfileBinding editProfileBinding;
    private final int PERMISSION_REQUEST_CODE_CAMERA = 1;
    private final int PERMISSION_REQUEST_CODE_GALLERY = 2;
    private final int PERMISSION_REQUEST_CODE_WRITE_GALLERY = 3;
    String userName, email, contactNumber, plateNo, profilePhoto, _id;

    MyApiConfig apiConfig1;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String userToken;
    SharedPreferences sharedPreferences;
    JSONObject uploadUserInfo = new JSONObject();

    ProgressDialog p;

    String carTypeId;

    private CarAdapter carAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    List<MyRideClass> rideList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);

        userToken = sharedPreferences.getString(SharedPrefKey.userToken, "defaultValue");

        Bundle extra = getIntent().getExtras();
        String mUserInfo = extra.getString("userInfo");
        Log.i(TAG, "EditProfile: onCreate: " + mUserInfo);
        try {
            userInfo = new JSONObject(mUserInfo);
            _id = userInfo.optString("_id");
            userName = userInfo.optString("username");
            email = userInfo.optString("email");
            plateNo = userInfo.optString("carPlateNumber");
            profilePhoto = userInfo.getString("profilePhoto");

            carTypeId = userInfo.optString("carType");

            Log.i(TAG, "EditProfile: onCreate: 1 " + carTypeId);

//            carTypeId = userInfo.optJSONObject("carType");

            editProfileBinding.editProfileNameEdt.setText(userName);
            editProfileBinding.editProfileEmailEdt.setText(email);
            editProfileBinding.editProfilePlateNoEdt.setText(plateNo);

            Picasso.get().load(userInfo.optString("profilePhoto")).into(editProfileBinding.editProfileCircleImage);

            JSONObject dl = userInfo.optJSONObject("DL");
            JSONObject registration = userInfo.optJSONObject("Registration");
            JSONObject insurance = userInfo.optJSONObject("Insurance");

            setImageView(dl, editProfileBinding.editProfileLicenseImage);
            setImageView(registration, editProfileBinding.editProfileRegistrationImage);
            setImageView(insurance, editProfileBinding.editProfileInsuranceImage);

            JSONArray carInside = userInfo.optJSONArray("CarInside");
            JSONArray carOutside = userInfo.optJSONArray("CarOutside");

            List<ImageView> carInsideViews = new ArrayList<>();
            carInsideViews.add(editProfileBinding.editProfileCarInImage1);
            carInsideViews.add(editProfileBinding.editProfileCarInImage2);
            carInsideViews.add(editProfileBinding.editProfileCarInImage3);
            carInsideViews.add(editProfileBinding.editProfileCarInImage4);

            List<ImageView> carOutsideViews = new ArrayList<>();
            carOutsideViews.add(editProfileBinding.editProfileCarOutImage1);
            carOutsideViews.add(editProfileBinding.editProfileCarOutImage2);
            carOutsideViews.add(editProfileBinding.editProfileCarOutImage3);
            carOutsideViews.add(editProfileBinding.editProfileCarOutImage4);

            setMultiImageView(carInside, carInsideViews);
            setMultiImageView(carOutside, carOutsideViews);

            uploadUserInfo.put("username", editProfileBinding.editProfileNameEdt.getText());
            uploadUserInfo.put("email", editProfileBinding.editProfileEmailEdt.getText());
            uploadUserInfo.put("plateNo", editProfileBinding.editProfilePlateNoEdt.getText());
            uploadUserInfo.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));

            uploadUserInfo.put("profilePhoto", profilePhoto);
            uploadUserInfo.put("DL", dl);
            uploadUserInfo.put("Registration", registration);
            uploadUserInfo.put("Insurance", insurance);
            uploadUserInfo.put("CarInside", carInside);
            uploadUserInfo.put("CarOutside", carOutside);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Retrofit retrofit = RetrofitClient.getInstance();
        apiConfig1 = retrofit.create(MyApiConfig.class);

        if (checkPermissionWriteGallery()) {

        } else {
            requestPermissionWriteGallery();
        }
        getCarsType();

        setRecyclerView(carTypeId);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getCarsType() {
//        /public/carTypes
        RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
        String mURL = baseUrl + "/public/carTypes";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mURL,
                null,
                response -> {

                    Log.i(TAG, "Mahdi: EditProfile: getCarsType: res 0 " + response);
                    JSONArray data = null;
                    data = response.optJSONArray("data");

                    Log.i(TAG, "Mahdi: EditProfile: getCarsType: res 1 " + data);
                    setTestimonialList(data);
                }, error -> {
//            profileBinding.myProfileRelativeLayoutProgress.setVisibility(View.GONE);
//            profileBinding.myProfileSwipeRefreshLayout.setRefreshing(false);
            Log.e("Mahdi", "Mahdi: EditProfile: getCarsType: Error 1 " + error.getMessage());
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("token", userToken);
                return params;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try {
                    Log.i(TAG, "Mahdi: EditProfile: getCarsType: res 1 " + response.data);
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

    private void setTestimonialList(JSONArray carItems) {
        rideList.clear();
        for (int i = 0; i < carItems.length(); i++) {
            JSONObject carItem = carItems.optJSONObject(i);
            MyRideClass ride = new MyRideClass("dateRide", "priceRide", carItem.optString("quantityCars"),
                    carItem.optString("carTypeName"), carItem.optString("carIcon"), "endLocationRide",
                    carItem.optString("_id"));
            rideList.add(ride);
        }
        carAdapter.notifyDataSetChanged();
    }

    OnTextClickListener onTextClickListener = carId -> {
        try {
            uploadUserInfo.put("carType", carId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        mCarId = carId;
    };

    private void setRecyclerView(String carTypeId) {
        carAdapter = new CarAdapter(EditProfile.this, rideList, onTextClickListener, carTypeId);
        mLayoutManager = new LinearLayoutManager(EditProfile.this, LinearLayoutManager.HORIZONTAL, false);
        editProfileBinding.recyclerView.setLayoutManager(mLayoutManager);
        editProfileBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        editProfileBinding.recyclerView.setAdapter(carAdapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setMultiImageView(JSONArray mJsonObject, List<ImageView> imageViews) {
        Log.i(TAG, "setMultiImageView: Mahdi: " + mJsonObject);
        try {
            Log.i(TAG, "setMultiImageView: Mahdi: " + mJsonObject);
            for (int i = 0; i < mJsonObject.length(); i++) {
                Picasso.get().load(mJsonObject.getString(i)).into(imageViews.get(i));
            }
        } catch (JSONException e) {
            Log.i(TAG, "setMultiImageView: Mahdi: Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editProfile_circleImage:
            case R.id.editProfile_cameraImage:
                selectImage(1);
                break;

            case R.id.editProfile_license_image:
            case R.id.editProfile_license_imgBtn:
                selectImage(2);
                break;

            case R.id.editProfile_registration_image:
            case R.id.editProfile_registration_imgBtn:
                selectImage(3);
                break;

            case R.id.editProfile_insurance_image:
            case R.id.editProfile_insurance_imgBtn:
                selectImage(4);
                break;

            case R.id.editProfile_car_in_image1:
            case R.id.editProfile_car_in_image2:
            case R.id.editProfile_car_in_image3:
            case R.id.editProfile_car_in_image4:
            case R.id.editProfile_carIn_imgBtn:
                selectMultipleImage(1);
                break;

            case R.id.editProfile_car_out_image1:
            case R.id.editProfile_car_out_image2:
            case R.id.editProfile_car_out_image3:
            case R.id.editProfile_car_out_image4:
            case R.id.editProfile_carOut_imgBtn:
                selectMultipleImage(2);
                break;

            case R.id.editProfile_edit_btn:
                if (validate()) {
//                    if (isUploading) {
//                        Toast.makeText(getApplicationContext(), "Uploading image please wait...", Toast.LENGTH_SHORT).show();
//                        return;
//                    }

                    final String requestBody = uploadUserInfo.toString();
                    updateProfile(requestBody);
                    p = GlobalVal.mProgressDialog(EditProfile.this, p, "Uploading profile...");
                }
                break;
        }
    }

    public void updateProfile(String requestBody) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditProfile.this);
        String mURL;

//        requestBody

        mURL = baseUrl + "/driver/profile/" + _id;
        Log.i(TAG, "Mahdi: EditProfile: sendRequest: 1 " + requestBody);
        Log.i(TAG, "Mahdi: EditProfile: sendRequest: 2 " + userToken);
        Log.i(TAG, "Mahdi: EditProfile: sendRequest: 3 " + mURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, mURL,
                null,
                response -> {
                    try {
                        Log.i(TAG, "Mahdi: EditProfile: sendRequest: res 0 " + response);
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        p.hide();
                        finish();
                    } catch (JSONException e) {
                        p.hide();
                        e.printStackTrace();
                    }
                }, error -> {
            p.hide();
            Log.e("Mahdi", "Mahdi: EditProfile: sendRequest: Error 0 " + error);
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=utf-8");
//                params.put("Content-Type", "application/json");
                params.put("token", userToken);
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
                    Log.i(TAG, "Mahdi: EditProfile: updateProfile: res 1 " + response.data);
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private void uploadImage(String typeDocument, File globalFileNam) {
        p = GlobalVal.mProgressDialog(EditProfile.this, p, "Uploading image...");

        String forDec = compressImage(FileUtils.getPath(getApplicationContext(),
                Uri.fromFile(new File(globalFileNam.getPath()))));

        File file = new File(forDec);

        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 1 " + file);
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("uploadFile",
                file.getName(), requestBody1);

        RequestBody category = RequestBody.create(MediaType.parse("multipart/form-data"), "documents");
        RequestBody permission = RequestBody.create(MediaType.parse("multipart/form-data"), "true");

        RequestBody token = RequestBody.create(MediaType.parse("multipart/form-data"),
                "7220A3B7F8D2FD2C236092E0918B4EA3");

        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 2 " + fileToUpload1);
        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 3 " + category);
        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 4 " + token);

        compositeDisposable.add(apiConfig1.uploadSingleImage(fileToUpload1, category, token, permission)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseBodyResponse -> {
                            Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 1 ");

                            Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 3 " + responseBodyResponse);

                            if (responseBodyResponse.isSuccessful()) {
                                try {
                                    assert responseBodyResponse.body() != null;
                                    String remoteResponse = responseBodyResponse.body().string();
                                    Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 2 ");
                                    JSONObject forecast = new JSONObject(remoteResponse);
                                    Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 3 " +
                                            forecast.getJSONObject("data").getString("uriPath"));

                                    JSONObject data = new JSONObject();

//                            TODO Image pick
                                    JSONObject imageBody = new JSONObject();
                                    imageBody.put("uriPath", data.optString("uriPath"));
                                    imageBody.put("_id", data.optString("_id"));
                                    uploadUserInfo.put(typeDocument, imageBody);

                                    //TODO for loading image
//                            isUploading = false;
                                } catch (JSONException | IOException e) {
                                    Log.e(TAG, "Mahdi: EditProfile: uploadImage: accept: error 4 ", e);
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: error 5 " + responseBodyResponse.errorBody());
                            }
                            p.hide();
                        }, error -> {
                            Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: error 3 " + error.getMessage());
                            p.hide();
                        })
        );
    }

    private void uploadMultiImages(String typeDocument, File file1, File file2, File file3, File file4) {
        p = GlobalVal.mProgressDialog(EditProfile.this, p, "Uploading images...");

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("category", "documents");

        builder.addFormDataPart("uploadFiles", file1.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file1));
        builder.addFormDataPart("uploadFiles", file2.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file2));
        builder.addFormDataPart("uploadFiles", file3.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file3));
        builder.addFormDataPart("uploadFiles", file4.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file4));

        MultipartBody requestBody = builder.build();

        compositeDisposable.add(apiConfig1.uploadMultiImage(requestBody)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseBodyResponse -> {
                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 1 ");

                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 2 " + responseBodyResponse);

                            if (responseBodyResponse.isSuccessful()) {
                                try {
                                    assert responseBodyResponse.body() != null;
                                    String remoteResponse = responseBodyResponse.body().string();
                                    Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 3 " + remoteResponse);
                                    JSONObject forecast = new JSONObject(remoteResponse);
//                                    Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 3 " +
//                                            forecast.getJSONArray("data").getString("uriPath"));

                                    JSONArray multiImageUri = new JSONArray();

                                    for (int i = 0; i < forecast.optJSONArray("data").length(); i++) {
                                        String uriPath = forecast.optJSONArray("data").optJSONObject(i).optString("uriPath");
                                        multiImageUri.put(uriPath);
                                    }
                                    uploadUserInfo.put(typeDocument, multiImageUri);
                                    Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 4 " + uploadUserInfo);

//                                    JSONObject data = new JSONObject();

//                            TODO Image pick
//                                    JSONObject imageBody = new JSONObject();
//                                    imageBody.put("uriPath", data.optString("uriPath"));
//                                    imageBody.put("_id", data.optString("_id"));
//                                    uploadUserInfo.put(typeDocument, imageBody);

                                    //TODO for loading image
//                            isUploading = false;
                                } catch (JSONException | IOException e) {
                                    Log.e(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 1 ", e);
                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 2 " + responseBodyResponse.errorBody());
                            }
                            p.hide();
                        }, error -> {
                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 3 " + error.getMessage());
                            p.hide();
                        })
        );

//        Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: 1 " + file1.getName());
//        Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: 2 " + file2.getName());
//        Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: 3 " + file3.getName());
//        Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: 4 " + file4.getName());
//
//        MultipartBody.Part[] surveyImagesParts = new MultipartBody.Part[4];
//
//        RequestBody surveyBody1 = RequestBody.create(MediaType.parse("image/*"), file1);
//        surveyImagesParts[0] = MultipartBody.Part.createFormData("uploadFiles", file1.getName(), surveyBody1);
//
//        RequestBody surveyBody2 = RequestBody.create(MediaType.parse("image/*"), file2);
//        surveyImagesParts[1] = MultipartBody.Part.createFormData("uploadFiles", file2.getName(), surveyBody2);
//
//        RequestBody surveyBody3 = RequestBody.create(MediaType.parse("image/*"), file3);
//        surveyImagesParts[2] = MultipartBody.Part.createFormData("uploadFiles", file3.getName(), surveyBody3);
//
//        RequestBody surveyBody4 = RequestBody.create(MediaType.parse("image/*"), file4);
//        surveyImagesParts[3] = MultipartBody.Part.createFormData("uploadFiles", file4.getName(), surveyBody4);
//
//        RequestBody draBody = null;
//        draBody = RequestBody.create(MediaType.parse("text/plain"), "documents");
//
//        compositeDisposable.add(apiConfig1.uploadMultiImage(userToken, surveyImagesParts, draBody)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(responseBodyResponse -> {
//                            isUploading = false;
//                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 1 ");
//
//                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 3 " + responseBodyResponse);
//
//                            if (responseBodyResponse.isSuccessful()) {
//                                try {
//                                    assert responseBodyResponse.body() != null;
//                                    String remoteResponse = responseBodyResponse.body().string();
//                                    Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 2 ");
//                                    JSONObject forecast = new JSONObject(remoteResponse);
//                                    Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: 3 " +
//                                            forecast.getJSONObject("data").getString("uriPath"));
//
//                                    JSONObject data = new JSONObject();
//
////                            TODO Image pick
//                                    JSONObject imageBody = new JSONObject();
//                                    imageBody.put("uriPath", data.optString("uriPath"));
//                                    imageBody.put("_id", data.optString("_id"));
//                                    uploadUserInfo.put(typeDocument, imageBody);
//
//                                    //TODO for loading image
////                            isUploading = false;
//                                } catch (JSONException | IOException e) {
//                                    Log.e(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 1 ", e);
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 2 " + responseBodyResponse.errorBody());
//                            }
//                        }, error -> {
//                            Log.i(TAG, "Mahdi: EditProfile: uploadMultiImages: accept: error 3 " + error.getMessage());
//                        })
//        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public boolean validate() {
        boolean valid = true;

        userName = editProfileBinding.editProfileNameEdt.getText().toString();
        email = editProfileBinding.editProfileEmailEdt.getText().toString();
        contactNumber = editProfileBinding.editProfilePlateNoEdt.getText().toString();
        //TODO Get Car id
//        carType = carTypeEdt.getText().toString();

        if (userName.isEmpty()) {
            editProfileBinding.editProfileNameEdt.setError("enter a valid user name address");
            valid = false;
        } else {
            editProfileBinding.editProfileNameEdt.setError(null);
        }
        if (email.isEmpty()) {
            editProfileBinding.editProfileEmailEdt.setError("enter a valid email address");
            valid = false;
        } else {
            editProfileBinding.editProfileEmailEdt.setError(null);
        }
        if (contactNumber.isEmpty()) {
            editProfileBinding.editProfilePlateNoEdt.setError("enter a valid contact number address");
            valid = false;
        } else {
            editProfileBinding.editProfilePlateNoEdt.setError(null);
        }
        if (editProfileBinding.editProfileCircleImage.getDrawable().getConstantState() == EditProfile.this
                .getResources().getDrawable(R.drawable.ic_account_circle_black_24dp)
                .getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your image profile!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileLicenseImage.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your image license!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileRegistrationImage.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your image registration!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileInsuranceImage.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your image insurance!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileCarInImage1.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick image from inside of your car!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileCarInImage1.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState() ||
                editProfileBinding.editProfileCarInImage4.getDrawable().getConstantState() ==
                        EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Please select 4 images from outside of your car!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileCarOutImage1.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick image from outside of your car!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (editProfileBinding.editProfileCarOutImage1.getDrawable().getConstantState() ==
                EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState() ||
                editProfileBinding.editProfileCarOutImage4.getDrawable().getConstantState() ==
                        EditProfile.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Please select 4 images from outside of your car!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void selectMultipleImage(int countImage) {
        String galleryNum = 3 + "" + countImage;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Integer.parseInt(galleryNum));
    }

    private void selectImage(int countImage) {
        String cameraNum = 1 + "" + countImage;
        String galleryNum = 2 + "" + countImage;
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (checkPermissionCamera()) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, Integer.parseInt(cameraNum));
                } else {
                    requestPermissionCamera();
                }
            } else if (options[item].equals("Choose from Gallery")) {
                if (checkPermissionGallery()) {

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Integer.parseInt(galleryNum));
                } else {
                    requestPermissionGallery();
                }
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    public String getRealPathFromURITemp(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
                "Title" + Calendar.getInstance().getTimeInMillis(), null);
        return Uri.parse(path);
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        File file = new File(filename);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
            out = new FileOutputStream(file);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 11) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                editProfileBinding.editProfileCircleImage.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                Uri tempUri = getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("profilePhoto", globalFileName);

            } else if (requestCode == 21) {
                assert data != null;
                Uri filePath = data.getData();

                File globalFileName = new File(getRealPathFromURITemp(filePath));
                editProfileBinding.editProfileCircleImage.setImageURI(filePath);
                uploadImage("profilePhoto", globalFileName);
            }
            if (requestCode == 12) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                editProfileBinding.editProfileLicenseImage.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                Uri tempUri = getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("DL", globalFileName);
            } else if (requestCode == 22) {
                assert data != null;
                editProfileBinding.editProfileLicenseImage.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                uploadImage("DL", globalFileName);
            }

            if (requestCode == 13) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                editProfileBinding.editProfileRegistrationImage.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                Uri tempUri = getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("Registration", globalFileName);

            } else if (requestCode == 23) {
                assert data != null;
                editProfileBinding.editProfileRegistrationImage.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                editProfileBinding.editProfileCircleImage.setImageURI(data.getData());
                uploadImage("Registration", globalFileName);

            }
            if (requestCode == 14) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                editProfileBinding.editProfileInsuranceImage.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                Uri tempUri = getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("Insurance", globalFileName);
            } else if (requestCode == 24) {
                assert data != null;
                editProfileBinding.editProfileInsuranceImage.setImageURI(data.getData());


                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                editProfileBinding.editProfileCircleImage.setImageURI(data.getData());
                uploadImage("Insurance", globalFileName);
            } else if (requestCode == 31 && null != data) {

                ClipData clipData = data.getClipData();
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

                if (clipData != null && clipData.getItemCount() == 4) {
                    editProfileBinding.editProfileCarInImage1.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarInImage1.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarInImage2.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarInImage2.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarInImage3.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarInImage3.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarInImage4.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarInImage4.setVisibility(View.VISIBLE);

                    editProfileBinding.editProfileCarInImage1.setImageURI(clipData.getItemAt(0).getUri());
                    editProfileBinding.editProfileCarInImage2.setImageURI(clipData.getItemAt(1).getUri());
                    editProfileBinding.editProfileCarInImage3.setImageURI(clipData.getItemAt(2).getUri());
                    editProfileBinding.editProfileCarInImage4.setImageURI(clipData.getItemAt(3).getUri());

                    String forDec1 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(0).getUri()));
                    File file1 = new File(forDec1);

                    String forDec2 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(1).getUri()));
                    File file2 = new File(forDec2);

                    String forDec3 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file3 = new File(forDec3);

                    String forDec4 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file4 = new File(forDec4);
//
//                    //TODO Something
                    uploadMultiImages("CarInside", file1, file2, file3, file4);

//                    File fileTemp2 = new File(forDec1);
//                    long length2 = fileTemp2.length() / 1024;

//                    Log.i("Hello: Mahdi", "onActivityResult: image length: 2 " + length2);

//                    File file1 = new File(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(0).getUri()));
//                    File file2 = new File(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(1).getUri()));
//                    File file3 = new File(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
//                    File file4 = new File(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(3).getUri()));
//
//                    uploadMultiImages("CarInside", file1, file2, file3, file4);


//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        ClipData.Item item = clipData.getItemAt(i);
//                        Uri uri = item.getUri();
//                        Log.i("Hello: Mahdi", "onActivityResult: " + clipData.toString());
//                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You must chose just four images!", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == 32) {
                ClipData clipData = data.getClipData();
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        getResources().getDisplayMetrics());

                if (clipData != null && clipData.getItemCount() == 4) {
                    editProfileBinding.editProfileCarOutImage1.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarOutImage1.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarOutImage2.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarOutImage2.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarOutImage3.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarOutImage3.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarOutImage4.getLayoutParams().width = dimensionInDp;
                    editProfileBinding.editProfileCarOutImage4.setVisibility(View.VISIBLE);
                    editProfileBinding.editProfileCarOutImage1.setImageURI(clipData.getItemAt(0).getUri());
                    editProfileBinding.editProfileCarOutImage2.setImageURI(clipData.getItemAt(1).getUri());
                    editProfileBinding.editProfileCarOutImage3.setImageURI(clipData.getItemAt(2).getUri());
                    editProfileBinding.editProfileCarOutImage4.setImageURI(clipData.getItemAt(3).getUri());

                    String forDec1 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(0).getUri()));
                    File file1 = new File(forDec1);

                    String forDec2 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(1).getUri()));
                    File file2 = new File(forDec2);

                    String forDec3 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file3 = new File(forDec3);

                    String forDec4 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file4 = new File(forDec4);
//
//                    //TODO Something
                    uploadMultiImages("CarOutside", file1, file2, file3, file4);

//                    File file1 = new File(clipData.getItemAt(0).getUri().getPath());
//                    File file2 = new File(clipData.getItemAt(1).getUri().getPath());
//                    File file3 = new File(clipData.getItemAt(2).getUri().getPath());
//                    File file4 = new File(clipData.getItemAt(3).getUri().getPath());
//
//                    uploadMultiImages("CarOutside", file1, file2, file3, file4);

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e("Hello: Mahdi", "onActivityResult: " + clipData.toString());
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You must chose just four images!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(EditProfile.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissionCamera();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;

            case PERMISSION_REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissionGallery();
                                            }
                                        }
                                    });
                        }
                    }
                }

            case PERMISSION_REQUEST_CODE_WRITE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissionWriteGallery();
                                            }
                                        }
                                    });
                        }
                    }
                }
        }
    }

    private boolean checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private boolean checkPermissionGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermissionCamera() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE_CAMERA);
    }

    private void requestPermissionGallery() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE_GALLERY);
    }


    private void requestPermissionWriteGallery() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE_WRITE_GALLERY);
    }

    private boolean checkPermissionWriteGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }
}

//        ArrayList<String> filePaths = new ArrayList<>();
//        filePaths.add(file1.toString());
//        filePaths.add(file2.toString());
//        filePaths.add(file3.toString());
//        filePaths.add(file4.toString());
//
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        builder.setType(MultipartBody.FORM);
//
//        builder.addFormDataPart("category", "documents");
//
//        for (int i = 0; i < filePaths.size(); i++) {
//            File file = new File(filePaths.get(i));
//            builder.addFormDataPart("file[]", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"),
//                    file));
//        }
//        MultipartBody requestBody = builder.build();

// TODO

//        MultipartBody requestBody = builder.build();
//        Call<ResponseBody> call = apiConfig1.uploadMultiFile(requestBody);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//            }
//        });

//        RequestBody requestBody1 = RequestBody.create(MediaType.parse("multipart/form-data"), globalFileName);
//
//        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("uploadFile",
//                globalFileName.getName(), requestBody1);

//        RequestBody category = RequestBody.create(MediaType.parse("multipart/form-data"), "documents");
//        RequestBody permission = RequestBody.create(MediaType.parse("multipart/form-data"), "true");
//
//        RequestBody token = RequestBody.create(MediaType.parse("multipart/form-data"),
//                "7220A3B7F8D2FD2C236092E0918B4EA3");
//
//        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 1 " + userToken);
//        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 2 " + fileToUpload1);
//        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 3 " + category);
//        Log.i(TAG, "Mahdi: EditProfile: uploadImage: 4 " + token);
//
//        compositeDisposable.add(apiConfig1.uploadSingleImage(userToken, fileToUpload1, category, token, permission)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(responseBodyResponse -> {
//                            isUploading = false;
//                            Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 1 ");
//
//                            Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 3 " + responseBodyResponse);
//
//                            if (responseBodyResponse.isSuccessful()) {
//                                try {
//                                    assert responseBodyResponse.body() != null;
//                                    String remoteResponse = responseBodyResponse.body().string();
//                                    Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 2 ");
//                                    JSONObject forecast = new JSONObject(remoteResponse);
//                                    Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 3 " +
//                                            forecast.getJSONObject("data").getString("uriPath"));
//
//                                    JSONObject data = new JSONObject();
//
////                            TODO Image pick
//                                    JSONObject imageBody = new JSONObject();
//                                    imageBody.put("uriPath", data.optString("uriPath"));
//                                    imageBody.put("_id", data.optString("_id"));
//                                    uploadUserInfo.put(typeDocument, imageBody);
//
//                                    //TODO for loading image
////                            isUploading = false;
//                                } catch (JSONException | IOException e) {
//                                    Log.e(TAG, "Mahdi: EditProfile: uploadImage: accept: error 4 ", e);
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 5 " + responseBodyResponse.errorBody());
//                            }
//                        })
//        );
