package com.development.taxiappproject.model;

public class MyRideClass {
    private String dateRide;
    private String priceRide;
    private String distanceRide;
    private String timeRide;
    private String startLocationRide;
    private String endLocationRide;

    public MyRideClass(String dateRide, String priceRide, String distanceRide, String timeRide, String startLocationRide, String endLocationRide) {
        this.dateRide = dateRide;
        this.priceRide = priceRide;
        this.distanceRide = distanceRide;
        this.timeRide = timeRide;
        this.startLocationRide = startLocationRide;
        this.endLocationRide = endLocationRide;
    }

    public String getDateRide() {
        return dateRide;
    }

    public void setDateRide(String dateRide) {
        this.dateRide = dateRide;
    }

    public String getEndLocationRide() {
        return endLocationRide;
    }

    public void setEndLocationRide(String endLocationRide) {
        this.endLocationRide = endLocationRide;
    }

    public String getStartLocationRide() {
        return startLocationRide;
    }

    public void setStartLocationRide(String startLocationRide) {
        this.startLocationRide = startLocationRide;
    }

    public String getTimeRide() {
        return timeRide;
    }

    public void setTimeRide(String timeRide) {
        this.timeRide = timeRide;
    }

    public String getDistanceRide() {
        return distanceRide;
    }

    public void setDistanceRide(String distanceRide) {
        this.distanceRide = distanceRide;
    }

    public String getPriceRide() {
        return priceRide;
    }

    public void setPriceRide(String priceRide) {
        this.priceRide = priceRide;
    }
}
