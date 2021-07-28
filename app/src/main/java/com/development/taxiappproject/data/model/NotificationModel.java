package com.development.taxiappproject.data.model;

public class NotificationModel {

    private String id;
    private String title;
    private String body;
    private String date;
    private String rideId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public NotificationModel( String title, String body, String date, String rideId) {

        this.title = title;
        this.body = body;
        this.date = date;
        this.rideId = rideId;
    }
}
