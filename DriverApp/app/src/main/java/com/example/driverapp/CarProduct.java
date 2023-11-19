package com.example.driverapp;

public class CarProduct {

    String shelveid;            // 货架编号
    String price;               // 商品价格
    String comName;             // 商品名称
    String number;              // 商品数量
    String imageURL;            // 商品图片地址
    String sellstate;           // 商品售卖状态

    public void setShelveid(String shelveid) {
        this.shelveid = shelveid;
    }

    public String getShelveid() {
        return shelveid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setSellstate(String sellstate) {
        this.sellstate = sellstate;
    }

    public String getComName() {
        return comName;
    }

    public String getNumber() {
        return number;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getSellstate() {
        return sellstate;
    }
}
