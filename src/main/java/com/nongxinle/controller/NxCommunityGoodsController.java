package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.UploadFile;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxcommunitygoods")
public class NxCommunityGoodsController {
    @Autowired
    private NxCommunityGoodsService cgService;

    @Autowired
    private NxCommunityFatherGoodsService cfgService;
    @Autowired
    private NxCommunityGoodsSetItemService nxCommunityGoodsSetItemService;
    @Autowired
    private NxCommunityGoodsSetPropertyService nxCgSetPropertyService;
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCommunityCouponService nxCommunityCouponService;
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;
    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;

//



    @ResponseBody
    @RequestMapping(value = "/delComGoods", method = RequestMethod.POST)
    public R delComGoods(Integer id, HttpSession session) {

        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(id);
        if (communityGoodsEntity.getNxCgNxGoodsFilePath() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(communityGoodsEntity.getNxCgNxGoodsFilePath());
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", id);
        System.out.println("ididiid" + map);

        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        if(subEntities.size() == 0){
            NxCommunityGoodsEntity nxCommunityGoodsEntity = cgService.queryObject(id);
            List<NxCommunityGoodsSetItemEntity> entities =  nxCommunityGoodsSetItemService.queryCgGoodsSetListByParams(map);
            if(entities.size() > 0){
                for(int i = 0; i < entities.size(); i++){
                    nxCommunityGoodsSetItemService.delete(entities.get(i).getNxCommunityGoodsSetItemId());
                }
            }

            Map<String, Object> mapP = new HashMap<>();
            mapP.put("goodsId", id);
            List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities =   nxCgSetPropertyService.queryCgGoodsPropertyListByParams(mapP);
            if(nxCgSetPropertyEntities.size() > 0){
                for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                    nxCgSetPropertyService.delete(nxCgSetPropertyEntities.get(i).getNxCommunityGoodsSetPropertyId());
                }
            }

            Integer NxCgDfgGoodsFatherId = nxCommunityGoodsEntity.getNxCgCfgGoodsFatherId();
            NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(NxCgDfgGoodsFatherId);
            fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() - 1);
            cfgService.update(fatherGoodsEntity);


            cgService.delete(id);


            return R.ok();
        }else{
            return R.error(-1,"有订单不能删除");
        }
    }



    @RequestMapping(value = "/updateAdsendse", method = RequestMethod.POST)
    @ResponseBody
    public R updateAdsendse (@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity  ) {

        String cgStartTime = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
        String startHour = cgStartTime.substring(0, 2);
        String startMinute = cgStartTime.substring(3, 5);
        BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
        BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStart.toString());

        String cgStopTime = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
        String stopHour = cgStopTime.substring(0, 2);
        String stopMinute = cgStopTime.substring(3, 5);
        BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
        BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStop.toString());

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxCommunityGoodsEntity.getNxCommunityGoodsId());
        NxCommunityAdsenseEntity adsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);

        String cgStartTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
        String startHourAd = cgStartTimeAd.substring(0, 2);
        String startMinuteAd = cgStartTimeAd.substring(3, 5);
        BigDecimal hourMinuteStartAd = new BigDecimal(startHourAd).multiply(new BigDecimal(60));
        BigDecimal decimalStartAd = hourMinuteStartAd.add(new BigDecimal(startMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStartAd.toString());

        String cgStopTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
        String stopHourAd = cgStopTimeAd.substring(0, 2);
        String stopMinuteAd = cgStopTimeAd.substring(3, 5);
        BigDecimal hourMinuteStopAd = new BigDecimal(stopHourAd).multiply(new BigDecimal(60));
        BigDecimal decimalStopAd = hourMinuteStopAd.add(new BigDecimal(stopMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
        nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStopAd.toString());

        adsenseEntity.setNxCaStartTimeZone(decimalStartAd.toString());
        adsenseEntity.setNxCaStopTimeZone(decimalStopAd.toString());
        adsenseEntity.setNxCaStartTime(nxCommunityGoodsEntity.getNxCgAdsenseStartTime());
        adsenseEntity.setNxCaStopTime(nxCommunityGoodsEntity.getNxCgAdsenseStopTime());

        nxCommunityAdsenseService.update(adsenseEntity);




            cgService.update(nxCommunityGoodsEntity);



        return R.ok();
    }


    @RequestMapping(value = "/updateComGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoods (@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

//        Map<String, Object> mapS = new HashMap<>();
//        mapS.put("goodsId", nxCommunityGoodsEntity.getNxCommunityGoodsId());
//        mapS.put("xiaoyuStatus", 5);
//        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapS);

//        if(subEntities.size() == 0){

            BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
            BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
            nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
            nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());

            if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null && nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice().length() > 0){
                BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
                BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());

                System.out.println("indiddiidDDD"+ nxCommunityGoodsEntity.getNxCgGoodsPriceDecimal());
                if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
                    nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
                    nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
                }
            }else{
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(null);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianPrice(null);
                nxCommunityGoodsEntity.setNxCgGoodsHuaxianQuantity(null);

            }


            if(nxCommunityGoodsEntity.getNxCgSellType() == 1){
                String cgStartTime = nxCommunityGoodsEntity.getNxCgStartTime();
                String startHour = cgStartTime.substring(0, 2);
                String startMinute = cgStartTime.substring(3, 5);
                System.out.println("starthOurr==" + startHour);
                System.out.println("starthOurr==" + startMinute);
                BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
                BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgStartTimeZone(decimalStart.toString());

                String cgStopTime = nxCommunityGoodsEntity.getNxCgStopTime();
                String stopHour = cgStopTime.substring(0, 2);
                String stopMinute = cgStopTime.substring(3, 5);
                System.out.println("stophOurr==" + stopHour);
                System.out.println("stopOurr==" + stopMinute);
                BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
                BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgStopTimeZone(decimalStop.toString());
                System.out.println("reesotototo" + nxCommunityGoodsEntity.getNxCgStopTimeZone());

            }else{

                BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60));
                nxCommunityGoodsEntity.setNxCgStopTimeZone(multiply.toString());
                nxCommunityGoodsEntity.setNxCgStartTimeZone("0");
                nxCommunityGoodsEntity.setNxCgStartTime("00:00");
                nxCommunityGoodsEntity.setNxCgStopTime("23:59");

            }
            System.out.println("adddddkdkdididid" + nxCommunityGoodsEntity.getNxCgIsOpenAdsense());

            if(nxCommunityGoodsEntity.getNxCgIsOpenAdsense() == 1){
                String cgStartTime = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
                String startHour = cgStartTime.substring(0, 2);
                String startMinute = cgStartTime.substring(3, 5);
                BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
                BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStart.toString());

                String cgStopTime = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
                String stopHour = cgStopTime.substring(0, 2);
                String stopMinute = cgStopTime.substring(3, 5);
                BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
                BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStop.toString());

                Map<String, Object> map = new HashMap<>();
                map.put("goodsId", nxCommunityGoodsEntity.getNxCommunityGoodsId());
                NxCommunityAdsenseEntity adsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);

                String cgStartTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStartTime();
                String startHourAd = cgStartTimeAd.substring(0, 2);
                String startMinuteAd = cgStartTimeAd.substring(3, 5);
                BigDecimal hourMinuteStartAd = new BigDecimal(startHourAd).multiply(new BigDecimal(60));
                BigDecimal decimalStartAd = hourMinuteStartAd.add(new BigDecimal(startMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStartTimeZone(decimalStartAd.toString());

                String cgStopTimeAd = nxCommunityGoodsEntity.getNxCgAdsenseStopTime();
                String stopHourAd = cgStopTimeAd.substring(0, 2);
                String stopMinuteAd = cgStopTimeAd.substring(3, 5);
                BigDecimal hourMinuteStopAd = new BigDecimal(stopHourAd).multiply(new BigDecimal(60));
                BigDecimal decimalStopAd = hourMinuteStopAd.add(new BigDecimal(stopMinuteAd)).setScale(0, BigDecimal.ROUND_HALF_UP);
                nxCommunityGoodsEntity.setNxCgAdsenseStopTimeZone(decimalStopAd.toString());

                adsenseEntity.setNxCaStartTimeZone(decimalStartAd.toString());
                adsenseEntity.setNxCaStopTimeZone(decimalStopAd.toString());
                adsenseEntity.setNxCaStartTime(nxCommunityGoodsEntity.getNxCgAdsenseStartTime());
                adsenseEntity.setNxCaStopTime(nxCommunityGoodsEntity.getNxCgAdsenseStopTime());


                Integer nxCgGoodsType = nxCommunityGoodsEntity.getNxCgGoodsType();
                Integer nxCgCardId = nxCommunityGoodsEntity.getNxCgCardId();

                String path = "";
                String url = "?nxCommunityGoodsId=" + nxCommunityGoodsEntity.getNxCommunityGoodsId() + "&from=index&orderType=0&spId=-1&pindanId=-1";
                if(nxCgGoodsType == 0){
                    path = "zeroGoodsPage/zeroGoodsPage";
                }else if(nxCgGoodsType == 1){
                    if(nxCgCardId != null){
                        path = "oneGoodsCardPage/oneGoodsCardPage";

                    }else{
                        path = "oneGoodsPage/oneGoodsPage";
                    }


                }else if(nxCgGoodsType == 2){
                    if(nxCgCardId != null){
                        path = "twoGoodsCardPage/twoGoodsCardPage";

                    }else{
                        path = "twoGoodsPage/twoGoodsPage";
                    }
                }
                else if(nxCgGoodsType == 3){
                    if(nxCgCardId != null){
                        path = "threeGoodsCardPage/threeGoodsCardPage";

                    }else{
                        path = "threeGoodsPage/threeGoodsPage";
                    }

                }
                adsenseEntity.setNxCaClickTo(path + url);
                nxCommunityAdsenseService.update(adsenseEntity);
            }

            cgService.update(nxCommunityGoodsEntity);

            if(nxCommunityGoodsEntity.getNxCgCardId() != null){
                Integer nxCgCardId = nxCommunityGoodsEntity.getNxCgCardId();
                NxCommunityCardEntity nxCommunityCardEntity = nxCommunityCardService.queryObject(nxCgCardId);
                nxCommunityGoodsEntity.setNxCommunityCardEntity(nxCommunityCardEntity);
            }



            return R.ok().put("data", nxCommunityGoodsEntity);

//        }else{
//            return R.error(-1, "有订单不能修改");
//        }

    }




    @ResponseBody
    @RequestMapping("/comSaveComCouponGoods")
    public R comSaveComCouponGoods(@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

        String goodsName = nxCommunityGoodsEntity.getNxCgGoodsName();

        nxCommunityGoodsEntity.setNxCgGoodsStatus(0);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        nxCommunityGoodsEntity.setNxCgGoodsPy(headPinyin);
        nxCommunityGoodsEntity.setNxCgGoodsPinyin(pinyin);
        nxCommunityGoodsEntity.setNxCgNxGoodsId(-1);
        nxCommunityGoodsEntity.setNxCgNxFatherId(-1);
        nxCommunityGoodsEntity.setNxCgNxGrandId(-1);
        nxCommunityGoodsEntity.setNxCgNxGreatGrandId(-1);
        nxCommunityGoodsEntity.setNxCgSellType(4);
        if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null){
            BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
            BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
            BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
            BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
            nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
            nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());
        }

        if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
        }

        cgService.save(nxCommunityGoodsEntity);

        List<NxCommunityGoodsSetItemEntity> itemSubEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetItemEntities();
        if(itemSubEntities.size() > 0){
            for(int i = 0; i < itemSubEntities.size(); i++){
                NxCommunityGoodsSetItemEntity itemEntity = itemSubEntities.get(i);
                itemEntity.setNxCgsiItemSort(i+1);
                itemEntity.setNxCgsiItemCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCommunityGoodsSetItemService.save(itemEntity);
            }
        }

        List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetPropertyEntities();

        if(nxCgSetPropertyEntities.size() > 0){
            for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                NxCommunityGoodsSetPropertyEntity itemEntity = nxCgSetPropertyEntities.get(i);
                itemEntity.setNxCgspSort(i+1);
                itemEntity.setNxCgspCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCgSetPropertyService.save(itemEntity);
            }
        }

       NxCommunityCouponEntity goodsCoupon = new NxCommunityCouponEntity();
       goodsCoupon.setNxCpStartDate(nxCommunityGoodsEntity.getCouponStartDate());
       goodsCoupon.setNxCpStartTime(nxCommunityGoodsEntity.getCouponStartTime());
       goodsCoupon.setNxCpStopDate(nxCommunityGoodsEntity.getCouponStopDate());
       goodsCoupon.setNxCpStopTime(nxCommunityGoodsEntity.getCouponStopTime());
        goodsCoupon.setNxCpCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
        goodsCoupon.setNxCommunityCouponName(nxCommunityGoodsEntity.getNxCgGoodsName());
        goodsCoupon.setNxCpCommunityId(nxCommunityGoodsEntity.getNxCgCommunityId());

