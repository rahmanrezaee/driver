package com.development.taxiappproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.development.taxiappproject.Global.GlobalVal;
import com.development.taxiappproject.Retrofit.MyApiConfig;
import com.development.taxiappproject.Retrofit.RetrofitClient;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.yalantis.ucrop.util.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;

public class SignUpScreen extends AppCompatActivity {
    private CircleImageView mCircleImageView;
    private ImageView licenseImage, registration, insurance, car_in1, car_in2, car_in3, car_in4, car_out1, car_out2, car_out3, car_out4;
    private EditText usernameEdt, emailEdt, contactNumberEdt, passwordEdt, carTypeEdt;
    String userName, email, contactNumber, password, carType;

    String imageEncoded;
    List<String> imagesEncodedList;

    ProgressDialog p;
    String newToken;
    private final int PERMISSION_REQUEST_CODE_CAMERA = 1;
    private final int PERMISSION_REQUEST_CODE_GALLERY = 2;
    private final int PERMISSION_REQUEST_CODE_WRITE_GALLERY = 3;
    private String TAG = "MAHDI";

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MyApiConfig apiConfig1;
    JSONObject uploadUserInfo = new JSONObject();

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        String token = MyFirebaseMessagingService.getToken(SignUpScreen.this);

        Log.i(TAG, "Mahdi: SignUpScreen onCreate: " + token);

        mCircleImageView = findViewById(R.id.signUp_profileImage);
        licenseImage = findViewById(R.id.license_image);
        registration = findViewById(R.id.registration_image);
        insurance = findViewById(R.id.insurance_image);
        car_in1 = findViewById(R.id.car_in_image1);
        car_in2 = findViewById(R.id.car_in_image2);
        car_in3 = findViewById(R.id.car_in_image3);
        car_in4 = findViewById(R.id.car_in_image4);

        car_out1 = findViewById(R.id.car_out_image1);
        car_out2 = findViewById(R.id.car_out_image2);
        car_out3 = findViewById(R.id.car_out_image3);
        car_out4 = findViewById(R.id.car_out_image4);

        usernameEdt = findViewById(R.id.sign_up_username);
        emailEdt = findViewById(R.id.sign_up_email);
        contactNumberEdt = findViewById(R.id.sign_up_contact_number);
        passwordEdt = findViewById(R.id.sign_up_password);
        carTypeEdt = findViewById(R.id.sign_up_car_type);

        Retrofit retrofit = RetrofitClient.getInstance();
        apiConfig1 = retrofit.create(MyApiConfig.class);

