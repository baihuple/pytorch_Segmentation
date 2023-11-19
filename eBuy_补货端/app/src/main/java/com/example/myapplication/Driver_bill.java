package com.example.myapplication;
public class Driver_bill {

    private String driverID;            // 司机 ID
    private String time;                // 司机预约时间
    private String product;             // 商品信息
    private String num;                 // 商品数量
    private String status;              // 订单状态

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDriverID() {
        return driverID;
    }

    public String getTime() {
        return time;
    }

    public String getProduct() {
        return product;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
