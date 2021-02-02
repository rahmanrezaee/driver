package com.development.taxiappproject.model;

public class MyRateCardClass {
    private String zip;
    private String rate;

    public MyRateCardClass(String zip, String rate) {
        this.zip = zip;
        this.rate = rate;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