        if (checkPermissionWriteGallery()) {

        } else {
            requestPermissionWriteGallery();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signUp_profileImage:
            case R.id.image_icon:
                selectImage(1);
                break;

            case R.id.license_image:
            case R.id.license_icon:
                selectImage(2);
                break;

            case R.id.registration_image:
            case R.id.registration_icon:
                selectImage(3);
                break;

            case R.id.insurance_image:
            case R.id.insurance_icon:
                selectImage(4);
                break;

            case R.id.car_in_image1:
            case R.id.car_in_icon:
                selectMultipleImage(1);
                break;

            case R.id.car_out_image1:
            case R.id.car_out_icon:
                selectMultipleImage(2);
                break;

            case R.id.sign_up_btn:
                if (validate()) {
                    try {

//                        "username": "Rajab",
//                                "email": "js.mohammadi@gmail.com",
//                                "phone": "+93767626554",
//                                "password": "123asd",
//                                "carType": "600fdcc646bc6409ae97e2ab",
//                                "fcmToken": "jlkfjdslkfjlkdsjfoiewroijowjfksjfoies",

                        uploadUserInfo.put("username", userName);
                        uploadUserInfo.put("email", email);
                        uploadUserInfo.put("phone", contactNumber);
                        uploadUserInfo.put("carType", carType);
                        uploadUserInfo.put("password", password);
                        uploadUserInfo.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));

                        Intent intent = new Intent(getBaseContext(), OTPScreen.class);
                        intent.putExtra("type", "signUp");
                        intent.putExtra("bodyData", uploadUserInfo.toString());
                        intent.putExtra("phone_number", contactNumber);
                        startActivity(intent);
                    } catch (Exception ex) {
                        Log.e("Mahdi: Login error: 1 ", String.valueOf(ex));
                    }
                }
                break;
        }
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

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
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
        try {
            out = new FileOutputStream(filename);
//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }


    private void uploadImage(String typeDocument, File globalFileNam) {
        p = GlobalVal.mProgressDialog(SignUpScreen.this, p, "Uploading image...");

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
                                            forecast.optJSONObject("data").optString("uriPath"));

                                    JSONObject data = forecast.optJSONObject("data");

//                            TODO Image pick
                                    JSONObject imageBody = new JSONObject();
                                    imageBody.put("uriPath", data.optString("uriPath"));
                                    imageBody.put("_id", data.optString("_id"));
                                    uploadUserInfo.put(typeDocument, imageBody);

                                    Log.i(TAG, "Mahdi: EditProfile: uploadImage: accept: 4 " + uploadUserInfo);
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
        p = GlobalVal.mProgressDialog(SignUpScreen.this, p, "Uploading images...");

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
                            Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: 1 ");

                            Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: 2 " + responseBodyResponse);

                            if (responseBodyResponse.isSuccessful()) {
                                try {
                                    assert responseBodyResponse.body() != null;
                                    String remoteResponse = responseBodyResponse.body().string();
                                    Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: 3 " + remoteResponse);
                                    JSONObject forecast = new JSONObject(remoteResponse);
//                                    Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: 3 " +
//                                            forecast.getJSONArray("data").getString("uriPath"));

                                    JSONArray multiImageUri = new JSONArray();

                                    for (int i = 0; i < forecast.optJSONArray("data").length(); i++) {
                                        String uriPath = forecast.optJSONArray("data").optJSONObject(i).optString("uriPath");
                                        multiImageUri.put(uriPath);
                                    }
                                    uploadUserInfo.put(typeDocument, multiImageUri);

//                                    JSONObject data = new JSONObject();

//                            TODO Image pick
//                                    JSONObject imageBody = new JSONObject();
//                                    imageBody.put("uriPath", data.optString("uriPath"));
//                                    imageBody.put("_id", data.optString("_id"));
//                                    uploadUserInfo.put(typeDocument, imageBody);

                                    //TODO for loading image
//                            isUploading = false;
                                } catch (JSONException | IOException e) {
                                    Toast.makeText(getApplicationContext(), "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: error 1 ", e);
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: error 2 " + responseBodyResponse.errorBody());
                            }
                            p.hide();
                        }, error -> {
                            Toast.makeText(getApplicationContext(), "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Mahdi: SignUpScreen: uploadMultiImages: accept: error 3 " + error.getMessage());
                            p.hide();
                        })
        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public boolean validate() {
        boolean valid = true;

        userName = usernameEdt.getText().toString();
        email = emailEdt.getText().toString();
        contactNumber = contactNumberEdt.getText().toString();
        password = passwordEdt.getText().toString();
        carType = carTypeEdt.getText().toString();

        if (userName.isEmpty()) {
            usernameEdt.setError("Enter a valid user name address");
            valid = false;
        } else {
            usernameEdt.setError(null);
        }
        if (email.isEmpty()) {
            emailEdt.setError("Enter a valid email address");
            valid = false;
        } else {
            emailEdt.setError(null);
        }

        String regexStr = "^[+]?[0-9]{10,13}$";

        if (contactNumber.isEmpty()) {
            contactNumberEdt.setError("Enter a valid contact number address");
            valid = false;
        }
//        else if (!contactNumber.matches(regexStr) || !contactNumber.contains("001") || contactNumber.contains("+1")) {
//            contactNumberEdt.setError("Your number must be american contact number");
//            valid = false;
//        }
        else {
            contactNumberEdt.setError(null);
        }
        if (password.isEmpty()) {
            passwordEdt.setError("Enter a valid password address");
            valid = false;
        } else if (password.length() < 6) {
            passwordEdt.setError("Your password must be bigger than 6 character");
            valid = false;
        } else {
            passwordEdt.setError(null);
        }
        if (carType.isEmpty()) {
            carTypeEdt.setError("Enter a valid car type address");
            valid = false;
        } else {
            carTypeEdt.setError(null);
        }
        if (mCircleImageView.getDrawable().getConstantState() == SignUpScreen.this
                .getResources().getDrawable(R.drawable.ic_account_circle_black_24dp)
                .getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your profile image!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (licenseImage.getDrawable().getConstantState() ==
                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your license image!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (registration.getDrawable().getConstantState() ==
                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your registration image!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (insurance.getDrawable().getConstantState() ==
                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your insurance image!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (car_in1.getDrawable().getConstantState() ==
                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your car in images!", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (car_out1.getDrawable().getConstantState() ==
                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
            Toast.makeText(getApplicationContext(), "Pick your car out image!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private boolean checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }


//    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    private boolean checkPermissionWriteGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private boolean checkPermissionReadGallery() {
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

    private void requestPermissionReadGallery() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE_GALLERY);
    }

    private void requestPermissionWriteGallery() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE_WRITE_GALLERY);
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
                                                requestPermissionReadGallery();
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SignUpScreen.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_picker, menu);
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void selectMultipleImage(int countImage) {
        if (checkPermissionReadGallery()) {
            String galleryNum = 3 + "" + countImage;
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Integer.parseInt(galleryNum));
        } else {
            requestPermissionReadGallery();
        }
    }


    private void selectImage(int countImage) {
        String cameraNum = 1 + "" + countImage;
        String galleryNum = 2 + "" + countImage;
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpScreen.this);
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
                if (checkPermissionReadGallery()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Integer.parseInt(galleryNum));
                } else {
                    requestPermissionReadGallery();
                }
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 11) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                mCircleImageView.setImageBitmap(photo);

                Uri tempUri = new EditProfile().getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("profilePhoto", globalFileName);

            } else if (requestCode == 21) {
                assert data != null;
                mCircleImageView.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                uploadImage("profilePhoto", globalFileName);
            }

            if (requestCode == 12) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                licenseImage.setImageBitmap(photo);


                Uri tempUri = new EditProfile().getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("DL", globalFileName);
            } else if (requestCode == 22) {
                assert data != null;
                licenseImage.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                uploadImage("DL", globalFileName);
            }

            if (requestCode == 13) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                registration.setImageBitmap(photo);

                Uri tempUri = new EditProfile().getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("Registration", globalFileName);
            } else if (requestCode == 23) {
                assert data != null;

                registration.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                uploadImage("Registration", globalFileName);
            }
            if (requestCode == 14) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                insurance.setImageBitmap(photo);

                Uri tempUri = new EditProfile().getImageUri(getApplicationContext(), photo);

                File globalFileName = new File(getRealPathFromURITemp(tempUri));
                uploadImage("Insurance", globalFileName);
            } else if (requestCode == 24) {
                assert data != null;
                insurance.setImageURI(data.getData());

                File globalFileName = new File(getRealPathFromURITemp(data.getData()));
                uploadImage("Insurance", globalFileName);
            } else if (requestCode == 31 && null != data) {

                ClipData clipData = data.getClipData();
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

                if (clipData != null && clipData.getItemCount() <= 4) {
                    car_in1.getLayoutParams().width = dimensionInDp;
                    car_in1.setVisibility(View.VISIBLE);
                    car_in2.getLayoutParams().width = dimensionInDp;
                    car_in2.setVisibility(View.VISIBLE);
                    car_in3.getLayoutParams().width = dimensionInDp;
                    car_in3.setVisibility(View.VISIBLE);
                    car_in4.getLayoutParams().width = dimensionInDp;
                    car_in4.setVisibility(View.VISIBLE);

                    car_in1.setImageURI(clipData.getItemAt(0).getUri());
                    car_in2.setImageURI(clipData.getItemAt(1).getUri());
                    car_in3.setImageURI(clipData.getItemAt(2).getUri());
                    car_in4.setImageURI(clipData.getItemAt(3).getUri());


                    String forDec1 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(0).getUri()));
                    File file1 = new File(forDec1);

                    String forDec2 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(1).getUri()));
                    File file2 = new File(forDec2);

                    String forDec3 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file3 = new File(forDec3);

                    String forDec4 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file4 = new File(forDec4);

