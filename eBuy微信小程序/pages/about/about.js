// 个人信息界面
var app = getApp()
var ip = app.globalData.ip

Page({
    data: {
        nickname: "微信用户",
        desc: "未登录",
        account: "",
        name: "",
        phone: "",
        id: "",
        logbuttonHide: false,
        jfHide: true,
        equipNum: "", // 记录设备编号
        anchor_hide: true
    },

    /* 扫码优惠 */
    scan_account(){
        wx.scanCode({
            success:res=>{
                // console.log(res.result)
                wx.setStorageSync('account', res.result)
                wx.showToast({
                  title: '获得'+ parseFloat(res.result)*10 + "折优惠！",
                  icon:"none",
                })
            }
        })
    },

    /* 退出登陆 */
    toLogout() {
        // 1) 清除登陆状态
        this.setData({
            name: "",
            phone: "",
            id: "",
            nickname: "微信用户",
            logbuttonHide: false,
            desc: "未登录",
            jfHide: true,
            equipNum: "",
            anchor_hide: true
        })

        // 2) 清除缓存 ( 个人信息 + 设备号 )
        wx.removeStorageSync('token')
        wx.removeStorageSync('userInfo')
        wx.removeStorageSync('equipNum')
        wx.removeStorageSync('item_list')
        wx.removeStorageSync('shoppingCar')
        wx.removeStorageSync('account')
        wx.removeStorageSync('driver_Info')

        // 3) 跳转登录页面，用 wx.reLaunch：关闭所有页面，跳转到某个页面
        wx.reLaunch({
            url: "../../pages/login/login",
        })
    },

    /*跳转填写设备编号 */
    equipNum() {
        if (wx.getStorageSync('token')) {
            wx.navigateTo({
                url: '../../about/pages/equipnum/equipnum',
            })
        } else {
            wx.showToast({
                title: '请先登录再操作',
                icon: "error"
            })
        }
    },

    // 加载页面的时候，请求个人信息
    onLoad(options) {

    },

    onReady() {

    },

    onShow() {
        // 首先获取 token 字段，如果有则请求身份信息
        var token = wx.getStorageSync('token')
        // console.log("加载获取token" + token)
        if (token) {
            // 请求用户身份信息
            wx.request({
                url: 'http://' + ip + "/passenger/ownInfo",
                method: "GET",
                header: {
                    "Authorization": token,
                    "identity": "2"
                },
                success: res2 => {
                    if (res2.data.code == 200) {
                        //console.log("成功获取个人信息")
                        wx.setStorageSync('userInfo', res2.data.data)
                        // 加载页面时检查本地存储是否有个人信息，有的话直接填写；
                        var userInfo = wx.getStorageSync('userInfo')
                        this.setData({
                            name: userInfo.name,
                            phone: userInfo.tel,
                            id: userInfo.passengerid,
                            account: userInfo.level,
                            logbuttonHide: true,
                            desc: "已登录",
                            jfHide: false
                        })
                    } else {
                        wx.showToast({
                            title: '获取个人信息失败',
                            icon: "error"
                        })
                    }
                },
                fail: res2 => {
                    console.log("获取个人信息失败")
                }
            })

            var equipNum = wx.getStorageSync('equipNum')
            if (equipNum) {
                this.setData({
                    equipNum: equipNum,
                    anchor_hide: false
                })
                // 获取设备所绑定的司机信息
                wx.request({
                    url: "http://" + ip + "/passenger/getDriverInfo",
                    method: "GET",
                    header: {
                        "Authorization": token,
                        "identity": 2
                    },
                    data: {
                        "deviceid":equipNum
                    },
                    success:res=>{
                        // console.log(res)
                        if(res.data.code == 200){
                            wx.setStorageSync('driver_Info', res.data.data)
                        }else{
                            console.log("获取司机信息失败")
                        }
                    }
                })

            }

        }
    },
})