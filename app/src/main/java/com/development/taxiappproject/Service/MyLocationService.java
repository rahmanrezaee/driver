//package com.development.taxiappproject.Service;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.location.Location;
//import android.os.IBinder;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.development.taxiappproject.Testing.Testing;
//import com.google.android.gms.location.LocationResult;
//
//public class MyLocationService extends BroadcastReceiver {
//    private final String TAG = "MYLOCATIONSERVICE";
//    public static final String ACTION_PROGRESS_UPDATE = "com.development.taxiappproject.Service.UPDATE_LOCATION";
//
//    public MyLocationService() {
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Log.i(TAG, "Hello Mahdi: onReceive: 1 " + intent.getAction());
//        Log.i(TAG, "Hello Mahdi: onReceive: 2 " + ACTION_PROGRESS_UPDATE);
//        Log.i(TAG, "Hello Mahdi: onReceive: 3 " + intent);
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_PROGRESS_UPDATE.equals(action)) {
//                Log.i(TAG, "Hello Mahdi: onReceive: 0");
//                LocationResult result = LocationResult.extractResult(intent);
//                if (result != null) {
//                    Location location = result.getLastLocation();
//                    String location_string = new StringBuilder(location.getLatitude() + "")
//                            .append("/")
//                            .append(location.getLongitude())
//                            .toString();
//                    try {
//                        Testing.getInstance().updateTextView(location_string);
//                        Toast.makeText(context, location_string, Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        Log.e(TAG, "onReceive: ", e);
//                    }
//                }
//            }
//        }
//    }
//}
