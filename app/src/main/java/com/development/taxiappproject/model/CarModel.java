package com.development.taxiappproject.model;

public class CarModel {

    private String id;
    private String cartName;

    private String urlIcon;


    public CarModel(String id, String cartName, String urlIcon) {
        this.id = id;
        this.cartName = cartName;
        this.urlIcon = urlIcon;
    }

    public CarModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCartName() {
        return cartName;
    }

    public void setCartName(String cartName) {
        this.cartName = cartName;
    }

    public String getUrlIcon() {
        return urlIcon;
    }

    public void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }
}