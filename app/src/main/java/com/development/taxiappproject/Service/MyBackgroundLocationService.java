package com.development.taxiappproject.Service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.OTPScreen;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;

public class MyBackgroundLocationService extends Service implements LocationListener {
    boolean isGPSEnable = false;
    private String TAG = "MAHDI";
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    public static String str_receiver = "com.development.taxiappproject.Service.receiver";
    Intent intent;
    private Socket mSocket;
    SharedPreferences sharedPreferences;

    public MyBackgroundLocationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public void socketConnection() {
        try {
            Log.i(TAG, "Mahdi: BackgroundService: socket 1 ");
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket(socketBaseUrl, options);

            Log.i(TAG, "Mahdi: BackgroundService: socket 2 ");
        } catch (URISyntaxException e) {
            Log.e(TAG, "Mahdi: BackgroundService: socket error ", e);
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Mahdi: BackgroundService: connected 1");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "onCreate: 1 ", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        socketConnection();

        fn_getLocation();

        mSocket.on(Socket.EVENT_CONNECT, onConnect);

        mSocket.connect();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "onCreate: 2 ", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Toast.makeText(getApplicationContext(), "onCreate: 3 ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        intent = new Intent(str_receiver);
        fn_getLocation();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent intent = new Intent("com.development.taxiappproject.Service");
        sendBroadcast(intent);
    }

    //        {driver: id, lat: lat, lng: lng}
//        {MyLocation: }
    private void sendDriverData(String lat, String lng, String status) {
        String userId = sharedPreferences.getString(SharedPrefKey.userId, "defaultValue");

        if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng) || TextUtils.isEmpty(status)) {
            return;
        }
        JSONObject userStatus = new JSONObject();

        try {
            userStatus.put("driver", userId);
            userStatus.put("lat", lat);
            userStatus.put("lng", lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "sendDriverData: " + userStatus);

        mSocket.emit("MyLocation", userStatus);

//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            jsonObject.put("driver", lat);
//            jsonObject.put("longitude", lng);
//            jsonObject.put("status", status);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        {driver: id, status: false/true}
    }

    private void fn_getLocation() {
        if (!isGPSEnable && !isNetworkEnable) {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        } else {
            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, (android.location.LocationListener) this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                        sendDriverData(location.getLatitude() + "", location.getLongitude() + "", "online");
                    }
                }
            }

            if (isGPSEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, (android.location.LocationListener) this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                        sendDriverData(location.getLatitude() + "", location.getLongitude() + "", "online");
                    }
                }
            }
        }
    }

    private void fn_update(Location location) {
        intent.putExtra("latutide", location.getLatitude() + "");
        intent.putExtra("longitude", location.getLongitude() + "");
        sendBroadcast(intent);
    }
}