//                    //TODO Something
                    uploadMultiImages("CarInside", file1, file2, file3, file4);

                } else {
                    Toast.makeText(getApplicationContext(), "You must choose just four images!", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == 32) {
                ClipData clipData = data.getClipData();
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                        getResources().getDisplayMetrics());

                if (clipData != null && clipData.getItemCount() <= 4) {
                    car_out1.getLayoutParams().width = dimensionInDp;
                    car_out1.setVisibility(View.VISIBLE);
                    car_out2.getLayoutParams().width = dimensionInDp;
                    car_out2.setVisibility(View.VISIBLE);
                    car_out3.getLayoutParams().width = dimensionInDp;
                    car_out3.setVisibility(View.VISIBLE);
                    car_out4.getLayoutParams().width = dimensionInDp;
                    car_out4.setVisibility(View.VISIBLE);

                    car_out1.setImageURI(clipData.getItemAt(0).getUri());
                    car_out2.setImageURI(clipData.getItemAt(1).getUri());
                    car_out3.setImageURI(clipData.getItemAt(2).getUri());
                    car_out4.setImageURI(clipData.getItemAt(3).getUri());


                    String forDec1 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(0).getUri()));
                    File file1 = new File(forDec1);

                    String forDec2 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(1).getUri()));
                    File file2 = new File(forDec2);

                    String forDec3 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file3 = new File(forDec3);

                    String forDec4 = compressImage(FileUtils.getPath(getApplicationContext(), clipData.getItemAt(2).getUri()));
                    File file4 = new File(forDec4);

                    //TODO Something
                    uploadMultiImages("CarOutside", file1, file2, file3, file4);

                } else {
                    Toast.makeText(getApplicationContext(), "You must chose just four or less than four image!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

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

//    private JSONObject createBody() {
//        JSONObject jsonBody = new JSONObject();
//        try {
//            try {
//                jsonBody.put("username", userName);
//                jsonBody.put("email", email);
//                jsonBody.put("phone", contactNumber);
//                jsonBody.put("password", password);
//                jsonBody.put("carType", carType);
//                jsonBody.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
//                jsonBody.put("profilePhoto", "https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            JSONObject document = new JSONObject();
//            try {
//                document.put("DL", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
//                document.put("Registration", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
//                document.put("Insurance", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
//                document.put("CarInside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
//                document.put("CarOutside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            jsonBody.put("documents", document.toString());
//
//            Log.i("MAHDI", "Hello: Mahdi: Data: " + jsonBody);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonBody;
////        final String requestBody = jsonBody.toString();
////
////        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
////                response -> Log.i("Mahdi", "Hello: Mahdi: " + response),
////                error -> Log.e("Mahdi", "Hello: Mahdi: Error " + error)
////        ) {
////            @Override
////            public String getBodyContentType() {
////                return "application/json; charset=utf-8";
////            }
////
////            @Override
////            public byte[] getBody() throws AuthFailureError {
////                try {
////                    return requestBody == null ? null : requestBody.getBytes("utf-8");
////                } catch (UnsupportedEncodingException uee) {
////                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
////                    return null;
////                }
////            }
////
////            @Override
////            protected Response<String> parseNetworkResponse(NetworkResponse response) {
////                String responseString = "";
////                if (response != null) {
////                    responseString = String.valueOf(response.statusCode);
////                    // can get more details such as response.headers
////                }
////                assert response != null;
////                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
////            }
////        };
////        requestQueue.add(stringRequest);
//    }

//    private void getRequest() {
//        RequestQueue queue = Volley.newRequestQueue(SignUpScreen.this);
//        String url = "A url for getting the value";
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
//            Log.i("Mahdi", "Hello: Mahdi: " + response);
//        }, error -> {
//            Log.i("Mahdi", "Hello: Mahdi: Error " + error);
//        });
//
//        queue.add(stringRequest);
//    }
}

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Integer.parseInt(galleryNum));

