package com.development.taxiappproject.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.development.taxiappproject.Const.SharedPrefKey;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.Restarter;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import static com.development.taxiappproject.Const.ConstantValue.socketBaseUrl;

public class TestService extends Service implements LocationListener {
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

    public int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);
        socketConnection();

        fn_getLocation();

        mSocket.connect();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) this);

        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        sharedPreferences = getSharedPreferences(OTPScreen.MyPREFERENCES, Context.MODE_PRIVATE);

        String switchFlag = sharedPreferences.getString("isOnline", "null");
        boolean switchFlagBool = switchFlag.equalsIgnoreCase("null") ? false : true;

        Log.i(TAG, "Mahdi: TestService onDestroy: " + switchFlag);

        mSocket.disconnect();

//        if (switchFlagBool) {
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("restartservice");
//            broadcastIntent.setClass(this, Restarter.class);
//            this.sendBroadcast(broadcastIntent);
//        } else {
//            mSocket.disconnect();
//        }
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  " + (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        intent = new Intent(str_receiver);
        fn_getLocation();
    }

}
