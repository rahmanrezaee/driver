package com.development.taxiappproject.Global;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.development.taxiappproject.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class GlobalVal {
    public static final String GOOGLE_MAP_API_KEY = "AIzaSyBY1nLDcGY1NNgV89rnDR8jg_eBsQBJ39E";

    public static String convertLatLng(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        return address;
    }

    public static ProgressDialog mProgressDialog(Context context, ProgressDialog progressDialog, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;
    }

    public static Bitmap destinationIcon() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(40, 40, conf);
        Canvas canvas1 = new Canvas(bmp);

        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.RED);

        canvas1.drawCircle(20f, 20f, 20f, color);
        return bmp;
    }

    public static Bitmap originIcon(Context context) {

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(40, 40, conf);
        Canvas canvas1 = new Canvas(bmp);

        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(context.getResources().getColor(R.color.colorAccent));

        canvas1.drawCircle(20f, 20f, 20f, color);
        return bmp;
    }

    public static BitmapDescriptor drawCircle(Context context) {
        //TODO draw circle
        int d = 500; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(context.getResources().getColor(R.color.colorAccent));
        p.setAlpha(128);
        c.drawCircle(d / 2, d / 2, d / 2, p);

        // generate BitmapDescriptor from circle Bitmap
        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
        return bmD;
    }
}