//public class MultipartRequest extends Request<String> {
//
//    private MultipartEntity entity = new MultipartEntity();
//
//    private static final String FILE_PART_NAME = "file";
//    private static final String STRING_PART_NAME = "text";
//
//    private final Response.Listener<String> mListener;
//    private final File mFilePart;
//    private final String mStringPart;
//
//    public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, File file, String stringPart)
//    {
//        super(Method.POST, url, errorListener);
//
//        mListener = listener;
//        mFilePart = file;
//        mStringPart = stringPart;
//        buildMultipartEntity();
//    }
//
//    private void buildMultipartEntity()
//    {
//        entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
//        try
//        {
//            entity.addPart(STRING_PART_NAME, new StringBody(mStringPart));
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            VolleyLog.e("UnsupportedEncodingException");
//        }
//    }
//
//    @Override
//    public String getBodyContentType()
//    {
//        return entity.getContentType().getValue();
//    }
//
//    @Override
//    public byte[] getBody() throws AuthFailureError
//    {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        try
//        {
//            entity.writeTo(bos);
//        }
//        catch (IOException e)
//        {
//            VolleyLog.e("IOException writing to ByteArrayOutputStream");
//        }
//        return bos.toByteArray();
//    }
//
//    @Override
//    protected Response<String> parseNetworkResponse(NetworkResponse response)
//    {
//        return Response.success("Uploaded", getCacheEntry());
//    }
//
//    @Override
//    protected void deliverResponse(String response)
//    {
//        mListener.onResponse(response);
//    }
//}

