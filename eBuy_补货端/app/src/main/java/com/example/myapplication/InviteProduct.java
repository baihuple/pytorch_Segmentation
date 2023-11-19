package com.example.myapplication;

// 邀请商品类
public class InviteProduct {

    private String invite_name;             // 邀约商品名称
    private String invite_price;            // 商品价格
    private String invite_num;              // 商品数量

    private String storeid;                 // 商品标识

    public InviteProduct(){}

    public String getStoreid() {
        return storeid;
    }

    public InviteProduct(String invite_name, String invite_price, String invite_num) {
        this.invite_name = invite_name;
        this.invite_price = invite_price;
        this.invite_num = invite_num;
    }

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public String getInvite_name() {
        return invite_name;
    }

    public String getInvite_price() {
        return invite_price;
    }

    public void setInvite_name(String invite_name) {
        this.invite_name = invite_name;
    }

    public void setInvite_price(String invite_price) {
        this.invite_price = invite_price;
    }

    public void setInvite_num(String invite_num) {
        this.invite_num = invite_num;
    }

    public String getInvite_num() {
        return invite_num;
    }
}
