var app = getApp()
var ip = app.globalData.ip

Page({

    data: {
        equipNum:""
    },

    inputEquipnum(e){
        // console.log(e.detail.value)
        this.setData({
            equipNum:e.detail.value
        })
    },

    /* 检查数据，并将设备号保存到本地 */
    keep(){
        if(this.data.equipNum){
            wx.setStorageSync('equipNum', this.data.equipNum)
            wx.showToast({
                title: '保存成功',
            })
            // 请求本货柜商品信息
            wx.request({
              url: 'http://' + ip + "/passenger/comList",
              method:"GET",
              header:{
                  "Authorization": wx.getStorageSync('token'),
                  "identity":"2"
              },
              data:{
                  "deviceid":this.data.equipNum
              },
              success:res=>{
                  //console.log(res)
                  if(res.data.code == 200){ // 成功获取商品列表
                    // 保存到缓存
                    wx.setStorageSync('item_list', res.data.data)
                  }else{
                      console.log("获取商品列表失败")
                  }
              }
            })

            // 跳转到我的页面
            wx.navigateBack()
        }else{
            wx.showToast({
              title: '请填写后再保存',
              icon:"none"
            })
        }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        wx.scanCode({
            success:(res)=>{
                // console.log(res.result)
                this.setData({
                    equipNum: res.result
                })
            }
        })
    },
})