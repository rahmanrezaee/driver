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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.development.taxiappproject.Service.MyFirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private String TAG = "MAHDI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        String token = MyFirebaseMessagingService.getToken(SignUpScreen.this);

        Log.i(TAG, "Mahdi: SignUpScreen onCreate: " + token);

        mCircleImageView = findViewById(R.id.profile_image);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
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
                Toast.makeText(getApplicationContext(), "Car in", Toast.LENGTH_SHORT).show();
                selectMultipleImage(1);
                break;

            case R.id.car_out_image1:
            case R.id.car_out_icon:
                selectMultipleImage(2);
                Toast.makeText(getApplicationContext(), "Car out", Toast.LENGTH_SHORT).show();
                break;

            case R.id.sign_up_btn:
                if (validate()) {
                    try {
                        JSONObject jsonObject = createBody();
                        Intent intent = new Intent(getBaseContext(), OTPScreen.class);
                        intent.putExtra("type", "signUp");
                        intent.putExtra("bodyData", jsonObject.toString());
                        intent.putExtra("phone_number", contactNumber);
                        startActivity(intent);
                    } catch (Exception ex) {
                        Log.e("Mahdi: Login error: 1 ", String.valueOf(ex));
                    }
                }
                break;
        }
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
            usernameEdt.setError("enter a valid user name address");
            valid = false;
        } else {
            usernameEdt.setError(null);
        }
        if (email.isEmpty()) {
            emailEdt.setError("enter a valid email address");
            valid = false;
        } else {
            emailEdt.setError(null);
        }
        if (contactNumber.isEmpty()) {
            contactNumberEdt.setError("enter a valid contact number address");
            valid = false;
        } else {
            contactNumberEdt.setError(null);
        }
        if (password.isEmpty()) {
            passwordEdt.setError("enter a valid password address");
            valid = false;
        } else if (password.length() < 6) {
            passwordEdt.setError("Your password must be bigger than 6 character");
            valid = false;
        } else {
            passwordEdt.setError(null);
        }
        if (carType.isEmpty()) {
            carTypeEdt.setError("enter a valid car type address");
            valid = false;
        } else {
            carTypeEdt.setError(null);
        }
//        if (mCircleImageView.getDrawable().getConstantState() == SignUpScreen.this
//                .getResources().getDrawable(R.drawable.ic_account_circle_black_24dp)
//                .getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image profile!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        } else if (licenseImage.getDrawable().getConstantState() ==
//                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image license!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        } else if (registration.getDrawable().getConstantState() ==
//                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image registration!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        } else if (insurance.getDrawable().getConstantState() ==
//                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image insurance!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        } else if (car_in1.getDrawable().getConstantState() ==
//                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image insurance!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        } else if (car_out1.getDrawable().getConstantState() ==
//                SignUpScreen.this.getResources().getDrawable(R.drawable.driving_license).getConstantState()) {
//            Toast.makeText(getApplicationContext(), "Pick your image insurance!", Toast.LENGTH_SHORT).show();
//            valid = false;
//        }
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

    private boolean checkPermissionGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void selectMultipleImage(int countImage) {
        String galleryNum = 3 + "" + countImage;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Integer.parseInt(galleryNum));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_picker, menu);
        return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 11) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                mCircleImageView.setImageBitmap(photo);
            } else if (requestCode == 21) {
                assert data != null;
                mCircleImageView.setImageURI(data.getData());
            }

            if (requestCode == 12) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                licenseImage.setImageBitmap(photo);
            } else if (requestCode == 22) {
                assert data != null;
                licenseImage.setImageURI(data.getData());
            }

            if (requestCode == 13) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                registration.setImageBitmap(photo);
            } else if (requestCode == 23) {
                assert data != null;
                registration.setImageURI(data.getData());
            }
            if (requestCode == 14) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                insurance.setImageBitmap(photo);
            } else if (requestCode == 24) {
                assert data != null;
                insurance.setImageURI(data.getData());
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

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.i("Hello: Mahdi", "onActivityResult: " + clipData.toString());
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You must chose just four or less than four image!", Toast.LENGTH_SHORT).show();
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

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e("Hello: Mahdi", "onActivityResult: " + clipData.toString());
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You must chose just four or less than four image!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public JSONArray createDocument(String imageUrl) {
        JSONObject dlObject = new JSONObject();
        try {
            dlObject.put("uriPath", imageUrl);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONArray dlArray = new JSONArray();
        dlArray.put(dlObject);

        return dlArray;
    }

    private JSONObject createBody() {

//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        String url = "https://taxiappapi.webfumeprojects.online/driver/signupFirebase";

        JSONObject jsonBody = new JSONObject();
        try {
//            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpScreen.this,
//                    instanceIdResult -> {
//
//                    });

            try {
                jsonBody.put("username", userName);
                jsonBody.put("email", email);
                jsonBody.put("phone", contactNumber);
                jsonBody.put("password", password);
                jsonBody.put("carType", carType);
                jsonBody.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
                jsonBody.put("profilePhoto", "https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject document = new JSONObject();

            try {
                document.put("DL", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
                document.put("Registration", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
                document.put("Insurance", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
                document.put("CarInside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
                document.put("CarOutside", createDocument("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonBody.put("documents", document.toString());

            Log.i("MAHDI", "Hello: Mahdi: Data: " + jsonBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonBody;
//        final String requestBody = jsonBody.toString();
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                response -> Log.i("Mahdi", "Hello: Mahdi: " + response),
//                error -> Log.e("Mahdi", "Hello: Mahdi: Error " + error)
//        ) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
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
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                    // can get more details such as response.headers
//                }
//                assert response != null;
//                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//            }
//        };
//        requestQueue.add(stringRequest);
    }


    private void getRequest() {
        RequestQueue queue = Volley.newRequestQueue(SignUpScreen.this);
        String url = "A url for getting the value";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.i("Mahdi", "Hello: Mahdi: " + response);
        }, error -> {
            Log.i("Mahdi", "Hello: Mahdi: Error " + error);
        });

        queue.add(stringRequest);
    }
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