//        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpScreen.this,
//                instanceIdResult -> {
//                    newToken = instanceIdResult.getToken();
//                });
//
//        Log.d("newToken", "Hello: Mahdi: newToken: 1" + newToken);
//        Log.d("newToken", "Hello: Mahdi: newToken: 2" + userName + " : " + email + " : " + contactNumber + ":" + password + ":" + carType);
//
//        RequestQueue requestQueue = Volley.newRequestQueue(SignUpScreen.this);
//        String url = "https://taxiappapi.webfumeprojects.online/driver/signupFirebase";
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("username", userName);
//            jsonBody.put("email", email);
//            jsonBody.put("phone", contactNumber);
//            jsonBody.put("password", password);
//            jsonBody.put("carType", carType);
//            jsonBody.put("fcmToken", "lljlkdsajfljasldfj;lsa");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        final String requestBody = jsonBody.toString();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
//            Log.i("Mahdi", "Hello: Mahdi: " + response);
//        }, error -> {
//            Log.i("Mahdi", "Hello: Mahdi: Error " + error);
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("Content-Type", "application/x-www-form-urlencoded");
//                params.put("firebaseToken", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjljZTVlNmY1MzBiNDkwMTFiYjg0YzhmYWExZWM1NGM1MTc1N2I2NTgiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vdGF4aWFwcC1kYjA3NiIsImF1ZCI6InRheGlhcHAtZGIwNzYiLCJhdXRoX3RpbWUiOjE2MTE4MjI5NjMsInVzZXJfaWQiOiJZQlp5UEdWNG9XWnBrVksxZEtUUjhFZU55MmMyIiwic3ViIjoiWUJaeVBHVjRvV1pwa1ZLMWRLVFI4RWVOeTJjMiIsImlhdCI6MTYxMjA3MDE1NCwiZXhwIjoxNjEyMDczNzU0LCJwaG9uZV9udW1iZXIiOiIrOTM3ODIyMjI4NDAiLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7InBob25lIjpbIis5Mzc4MjIyMjg0MCJdfSwic2lnbl9pbl9wcm92aWRlciI6InBob25lIn19.N8Q6UtPHPr6DPzWZkMCHgxI4z_JmMUJuV4WrD4UloNE7mPf9tFhnnOHNUL9iUgPnrbKXmxsYcMbvS9lFqJ1xj802yJG8v4ehUULn4vpnFMd5aH0PiXEzFbRwkfd2h82dq2MjA-LR9OS88S7MeKDWiZzVyMq13S1RooViHZaiw5mGeexhGW4P-DbwpMVtn2sQZECxXQDAYV0cg-NCVgSeuOHZSuy6FfsRELgRxZr8f2SE7Jb70rtveNp7YKMUbX3-tz9QbIzcTtjh-RJ-FySDkQ4SRTrVsVCdTizcJPvvfpLE3txs02zKh7hpaVn79gO2EhFAh5t204hxuyEBEyZ2Xw");
//
//                return params;
//            }

//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }

//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    return requestBody.getBytes(StandardCharsets.UTF_8);
//                }
//                return null;
//            }
//        };
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        requestQueue.add(stringRequest);
//    class SignUpAsync extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            p = new ProgressDialog(SignUpScreen.this);
//            p.setMessage("Please wait...It is downloading");
//            p.setIndeterminate(false);
//            p.setCancelable(false);
//            p.show();
//        }
//
//        @Override
//        protected String doInBackground(String... currency) {
//            try {
//                URL url = new URL(currency[0]);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                String content = "";
//
//                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpScreen.this,
//                        instanceIdResult -> {
//                            newToken = instanceIdResult.getToken();
//                            Log.d("newToken", "newToken: " + newToken);
//                        });
//                try {
//                    connection.setRequestMethod("POST");
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                }
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("firebaseToken", newToken);
//
//                try {
//                    connection.connect();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                InputStream is = null;
//                try {
//                    is = connection.getInputStream();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                InputStreamReader isr = new InputStreamReader(is);
//
//                int data = 0;
//                try {
//                    data = isr.read();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                char ch;
//                while (data != -1) {
//                    ch = (char) data;
//                    content = content + ch;
//                    try {
//                        data = isr.read();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return content;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            p.hide();
//        }
//    }

