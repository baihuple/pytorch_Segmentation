// pages/shop/shop.js
var app = getApp()
var ip = app.globalData.ip

Page({
    data: {
        startX: 0, // X 轴起始位置
        list: [], // 购物车中商品列表数据
        selectedNum: 0, // 选中的商品的数量
        selectedAll: false, // 全选按钮样式控制
        totalPrice: '0.00', // 总价格
        accountPrice: 0.00,
        isShow: false
    },

    /* Assist-1: 获取商品本地信息 */
    getshoppingCar() {
        if (wx.getStorageSync('shoppingCar')) {
            var list = wx.getStorageSync('shoppingCar')
            this.setData({
                list: list
            })
            // console.log(this.data.list)
        } else {
            wx.showToast({
                title: '您的购物车为空',
                icon: 'none'
            })
            this.setData({
                list: []
            })
        }
    },

    /* Assist-2: 计算选中商品价格 */
    getTotalPrice() {
        var list = this.data.list
        var totalPrice = 0
        // 有折扣，取出折扣，没有则为1
        var account = 1
        if (wx.getStorageSync('account')) {
            account = wx.getStorageSync('account')
        }

        for (var i = 0; i < list.length; i++) { // 遍历列表，找到所有被选中元素
            if (list[i].selected) {
                totalPrice += list[i].price * list[i].purchase_num
            }
        }

        var accountPrice = totalPrice * parseFloat(account)
        this.setData({
            totalPrice: totalPrice.toFixed(2),
            accountPrice: accountPrice.toFixed(2)
        })
    },

    /* 1）删除商品 */
    //1.1) 记录手滑动起始位置 [ 存储 X 到全局中 ]
    touchStart(e) {
        // console.log("手指触摸开始位置",e.changedTouches[0].clientX,e.changedTouches[0].clientY)
        this.setData({
            startX: e.changedTouches[0].clientX
        })
    },

    // 1.2）记录手滑动结束位置 [ 与之前存储的 X 做对比 ]        --- 展示版本
    touchMove(e) {
        // console.log(e.changedTouches[0].clientX)
        var list = this.data.list
        var index = e.currentTarget.dataset.index

        // 操作元素为 list[index], 为每个元素新增一个属性 isTouchMove
        list[index].isTouchMove = true
        var startX = this.data.startX
        var endX = e.changedTouches[0].clientX

        if (endX < startX) {
            list[index].isTouchMove = true
        } else {
            list[index].isTouchMove = false
        }

        this.setData({ // 回传数组对象，修改对象样式
            list: list
        })

    },

    // 1.3) 实现删除
    remove(e) {
        // console.log(e.currentTarget.dataset)
        var list = this.data.list
        var index = e.currentTarget.dataset.index
        console.log("这是：" + index)
        list.splice(index, 1) // 删除当前序号商品，并且回传到list中,清除指定缓存
        var totalPrice = this.getTotalPrice() // 重新计算价格,并且返回值

        var shoppingCar = wx.getStorageSync('shoppingCar')
        if (shoppingCar.length == 1) { // 购物车为1，清除该缓存
            wx.removeStorageSync('shoppingCar')
            this.setData({
                selectedNum: 0,
                selectedAll: false
            })
        } else {
            shoppingCar.splice(index, 1)
            wx.setStorageSync('shoppingCar', shoppingCar)
            this.setData({
                selectedNum: this.data.selectedNum - 1
            })
        }

        this.setData({
            list: list,
            totalPrice: totalPrice,
        })
    },

    /* 2) 选择与全选 */
    // 2.1) 选择指定项
    selectedList(e) {
        var list = this.data.list
        var index = e.currentTarget.dataset.index
        var num = this.data.selectedNum

        list[index].selected = !list[index].selected // 获取原来的状态取反
        if (list[index].selected) { // 统计选中数量
            num++
        } else {
            num--
        }
        // console.log(num,list.length)
        if (num == list.length) { // 全部选中激活全选按钮
            this.setData({
                selectedAll: true
            })
        } else {
            this.setData({
                selectedAll: false
            })
        }

        this.setData({ // 重新反馈到list中
            list: list,
            selectedNum: num
        })

        // 调用价格计算
        this.getTotalPrice()

        if (num > 0) { // 有一项被选中，结算高亮
            this.setData({
                isShow: true
            })
        } else {
            this.setData({
                isShow: false
            })
        }

    },

    // 2.2）全选按钮
    selectAll() {
        // 获取当前全选的取反状态
        var selectedAll = !this.data.selectedAll

        var list = this.data.list
        var num = this.data.selectedNum
        var isShow = this.data.isShow

        //  控制所有的 list 里面 selected 状态
        for (var i = 0; i < list.length; i++) {
            list[i].selected = selectedAll // 购物车列表选中的 == 当前全选状态
        }

        // 4、全选按钮选中的时候 num = 总长度,  取消 num 为 0   【非常重要，需要同步 num】
        //   结算样式同步修改
        if (selectedAll) {
            num = list.length
            isShow = true
        } else {
            num = 0
            isShow = false
        }

        this.setData({
            selectedAll: selectedAll,
            list: list,
            selectedNum: num,
            isShow: isShow
        })

        this.getTotalPrice() // 重新计算价格
    },

    /* 3) 增加、减少购物车商品数量 */
    // 3.1) 增加购物车商品数量
    addNum(e) {
        // console.log(e.currentTarget.dataset.item)
        var list = this.data.list
        var index = e.currentTarget.dataset.index
        var purchase_num = e.currentTarget.dataset.num
        var num = list[index].number
        if (purchase_num == num) { // 数量已经达到上线
            wx.showToast({
                title: '数量达到上限',
                icon: "none"
            })
            return
        }

        // 否则继续加
        purchase_num++
        list[index].purchase_num = purchase_num // 修改数据在 list 中
        this.setData({
            list: list
        })

        var shoppingCar = wx.getStorageSync('shoppingCar') // 获取购物车数据
        shoppingCar[index].purchase_num = purchase_num
        wx.setStorageSync('shoppingCar', shoppingCar)
        this.getTotalPrice()
    },

    // 3.2）减少购物车商品数量 [修改list，修改缓存指定数据的 num]
    reduceNum(e) {
        var list = this.data.list
        var index = e.currentTarget.dataset.index
        var purchase_num = e.currentTarget.dataset.num

        purchase_num-- // 数据判断
        if (purchase_num < 1) {
            wx.showToast({
                title: '数量最少为1',
                icon: "none"
            })
            return
        }

        list[index].purchase_num = purchase_num // 修改数据在 list 中
        this.setData({
            list: list
        })

        var shoppingCar = wx.getStorageSync('shoppingCar') // 获取购物车数据
        shoppingCar[index].purchase_num = purchase_num
        wx.setStorageSync('shoppingCar', shoppingCar)

        this.getTotalPrice()
    },

    /* 4) 结算调用支付，发送给后台 */
    calculate() {
        // 只计算选中的商品
        if (this.data.selectedNum == 0) {
            wx.showToast({
                title: '请先勾选商品',
                icon: "none"
            })
            return
        }

        // 获取总价格
        var price = this.data.totalPrice
        console.log("原价：" + price)

        // 获取折扣
        var account = 1
        if (wx.getStorageSync('account')) { // 有折扣
            account *= parseFloat(wx.getStorageSync('account'))
        }

        var pay_price = price * account
        console.log("现价：" + pay_price)

        // 遍历购物车，获取选中的商品, 发送付款订单
        var list = this.data.list
        for (var i = 0; i < list.length; i++) {
            if (list[i].selected) {
                // 发送网络请求
                wx.request({
                    url: 'http://' + ip + "/order/buy",
                    method: "POST",
                    header: {
                        "Authorization": wx.getStorageSync('token'),
                        "identity": "2"
                    },
                    data: {
                        "shelveid": list[i].shelveid,
                        "purchaserid": wx.getStorageSync('userInfo').passengerid,
                        "sellerid": wx.getStorageSync('driver_Info').driverid,
                        "purchaser": wx.getStorageSync('userInfo').name,
                        "seller": wx.getStorageSync('driver_Info').name,
                        "comid": list[i].comid,
                        "comname": list[i].comName,
                        "purchasenumber": list[i].purchase_num,
                        "sumprice": list[i].purchase_num * list[i].price * account,
                        "brand": list[i].comBrand
                    },
                    success: res => {
                        console.log("发送订单成功:", res)
                        if (res.data.code == 200) {
                            //console.log("订单交易成功")
                            // 清空购物车
                            wx.removeStorageSync('shoppingCar')
                            // 重新渲染页面
                            this.setData({
                                list: [],
                                totalPrice: 0.00,
                                selectedAll: false,
                                accountPrice: 0,
                                isShow: false
                            })
                        }
                    },
                    fail: res => {
                        console.log("订单交易失败")
                    }
                })
            }
        }

        // 重新请求数据，保存到缓存
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
                console.log("返回结果:", res)
                if (res.data.code == 200) { // 成功获取商品列表
                    // 保存到缓存
                    wx.setStorageSync('item_list', res.data.data)
                } else {
                    console.log("获取商品列表失败")
                }
            }
        })

        // 弹出二维码进行付款
        wx.navigateTo({
            url: '../../shop/pages/paycode/paycode?price=' + pay_price,
        })

    },

    onShow() {
        // 1) 先检查 userInfo,再获取购物车数据准备渲染
        if (wx.getStorageSync('equipNum')) { // 每次加载页面，设置为 0 选中，全选按钮未选中
            this.getshoppingCar()
            this.getTotalPrice()
            this.setData({
                selectedNum: 0,
                selectedAll: false
            })
            console.log(this.data.selectedNum)
        } else {
            wx.showToast({
                title: '请先绑定设备',
                icon: 'error'
            })
        }
    },


})