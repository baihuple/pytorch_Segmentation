package com.example.driverapp;

public class SellProdudct {

    String pro_title;       // 商品名称
    String pro_num;         // 商品价格
    String date;            // 交易时间
    String sumprice;        // 消费总价格

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    String orderid;         // 交易id
    String imageURI;        // 图片路径

    public String getPro_title() {
        return pro_title;
    }

    public String getPro_num() {
        return pro_num;
    }

    public String getDate() {
        return date;
    }

    public String getSumprice() {
        return sumprice;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setPro_title(String pro_title) {
        this.pro_title = pro_title;
    }

    public void setPro_num(String pro_num) {
        this.pro_num = pro_num;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSumprice(String sumprice) {
        this.sumprice = sumprice;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }
}
