//package com.development.taxiappproject.Retrofit;
//
//import com.google.gson.annotations.SerializedName;
//
//import org.json.JSONObject;
//
//public class ServerResponse {
//    // variable name should be same as in the json response from php
//    @SerializedName("status")
//    boolean status;
//
//    @SerializedName("message")
//    String message;
//
//    @SerializedName("data")
//    JSONObject data;
//
//    public String getMessage() {
//        return message;
//    }
//    public boolean getSuccess() {
//        return status;
//    }
//
//    public JSONObject getData(){
//        return data;
//    }
//}