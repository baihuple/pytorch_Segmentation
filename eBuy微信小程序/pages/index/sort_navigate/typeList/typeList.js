// pages/index/sort_navigate/typeList/typeList.js
Page({

    /**
     * 页面的初始数据
     */
    data: {
        list:[]
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        // console.log(options.id)                  0-饮品、1-食物、2-日用、3-医疗
        // 向后台请求数据
        // wx.request({
        //   url: 'http://xxx/xxx/xxx',
        //   data:{
        //       type:options.id                       // 用商品的 type 在后台进行搜索
        //   },
        //   success:(res)=>{
        //     //   console.log("商品:"+res.data)
        //     if(res.data.status==200){
        //         this.setData({
        //             list:res.data.data,          // 获取返回的商品列表数据
        //             openid:wx.getStorageSync('userInfo').openid
        //         })
        //     }else{
        //         console.log("分类商品数据请求失败")
        //     }
        //   },
        //   fail:()=>{
        //       console.log("数据请求失败")
        //   }
        // })
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {

    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide() {

    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    }
})