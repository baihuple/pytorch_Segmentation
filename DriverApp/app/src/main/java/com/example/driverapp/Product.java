package com.example.driverapp;

public class Product {
    private String comBrand;            // 商品类别

    private int imgResId;                // 商品图片
    private String comname;             // 商品名称
    private String price;               // 商品价格

    private String number;               // 商品数量
    private String image_uri;            // 图像传输

    private String storeid;              // 补货站处商品id

    private String comid;               // 商品编号

    private String real_num;            // 补货站端剩余商品数量

    public String getComBrand() {
        return comBrand;
    }

    public void setComBrand(String comBrand) {
        this.comBrand = comBrand;
    }

    public void setReal_num(String real_num) {
        this.real_num = real_num;
    }

    public String getReal_num() {
        return real_num;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public String getStoreid() {
        return storeid;
    }

    public String getComid() {
        return comid;
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
