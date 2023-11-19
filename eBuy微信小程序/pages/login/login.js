var app = getApp()
var ip = app.globalData.ip

Page({
    data: {
        "account": "",
        "password": ""
    },

    /* 账号输入 */
    inputAccount(e) {
        // console.log(e.detail.value)              输入内容保存在 e.detail.value 中
        this.setData({
            account: e.detail.value
        })
    },

    /* 密码输入 */
    inputPassword(e) {
        // console.log(e.detail.value)              输入内容保存在 e.detail.value 中
        this.setData({
            password: e.detail.value
        })
    },

    /* 跳转注册界面 */
    toSign() {
        wx.navigateTo({
            url: '../../about/pages/sign/sign',
        })
    },

    /* 登录函数 */
    login() {
        // 1) 检查两项数据是否为空
        var account = this.data.account
        var password = this.data.password
        if (account == "" || password == "") {
            wx.showToast({
                title: '请填写完整登录信息',
                icon: "none"
            })
            return
        } else {
            wx.request({
                url: 'http://' + ip + "/login",
                method: "POST",
                header: {
                    "identity": "2"
                },
                data: {
                    "account": account,
                    "password": password
                },
                success: res => {
                    console.log(res)
                    if (res.data.code == 200) {
                        // 存token
                        wx.setStorageSync('token', res.data.data)
                        // 弹提示
                        wx.showToast({
                            title: '登录成功',
                            icon: "success"
                        })
                        wx.switchTab({
                            url: '../../pages/about/about',
                        })
                    } else {
                        wx.showToast({
                            title: '登录失败',
                            icon: "error"
                        })
                    }
                }
            })
        }
    },
})