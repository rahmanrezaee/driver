package com.development.taxiappproject.Const;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class SharedPrefKey {
    public static String firebaseToken = "firebaseToken";
    public static String userToken = "userToken";
    public static String userId = "userId";
    public static String isOnline = "isOnline";
    public static String userName = "userName";
    public static String profilePath = "profilePath";
    public static String expireDate = "";


    public static String addHoursToJavaUtilDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 24);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String strDate = dateFormat.format(calendar.getTime());
        return strDate;
    }

    public static Date convertStringToDate(String date){

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
         return  dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }

    public static boolean isAfterNow(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        Log.i("Time", "current time: "+calendar.getTime().toString());
        Log.i("Time", "sharepre: "+d.toString());

        if (calendar.getTime().before(d)){
            return  true;
        }else {
            return  false;
        }


    }
}