//        String nxCpStopTimeZone = "2024-05-01-00-00-00";
        String startDate =  nxCommunityGoodsEntity.getCouponStartDate();
        String stopDate =  nxCommunityGoodsEntity.getCouponStopDate();
        String couponStartTime = nxCommunityGoodsEntity.getCouponStartTime();
        String couponStopTime = nxCommunityGoodsEntity.getCouponStopTime();

        String replaceStart = couponStartTime.replace(":", "-");
        String replaceStop = couponStopTime.replace(":", "-");
        String start = startDate + "-" + replaceStart;
        String  stop = stopDate + "-" + replaceStop;
        System.out.println("dadfaf" + start + "stop=====" + stop);

        String[] splitStart = start.split("-");
        int year = Integer.parseInt(splitStart[0]);
        int month = Integer.parseInt(splitStart[1]);
        int day = Integer.parseInt(splitStart[2]);
        int hour = Integer.parseInt(splitStart[3]);
        int minute = Integer.parseInt(splitStart[4]);
        int haomiao = Integer.parseInt(splitStart[5]);
        LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);

        String[] splitStop = stop.split("-");
        int yearS = Integer.parseInt(splitStop[0]);
        int monthS = Integer.parseInt(splitStop[1]);
        int dayS = Integer.parseInt(splitStop[2]);
        int hourS = Integer.parseInt(splitStop[3]);
        int minuteS = Integer.parseInt(splitStop[4]);
        int haomiaoS = Integer.parseInt(splitStop[5]);
        LocalDateTime stopTime = LocalDateTime.of(yearS, monthS, dayS, hourS, minuteS, haomiaoS);
        System.out.println("adafasd" + beginTime + "stttt" + stopTime);

        goodsCoupon.setNxCpStartTimeZone(beginTime.toString());
        goodsCoupon.setNxCpStopTimeZone(stopTime.toString());
        goodsCoupon.setNxCpStatus(0);
        goodsCoupon.setNxCpType(0);
        goodsCoupon.setNxCpQuantity(nxCommunityGoodsEntity.getNxCgGoodsHuaxianQuantity());

        nxCommunityCouponService.save(goodsCoupon);
        goodsCoupon.setNxCommunityGoodsEntity(nxCommunityGoodsEntity);
        return R.ok().put("data", goodsCoupon);

    }


    @ResponseBody
    @RequestMapping("/comSaveComGoods")
    public R comSaveComGoods(@RequestBody NxCommunityGoodsEntity nxCommunityGoodsEntity) {

        String goodsName = nxCommunityGoodsEntity.getNxCgGoodsName();

        nxCommunityGoodsEntity.setNxCgGoodsStatus(0);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        nxCommunityGoodsEntity.setNxCgGoodsPy(headPinyin);
        nxCommunityGoodsEntity.setNxCgGoodsPinyin(pinyin);
        nxCommunityGoodsEntity.setNxCgNxGoodsId(-1);
        nxCommunityGoodsEntity.setNxCgNxFatherId(-1);
        nxCommunityGoodsEntity.setNxCgNxGrandId(-1);
        nxCommunityGoodsEntity.setNxCgNxGreatGrandId(-1);
        nxCommunityGoodsEntity.setNxCgIsOpenAdsense(0);

        BigDecimal goodsPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsPrice());
        BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN)).multiply(new BigDecimal(10)).setScale(0,BigDecimal.ROUND_HALF_UP);
        BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
        nxCommunityGoodsEntity.setNxCgGoodsPriceInteger(integerPart.toString());
        nxCommunityGoodsEntity.setNxCgGoodsPriceDecimal(fractionalPart.toString());
        if(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice() != null ){
            BigDecimal huaxianPrice = new BigDecimal(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
            BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
        }

        if(nxCommunityGoodsEntity.getNxCgGoodsType() == 2){
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
            nxCommunityGoodsEntity.setNxCgBuyingPriceExchange(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
        }

        if(nxCommunityGoodsEntity.getNxCgSellType() == 1){
            String cgStartTime = nxCommunityGoodsEntity.getNxCgStartTime();
            String startHour = cgStartTime.substring(0, 2);
            String startMinute = cgStartTime.substring(3, 5);
            BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
            BigDecimal decimalStart = hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgStartTimeZone(decimalStart.toString());

            String cgStopTime = nxCommunityGoodsEntity.getNxCgStopTime();
            String stopHour = cgStopTime.substring(0, 2);
            String stopMinute = cgStopTime.substring(3, 5);
            BigDecimal hourMinuteStop = new BigDecimal(stopHour).multiply(new BigDecimal(60));
            BigDecimal decimalStop = hourMinuteStop.add(new BigDecimal(stopMinute)).setScale(0, BigDecimal.ROUND_HALF_UP);
            nxCommunityGoodsEntity.setNxCgStopTimeZone(decimalStop.toString());

        }else{

            BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60));
            nxCommunityGoodsEntity.setNxCgStopTimeZone(multiply.toString());
            nxCommunityGoodsEntity.setNxCgStartTimeZone("0");
            nxCommunityGoodsEntity.setNxCgStartTime("00:00");
            nxCommunityGoodsEntity.setNxCgStopTime("23:59");

        }


        cgService.save(nxCommunityGoodsEntity);

        List<NxCommunityGoodsSetItemEntity> itemSubEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetItemEntities();
        if(itemSubEntities.size() > 0){
            for(int i = 0; i < itemSubEntities.size(); i++){
                NxCommunityGoodsSetItemEntity itemEntity = itemSubEntities.get(i);
                itemEntity.setNxCgsiItemSort(i+1);
                itemEntity.setNxCgsiItemCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCommunityGoodsSetItemService.save(itemEntity);
            }
        }

        List<NxCommunityGoodsSetPropertyEntity> nxCgSetPropertyEntities = nxCommunityGoodsEntity.getNxCommunityGoodsSetPropertyEntities();

        if(nxCgSetPropertyEntities.size() > 0){
            for(int i = 0; i < nxCgSetPropertyEntities.size(); i++){
                NxCommunityGoodsSetPropertyEntity itemEntity = nxCgSetPropertyEntities.get(i);
                itemEntity.setNxCgspSort(i+1);
                itemEntity.setNxCgspCgGoodsId(nxCommunityGoodsEntity.getNxCommunityGoodsId());
                nxCgSetPropertyService.save(itemEntity);
            }
        }

        Integer NxCgDfgGoodsFatherId = nxCommunityGoodsEntity.getNxCgCfgGoodsFatherId();
        NxCommunityFatherGoodsEntity fatherGoodsEntity = cfgService.queryObject(NxCgDfgGoodsFatherId);
        fatherGoodsEntity.setNxCfgGoodsAmount(fatherGoodsEntity.getNxCfgGoodsAmount() + 1);
        cfgService.update(fatherGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId",nxCommunityGoodsEntity.getNxCommunityGoodsId());
        NxCommunityGoodsEntity newCgGoods = cgService.queryComGoodsDetail(map);

        return R.ok().put("data", newCgGoods);

    }


    @RequestMapping(value = "/commGetComAppointSupplierGoods/{supplierId}")
    @ResponseBody
    public R commGetComAppointSupplierGoods(@PathVariable Integer supplierId) {

        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", supplierId);
        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryComGoodsByParams(map);

        return R.ok().put("data", goodsEntities);
    }






    /**
     * @param searchStr 搜索字符串
     * @param comId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryComGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryComGoodsByQuickSearch(String searchStr, String comId) {

        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        map.put("commId", comId);

        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchStrPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<NxCommunityGoodsEntity> goodsEntities = cgService.queryComGoodsQuickSearchStr(map);
        if (goodsEntities.size() > 0) {
            return R.ok().put("data", goodsEntities);
        }
        return R.error(-1, "没有商品");
    }



    @RequestMapping(value = "/comGetGoodsDetail/{comGoodsId}")
    @ResponseBody
    public R comGetGoodsDetail(@PathVariable Integer comGoodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", comGoodsId);
        System.out.println("deetailalalallala" + map);
        NxCommunityGoodsEntity comGoods = cgService.queryComGoodsDetail(map);
        return R.ok().put("data", comGoods);
    }





    @RequestMapping(value = "/getCommunityGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getCommunityGoodsDetail(Integer goodsId, Integer orderUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("goodsId", goodsId);
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryComGoodsDetail(map);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("stopTime", formatWhatDay(0));
        mapC.put("status", 1);
        System.out.println("cccckckkckkckc" + mapC);
        NxCustomerUserCardEntity card = nxCustomerUserCardService.queryUserGoodsCard(mapC);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("goods",communityGoodsEntity);
        mapR.put("card", card);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/getRemarkCommunityGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R getRemarkCommunityGoodsDetail(Integer goodsId, Integer orderUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryRemarkComGoodsDetail(map);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("stopTime", formatWhatDay(0));
        mapC.put("status", 1);
        System.out.println("cccckckkckkckc" + mapC);
        NxCustomerUserCardEntity card = nxCustomerUserCardService.queryUserGoodsCard(mapC);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("goods",communityGoodsEntity);
        mapR.put("card", card);
        return R.ok().put("data", mapR);
    }





    @RequestMapping(value = "/updateComGoodsWithFile", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoodsWithFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam("goodsId") Integer goodsId,
                                    HttpSession session) {
        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
        if (communityGoodsEntity.getNxCgNxGoodsFilePath() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(communityGoodsEntity.getNxCgNxGoodsFilePath());
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }

        communityGoodsEntity.setNxCgNxGoodsFilePath(filePath);

        cgService.update(communityGoodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/updateComGoodsWithFileTop", method = RequestMethod.POST)
    @ResponseBody
    public R updateComGoodsWithFileTop(@RequestParam("file") MultipartFile file,
                                    @RequestParam("goodsId") Integer goodsId,
                                    HttpSession session) {
        //1,上传图片
        String newUploadName = "goodsImage";
        String realPath = UploadFile.upload(session, newUploadName, file);

        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + filename;

        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(goodsId);
        if (communityGoodsEntity.getNxCgNxGoodsTopFilePath() != null) {
            ServletContext servletContext = session.getServletContext();
            String realPath1 = servletContext.getRealPath(communityGoodsEntity.getNxCgNxGoodsTopFilePath());
            File file1 = new File(realPath1);
            if (file1.exists()) {
                file1.delete();
            }
        }

        communityGoodsEntity.setNxCgNxGoodsTopFilePath(filePath);
        cgService.update(communityGoodsEntity);

        if(communityGoodsEntity.getNxCgIsOpenAdsense() == 1){
            Map<String, Object> map = new HashMap<>();
            map.put("goodsId", goodsId);
            NxCommunityAdsenseEntity communityAdsenseEntity = nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);
            communityAdsenseEntity.setNxCaFilePath(filePath);
            nxCommunityAdsenseService.update(communityAdsenseEntity);
        }


        return R.ok();
    }

    @RequestMapping(value = "/getCommunityGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R getCommunityGoodsByFatherId(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("getCommunityGoodsByFatherIdssss" + map);

        //查询列表数据
        List<NxCommunityGoodsEntity> dgGoodsLit = cgService.queryCommunityGoods(map);

        return R.ok().put("data", dgGoodsLit);
    }




    /**
     * 信息
     */
    @ResponseBody
    @RequestMapping("/info/{cgGoodsId}")
//    @RequiresPermissions("nxCommunityGoodsEntity:info")
    public R info(@PathVariable("cgGoodsId") Integer cgGoodsId) {
        NxCommunityGoodsEntity communityGoodsEntity = cgService.queryObject(cgGoodsId);

        return R.ok().put("data", communityGoodsEntity);
    }


}
