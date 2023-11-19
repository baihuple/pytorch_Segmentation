package com.example.driverapp;

public class Order {
    String stationname;         // 捕获站名称
    String comname;             // 商品名称
    String num;                 // 商品数量
    String time;                // 交易时间
    String orderid;             // 交易订单号

    String orderimage;          // 商品图片

    public String getOrderimage() {
        return orderimage;
    }

    public void setOrderimage(String orderimage) {
        this.orderimage = orderimage;
    }

    public String getSumPrice() {
        return sumPrice;
    }

    String sumPrice;            // 总价

    public void setSumPrice(String sumPrice) {
        this.sumPrice = sumPrice;
    }

    public String getStationname() {
        return stationname;
    }

    public void setStationname(String stationname) {
        this.stationname = stationname;
    }

    public void setComname(String comname) {
        this.comname = comname;
    }


    public String getComname() {
        return comname;
    }

    public String getNum() {
        return num;
    }

    public String getTime() {
        return time;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }
}
