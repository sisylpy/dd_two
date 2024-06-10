package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.wxpay.sdk.WXPay;
import com.nongxinle.dao.NxCustomerUserGoodsDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.CommonUtils.generatePickNumber;
import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;


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
    private NxCommunitySplicingOrdersService nxCommunitySplicingOrdersService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;

    @Autowired
    private NxCustomerUserGoodsDao nxCustomerUserGoodsDao;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;


    @RequestMapping(value = "/updateAllSplicing", method = RequestMethod.POST)
    @ResponseBody
    public R updateAllSplicing (@RequestBody  NxCommunityOrdersEntity ordersEntity) {

        List<NxCommunitySplicingOrdersEntity> allSplicingOrders = ordersEntity.getAllSplicingOrders();

        for(NxCommunitySplicingOrdersEntity splicingOrdersEntity: allSplicingOrders){
            BigDecimal total = new BigDecimal(0);
            BigDecimal huaxianTotal = new BigDecimal(0);
            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = splicingOrdersEntity.getNxCommunityOrdersSubEntities();

            if(nxCommunityOrdersSubEntities.size() > 0){

                for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
                    total = total.add(new BigDecimal(subEntity.getNxCosSubtotal()));
                    if(subEntity.getNxCosHuaxianSubtotal() != null){
                        huaxianTotal = huaxianTotal.add(new BigDecimal(subEntity.getNxCosHuaxianSubtotal()));
                    }
                }
                splicingOrdersEntity.setNxCsoTotal(total.toString());
                splicingOrdersEntity.setNxCsoYouhuiTotal(huaxianTotal.toString());
                nxCommunitySplicingOrdersService.update(splicingOrdersEntity);
            }

        }

        return R.ok();
    }


    @RequestMapping(value = "/getDate")
    @ResponseBody
    public R getToday() {

        Map<String, Object> map = new HashMap<>();


        // day
        Map<String, Object> day = new HashMap<>();
        Map<String, Object> mapYesterday = new HashMap<>();
        mapYesterday.put("yesterdayDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayStartDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayStopDate", formatWhatDay(-1));
        mapYesterday.put("yesterdayString", formatWhatDayString(-1));
        mapYesterday.put("yesterdayWeek", getWeek(-1));
        day.put("yesterday", mapYesterday);

        Map<String, Object> mapToday = new HashMap<>();
        mapToday.put("todayDate", formatWhatDay(0));
        mapToday.put("todayStartDate", formatWhatDay(0));
        mapToday.put("todayStopDate", formatWhatDay(0));
        mapToday.put("todayString", formatWhatDayString(0));
        mapToday.put("todayWeek", getWeek(0));
        day.put("today", mapToday);


        // week
        Map<String, Object> week = new HashMap<>();
        Map<String, Object> lastSevenDay = new HashMap<>();
        lastSevenDay.put("lastSevenDayStartDate", formatWhatDay(-7));
        lastSevenDay.put("lastSevenDayStartDateString", formatWhatDayString(-7));
        lastSevenDay.put("lastSevenDayStopDate", formatWhatDay(-1));
        lastSevenDay.put("lastSevenDayStopDateString", formatWhatDayString(-1));
        week.put("lastSevenDay", lastSevenDay);

        Map<String, Object> thisWeek = new HashMap<>();
        thisWeek.put("thisWeekStartDate", thisWeekMonday());
        thisWeek.put("thisWeekStartString", thisWeekMondayString());
        thisWeek.put("thisWeekStopDate", thisWeekSunday());
        thisWeek.put("thisWeekStopString", thisWeekSundayString());
        week.put("thisWeek", thisWeek);

        Map<String, Object> lastWeek = new HashMap<>();
        lastWeek.put("lastWeekStartDate", getLastWeek());
        lastWeek.put("lastWeekStartString", thisWeekMondayString());
        lastWeek.put("lastWeekStopDate", thisWeekSunday());
        lastWeek.put("lastWeekStopString", thisWeekSundayString());
        week.put("lastWeek", lastWeek);

        // month
        Map<String, Object> month = new HashMap<>();
        Map<String, Object> lastThirtyDay = new HashMap<>();
        lastThirtyDay.put("lastThirtyDayStartDate", formatWhatDay(-30));
        lastThirtyDay.put("lastThirtyDayStartDateString", formatWhatDayString(-30));
        lastThirtyDay.put("lastThirtyDayStopDate", formatWhatDay(0));
        lastThirtyDay.put("lastThirtyDayStopDateString", formatWhatDayString(0));
        month.put("lastThirtyDay", lastThirtyDay);

        Map<String, Object> thisMonth = new HashMap<>();
        thisMonth.put("thisMonthStartDate", getThisMonthFirstDay());
        thisMonth.put("thisMonthStartDateString", formatWhatMonthString(0));
        thisMonth.put("thisMonthStopDate", getThisMonthLastDay());
        thisMonth.put("thisMonthStopDateString", formatWhatDayString(-1));
        month.put("thisMonth", thisMonth);
        Map<String, Object> lastMonth = new HashMap<>();
        lastMonth.put("lastMonthStartDate", getLastMonthFirstDay());
        lastMonth.put("lastMonthStartDateString", getLastMonthString());
        lastMonth.put("lastMonthStopDate", getLastMonthLastDay());
        lastMonth.put("lastMonthStopDateString", formatWhatDayString(-1));
        month.put("lastMonth", lastMonth);

        map.put("day", day);
        map.put("week", week);
        map.put("month", month);
        return R.ok().put("data", map);
    }


    @RequestMapping(value = "/getDayOrder", method = RequestMethod.POST)
    @ResponseBody
    public R getDayOrder(Integer commId, String date) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("date", date);
        map.put("status", 5);
        System.out.println("dateeee" + map);
        List<NxCommunityOrdersEntity> ordersEntities = nxCommunityOrdersService.queryCustomerOrder(map);

        return R.ok().put("data", ordersEntities);
    }


    @RequestMapping(value = "/getSalesEveryDay", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> getSalesEveryDay(String startDate, String stopDate, Integer commId) {

        System.out.println("getfrisheeieieidydyydydydyydydyydydydyy");
        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> itemList = new ArrayList<>();
        List<String> dateList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        if (howManyDaysInPeriod > 0) {

            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("date", whichDay);
                map.put("commId", commId);
                map.put("status", 5);
                String substring = whichDay.substring(8, 10);
                dateList.add(substring);

                String dailyFresh = "0";
                Integer integer = nxCommunityOrdersService.queryCommOrderCount(map);
                if (integer > 0) {
                    double subtotal = nxCommunityOrdersService.queryCommOrderSubtotal(map);
                    dailyFresh = new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                }
                totalList.add(dailyFresh);
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("day", whichDay);
                mapItem.put("value", dailyFresh);
                itemList.add(mapItem);
                mapR.put("date", dateList);
                mapR.put("list", totalList);
                mapR.put("arr", itemList);

            }

        }
        return R.ok().put("data", mapR);

    }

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

        if(orders.getNxCoType() == 0){
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
        }else{
            Map<String, Object> map = new HashMap<>();
            map.put("id", nxCommunityOrdersId);
            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
            if(nxCommunitySplicingOrdersEntities.size() > 0){
                for(NxCommunitySplicingOrdersEntity splicingOrdersEntity: nxCommunitySplicingOrdersEntities){
                    Integer nxCommunitySplicingOrdersId = splicingOrdersEntity.getNxCommunitySplicingOrdersId();
                    Map<String, Object> mapSp = new HashMap<>();
                    mapSp.put("splicingOrderId", nxCommunitySplicingOrdersId);
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSp);
                    if (subEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                            subEntity.setNxCosStatus(1);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }
                }
            }
            orders.setNxCoStatus(2);
            nxCommunityOrdersService.update(orders);
        }
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


        if (orders.getNxCoType() == 0) {

            Map<String, Object> mapC = new HashMap<>();
            mapC.put("userId", orders.getNxCoUserId());
            mapC.put("status", 0);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
            if (cardEntities.size() > 0) {
                for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                    userCardEntity.setNxCucaStatus(1);
                    userCardEntity.setNxCucaIsSelected(null);
                    nxCustomerUserCardService.update(userCardEntity);
                }
            }
            //update userInfo
            NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(orders.getNxCoUserId());
            BigDecimal decimal = new BigDecimal(userEntity.getNxCuOrderAmount()).add(new BigDecimal(orders.getNxCoTotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
            userEntity.setNxCuOrderAmount(decimal.toString());
            userEntity.setNxCuOrderTimes(userEntity.getNxCuOrderTimes() + 1);
            nxCustomerUserService.update(userEntity);


        } else {
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("id", orders.getNxCommunityOrdersId());
            List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
            if (nxCommunitySplicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {
                    Map<String, Object> mapC = new HashMap<>();
                    mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                    mapC.put("status", 0);
                    List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                    if (cardEntities.size() > 0) {
                        for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                            userCardEntity.setNxCucaStatus(1);
                            userCardEntity.setNxCucaIsSelected(null);
                            nxCustomerUserCardService.update(userCardEntity);
                        }
                    }

                    //update userInfo
                    NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(splicingOrdersEntity.getNxCsoUserId());
                    BigDecimal decimal = new BigDecimal(userEntity.getNxCuOrderAmount()).add(new BigDecimal(orders.getNxCoTotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    userEntity.setNxCuOrderAmount(decimal.toString());
                    userEntity.setNxCuOrderTimes(userEntity.getNxCuOrderTimes() + 1);
                    nxCustomerUserService.update(userEntity);


                }
            }
        }


        return R.ok();
    }


    @RequestMapping(value = "/printWholeOrder", method = RequestMethod.POST)
    @ResponseBody
    public R printWholeOrder(@RequestBody NxCommunityOrdersEntity orders) {
        Integer nxCommunityOrdersId = orders.getNxCommunityOrdersId();

        if(orders.getNxCoType() == 0){
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", nxCommunityOrdersId);
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            if (subEntities.size() > 0) {
                for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                    subEntity.setNxCosStatus(3);
                    nxCommunityOrdersSubService.update(subEntity);
                }
            }

        }else{
            Map<String, Object> map = new HashMap<>();
            map.put("id", nxCommunityOrdersId);
            System.out.println("iddiididid" + map);
            List<NxCommunitySplicingOrdersEntity> splicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(map);
            if (splicingOrdersEntities.size() > 0) {
                for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : splicingOrdersEntities) {
                    Map<String, Object> mapSub = new HashMap<>();
                    mapSub.put("orderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                    List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSub);
                    if (subEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                            subEntity.setNxCosStatus(3);
                            nxCommunityOrdersSubService.update(subEntity);
                        }
                    }
                }
            }
        }

        orders.setNxCoStatus(4);
        nxCommunityOrdersService.update(orders);



        Integer nxCoUserId = orders.getNxCoUserId();
        NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(nxCoUserId);
        String nxCuWxOpenId = userEntity.getNxCuWxOpenId();

        Map<String, TemplateData> mapNotice = new HashMap<>();
        mapNotice.put("character_string1", new TemplateData(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())));
        mapNotice.put("amount2", new TemplateData("12"));
        mapNotice.put("phrase3", new TemplateData("已到货"));
        mapNotice.put("thing6", new TemplateData("BJYSL-SN-500"));
        mapNotice.put("number7", new TemplateData("100"));
        System.out.println("nociiciiiicic" + mapNotice);
        WeNoticeService.subscribeOrderFinishMessage(nxCuWxOpenId, "pages/index/index", mapNotice);


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

    @RequestMapping(value = "/customerIndexData", method = RequestMethod.POST)
    @ResponseBody
    public R customerIndexData(Integer commId, Integer orderUserId) {

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("commId", commId);
        mapA.put("nowMinute", getNowMinute());
        System.out.println("ampapappa" + mapA);
        List<NxCommunityAdsenseEntity> adsenseEntities = nxCommunityAdsenseService.queryAdsenseByParams(mapA);
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

        MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
        String shipinUserName = myAPPIDConfig.getShipinUserName();
        String shipinId = myAPPIDConfig.getShipinId();
        NxCommunityVideoEntity videoEntity = new NxCommunityVideoEntity();
        videoEntity.setNxCommunityVideoUserName(shipinUserName);
        videoEntity.setNxCommunityVideoId(shipinId);
        map.put("shipin", videoEntity);


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


    @RequestMapping(value = "/customerGetOrders", method = RequestMethod.POST)
    @ResponseBody
    public R customerGetOrders(Integer nxOrdersUserId, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("orderUserId", nxOrdersUserId);
        List<NxCommunityOrdersEntity> ordersEntityList = nxCommunityOrdersService.queryCustomerOrder(map);
        int total = nxCommunityOrdersService.queryTotal(map);


        PageUtils pageUtil = new PageUtils(ordersEntityList, total, limit, page);
        System.out.println("paapa" + pageUtil);
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
        System.out.println("mapappaidid" + map);
        if (nxCommunitySplicingOrdersEntities.size() > 0) {
            for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                splicingOrdersEntity.setNxCsoStatus(3);
                nxCommunitySplicingOrdersService.update(splicingOrdersEntity);

                //gegnixnusergoods
                Map<String, Object> mapSp = new HashMap<>();
                mapSp.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapSp);
                if (nxOrdersSubEntities.size() > 0) {
                    for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                        subEntity.setNxCosStatus(0);
                        nxCommunityOrdersSubService.update(subEntity);

                    }
                }

            }
        }


        return R.ok().put("data", nxOrders.getNxCommunityOrdersId());

    }


    private  void saveUserGoods(NxCommunityOrdersSubEntity subEntity){

        Integer nxOsCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
        Map<String, Object> mapUG = new HashMap<>();
        mapUG.put("nxOsCommunityGoodsId", nxOsCommunityGoodsId);
        mapUG.put("nxCugUserId", subEntity.getNxCosOrderUserId());
        NxCustomerUserGoodsEntity userGoodsEntity = nxCustomerUserGoodsDao.queryByCommunityGoodsId(mapUG);

        if (userGoodsEntity != null) {
            userGoodsEntity.setNxCugLastOrderTime(formatWhatDayTime(0));
            userGoodsEntity.setNxCugLastOrderQuantity(subEntity.getNxCosQuantity());
            userGoodsEntity.setNxCugLastOrderStandard(subEntity.getNxCosStandard());
            userGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
            userGoodsEntity.setNxCugJoinMyTemplate(0);
            Integer nxCugOrderTimes = userGoodsEntity.getNxCugOrderTimes();
            userGoodsEntity.setNxCugOrderTimes(nxCugOrderTimes + 1);
            String nxCugOrderAmount = userGoodsEntity.getNxCugOrderAmount();
            String nxOsQuantity = subEntity.getNxCosQuantity();
            BigDecimal addS = new BigDecimal(nxCugOrderAmount).add(new BigDecimal(nxOsQuantity));
            userGoodsEntity.setNxCugOrderAmount(addS.toString());
            nxCustomerUserGoodsDao.update(userGoodsEntity);
        } else {
            NxCustomerUserGoodsEntity newUserGoodsEntity = new NxCustomerUserGoodsEntity();
            newUserGoodsEntity.setNxCugFirstOrderTime(formatWhatDay(0));
            newUserGoodsEntity.setNxCugOrderAmount(subEntity.getNxCosQuantity());
            newUserGoodsEntity.setNxCugCommunityGoodsId(subEntity.getNxCosCommunityGoodsId());
            newUserGoodsEntity.setNxCugOrderTimes(1);
            newUserGoodsEntity.setNxCugUserId(subEntity.getNxCosOrderUserId());
            newUserGoodsEntity.setNxCugLastOrderTime(formatWhatDay(0));
            newUserGoodsEntity.setNxCugJoinMyTemplate(0);
            newUserGoodsEntity.setNxCugLastOrderQuantity(subEntity.getNxCosQuantity());
            newUserGoodsEntity.setNxCugLastOrderStandard(subEntity.getNxCosStandard());
            newUserGoodsEntity.setNxCugType(0);
            nxCustomerUserGoodsDao.save(newUserGoodsEntity);
        }
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
//        nxCommunityOrdersService.justSaveWithUserGoods(nxOrders);
        nxCommunityOrdersService.justSave(nxOrders);

        List<NxCommunityOrdersSubEntity> nxOrdersSubEntities = nxOrders.getNxOrdersSubEntities();
        if (nxOrdersSubEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : nxOrdersSubEntities) {
                subEntity.setNxCosOrdersId(nxOrders.getNxCommunityOrdersId());
                subEntity.setNxCosStatus(0);
                nxCommunityOrdersSubService.update(subEntity);
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

                if (billEntity.getNxCoType() == 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", billEntity.getNxCommunityOrdersId());
                    List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                    if (nxCommunityOrdersSubEntities.size() > 0) {
                        for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {
                            //检查 Adsense
                            checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                            subEntity.setNxCosStatus(1);
                            subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                            subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                            subEntity.setNxCosService(billEntity.getNxCoService());
                            subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                            subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                            nxCommunityOrdersSubService.update(subEntity);

                            saveUserGoods(subEntity);

                            if (subEntity.getNxCosCucId() != null) {
                                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                                customerUserCouponEntity.setNxCucStatus(2);
                                nxCustomerUserCouponService.update(customerUserCouponEntity);
                            }
                        }
                    }
                    Map<String, Object> mapC = new HashMap<>();
                    mapC.put("userId", billEntity.getNxCoUserId());
                    mapC.put("status", -1);
                    List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                    if (cardEntities.size() > 0) {
                        for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                            if (userCardEntity.getNxCucaIsSelected() == 1) {
                                userCardEntity.setNxCucaStatus(0);
                                userCardEntity.setNxCucaComOrderId(billEntity.getNxCommunityOrdersId());
                                nxCustomerUserCardService.update(userCardEntity);
                            } else {
                                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                            }
                        }
                    }

                } else {
                    Map<String, Object> mapS = new HashMap<>();
                    mapS.put("id", billEntity.getNxCommunityOrdersId());
                    System.out.println("bilosororororororoorro" +mapS);
                    List<NxCommunitySplicingOrdersEntity> nxCommunitySplicingOrdersEntities = nxCommunitySplicingOrdersService.querySplicingListByParams(mapS);
                    if (nxCommunitySplicingOrdersEntities.size() > 0) {
                        for (NxCommunitySplicingOrdersEntity splicingOrdersEntity : nxCommunitySplicingOrdersEntities) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("splicingOrderId", splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            System.out.println("pspsosoosossos" + splicingOrdersEntity.getNxCommunitySplicingOrdersId());
                            List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
                            if (nxCommunityOrdersSubEntities.size() > 0) {
                                for (NxCommunityOrdersSubEntity subEntity : nxCommunityOrdersSubEntities) {

                                    //检查 Adsense
                                    checkAdsenseGoods(subEntity.getNxCosCommunityGoodsId(), subEntity);

                                    subEntity.setNxCosStatus(1);
                                    subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                    subEntity.setNxCosPickUpCode(billEntity.getNxCoWeighNumber());
                                    subEntity.setNxCosService(billEntity.getNxCoService());
                                    subEntity.setNxCosServiceDate(billEntity.getNxCoServiceDate());
                                    subEntity.setNxCosServiceTime(billEntity.getNxCoServiceTime());
                                    nxCommunityOrdersSubService.update(subEntity);
                                    saveUserGoods(subEntity);

                                    if (subEntity.getNxCosCucId() != null) {
                                        NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                                        customerUserCouponEntity.setNxCucStatus(2);
                                        nxCustomerUserCouponService.update(customerUserCouponEntity);
                                    }


                                }
                            }

                            Map<String, Object> mapC = new HashMap<>();
                            mapC.put("userId", splicingOrdersEntity.getNxCsoUserId());
                            mapC.put("status", -1);
                            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
                            if (cardEntities.size() > 0) {
                                for (NxCustomerUserCardEntity userCardEntity : cardEntities) {
                                    if (userCardEntity.getNxCucaIsSelected() == 1) {
                                        userCardEntity.setNxCucaStatus(0);
                                        userCardEntity.setNxCucaComOrderId(billEntity.getNxCommunityOrdersId());
                                        nxCustomerUserCardService.update(userCardEntity);
                                    } else {
                                        nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                                    }
                                }

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


    private void checkAdsenseGoods(Integer goodsId, NxCommunityOrdersSubEntity subEntity){
        System.out.println("checkckkckckckckckc");
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if(goodsEntity.getNxCgIsOpenAdsense() == 1){

            System.out.println("minieiieieiieiieieei");
            int nowMinute = getNowMinute();
            int nxCgAdsenseStartTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStartTimeZone()) ;
            int nxCgAdsenseStopTimeZone = Integer.parseInt(goodsEntity.getNxCgAdsenseStopTimeZone());
            if(nowMinute > nxCgAdsenseStartTimeZone && nowMinute < nxCgAdsenseStopTimeZone){
                System.out.println("minieiieieiieiieieei" + nowMinute);
                BigDecimal adQuantity = new BigDecimal(goodsEntity.getNxCgAdsenseRestQuantity());
                BigDecimal subtract = adQuantity.subtract(new BigDecimal(subEntity.getNxCosQuantity()));
                goodsEntity.setNxCgAdsenseRestQuantity(Integer.valueOf(subtract.toString()));
                nxCommunityGoodsService.update(goodsEntity);
            }

        }

    }



}