package com.development.taxiappproject.Retrofit;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.development.taxiappproject.Const.ConstantValue.baseUrl;

public class RetrofitClient {
    private static Retrofit ourInstance;
    private static final String RetBaseUrl = baseUrl + "/";

    public static Retrofit getInstance() {
        if (ourInstance == null) {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .writeTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .protocols(Util.immutableList(Protocol.HTTP_1_1))
                    .build();
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .connectTimeout(100, TimeUnit.SECONDS)
//                    .writeTimeout(100, TimeUnit.SECONDS)
//                    .protocols(Util.immutableList(Protocol.HTTP_1_1))
//                    .readTimeout(100, TimeUnit.SECONDS).build();

            ourInstance = new Retrofit.Builder()
                    .baseUrl(RetBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return ourInstance;
    }
    private RetrofitClient() {
    }
}
