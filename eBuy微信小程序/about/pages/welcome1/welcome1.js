// about/pages/welcome1/welcome1.js
Page({

    /**
     * 页面的初始数据
     */
    data: {

    },

    next_step(){
        wx.navigateTo({
          url: '../welcome2/welcome2',
        })
    },

    last_step(){
        // 确认返回登陆界面
        wx.showModal({
          title: '提示',
          content: '您即将返回到登陆界面',
          complete: (res) => {
            if (res.cancel) {
            }
            if (res.confirm) {
              wx.reLaunch({
                url: '../../../pages/login/login',
              })
            }
          }
        })
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {

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