//    public void signUpUser() {
//
//        String content;
//        SignUpAsync currencySub = new SignUpAsync();
//
//        try {
//            content = currencySub.execute("https://taxiappapi.webfumeprojects.online/driver/signupFirebase").get();
//
//            JSONObject jsonObject = new JSONObject(content);
//
//            Log.i("Mahdi", "Mahdi: Hello: SignUp " + jsonObject);
//
//        } catch (ExecutionException | InterruptedException | JSONException e) {
//            Log.e("Mahdi", "Mahdi: Hello: SignUp Error " + e);
//            e.printStackTrace();
//        }
//    }

//    private void sendPostRequest() {
//
//        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
//
//            @Override
//            protected String doInBackground(String... params) {
//                FirebaseMessaging.getInstance().getToken()
//                        .addOnCompleteListener(task -> {
//                            if (!task.isSuccessful()) {
//                                Log.w("Hello: Mahdi", "Mahdi: Token 1 ", task.getException());
//                                return;
//                            }
//
//                            // Get new FCM registration token
//
//                            // Log and toast
//                            String msg = task.getResult();
//                            Log.i("Hello: Mahdi", "Mahdi: Token 2 " + msg);
//                            Toast.makeText(SignUpScreen.this, msg, Toast.LENGTH_SHORT).show();
//                        });
//                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpScreen.this, instanceIdResult -> {
//                    String newToken = instanceIdResult.getToken();
//                    Log.w("Hello: Mahdi", "Mahdi: Token 3 " + newToken);
//
//                    OkHttpClient client = new OkHttpClient();
//
//                    RequestBody formBody = new FormBody.Builder()
//                            .add("username", userName)
//                            .add("email", email)
//                            .add("phone", contactNumber)
//                            .add("password", contactNumber)
//                            .add("carType", carType)
//                            .add("fcmToken", newToken)
//                            .build();
//                    Request request = new Request.Builder()
//                            .url("https://taxiappapi.webfumeprojects.online/driver/signupFirebase")
//                            .post(formBody)
//                            .build();
//
//                    try {
//                        Response response = client.newCall(request).execute();
//                        Log.w("Hello: Mahdi", "Mahdi: Token 4 " + response);
//                    } catch (IOException e) {
//                        Log.w("Hello: Mahdi", "Mahdi: Token 4 " + e);
//
//                        e.printStackTrace();
//                    }
//                });
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                super.onPostExecute(result);
//
//                if (result.equals("working")) {
//                    Toast.makeText(getApplicationContext(), "HTTP POST is working...", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Invalid POST req...", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//
//        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
//        sendPostReqAsyncTask.execute();
//    }

//                try {
//                    // When an Image is picked
//                    // Get the Image from data
//
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//                    imagesEncodedList = new ArrayList<>();
//                    if (data.getData() != null) {
//
//                        Uri mImageUri = data.getData();
//
//                        // Get the cursor
//                        Cursor cursor = getContentResolver().query(mImageUri,
//                                filePathColumn, null, null, null);
//                        // Move to first row
//                        assert cursor != null;
//                        cursor.moveToFirst();
//
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        imageEncoded = cursor.getString(columnIndex);
//                        cursor.close();
//                    } else {
//                        if (data.getClipData() != null) {
//                            ClipData mClipData = data.getClipData();
//                            ArrayList<Uri> mArrayUri = new ArrayList<>();
//                            for (int i = 0; i < mClipData.getItemCount(); i++) {
//
//                                ClipData.Item item = mClipData.getItemAt(i);
//                                Uri uri = item.getUri();
//                                mArrayUri.add(uri);
//                                // Get the cursor
//                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
//                                // Move to first row
//                                assert cursor != null;
//                                cursor.moveToFirst();
//
//                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                                imageEncoded = cursor.getString(columnIndex);
//                                imagesEncodedList.add(imageEncoded);
//                                cursor.close();
//                            }
//                            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
//                        }
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
//                            .show();
//                }

