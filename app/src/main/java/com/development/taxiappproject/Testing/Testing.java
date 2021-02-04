package com.development.taxiappproject.Testing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.development.taxiappproject.HomeScreen;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.R;
import com.development.taxiappproject.Service.MyFirebaseMessagingService;
import com.development.taxiappproject.SignUpScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;
import static com.development.taxiappproject.OTPScreen.MyPREFERENCES;

public class Testing extends AppCompatActivity {
    private CircleImageView circleImageView;

    private final int PERMISSION_REQUEST_CODE_CAMERA = 1;
    private final int PERMISSION_REQUEST_CODE_GALLERY = 2;
    private Uri profileUri;
    private String TAG = "MAHDI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        circleImageView = findViewById(R.id.selectTestImage);
    }

    public String getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
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

    private boolean checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void selectImage(int countImage) {
        String cameraNum = 1 + "" + countImage;
        String galleryNum = 2 + "" + countImage;
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Testing.this);
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
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, Integer.parseInt(galleryNum));
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.selectTestImage:
                selectImage(1);
                break;

            case R.id.click_me:
                postData();
                break;
        }
    }

    private JSONObject postData() {

        String url = baseUrl + "/driver/signupFirebase";

        JSONObject jsonBody = new JSONObject();

        Log.i("MAHDI", "Hello: Mahdi: Data: 0 " + profileUri);
        Log.i("MAHDI", "Hello: Mahdi: Data: 1 " + getPath(profileUri));

        try {
            try {
                jsonBody.put("username", "Hello");
                jsonBody.put("email", "email");
                jsonBody.put("phone", "contactNumber");
                jsonBody.put("password", "password");
                jsonBody.put("carType", "carType");
                jsonBody.put("fcmToken", MyFirebaseMessagingService.getToken(getApplicationContext()));
                jsonBody.put("profilePhoto", getPath(profileUri));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MAHDI", "Hello: Mahdi: Data: error 0 ", e);
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
                Log.e("MAHDI", "Hello: Mahdi: Data: error 1 ", e);
            }
            jsonBody.put("documents", document.toString());

            Log.i("MAHDI", "Hello: Mahdi: Data: 2 " + jsonBody);
        } catch (JSONException e) {
            Log.e("MAHDI", "Hello: Mahdi: Data: error 2 ", e);
            e.printStackTrace();
        }


        final String requestBody = jsonBody.toString();

        RequestQueue requestQueue = Volley.newRequestQueue(Testing.this);
        String mURL;

        mURL = baseUrl + "/driver/signupFirebase";

        Log.i(TAG, "Mahdi: OTPScreen: signUp: 1 " + jsonBody);
//        Log.i(TAG, "Mahdi: OTPScreen: signUp: 2 " + idToken);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mURL,
                null,
                response -> {
                    try {
                        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

                        Log.i(TAG, "Mahdi: OTPScreen: signUp: res 0 " + response);
                        JSONObject data = response.getJSONObject("data");
                        JSONObject user = data.getJSONObject("user");
                        String userToken = data.getString("token");
                        String firebaseToken = data.getString("firebaseToken");

                        String userId = user.getString("_id");
                        String isOnline = user.getString("isOnline");

                        Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + userId);
                        Log.i(TAG, "Mahdi: OTPScreen: signUp: res 00 " + isOnline);

                        SharedPreferences.Editor editor = sharedpreferences.edit();

                        editor.putString(SharedPrefKey.firebaseToken, firebaseToken);
                        editor.putString(SharedPrefKey.userToken, userToken);
                        editor.putString(SharedPrefKey.userId, userId);
                        editor.putString(SharedPrefKey.isOnline, isOnline);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(getApplicationContext(), HomeScreen.class));
                }, error -> {
            Log.e("Mahdi", "Mahdi: OTPScreen: signUp: Error " + error.getMessage());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
//                params.put("firebaseToken", idToken);
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

        return jsonBody;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            profileUri = data.getData();
            if (requestCode == 11) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                circleImageView.setImageBitmap(photo);
            } else if (requestCode == 21) {
                assert data != null;
                circleImageView.setImageURI(data.getData());
            }
        }
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
        new AlertDialog.Builder(Testing.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
