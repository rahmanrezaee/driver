package com.development.taxiappproject.model;

public class MyEarningClass {
    private String time;
    private String fare;
    private String miles;
    private String paidTo;

    public MyEarningClass(String time, String fare, String miles, String paidTo) {
        this.time = time;
        this.fare = fare;
        this.miles = miles;
        this.paidTo = paidTo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getMiles() {
        return miles;
    }

    public void setMiles(String miles) {
        this.miles = miles;
    }

    public String getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }
}
