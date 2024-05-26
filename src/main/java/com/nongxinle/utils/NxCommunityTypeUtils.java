package com.nongxinle.utils;


/**
 * 商品类型
 */
public class NxCommunityTypeUtils {


    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_ALL_TIME = 1;
    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_PART_TIME = 2;
    public final static Integer Nx_COMMUNITY_GOODS_SELL_TYPE_COUPON = 3;

    public final static Integer NX_COMMUNITY_ORDER_STATUS_NEW = 0;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_STATUS_TO_PAY = 1;  //提交微信支付
    public final static Integer NX_COMMUNITY_ORDER_STATUS_PAYED = 2;  //微信支付成功
    public final static Integer NX_COMMUNITY_ORDER_STATUS_LABELS_PRINT = 3;  // 标签打印完成已打印
    public final static Integer NX_COMMUNITY_ORDER_STATUS_BILL_PRINT = 4;  // 总单已打印
    public final static Integer NX_COMMUNITY_ORDER_STATUS_ORDER_FINISH = 5;  //顾客完成

    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_SAVE = -1;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_ORDER_SAVE = 0;  //新订单
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_PAYED = 1; //微信支付成功
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_ANDROID_PRINTED = 2;  //打印Label
    public final static Integer NX_COMMUNITY_ORDER_SUB_STATUS_BILL_PRINT = 3;// 总单已打印
    public final static Integer NX_COMMUNITY_ORDER_sUB_STATUS_ORDER_FINISH = 4;// 顾客完成

}
