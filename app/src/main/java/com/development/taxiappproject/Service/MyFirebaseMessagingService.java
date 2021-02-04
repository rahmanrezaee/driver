package com.development.taxiappproject.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.development.taxiappproject.NewRideRequest;
import com.development.taxiappproject.OTPScreen;
import com.development.taxiappproject.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static String fcmToken = "gettingFcmToken";
    public static final String TAG = "MAHDI";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "Mahdi: onNewToken: " + s);
        getSharedPreferences(OTPScreen.MyPREFERENCES, MODE_PRIVATE).edit().putString(fcmToken, s).apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(OTPScreen.MyPREFERENCES, MODE_PRIVATE).getString(fcmToken, "defaultValue");
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, "Mahdi: onMessageReceived: 1 " + remoteMessage.getNotification().getBody());
        Log.i(TAG, "Mahdi: onMessageReceived: 2 " + remoteMessage.getNotification().getChannelId());
        Log.i(TAG, "Mahdi: onMessageReceived: 3 " + remoteMessage.getNotification().getTitle());
        Log.i(TAG, "Mahdi: onMessageReceived: 4 " + remoteMessage.getData().get("click_action"));
        Log.i(TAG, "Mahdi: onMessageReceived: 5 " + remoteMessage.getNotification().getClickAction());
        String click_action = remoteMessage.getData().get("click_action");
        Intent intent1 = new Intent(click_action);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, intent1, PendingIntent.FLAG_ONE_SHOT);
//        try {
//
//
////            sendNotification("Hello", click_action);
//
//            Log.i(TAG, "Mahdi: onMessageReceived: try ");
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyFirebaseMessagingService.this, "");
//
////            Intent intent = new Intent(MyFirebaseMessagingService.this, NewRideRequest.class);
//            PendingIntent contentIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this,
//                    0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            String sticky = String.valueOf(remoteMessage.getNotification().getSticky());
//
//            builder.setAutoCancel(true)
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setWhen(0)
//                    .setSmallIcon(R.drawable.car_image)
//                    .setTicker(sticky)
//                    .setContentTitle(remoteMessage.getNotification().getTitle())
//                    .setContentText(remoteMessage.getNotification().getBody())
//                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
//                    .setContentIntent(contentIntent)
//                    .setContentInfo("Info");
//
//            NotificationManager notificationManager = (NotificationManager) MyFirebaseMessagingService.this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                builder.setChannelId("com.development.taxiappproject");
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(
//                        "com.development.taxiappproject",
//                        "TaxiAppProject",
//                        NotificationManager.IMPORTANCE_DEFAULT
//                );
//                if (notificationManager != null) {
//                    notificationManager.createNotificationChannel(channel);
//                }
//            }
//            notificationManager.notify(1, builder.build());
//
////            notificationManager.notify(1, b.build());
//
//        } catch (Exception e) {
//            Log.e(TAG, "Mahdi: onMessageReceived: error: ", e);
//        }
//
////        Notification notification = new NotificationCompat.Builder(this, "All")
////                .setContentTitle(remoteMessage.getNotification().getTitle())
////                .setContentText(remoteMessage.getNotification().getBody())
////                .setSmallIcon(R.mipmap.ic_launcher)
////                .build();
////
////        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
////        manager.notify(123, notification);
//    }
//
//    private void sendNotification(String msg) {
//        NotificationManager mNotificationManager;
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        long notificatioId = System.currentTimeMillis();
//
//        Intent intent = new Intent(MyFirebaseMessagingService.this, NewRideRequest.class); // Here pass your activity where you want to redirect.
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), intent, 0);
//
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            currentapiVersion = R.drawable.car_image;
//        } else {
//            currentapiVersion = R.mipmap.ic_launcher;
//        }
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(currentapiVersion)
//                .setContentTitle(this.getResources().getString(R.string.app_name))
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//                .setContentText(msg)
//                .setAutoCancel(true)
//                .setPriority(Notification.PRIORITY_HIGH)
//                .setDefaults(Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
//                .setContentIntent(contentIntent);
//        mNotificationManager.notify((int) notificatioId, notificationBuilder.build());
    }
}
