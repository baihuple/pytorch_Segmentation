// index/pages/search/search.js
Page({
    /**
     * 页面的初始数据
     */
    data: {
        list:[]                     // 保存搜索到的商品信息
    },

    /* 获取搜索框中输入内容，并向后端请求数据 */
    searchInput(e){
        // console.log(e.detail)
        var search_content = e.detail.value         // 搜索内容
        console.log(search_content)

        if(search_content == ""){
            // 如果搜索栏为空，则清空搜索列表
            this.setData({
                list:[]
            })
            return
        }

        // 如果输入框不为空，进行搜索
        // 在缓存中进行搜索
        var list = wx.getStorageSync('item_list')
        var search_result = []
        for(var i=0;i<list.length;i++){
            if(list[i].comName.indexOf(search_content)!=-1){
                search_result[search_result.length] = list[i]
            }
        }
        console.log(search_result)
        this.setData({
            list:search_result
        })
    },

        /* 点击加入购物车，只有已经绑定设备了才会加载列表。将其标志位 id 保存在缓存中,rr */
        addMall(e){
            console.log(e.currentTarget.dataset.item)
            var item = e.currentTarget.dataset.item
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