//                if (data.getClipData() != null) {
//                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
//                    Uri imageUri;
//                    for (int i = 0; i < count; i++)
//                        imageUri = data.getClipData().getItemAt(i).getUri();
//                    //do something with the image (save it to some directory or whatever you need to do with it here)
//                } else if (data.getData() != null) {
//                    String imagePath = data.getData().getPath();
//                    //do something with the image (save it to some directory or whatever you need to do with it here)
//                }

//                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");

//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//pic = f;

//                startActivityForResult(intent, 1);

//h=0;
//                File f = new File(Environment.getExternalStorageDirectory().toString());
//
//                for (File temp : Objects.requireNonNull(f.listFiles())) {
//
//                    if (temp.getName().equals("temp.jpg")) {
//
//                        f = temp;
//                        File photo = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
//                        //pic = photo;
//                        break;
//
//                    }
//
//                }
//
//                try {
//
//                    Bitmap bitmap;
//
//                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//
//
//                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
//
//                            bitmapOptions);
//
//
//                    imageView.setImageBitmap(bitmap);
//
//
//                    String path = android.os.Environment
//
//                            .getExternalStorageDirectory()
//
//                            + File.separator
//
//                            + "Phoenix" + File.separator + "default";
//                    //p = path;
//
//                    f.delete();
//
//                    OutputStream outFile;
//
//                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
//
//                    try {
//
//                        outFile = new FileOutputStream(file);
//
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
//                        //pic=file;
//                        outFile.flush();
//
//                        outFile.close();
//
//
//                    } catch (FileNotFoundException e) {
//
//                        e.printStackTrace();
//
//                    } catch (IOException e) {
//
//                        e.printStackTrace();
//
//                    } catch (Exception e) {
//
//                        e.printStackTrace();
//
//                    }
//
//                } catch (Exception e) {
//
//                    e.printStackTrace();
//
//                }

//                Uri selectedImage = data.getData();
//                // h=1;
//                //imgui = selectedImage;
//                String[] filePath = {MediaStore.Images.Media.DATA};
//                assert selectedImage != null;
//                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
//                assert c != null;
//                c.moveToFirst();
//                int columnIndex = c.getColumnIndex(filePath[0]);
//                String picturePath = c.getString(columnIndex);
//                c.close();
//                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
//                Log.w("path of image from gallery......******************.........", picturePath+"");
//                imageView.setImageBitmap(thumbnail);
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//            } else {
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
//            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
//            imageView.setImageBitmap(photo);
//        }
//    }

//
//    public void imagePick() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//                //permission not granted, request it.
//                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
//                //show popup for runtime permission
//                pickImageFromCamera();
//            } else {
//                //permission already granted.
//            }
//        } else {
//            //system os is less than marshmallow
//            pickImageFromCamera();
//        }
//    }
//
//    private void pickImageFromGallery() {
//        //intent to pick image
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, IMAGE_PICK_CODE);
//    }
//
//    private void pickImageFromCamera() {
//        //intent to pick image
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
//        {
//            assert data != null;
//            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
//            mCircleImageView.setImageBitmap(photo);
//        }
////        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
////            //set image to image view
////            assert data != null;
////            mCircleImageView.setImageURI(data.getData());
////        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_CODE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //permission was granted
//                    pickImageFromGallery();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Permission denied...!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//}
//package com.development.taxiappproject
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class SignUpScreen : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sign_up_screen)
//    }
//}

//    private File savebitmap(Bitmap bmp) {
//        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
//        OutputStream outStream = null;
//        // String temp = null;
//        File file = new File(extStorageDirectory, "temp.png");
//        if (file.exists()) {
//            file.delete();
//            file = new File(extStorageDirectory, "temp.png");
//
//        }
//
//        try {
//            outStream = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//            outStream.flush();
//            outStream.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return file;
//    }

//                File f = new File(Environment.getExternalStorageDirectory().toString());
//
//                Bitmap bitmap;
//
//                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//
//                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
//                        bitmapOptions);
//
//                imageView.setImageBitmap(bitmap);

//        ic_account_circle_black_24dp



