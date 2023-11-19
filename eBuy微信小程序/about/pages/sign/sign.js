// about/pages/sign.js
var app = getApp()
var ip = app.globalData.ip

Page({
    data: {
        "name":"",
        "phone":"",
        "password":"",
    },

    /* 姓名输入 */
    inputName(e){
        // console.log(e.detail.value)              输入内容保存在 e.detail.value 中
        this.setData({
            name:e.detail.value
        })
    },

    /* 电话输入 */
    inputPhone(e){
        // console.log(e.detail.value)              输入内容保存在 e.detail.value 中
        this.setData({
            phone:e.detail.value
        })
    },

    /* 密码输入 */
    inputPassword(e){
        this.setData({
            password:e.detail.value
        })
    },

    /* 注册事件,注册，缓存对应的信息 */
    sign(){
        // 1) 检查四项数据是否为空
        var name = this.data.name
        var phone = this.data.phone
        var password = this.data.password
        if(name=="" || phone=="" || password==""){
            wx.showToast({
              title: '请完善所有信息后再注册',
              icon:"none"
            })
            return
        }else if(phone.length != 11){
            wx.showToast({
              title: '请输入正确的手机号',
              icon:"none"
            })
        }else{
            // 发送请求，开始注册
            wx.request({
              url: 'http://' + ip + "/passenger/register",
              method:"POST",
              header:{
                  "identity":"2"
              },
              data:{
                  "name":name,
                  "tel":phone,
                  "password":password
              },
              success:res=>{
                //   console.log(res)
                // 保存 token，并且跳转到主界面
                if(res.data.code == 200){

                    // 保存 token，跳转到欢迎界面
                    wx.setStorageSync('token', res.data.data)

                    // 提问：是否进入新手引导？
                    wx.showModal({
                      title: '提示',
                      content: '是否进入新手引导？',
                      complete: (res) => {
                        if (res.cancel) {           // 取消：直接登录
                        wx.switchTab({
                          url: '../../../pages/about/about',
                        })
                        }
                        if (res.confirm) {          // 确认：跳转引导界面
                            wx.reLaunch({
                              url: '../../../about/pages/welcome1/welcome1',
                            })
                        }
                      }
                    })
                
                }else{
                    var message = res.data.message
                    wx.showToast({
                      title: message,
                      icon:"error"
                    })
                }
              }
            })

        }
    },

})