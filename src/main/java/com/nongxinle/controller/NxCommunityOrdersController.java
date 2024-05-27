package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generatePickNumber;
import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxorders")
public class NxCommunityOrdersController {
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCustomerService nxCustomerService;
    @Autowired
    private NxCommunitySplicingOrdersService nxCommunitySplicingOrdersService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;


    private static final WebSocketEndPoint webSocketEndPoint = new WebSocketEndPoint();

    @RequestMapping(value = "/changeServiceTime/{id}")
    @ResponseBody
    public R changeServiceTime(@PathVariable Integer id) {
        System.out.println("dchhchhc");
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(id);
        nxCommunityOrdersEntity.setNxCoService("0");
        nxCommunityOrdersEntity.setNxCoServiceDate(formatWhatDay(0));
        nxCommunityOrdersEntity.setNxCoServiceHour(formatWhatHour(0));
        nxCommunityOrdersEntity.setNxCoServiceMinute(formatWhatMinute(0));
        BigDecimal multiply = new BigDecimal(nxCommunityOrdersEntity.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxCommunityOrdersEntity.getNxCoServiceMinute()));
        nxCommunityOrdersEntity.setNxCoServiceTime(add.toString());
        nxCommunityOrdersService.update(nxCommunityOrdersEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosService("0");
                subEntity.setNxCosServiceDate(formatWhatDate(0));
                subEntity.setNxCosServiceTime(add.toString());
                nxCommunityOrdersSubService.update(subEntity);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/getStatusCommOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getStatusCommOrder(Integer commId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", status);
        System.out.println("ststmap" + map);
        List<NxCommunityOrdersEntity> nxCommunityOrdersEntities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", nxCommunityOrdersEntities);
    }

    @RequestMapping(value = "/rePrintOrder", method = RequestMethod.POST)
    @ResponseBody
    public R rePrintOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCommunityOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);

        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosStatus(1);
                nxCommunityOrdersSubService.update(subEntity);
            }
        }
        orders.setNxCoStatus(2);
        nxCommunityOrdersService.update(orders);
        return R.ok();
    }

    //
    @RequestMapping(value = "/customerFetchOrder", method = RequestMethod.POST)
    @ResponseBody
    public R customerFetchOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCommunityOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosStatus(4);
                nxCommunityOrdersSubService.update(subEntity);
                if (subEntity.getNxCosCucId() != null) {
                    NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                    customerUserCouponEntity.setNxCucStatus(3);
                    nxCustomerUserCouponService.update(customerUserCouponEntity);
                }
            }
        }
        orders.setNxCoStatus(5);
        nxCommunityOrdersService.update(orders);
        if (orders.getNxCoBuyMemberCardTime() == 1) {
            Map<String, Object> mapC = new HashMap<>();
            mapC.put("orderId", orders.getNxCommunityOrdersId());
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
            if (cardEntities.size() > 0) {
                for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                    userCardEntity.setNxCucaStatus(0);
                    userCardEntity.setNxCucaComOrderId(orders.getNxCommunityOrdersId());
                    nxCustomerUserCardService.update(userCardEntity);
                }
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/printWholeOrder", method = RequestMethod.POST)
    @ResponseBody
    public R printWholeOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCommunityOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                subEntity.setNxCosStatus(3);
                nxCommunityOrdersSubService.update(subEntity);
            }
        }
        orders.setNxCoStatus(4);
        nxCommunityOrdersService.update(orders);
        return R.ok();
    }


    @RequestMapping(value = "/getCommOrder/{comId}")
    @ResponseBody
    public R getCommOrder(@PathVariable String comId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);
        map.put("xiaoyuStatus", 3);
        map.put("type", 0);
        System.out.println("cociicigetororororoor" + map);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", entities);
    }


    @RequestMapping(value = "/delPindan/{id}")
    @ResponseBody
    public R delPindan(@PathVariable Integer id) {
        nxCommunityOrdersService.delete(id);
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        System.out.println("iddiididid" + map);
        List<NxCommunitySplicingOrdersEntity> splicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
        if (splicingOrdersEntities.size() > 0) {
            for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : splicingOrdersEntities) {
                nxCommunitySplicingOrdersService.delete(splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                Map<String, Object> mapSub = new HashMap<>();
                mapSub.put("orderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
                if (subEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                        nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());
                    }
                }
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("status", -1);
                mapC.put("type", 1);
                mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                if (cardEntities.size() > 0) {
                    for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                        nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                    }
                }
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/getMyPindanDetailOnly", method = RequestMethod.POST)
    @ResponseBody
    public R getMyPindanDetailOnly(Integer pindanId, Integer orderUserId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", pindanId);
        System.out.println("pindfiaifajfamamma" + map);
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        if (nxCommunityOrdersEntity != null) {
            Map<String, Object> mapO = new HashMap<>();
            mapO.put("id", pindanId);
            mapO.put("orderUserId", orderUserId);
            System.out.println("spsoosmappa" + mapO);
            NxCommunitySplicingOrdersEntity splicingOrders = nxCommunitySplicingOrdersService.queryNewPindan(mapO);
            NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orderUserId);
            splicingOrders.setOrderUser(userEntity);
            nxCommunityOrdersEntity.setOrderUserSplicingOrder(splicingOrders);

            return R.ok().put("data", nxCommunityOrdersEntity);
        } else {
            return R.error(-1, "拼单取消");
        }


    }


    @RequestMapping(value = "/customerIndexData", method = RequestMethod.POST)
    @ResponseBody
    public R customerIndexData(Integer commId, Integer orderUserId) {

        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByNxCommunityId(commId);
        List<NxCommunityOrdersEntity> nxCommunityOrdersEntities = new ArrayList<>();
        if (orderUserId != -1) {
            Map<String, Object> mapU = new HashMap<>();
            mapU.put("orderUserId", orderUserId);
            mapU.put("xiaoyuStatus", 5);
            nxCommunityOrdersEntities = nxCommunityOrdersService.queryOrderWithUserInfo(mapU);

        }


        Map<String, Object> map = new HashMap<>();
        map.put("adsense", adsenseEntities);
        map.put("orders", nxCommunityOrdersEntities);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("commId", commId);
        mapC.put("status", 0);
        NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryCustomerShowCoupon(mapC);
        if (communityCouponEntity != null) {
            Map<String, Object> mapIF = new HashMap<>();
            mapIF.put("coupId", communityCouponEntity.getNxCommunityCouponId());
            mapIF.put("userId", orderUserId);
            List<NxCustomerUserCouponEntity> nxCustomerUserCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(mapIF);
            if (nxCustomerUserCouponEntities.size() == 0) {
                map.put("coupon", communityCouponEntity);
            } else {
                map.put("coupon", null);
            }
        } else {
            map.put("coupon", null);
        }
        return R.ok().put("data", map);
    }


    /**
     * 删除订单
     *
     * @param nxOrdersId 订单id
     * @return o
     */
    @RequestMapping(value = "/deleteOrder/{nxOrdersId}")
    @ResponseBody
    public R deleteOrder(@PathVariable Integer nxOrdersId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxOrdersId);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                if (subEntity.getNxCosCucId() != null) {
                    NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                    customerUserCouponEntity.setNxCucStatus(0);
                    nxCustomerUserCouponService.update(customerUserCouponEntity);
                }
                subEntity.setNxCosStatus(99);
                nxCommunityOrdersSubService.update(subEntity);

            }
        }

        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
        if (cardEntities.size() > 0) {
            for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }

        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(nxOrdersId);
        nxCommunityOrdersEntity.setNxCoStatus(99);
        nxCommunityOrdersService.update(nxCommunityOrdersEntity);

        return R.ok();
    }


    @RequestMapping(value = "cust/customerGetOrders", method = RequestMethod.POST)
    @ResponseBody
    public R customerGetOrders(Integer nxOrdersUserId, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("orderUserId", nxOrdersUserId);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryCustomerOrder(map);
        int total = nxCommunityOrdersService.queryTotal(map);

        PageUtils pageUtil = new PageUtils(ordersEntityList, total, limit, page);

        return R.ok().put("page", pageUtil);

    }


    /**
     * 以下是订单接口
     */


    @RequestMapping(value = "/getIsDeliveryOrders/{deliveryUserId}")
    @ResponseBody
    public R getIsDeliveryOrders(@PathVariable Integer deliveryUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryUserId", deliveryUserId);
        map.put("status", 4);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryDeliveryOrder(map);

        return R.ok().put("data", ordersEntityList);
    }


    /**
     * 称重中
     *
     * @param disId 批发商id
     * @return 称重中订单
     */
    @RequestMapping(value = "/getWeighingOrder/{disId}")
    @ResponseBody
    public R getWeighingOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 1);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);

        return R.ok().put("data", entities);
    }

    /**
     * 未称重
     *
     * @param disId 批发商id
     * @return 未称重订单
     */
    @RequestMapping(value = "/getUnWeightOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderList(Integer disId, String serviceDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("serviceDate", serviceDate);
        map.put("status", 0);
        List<NxCommunityOrdersEntity> entities = nxCommunityOrdersService.queryOrdersDetail(map);
        return R.ok().put("data", entities);
    }


    /**
     * 获取订单详细
     *
     * @param orderId 订单ids
     * @return 订单
     */
    @RequestMapping(value = "/getOrderDetail/{orderId}")
    @ResponseBody
    public R getOrderDetail(@PathVariable Integer orderId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        System.out.println("dfodaof" + map);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryOrdersItemDetail(map);
        System.out.println("wwwhwhhwhwhwhhwhw" + ordersEntity);
        return R.ok().put("data", ordersEntity);
    }

    @RequestMapping(value = "/getOrderDetailPindan/{orderId}")
    @ResponseBody
    public R getOrderDetailPindan(@PathVariable Integer orderId) {

        Map<String, Object> map = new HashMap<>();
        map.put("id", orderId);
        map.put("orderType", 1);
        System.out.println("pinddnddid" + map);
        NxCommunityOrdersEntity ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        return R.ok().put("data", ordersEntity);
    }


    @ResponseBody
    @RequestMapping(value = "/customerCashPayPindan", method = RequestMethod.POST)
    public R customerCashPayPindan(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println("sisyBrachssswww");
        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8445/nongxinle/api/nxorders/notifyPindan");
        params.put("trade_type", "JSAPI");
        params.put("openid", nxOrders.getNxCoUserOpenId());

        //map转xml
        try {
            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            String pickUpCode = generatePickNumber(3);
            nxOrders.setNxCoWeighNumber(pickUpCode);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxOrders.setNxCoDate(formatWhatDate(0));
            nxOrders.setNxCoStatus(0);
            nxOrders.setNxCoPaymentStatus(0);

            BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
            BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
            nxOrders.setNxCoServiceTime(add.toString());
            nxOrders.setNxCoDate(formatWhatDate(0));
            System.out.println("updoafdpasfapsfas");
            nxCommunityOrdersService.update(nxOrders);


            Map<String, Object> map = new HashMap<>();
            map.put("id", nxOrders.getNxCommunityOrdersId());

            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);

            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                    splicingOrdersEntity.setNxCsoStatus(0);
                    splicingOrdersEntity.setNxCsoPaymentStatus(0);
                    splicingOrdersEntity.setNxCsoWeighNumber(pickUpCode);
                    splicingOrdersEntity.setNxCsoWxOutTradeNo(tradeNo);

                    splicingOrdersEntity.setNxCsoServiceTime(add.toString());
                    splicingOrdersEntity.setNxCsoDate(formatWhatDate(0));

                    if (splicingOrdersEntity.getNxCsoBuyMemberCardTime() > 0) {
                        String memberTime = formatWhatDay(nxOrders.getNxCoBuyMemberCardTime() * 30);
                        Integer nxCoCustomerId = splicingOrdersEntity.getNxCsoCustomerId();
                        NxCustomerEntity nxCustomerEntity = nxCustomerService.queryObject(nxCoCustomerId);
                        nxCustomerEntity.setNxCustomerCardWasteDate(memberTime);
                        nxCustomerService.update(nxCustomerEntity);
                    }

                    nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                }
            }

            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();

    }


    @RequestMapping(value = "/deliverySavePindan", method = RequestMethod.POST)
    @ResponseBody
    public R deliverySavePindan(Integer commId, Integer deliveryUserId) {

        NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(deliveryUserId);
        NxCommunityOrdersEntity ordersEntity = new NxCommunityOrdersEntity();
        ordersEntity.setNxCoUserId(deliveryUserId);
        ordersEntity.setNxCoDeliveryUserId(deliveryUserId);
        ordersEntity.setNxCoCommunityId(commId);
        ordersEntity.setNxCoCustomerId(userEntity.getNxCuCustomerId());
        ordersEntity.setNxCoType(1);
        ordersEntity.setNxCoStatus(-1);
        nxCommunityOrdersService.justSave(ordersEntity);


        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        splicingOrdersEntity.setNxCsoUserId(deliveryUserId);
        splicingOrdersEntity.setNxCsoCoOrderId(ordersEntity.getNxCommunityOrdersId());
        splicingOrdersEntity.setNxCsoStatus(0);
        splicingOrdersEntity.setNxCsoCommunityId(userEntity.getNxCuCommunityId());
        splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
        splicingOrdersEntity.setNxCsoTotal("0");
        splicingOrdersEntity.setNxCsoYouhuiTotal("0");
        splicingOrdersEntity.setNxCsoDate(formatWhatDate(0));
        nxCommunitySplicingOrdersService.save(splicingOrdersEntity);
        splicingOrdersEntity.setOrderUser(userEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("id", ordersEntity.getNxCommunityOrdersId());
        System.out.println("dfadfasfsa" + map);
        ordersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        ordersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

        return R.ok().put("data", ordersEntity);
    }


    @RequestMapping(value = "/getMyPindanDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getMyPindanDetail(Integer pindanId, Integer orderUserId) {

        NxCommunitySplicingOrdersEntity splicingOrdersEntity = new NxCommunitySplicingOrdersEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("id", pindanId);
        map.put("orderType", 1);
        System.out.println("pindfiaifajfamamma" + map);
        NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
        if (nxCommunityOrdersEntity != null) {
            Map<String, Object> mapO = new HashMap<>();
            mapO.put("id", pindanId);
            mapO.put("orderUserId", orderUserId);
            mapO.put("orderType", 1);
            System.out.println("spsoosmappa" + mapO);
            NxCommunitySplicingOrdersEntity deliverySplicingOrder = nxCommunitySplicingOrdersService.queryNewPindan(mapO);
            if (deliverySplicingOrder == null) {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orderUserId);
                splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
                splicingOrdersEntity.setNxCsoUserId(orderUserId);
                splicingOrdersEntity.setNxCsoCoOrderId(pindanId);
                splicingOrdersEntity.setNxCsoStatus(0);
                splicingOrdersEntity.setNxCsoCustomerId(userEntity.getNxCuCustomerId());
                splicingOrdersEntity.setNxCsoYouhuiTotal("0");
                splicingOrdersEntity.setNxCsoTotal("0");
                splicingOrdersEntity.setNxCsoCommunityId(nxCommunityOrdersEntity.getNxCoCommunityId());
                splicingOrdersEntity.setNxCsoDate(formatWhatDate(0));
                nxCommunitySplicingOrdersService.save(splicingOrdersEntity);

                splicingOrdersEntity.setOrderUser(userEntity);
                nxCommunityOrdersEntity = nxCommunityOrdersService.queryPindanDetail(map);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(splicingOrdersEntity);

            } else {
                NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orderUserId);
                deliverySplicingOrder.setOrderUser(userEntity);
                nxCommunityOrdersEntity.setOrderUserSplicingOrder(deliverySplicingOrder);
            }

            return R.ok().put("data", nxCommunityOrdersEntity);
        } else {
            return R.error(-1, "拼单取消");
        }


    }


    @ResponseBody
    @RequestMapping(value = "/pindanSaveOrder", method = RequestMethod.POST)
    public R pindanSaveOrder(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println(nxOrders);
        nxOrders.setNxCoDate(formatWhatDate(0));
        nxOrders.setNxCoStatus(0);
        String pickUpCode = generatePickNumber(3);
        nxOrders.setNxCoWeighNumber(pickUpCode);
        BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
        nxOrders.setNxCoServiceTime(add.toString());
        nxOrders.setNxCoDate(formatWhatDate(0));
        nxCommunityOrdersService.update(nxOrders);


        Map<String, Object> map = new HashMap<>();
        map.put("id", nxOrders.getNxCommunityOrdersId());

        List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
        if (nxCommunitySplicingOrdersEntities.size() > 0) {
            for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                splicingOrdersEntity.setNxCsoStatus(3);
                nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                Map<String, Object> mapSp = new HashMap<>();
                mapSp.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSp);
                if (nxOrdersSubEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                        subEntity.setNxCosOrdersId(nxOrders.getNxCommunityOrdersId());
                        subEntity.setNxCosStatus(0);
                        nxCommunityOrdersSubService.update(subEntity);
                    }
                }

            }
        }


        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }

    @ResponseBody
    @RequestMapping(value = "/customerSaveOrder", method = RequestMethod.POST)
    public R customerSaveOrder(@RequestBody NxCommunityOrdersEntity nxOrders) {

        System.out.println(nxOrders);
        nxOrders.setNxCoDate(formatWhatDate(0));
        nxOrders.setNxCoStatus(0);
        nxOrders.setNxCoType(0);
        String pickUpCode = generatePickNumber(3);
        nxOrders.setNxCoWeighNumber(pickUpCode);
        BigDecimal multiply = new BigDecimal(nxOrders.getNxCoServiceHour()).multiply(new BigDecimal(60));
        BigDecimal add = multiply.add(new BigDecimal(nxOrders.getNxCoServiceMinute()));
        nxOrders.setNxCoServiceTime(add.toString());
        nxOrders.setNxCoDate(formatWhatDate(0));
        nxCommunityOrdersService.justSaveWithUserGoods(nxOrders);

        List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();
        if (nxOrdersSubEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                subEntity.setNxCosOrdersId(nxOrders.getNxCommunityOrdersId());
                subEntity.setNxCosStatus(0);
                nxCommunityOrdersSubService.update(subEntity);
            }
        }

        if (nxOrders.getNxCoBuyMemberCardTime() > 0) {
            List<NxCustomerUserCardEntity> nxCustomerUserCardEntities = nxOrders.getNxCustomerUserCardEntities();
            for (NxCustomerUserCardEntity userCardEntity : nxCustomerUserCardEntities) {
                userCardEntity.setNxCucaComOrderId(nxOrders.getNxCommunityOrdersId());
                nxCustomerUserCardService.update(userCardEntity);
            }
        }

        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }

    @ResponseBody
    @RequestMapping(value = "/customerCashPay", method = RequestMethod.POST)
    public R customerCashPay(Integer orderId, String openId) {

        NxCommunityOrdersEntity nxOrders = nxCommunityOrdersService.queryObject(orderId);

        MyWxShixianliliPayConfig config = new MyWxShixianliliPayConfig();

        String nxRbTotal = nxOrders.getNxCoTotal();
        Double aDouble = Double.parseDouble(nxRbTotal) * 100;
        int i = aDouble.intValue();
        System.out.println("dfdafkdaksfas" + nxOrders.getNxCoTotal());
        String s1 = String.valueOf(i);
        String tradeNo = CommonUtils.generateOutTradeNo();
        SortedMap<String, String> params = new TreeMap<>();
        params.put("appid", config.getAppID());
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", CommonUtils.generateUUID());
        params.put("body", "订单支付");
        params.put("out_trade_no", tradeNo);
        params.put("fee_type", "CNY");
        params.put("total_fee", s1);
        params.put("spbill_create_ip", "101.42.222.149");
        params.put("notify_url", "https://grainservice.club:8445/nongxinle/api/nxorders/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);


        //map转xml
        try {
            WXPay wxpay = new WXPay(config);
            long time = System.currentTimeMillis();
            String tString = String.valueOf(time / 1000);
            Map<String, String> resp = wxpay.unifiedOrder(params);
            System.out.println(resp);
            SortedMap<String, String> reMap = new TreeMap<>();
            reMap.put("appId", config.getAppID());
            reMap.put("nonceStr", resp.get("nonce_str"));
            reMap.put("package", "prepay_id=" + resp.get("prepay_id"));
            reMap.put("signType", "MD5");
            reMap.put("timeStamp", tString);
            String s = WxPayUtils.creatSign(reMap, config.getKey());
            reMap.put("paySign", s);

            nxOrders.setNxCoStatus(1);
            nxOrders.setNxCoPaymentStatus(0);
            nxOrders.setNxCoWxOutTradeNo(tradeNo);
            nxCommunityOrdersService.update(nxOrders);
            reMap.put("orderId", nxOrders.getNxCommunityOrdersId().toString());

            return R.ok().put("map", reMap);


        } catch (Exception e) {
            e.printStackTrace();
        }


        return R.ok();

    }


    /**
     * @Title: callBack
     * @Description: 支付完成的回调函数
     * @param:
     * @return:
     */
    @RequestMapping("/notify")
    public String callBack(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map
            System.out.println("微信返回给回调函数的信息为：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxCommunityOrdersEntity billEntity = nxCommunityOrdersService.queryOrderByTradeNo(ordersSn);
                billEntity.setNxCoStatus(2);
                billEntity.setNxCoPaymentStatus(1);

                nxCommunityOrdersService.update(billEntity);

                Map<String, Object> map = new HashMap<>();
                map.put("orderId", billEntity.getNxCommunityOrdersId());
                List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                if (nxCommunityOrdersSubEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                        subEntity.setNxCosStatus(1);
                        subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                        subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                        subEntity.setNxCosService(billEntity.getNxCoService());
                        subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                        subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                        nxCommunityOrdersSubService.update(subEntity);
                        if (subEntity.getNxCosCucId() != null) {
                            NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                            customerUserCouponEntity.setNxCucStatus(2);
                            nxCustomerUserCouponService.update(customerUserCouponEntity);
                        }
                    }
                }


            }

            // 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    @RequestMapping("/notifyPindan")
    public String callBackPindan(HttpServletRequest request, HttpServletResponse response) {
        // System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        InputStream is = null;
        try {

            is = request.getInputStream();// 获取请求的流信息(这里是微信发的xml格式所有只能使用流来读)
            String xml = WxPayUtils.InputStream2String(is);
            Map<String, String> notifyMap = WxPayUtils.xmlToMap(xml);// 将微信发的xml转map
            System.out.println("微信返回给回调函数的信息为：" + xml);
            if (notifyMap.get("result_code").equals("SUCCESS")) {
                /*
                 * 以下是自己的业务处理------仅做参考 更新order对应字段/已支付金额/状态码
                 * 更新bill支付状态
                 */
                System.out.println("===notify===回调方法已经被调！！！");
                String ordersSn = notifyMap.get("out_trade_no");// 商户订单号
                NxCommunityOrdersEntity billEntity = nxCommunityOrdersService.queryOrderByTradeNo(ordersSn);
                billEntity.setNxCoStatus(2);
                billEntity.setNxCoPaymentStatus(1);
                nxCommunityOrdersService.update(billEntity);


                Map<String, Object> map = new HashMap<>();
                map.put("id", billEntity.getNxCommunityOrdersId());

                List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);

                if (nxCommunitySplicingOrdersEntities.size() > 0) {
                    for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                        splicingOrdersEntity.setNxCsoStatus(2);
                        splicingOrdersEntity.setNxCsoPaymentStatus(1);
                        nxCommunitySplicingOrdersService.update(splicingOrdersEntity);
                        Map<String, Object> mapSub = new HashMap<>();
                        mapSub.put("orderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
                        if (nxCommunityOrdersSubEntities.size() > 0) {
                            for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                                subEntity.setNxCosBuyStatus(1);
                                subEntity.setNxCosStatus(1);
                                subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                                subEntity.setNxCosService(billEntity.getNxCoService());
                                subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                                subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                nxCommunityOrdersSubService.update(subEntity);
                            }
                        }

                    }
                }

            }

            // 告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
            response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


}