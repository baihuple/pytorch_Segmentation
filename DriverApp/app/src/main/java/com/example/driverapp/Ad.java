package com.example.driverapp;

public class Ad {
    String brand;       // 广告类别
    String type;        // 广告类型
    String id;          // 广告 id
    String price;       // 广告价格

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }
}
