package com.example.driverapp;

public class StationInfo {

    private String stationid;       // 站点 id
    private String stationname;     // 补货站名称
    private String stationaddr;     // 站点地址
    private String adminName;         // 站点管理员名字
    private String adminTel;        // 站点管理员电话
    private String stationstate;    // 站点状态

    private String stationla;       // 站点纬度
    private String stationlo;       // 站点经度

    public StationInfo(String stationid, String stationname, String stationaddr, String adminName, String adminTel, String stationstate, String stationla, String stationlo) {
        this.stationid = stationid;
        this.stationname = stationname;
        this.stationaddr = stationaddr;
        this.adminName = adminName;
        this.adminTel = adminTel;
        this.stationstate = stationstate;
        this.stationla = stationla;
        this.stationlo = stationlo;
    }

    public StationInfo(){}

    public void setStationla(String stationla) {
        this.stationla = stationla;
    }

    public void setStationlo(String stationlo) {
        this.stationlo = stationlo;
    }

    public String getStationla() {
        return stationla;
    }

    public String getStationlo() {
        return stationlo;
    }

    public String getStationid() {
        return stationid;
    }

    public void setStationid(String stationid) {
        this.stationid = stationid;
    }

    public void setStationname(String stationname) {
        this.stationname = stationname;
    }

    public void setStationaddr(String stationaddr) {
        this.stationaddr = stationaddr;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public void setAdminTel(String adminTel) {
        this.adminTel = adminTel;
    }

    public void setStationstate(String stationstate) {
        this.stationstate = stationstate;
    }

    public String getStationname() {
        return stationname;
    }

    public String getStationaddr() {
        return stationaddr;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getAdminTel() {
        return adminTel;
    }

    public String getStationstate() {
        return stationstate;
    }
}
