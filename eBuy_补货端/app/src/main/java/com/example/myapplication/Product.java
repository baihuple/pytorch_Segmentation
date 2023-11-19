package com.example.myapplication;

public class Product {

    private int imgResId;                // 商品图片
    private String comname;             // 商品名称
    private String price;               // 商品价格

    private String number;               // 商品数量
    private String image_uri;            // 图像传输

    private String storeid;              // 补货站处商品id

    public String getStoreid() {
        return storeid;
    }

    public Product(){}

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public String getNumber() {
        return number;
    }

    public String getComname() {
        return comname;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Product(int imgResId, String comname, String price) {
        this.imgResId = imgResId;
        this.comname = comname;
        this.price = price;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public int getImgResId() {
        return imgResId;
    }

    public void setImgResId(int imgResId) {
        this.imgResId = imgResId;
    }

    public void setComname(String comname) {
        this.comname = comname;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

}
