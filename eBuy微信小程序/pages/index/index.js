// pages/index/index.js
// 引入 typeData 分类检索模块
var myData = require("./sort_navigate/typeData.js")
var app = getApp()
var ip = app.globalData.ip
// console.log(myData)

Page({
    data: {
        swiperArr:[],           //空数组，保存对象类型数据
        current:1,              // 轮播图计数
        typeData:myData.list,    // 导航栏数组
        list:[],                  // 商品信息栏    
    },

    /* 轮播图切换计数函数 */
    swiperChange(e){
        // console.log(e.detail.current)
        var index = e.detail.current + 1;
        this.setData({
            current:index
        })
    },

    /* 点击加入购物车，只有已经绑定设备了才会加载列表。将其标志位 id 保存在缓存中,rr */
    addMall(e){
        console.log(e.currentTarget.dataset.item)
        var item = e.currentTarget.dataset.item
        console.log("该商品数量为:"+item.number)
        // 如果商品数量为 0
        if(item.number==0){
            wx.showToast({
              title: '商品已售光',
              icon:"error"
            })
            return
        }

        // 1) 获取是否有对应的缓存
        if(wx.getStorageSync('shoppingCar')){          // 有，获取数组，重新添加
            var shoppingCar = wx.getStorageSync('shoppingCar')
            for(var i=0;i<shoppingCar.length;i++){     // 避免重复添加商品
                if(shoppingCar[i].shelveid == item.shelveid){
                    wx.showToast({
                      title: '请勿重复添加商品',
                      icon:"none"
                    })
                    return
                }
            }
            // 不重复则添加商品
            item.purchase_num = 1
            shoppingCar[shoppingCar.length] = item
            wx.setStorageSync('shoppingCar', shoppingCar)
            wx.showToast({
                title: '添加成功',
              })

        }else{                                          // 无，创建缓存，进行添加
            var shoppingCar = []
            item.purchase_num = 1                 // 增加属性，购买数量为 1
            shoppingCar[0] = item
            wx.setStorageSync('shoppingCar', shoppingCar)
            wx.showToast({
              title: '添加成功',
            })
        }
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    onShow() {
        // 1）看是否绑定设备，没绑定设备则提示，并且不渲染列表
        if(wx.getStorageSync('equipNum')){
            // 2) 获取列表信息
            var arrayList = wx.getStorageSync('item_list')
            this.setData({
                list:arrayList,
                swiperArr:arrayList
            })
        }else{
            wx.showToast({
              title: '请先绑定设备',
              icon:"none"
            })
        }

    },

        // 设置下拉刷新事件，重新请求商品列表
        onPullDownRefresh() {
            // 重新请求数据
            wx.request({
                url: 'http://' + ip + "/passenger/comList",
                method: "GET",
                header: {
                    "Authorization": wx.getStorageSync('token'),
                    "identity": "2"
                },
                data: {
                    "deviceid": wx.getStorageSync('equipNum')
                },
                success: res => {
                    console.log(res)
                    if (res.data.code == 200) { // 成功获取商品列表
                        // 保存到缓存
                        wx.setStorageSync('item_list', res.data.data)
                        this.setData({
                            list:res.data.data
                        })

                        // 成功后，收起
                        wx.stopPullDownRefresh()
                    } else {
                        console.log("获取商品列表失败")
                    }
                }
            })
    
        }
})