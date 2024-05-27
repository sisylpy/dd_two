
package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */


import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;


@RestController
@RequestMapping("api/nxorderssub")
public class NxCommunityOrdersSubController {
    @Autowired
    private NxCommunityOrdersSubService nxCommunityOrdersSubService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxCommunitySplicingOrdersService nxCommSplicingOrdersService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private NxCustomerUserCouponService nxCustomerUserCouponService;
    @Autowired
    private NxCommunityCardService nxCommunityCardService;
    @Autowired
    private NxCustomerUserCardService nxCustomerUserCardService;



    @RequestMapping(value = "/printSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R printSubOrders(Integer subOrderId, Integer status) {

        NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(subOrderId);
        subEntity.setNxCosStatus(status);
        subEntity.setNxCosPrintTime(formatFullTime());
        System.out.println("printSubOrdersprintSubOrders" + formatWhatTime(0) + "orderid=" + subEntity.getNxCommunityOrdersSubId());
        System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "timemeemmelog111" + testPrintTime());
        nxCommunityOrdersSubService.update(subEntity);

        Integer nxCosOrdersId = subEntity.getNxCosOrdersId();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", nxCosOrdersId);
        map.put("status", 1);
        System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "ordsusssnummmmmmmmmmm" + map);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        System.out.println("sisisisiisisisisiziziz" + subEntities.size());
        if (subEntities.size() == 0) {
            NxCommunityOrdersEntity nxCommunityOrdersEntity = nxCommunityOrdersService.queryObject(nxCosOrdersId);
            nxCommunityOrdersEntity.setNxCoStatus(3);
            nxCommunityOrdersService.update(nxCommunityOrdersEntity);
            System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "timemeemmelog222" + testPrintTime());
        }

        System.out.println("orderid=" + subEntity.getNxCommunityOrdersSubId() + "timemeemmelog333" + testPrintTime());

        return R.ok();
    }


    @RequestMapping(value = "/getUnPrintSubOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getUnPrintSubOrders(Integer commId, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("commId", commId);
        map.put("status", status);
        map.put("service", 0);
        System.out.println("priririir" + map);
        List<NxCommunityPrintOrdersSubEntity> subEntities = nxCommunityOrdersSubService.queryPrintSubOrders(map);

        map.put("service", 1);
        map.put("date", formatWhatDate(0));
        map.put("time", formatWhatDayMinute(30));
        System.out.println("kadnkafdanfdkameiieirpiriririrmap" + map);
        List<NxCommunityPrintOrdersSubEntity> subEntities1 = nxCommunityOrdersSubService.queryPrintSubOrders(map);
        subEntities.addAll(subEntities1);

        NxCommunityOrdersSubEntity entity = null;
        for (NxCommunityPrintOrdersSubEntity pEntity : subEntities) {
            entity = new NxCommunityOrdersSubEntity();
            entity.setNxCommunityOrdersSubId(pEntity.getNxCommunityOrdersPrintSubId());
            entity.setNxCosStatus(9); //锁定状态
            nxCommunityOrdersSubService.update(entity);
        }


        return R.ok().put("data", subEntities);
    }





    @RequestMapping(value = "/changeMemberCard", method = RequestMethod.POST)
    @ResponseBody
    public R changeMemberCard ( Integer orderUserId, Integer status, Integer orderType, Integer userCardId, Integer cardId) {

        if(status == 1){
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("dayuDiffPrice", 0);
            map.put("cardId", cardId);
            //查询
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            //status = 1 ,则取消会员卡功能
            if(subEntities.size() > 0){
                for(NxCommunityOrdersSubEntity subEntity: subEntities){
                    //先查是否有一样的没有优惠价格的订单

                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    Map<String, Object> mapSame = new HashMap<>();
                    mapSame.put("orderUserId", orderUserId);
                    mapSame.put("orderType", orderType);
                    mapSame.put("status", -1);
                    mapSame.put("diffPrice", 0);
                    mapSame.put("goodsId", nxCosCommunityGoodsId);
                    if(subEntity.getNxCosRemark() != null){
                        mapSame.put("remark", subEntity.getNxCosRemark());
                    }
                    NxCommunityOrdersSubEntity sameGoodsOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapSame);
                    if(sameGoodsOrder != null){
                        //jiashulaing
                        BigDecimal changeQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                        BigDecimal add = new BigDecimal(sameGoodsOrder.getNxCosQuantity()).add(changeQuantity);
                        BigDecimal decimal = new BigDecimal(sameGoodsOrder.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        sameGoodsOrder.setNxCosQuantity(add.toString());
                        sameGoodsOrder.setNxCosSubtotal(decimal.toString());
                        nxCommunityOrdersSubService.update(sameGoodsOrder);

                        nxCommunityOrdersSubService.delete(subEntity.getNxCommunityOrdersSubId());

                    }else{
                        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                        BigDecimal orderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                        BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                        subEntity.setNxCosSubtotal(decimal.toString());
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        nxCommunityOrdersSubService.update(subEntity);

                    }
                }
            }

            //xiugai
            NxCustomerUserCardEntity userCardEntity = nxCustomerUserCardService.queryObject(userCardId);
            userCardEntity.setNxCucaIsSelected(0);
            nxCustomerUserCardService.update(userCardEntity);


        }


        //
        if(status == 0){
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("diffPrice", 0);
            map.put("cardId", cardId);
            System.out.println("stssssssss00000000" + map);
            //查询
            List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
            //status = 1 ,则取消会员卡功能
            if(subEntities.size() > 0){
                for(NxCommunityOrdersSubEntity subEntity: subEntities){
                    //先查是否有一样的没有优惠价格的订单
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    //只有划线商品的订单才改动
                    if(goodsEntity.getNxCgGoodsHuaxianPrice() != null){
                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                        BigDecimal orderQuantity = new BigDecimal(subEntity.getNxCosQuantity());
                        BigDecimal moreQuantity = huaxianQuantity.subtract(orderQuantity);
                        //划线优惠数量没有用完，直接修改订单
                        System.out.println("huaxianqqqqqq" + huaxianQuantity);
                        System.out.println("moreQuantitymoreQuantitymoreQuantity" + moreQuantity);
                        if(moreQuantity.compareTo(new BigDecimal(0))  == 1){

                            BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                            subEntity.setNxCosSubtotal(decimal.toString());
                            nxCommunityOrdersSubService.update(subEntity);

                        }else{
                            //划线优惠数量用完了
                            //查询是否有同样商品的没有优惠的订单
                            Map<String, Object> mapSame = new HashMap<>();
                            mapSame.put("orderUserId", orderUserId);
                            mapSame.put("orderType", orderType);
                            mapSame.put("status", -1);
                            mapSame.put("diffPrice", 0);
                            mapSame.put("goodsId", nxCosCommunityGoodsId);
                            if(subEntity.getNxCosRemark() != null){
                                mapSame.put("remark", subEntity.getNxCosRemark());
                            }
                            System.out.println("mapososososos" + mapSame);
                            NxCommunityOrdersSubEntity sameGoodsOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapSame);

                            if(sameGoodsOrder != null){

                                if(moreQuantity.compareTo(new BigDecimal(0)) == 0){
                                    BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                                    subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                    subEntity.setNxCosSubtotal(decimal.toString());
                                    nxCommunityOrdersSubService.update(subEntity);
                                }else {
                                    System.out.println("hasamdmmdmdmdmd" + sameGoodsOrder.getNxCosQuantity());

                                    //有相同的没有优惠的订单,把一个订房分成 2 部分
                                    NxCommunityOrdersSubEntity limitOrder = new NxCommunityOrdersSubEntity();
                                    limitOrder.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                    limitOrder.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                    limitOrder.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                    limitOrder.setNxCosOrderUserId(orderUserId);
                                    limitOrder.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                    limitOrder.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                    limitOrder.setNxCosQuantity(goodsEntity.getNxCgGoodsHuaxianQuantity().toString());
                                    limitOrder.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                    limitOrder.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                    BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    limitOrder.setNxCosSubtotal(decimal.toString());
                                    limitOrder.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                    limitOrder.setNxCosStatus(-1);
                                    limitOrder.setNxCosRemark(subEntity.getNxCosRemark());
                                    limitOrder.setNxCosType(orderType);
                                    limitOrder.setNxCosSplicingOrdersId(subEntity.getNxCosSplicingOrdersId());
                                    System.out.println("sanenennenenennenlimlmeoror" + limitOrder.getNxCosQuantity());
                                    nxCommunityOrdersSubService.save(limitOrder);



                                    BigDecimal restQuantity = new BigDecimal(sameGoodsOrder.getNxCosQuantity()).subtract(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity()));
                                    BigDecimal decimal1 = restQuantity.multiply(new BigDecimal(goodsEntity.getNxCgGoodsHuaxianPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                                    sameGoodsOrder.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                    sameGoodsOrder.setNxCosQuantity(restQuantity.toString());
                                    sameGoodsOrder.setNxCosHuaxianDifferentPrice("0");
                                    sameGoodsOrder.setNxCosSubtotal(decimal1.toString());
                                    nxCommunityOrdersSubService.update(sameGoodsOrder);
                                    System.out.println("ovdiireiieiieeiieeieie" + sameGoodsOrder.getNxCosQuantity());
                                }


                            }else{

                                BigDecimal decimal = new BigDecimal(goodsEntity.getNxCgGoodsPrice()).multiply(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                                subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                subEntity.setNxCosSubtotal(decimal.toString());
                                nxCommunityOrdersSubService.update(subEntity);
                            }
                        }



                    }

                }
            }



            NxCustomerUserCardEntity userCardEntity = nxCustomerUserCardService.queryObject(userCardId);
            userCardEntity.setNxCucaIsSelected(1);
            nxCustomerUserCardService.update(userCardEntity);


        }






        //------

        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        Map<String, Object> mapC = new HashMap<>();
        mapC.put("userId", orderUserId);
        mapC.put("status", -1);
        mapC.put("type", orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subOrders", nxCommunityOrdersSubEntities);
        mapR.put("cardList", cardEntities);
        return R.ok().put("data", mapR);
    }





    @ResponseBody
    @RequestMapping(value = "/saveSubOrder", method = RequestMethod.POST)
    public R saveSubOrder(Integer goodsId, Integer orderUserId, Integer spId, Integer orderType, Integer pindanId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
        subEntity.setNxCosOrderUserId(orderUserId);
        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
        subEntity.setNxCosQuantity("1");
        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());

        if (goodsEntity.getNxCgGoodsHuaxianPrice() != null) {
            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
        } else {
            subEntity.setNxCosHuaxianDifferentPrice("0");
        }

        subEntity.setNxCosStatus(-1);
        subEntity.setNxCosType(orderType);
        subEntity.setNxCosSplicingOrdersId(spId);
        subEntity.setNxCosOrdersId(pindanId);
        nxCommunityOrdersSubService.save(subEntity);

        //判断是否是会员卡商品
        if (goodsEntity.getNxCgCardId() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("cardId", goodsEntity.getNxCgCardId());
            map.put("userId", orderUserId);
            map.put("type", orderType);
            map.put("status", -1);
            System.out.println("checkckckckdkusrcarr-1-1--1-1-1-1-1" + map);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
            if (cardEntities.size() == 0) {
                Map<String, Object> mapU = new HashMap<>();
                mapU.put("cardId", goodsEntity.getNxCgCardId());
                mapU.put("stopTime", formatWhatDay(0));
                mapU.put("userId", orderUserId);
                mapU.put("type", orderType);
                System.out.println("checkckckckdkusrcarr222222" + mapU);
                List<NxCustomerUserCardEntity> cardEntitiesU = nxCustomerUserCardService.queryUserCardByParams(mapU);
                if(cardEntitiesU.size() == 0){
                    NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
                    NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
                    userCardEntity.setNxCucaStatus(-1);
                    userCardEntity.setNxCucaCustomerUserId(orderUserId);
                    userCardEntity.setNxCucaStartDate(formatWhatDay(0));
                    userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
                    userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
                    userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
                    userCardEntity.setNxCucaIsSelected(1);
                    userCardEntity.setNxCucaType(orderType);
                    nxCustomerUserCardService.save(userCardEntity);

                }

            }

        }


        return R.ok().put("data", subEntity);

    }


    @RequestMapping(value = "/memberSaveMemberOrderRemark", method = RequestMethod.POST)
    @ResponseBody
    public R memberSaveMemberOrderRemark (Integer goodsId, Integer orderUserId, String remark, Integer orderType,Integer spId, Integer pindanId) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("orderUserId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("status", -1);
        mapC.put("diffPrice", 0);
        mapC.put("remark", remark);
        mapC.put("orderType", orderType);
        //1.1.1 先查询优惠订单是否超过数量
        System.out.println("ccccccccccaaaammmmmmmmmm" + mapC);
        NxCommunityOrdersSubEntity communityOrdersSubEntityC = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
        if (communityOrdersSubEntityC != null) {
            BigDecimal add = new BigDecimal(communityOrdersSubEntityC.getNxCosQuantity()).add(new BigDecimal(1));
            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntityC.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
            communityOrdersSubEntityC.setNxCosQuantity(add.toString());
            communityOrdersSubEntityC.setNxCosSubtotal(subtotal.toString());
            nxCommunityOrdersSubService.update(communityOrdersSubEntityC);
        }else{
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("dayuDiffPrice", 0);
            map.put("remark", remark);
            map.put("orderType", orderType);
            System.out.println("ccccccccccaaaa" + map);
            NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (communityOrdersSubEntity != null) {
                BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                BigDecimal orderQuantity = new BigDecimal(communityOrdersSubEntity.getNxCosQuantity());
                Map<String, Object> mapT = new HashMap<>();
                mapT.put("orderUserId", orderUserId);
                mapT.put("goodsId", goodsId);
                mapT.put("status", -1);
                mapT.put("dayuDiffPrice", 0);
                mapT.put("orderType", orderType);
                int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                //1.1.1.1剩余数量大于 1，则加1
                System.out.println("restttoototot" + restQuantity);
                if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    communityOrdersSubEntity.setNxCosQuantity(add.toString());
                    communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                    communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                    nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                } else {
                    //1.1.1.2保存普通订单之前先查是否有同样的订单
                    Map<String, Object> mapZ = new HashMap<>();
                    mapZ.put("orderUserId", orderUserId);
                    mapZ.put("goodsId", goodsId);
                    mapZ.put("status", -1);
                    mapZ.put("diffPrice", 0);
                    mapZ.put("remark", remark);
                    mapZ.put("orderType", orderType);
                    System.out.println("mappppzzzzzzRRRRRRRRRR" + mapZ);
                    NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                    //已经有了普通订单，则修改数量
                    if (communityOrdersSubZero != null) {
                        System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubZero.setNxCosQuantity(add.toString());
                        communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                        System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                        nxCommunityOrdersSubService.update(communityOrdersSubZero);

                    } else {
                        //添加信息普通订单
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                        subEntity.setNxCosOrderUserId(orderUserId);
                        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                        subEntity.setNxCosQuantity("1");
                        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        subEntity.setNxCosStatus(-1);
                        subEntity.setNxCosRemark(remark);
                        subEntity.setNxCosType(orderType);
                        subEntity.setNxCosSplicingOrdersId(spId);
                        subEntity.setNxCosOrdersId(pindanId);
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }
            }else{
                //diyige
                System.out.println("idigyigeeiie111111111");
                System.out.println("whgoodsnewCommmonssssssaa" + goodsEntity.getNxCgGoodsName());
                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                subEntity.setNxCosOrderUserId(orderUserId);
                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                subEntity.setNxCosQuantity("1");
                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());
                subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                subEntity.setNxCosStatus(-1);
                subEntity.setNxCosRemark(remark);
                subEntity.setNxCosType(orderType);
                subEntity.setNxCosSplicingOrdersId(spId);
                subEntity.setNxCosOrdersId(pindanId);
                System.out.println("fakdfalfjaslfjsafahfahef");
                nxCommunityOrdersSubService.save(subEntity);
            }

        }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemark", method = RequestMethod.POST)
    public R saveSubOrderRemark(Integer goodsId, Integer orderUserId, String remark, Integer orderType, Integer spId, Integer pindanId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("type", orderType);
        System.out.println("wkwkjekejrelqelwqrekr" + map);
        NxCustomerUserCardEntity userCardEntitys = nxCustomerUserCardService.queryUserGoodsCard(map);
        // 一，如果已经有会员卡
        if (userCardEntitys != null) {
            //1.1 如果会员卡被选择，则继续查询是否可以按照优惠价格保存订单
            if (userCardEntitys.getNxCucaIsSelected() == 1) {
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("dayuDiffPrice", 0);
                mapC.put("remark", remark);
                mapC.put("orderType", orderType);
                //1.1.1 先查询优惠订单是否超过数量
                System.out.println("ccccccccccaaaa" + mapC);
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                if (communityOrdersSubEntity != null) {
                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    BigDecimal orderQuantity = new BigDecimal(communityOrdersSubEntity.getNxCosQuantity());
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);


                    int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                    if (count > 0) {
                        int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                        BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                        //1.1.1.1剩余数量大于 1，则加1
                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                            BigDecimal add = orderQuantity.add(new BigDecimal(1));
                            BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            communityOrdersSubEntity.setNxCosQuantity(add.toString());
                            communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                            communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                            nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                        } else {
                            //1.1.1.2保存普通订单之前先查是否有同样的订单
                            Map<String, Object> mapZ = new HashMap<>();
                            mapZ.put("orderUserId", orderUserId);
                            mapZ.put("goodsId", goodsId);
                            mapZ.put("status", -1);
                            mapZ.put("diffPrice", 0);
                            mapZ.put("remark", remark);
                            mapZ.put("orderType", orderType);
                            System.out.println("mappppzzzzzzRRRRRRRRRR" + mapZ);
                            NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                            //已经有了普通订单，则修改数量
                            if (communityOrdersSubZero != null) {
                                System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                                BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                                BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                                BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                                communityOrdersSubZero.setNxCosQuantity(add.toString());
                                communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                                System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                                nxCommunityOrdersSubService.update(communityOrdersSubZero);

                            } else {
                                //添加信息普通订单
                                System.out.println("whgoodsnewCommmonssssss111" + goodsEntity.getNxCgGoodsName());
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                nxCommunityOrdersSubService.save(subEntity);
                            }
                        }
                    }


                } else {
                    //2，如果

                    Map<String, Object> mapZ = new HashMap<>();
                    mapZ.put("orderUserId", orderUserId);
                    mapZ.put("goodsId", goodsId);
                    mapZ.put("status", -1);
                    mapZ.put("diffPrice", 0);
                    mapZ.put("remark", remark);
                    mapZ.put("orderType", orderType);
                    System.out.println("mappppzzzzzz" + mapZ);
                    NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                    if (communityOrdersSubZero != null) {
                        System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                        BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubZero.setNxCosQuantity(add.toString());
                        communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                        System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                        nxCommunityOrdersSubService.update(communityOrdersSubZero);

                    } else {
                        Map<String, Object> mapT = new HashMap<>();
                        mapT.put("orderUserId", orderUserId);
                        mapT.put("goodsId", goodsId);
                        mapT.put("status", -1);
                        mapT.put("dayuDiffPrice", 0);
                        mapT.put("orderType", orderType);
                        int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                        if (count > 0) {
                            int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                            BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                            BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                            //1.1.1.1剩余数量大于 1，则加1
                            if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                                subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());
                                subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                                subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosOrdersId(pindanId);
                                nxCommunityOrdersSubService.save(subEntity);
                            } else {
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                nxCommunityOrdersSubService.save(subEntity);
                            }

                        }else{
                            //diyige
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());
                            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosOrdersId(pindanId);
                            nxCommunityOrdersSubService.save(subEntity);
                        }


                        //todo zehliyouwent
                        System.out.println("whgoodsnewCommmonssssss" + goodsEntity.getNxCgGoodsName());
//


                    }
                }

            } else {
                //如果会员卡没有被选择，则按照划线价格添加订单
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("orderUserId", orderUserId);
                mapS.put("goodsId", goodsId);
                mapS.put("status", -1);
                mapS.put("remark", remark);
                mapS.put("diffPrice", 0);
                mapS.put("orderType", orderType);
                NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                if (subOrderEntity != null) {
                    BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    subOrderEntity.setNxCosQuantity(add.toString());
                    subOrderEntity.setNxCosSubtotal(subtotal.toString());
                    nxCommunityOrdersSubService.update(subOrderEntity);

                } else {

                    System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosHuaxianDifferentPrice("0");
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    nxCommunityOrdersSubService.save(subEntity);
                }
            }
        } else {
            //第一次保存订单
            System.out.println("dyiicbaocincindnd");

            NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
            subEntity.setNxCosOrderUserId(orderUserId);
            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
            subEntity.setNxCosQuantity("1");
            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());

            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosStatus(-1);
            subEntity.setNxCosRemark(remark);
            subEntity.setNxCosType(orderType);
            subEntity.setNxCosSplicingOrdersId(spId);
            subEntity.setNxCosOrdersId(pindanId);
            nxCommunityOrdersSubService.save(subEntity);

            NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
            userCardEntity.setNxCucaStatus(-1);
            userCardEntity.setNxCucaCustomerUserId(orderUserId);
            userCardEntity.setNxCucaStartDate(formatWhatDay(0));
            userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
            userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
            userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
            userCardEntity.setNxCucaIsSelected(1);
            userCardEntity.setNxCucaType(orderType);
            System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
            nxCustomerUserCardService.save(userCardEntity);

        }


        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        return R.ok().put("data", nxCommunityOrdersSubEntities);

    }


    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemarkHuaxian", method = RequestMethod.POST)
    public R saveSubOrderRemarkHuaxian(Integer goodsId, Integer orderUserId, String remark, Integer orderType, Integer spId, Integer pindanId) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        //一，如果已经有会员卡，则按照优惠订单保存

        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("type", orderType);
        System.out.println("zehlieyoeucarddddddd" + map);
        NxCustomerUserCardEntity userCardEntitys = nxCustomerUserCardService.queryUserGoodsCard(map);
        // 一，如果已经有会员卡
        if (userCardEntitys != null) {

            //1.1 如果会员卡被选择，则继续查询是否可以按照优惠价格保存订单
            if (userCardEntitys.getNxCucaIsSelected() == 1) {

                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("dayuDiffPrice", 0);
                mapC.put("remark", remark);
                mapC.put("orderType", orderType);
                //1.1.1 先查询优惠订单是否超过数量
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                System.out.println("wwwhwiiwiwiwiwwiiwiiwiiw" + communityOrdersSubEntity);

                if (communityOrdersSubEntity != null) {

                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    BigDecimal orderQuantity = new BigDecimal(communityOrdersSubEntity.getNxCosQuantity());
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);
                    int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                    BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                    //1.1.1.1剩余数量大于 1，则加1
                    if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                        BigDecimal add = orderQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubEntity.setNxCosQuantity(add.toString());
                        communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                        communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                        nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                    } else {
                        //1.1.1.2保存普通订单之前先查是否有同样的订单
                        Map<String, Object> mapZ = new HashMap<>();
                        mapZ.put("orderUserId", orderUserId);
                        mapZ.put("goodsId", goodsId);
                        mapZ.put("status", -1);
                        mapZ.put("diffPrice", 0);
                        mapZ.put("remark", remark);
                        mapZ.put("orderType", orderType);
                        System.out.println("mappppzzzzzz" + mapZ);
                        NxCommunityOrdersSubEntity communityOrdersSubZero = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapZ);
                        //已经有了普通订单，则修改数量
                        if (communityOrdersSubZero != null) {
                            System.out.println("meiyouzuozuozzlididiididm" + communityOrdersSubZero.getNxCosQuantity());
                            BigDecimal nxCosQuantity = new BigDecimal(communityOrdersSubZero.getNxCosQuantity());
                            BigDecimal add = nxCosQuantity.add(new BigDecimal(1));
                            BigDecimal subtotal = new BigDecimal(communityOrdersSubZero.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                            communityOrdersSubZero.setNxCosQuantity(add.toString());
                            communityOrdersSubZero.setNxCosSubtotal(subtotal.toString());
                            System.out.println("updddateeeee" + communityOrdersSubZero.getNxCosQuantity());
                            nxCommunityOrdersSubService.update(communityOrdersSubZero);

                        } else {
                            //添加信息普通订单
                            //----
                            System.out.println("whgoodsnewCommmonssssss" + goodsEntity.getNxCgGoodsName());
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosHuaxianDifferentPrice("0");
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosOrdersId(pindanId);
                            nxCommunityOrdersSubService.save(subEntity);
                            //todo zehliyouwent 111
                        }
                    }

                } else {


                    //todo  ooo ooooo2222
                    Map<String, Object> mapT = new HashMap<>();
                    mapT.put("orderUserId", orderUserId);
                    mapT.put("goodsId", goodsId);
                    mapT.put("status", -1);
                    mapT.put("dayuDiffPrice", 0);
                    mapT.put("orderType", orderType);
                    int count = nxCommunityOrdersSubService.querySubOrderCount(mapT);
                    if (count > 0) {
                        int total = nxCommunityOrdersSubService.querySubOrderTotalHuaxianQuantity(mapT);
                        BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                        BigDecimal restQuantity = huaxianQuantity.subtract(new BigDecimal(total)); //剩余可用划线后优惠价格的数量
                        //1.1.1.1剩余数量大于 1，则加1
                        if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                            System.out.println("nanndndndnddnzleiie");
                            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                            subEntity.setNxCosOrderUserId(orderUserId);
                            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                            subEntity.setNxCosQuantity("1");
                            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());

                            subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                            subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                            subEntity.setNxCosStatus(-1);
                            subEntity.setNxCosRemark(remark);
                            subEntity.setNxCosType(orderType);
                            subEntity.setNxCosSplicingOrdersId(spId);
                            subEntity.setNxCosOrdersId(pindanId);
                            nxCommunityOrdersSubService.save(subEntity);
                        } else {

                            System.out.println("jianchaputoggnfndndndndn");
                            Map<String, Object> mapS = new HashMap<>();
                            mapS.put("orderUserId", orderUserId);
                            mapS.put("goodsId", goodsId);
                            mapS.put("status", -1);
                            mapS.put("remark", remark);
                            mapS.put("diffPrice", 0);
                            mapS.put("orderType", orderType);
                            NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                            if (subOrderEntity != null) {
                                BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                                BigDecimal add = orderQuantity.add(new BigDecimal(1));
                                BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                                subOrderEntity.setNxCosQuantity(add.toString());
                                subOrderEntity.setNxCosSubtotal(subtotal.toString());
                                nxCommunityOrdersSubService.update(subOrderEntity);

                            } else {

                                System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                                NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                                subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                                subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                                subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                                subEntity.setNxCosOrderUserId(orderUserId);
                                subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                                subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                                subEntity.setNxCosQuantity("1");
                                subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                                subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                                subEntity.setNxCosHuaxianDifferentPrice("0");
                                subEntity.setNxCosStatus(-1);
                                subEntity.setNxCosRemark(remark);
                                subEntity.setNxCosType(orderType);
                                subEntity.setNxCosSplicingOrdersId(spId);
                                subEntity.setNxCosOrdersId(pindanId);
                                nxCommunityOrdersSubService.save(subEntity);
                            }
                        }
                    } else {

                        //todo kk
                        System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsPrice());
                    subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsPrice());
                    subEntity.setNxCosHuaxianPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosHuaxianDifferentPrice(goodsEntity.getNxCgGoodsHuaxianPriceDifferent());
                    subEntity.setNxCosHuaxianSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }


            } else {
                //如果会员卡没有被选择，则按照划线价格添加订单
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("orderUserId", orderUserId);
                mapS.put("goodsId", goodsId);
                mapS.put("status", -1);
                mapS.put("remark", remark);
                mapS.put("diffPrice", 0);
                mapS.put("orderType", orderType);
                NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapS);
                if (subOrderEntity != null) {
                    System.out.println("neexxxx222");
                    BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                    BigDecimal add = orderQuantity.add(new BigDecimal(1));
                    BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                    subOrderEntity.setNxCosQuantity(add.toString());
                    subOrderEntity.setNxCosSubtotal(subtotal.toString());
                    nxCommunityOrdersSubService.update(subOrderEntity);

                } else {

                    System.out.println("whgoodsnewCommmon222222" + goodsEntity.getNxCgGoodsName());
                    NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                    subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                    subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                    subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                    subEntity.setNxCosOrderUserId(orderUserId);
                    subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                    subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                    subEntity.setNxCosQuantity("1");
                    subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                    subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                    subEntity.setNxCosHuaxianDifferentPrice("0");
                    subEntity.setNxCosStatus(-1);
                    subEntity.setNxCosRemark(remark);
                    subEntity.setNxCosType(orderType);
                    subEntity.setNxCosSplicingOrdersId(spId);
                    subEntity.setNxCosOrdersId(pindanId);
                    nxCommunityOrdersSubService.save(subEntity);
                }
            }



        } else {


            //二，如果没有会员卡，则按照普通订单保存，并添加未选择会员卡
            System.out.println("whgoodsnewCommmon222222333" + goodsEntity.getNxCgGoodsName());
            NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
            subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
            subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
            subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
            subEntity.setNxCosOrderUserId(orderUserId);
            subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
            subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
            subEntity.setNxCosQuantity("1");
            subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
            subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
            subEntity.setNxCosHuaxianDifferentPrice("0");
            subEntity.setNxCosStatus(-1);
            subEntity.setNxCosRemark(remark);
            subEntity.setNxCosType(orderType);
            subEntity.setNxCosSplicingOrdersId(spId);
            subEntity.setNxCosOrdersId(pindanId);
            nxCommunityOrdersSubService.save(subEntity);

            NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
            NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
            userCardEntity.setNxCucaStatus(-1);
            userCardEntity.setNxCucaCustomerUserId(orderUserId);
            userCardEntity.setNxCucaStartDate(formatWhatDay(0));
            userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
            userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
            userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
            userCardEntity.setNxCucaIsSelected(0);
            userCardEntity.setNxCucaType(orderType);
            System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
            nxCustomerUserCardService.save(userCardEntity);


        }


        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/addGoodsOrder", method = RequestMethod.POST)
    public R addGoodsOrder(Integer goodsId, Integer orderUserId, Integer orderType, Integer spId, Integer pindanId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianPrice() == null) {
            //commAdd
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("orderType", orderType);
            map.put("status", -1);
            NxCommunityOrdersSubEntity subOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (subOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(subOrderEntity.getNxCosQuantity());
                BigDecimal add = orderQuantity.add(new BigDecimal(1));
                BigDecimal subtotal = new BigDecimal(subOrderEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                subOrderEntity.setNxCosQuantity(add.toString());
                subOrderEntity.setNxCosSubtotal(subtotal.toString());
                nxCommunityOrdersSubService.update(subOrderEntity);
            }

        } else {
            //pandan
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("orderType", orderType);
            map.put("status", -1);
            map.put("diffPrice", 0);
            //先查是否有普通订单
            System.out.println("addddddd" + map);
            NxCommunityOrdersSubEntity putongSubOrder = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (putongSubOrder != null) {
                //普通订单加 1
                BigDecimal orderQuantity = new BigDecimal(putongSubOrder.getNxCosQuantity());
                BigDecimal add = orderQuantity.add(new BigDecimal(1));

                BigDecimal subtotal = new BigDecimal(putongSubOrder.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                putongSubOrder.setNxCosQuantity(add.toString());
                putongSubOrder.setNxCosSubtotal(subtotal.toString());
                nxCommunityOrdersSubService.update(putongSubOrder);

            } else {
                //查询优惠订单
                Map<String, Object> mapC = new HashMap<>();
                mapC.put("orderUserId", orderUserId);
                mapC.put("goodsId", goodsId);
                mapC.put("status", -1);
                mapC.put("orderType", orderType);
                mapC.put("dayuDiffPrice", 0);
                //先查询优惠订单是否超过数量
                System.out.println("youhuidiadddddd" + mapC);
                NxCommunityOrdersSubEntity communityOrdersSubEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapC);
                if (communityOrdersSubEntity != null) {

                    BigDecimal huaxianQuantity = new BigDecimal(goodsEntity.getNxCgGoodsHuaxianQuantity());
                    BigDecimal orderQuantity = new BigDecimal(communityOrdersSubEntity.getNxCosQuantity());
                    //剩余可用划线后优惠价格的数量
                    BigDecimal restQuantity = huaxianQuantity.subtract(orderQuantity);
                    System.out.println("resssddfsadsfdafaf" + restQuantity);
                    //剩余数量大于 1，则加1
                    if (restQuantity.compareTo(new BigDecimal(0)) == 1) {
                        BigDecimal add = orderQuantity.add(new BigDecimal(1));
                        BigDecimal subtotal = new BigDecimal(communityOrdersSubEntity.getNxCosPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal huaxianSubtotal = new BigDecimal(communityOrdersSubEntity.getNxCosHuaxianPrice()).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        communityOrdersSubEntity.setNxCosQuantity(add.toString());
                        communityOrdersSubEntity.setNxCosSubtotal(subtotal.toString());
                        communityOrdersSubEntity.setNxCosHuaxianSubtotal(huaxianSubtotal.toString());
                        nxCommunityOrdersSubService.update(communityOrdersSubEntity);
                    } else {
                        System.out.println("whgoodsnewCommmon" + goodsEntity.getNxCgGoodsName());
                        NxCommunityOrdersSubEntity subEntity = new NxCommunityOrdersSubEntity();
                        subEntity.setNxCosCommunityId(goodsEntity.getNxCgCommunityId());
                        subEntity.setNxCosCommunityGoodsId(goodsEntity.getNxCommunityGoodsId());
                        subEntity.setNxCosCommunityGoodsFatherId(goodsEntity.getNxCgCfgGoodsFatherId());
                        subEntity.setNxCosOrderUserId(orderUserId);
                        subEntity.setNxCosGoodsType(goodsEntity.getNxCgGoodsType());
                        subEntity.setNxCosGoodsSellType(goodsEntity.getNxCgSellType());
                        subEntity.setNxCosQuantity("1");
                        subEntity.setNxCosStandard(goodsEntity.getNxCgGoodsStandardname());
                        subEntity.setNxCosPrice(goodsEntity.getNxCgGoodsHuaxianPrice());
                        subEntity.setNxCosSubtotal(goodsEntity.getNxCgGoodsHuaxianPrice());
                        subEntity.setNxCosHuaxianDifferentPrice("0");
                        subEntity.setNxCosStatus(-1);
                        subEntity.setNxCosType(orderType);
                        subEntity.setNxCosSplicingOrdersId(spId);
                        subEntity.setNxCosOrdersId(pindanId);
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }
            }
        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("oderType", orderType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        return R.ok().put("data", nxCommunityOrdersSubEntities);


    }


    @ResponseBody
    @RequestMapping(value = "/reduceGoodsOrder", method = RequestMethod.POST)
    public R reduceGoodsOrder(Integer goodsId, Integer orderUserId, Integer orderType) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianPrice() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("diffPrice", 0);
            map.put("orderType", orderType);
            NxCommunityOrdersSubEntity diffZeroSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (diffZeroSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(diffZeroSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal subtract = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(diffZeroSubOrderEntity.getNxCosPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffZeroSubOrderEntity.setNxCosQuantity(subtract.toString());
                    diffZeroSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    nxCommunityOrdersSubService.update(diffZeroSubOrderEntity);
                }
            } else {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("orderUserId", orderUserId);
                mapD.put("goodsId", goodsId);
                mapD.put("status", -1);
                mapD.put("dayuDiffPrice", 0);
                mapD.put("orderType", orderType);
                System.out.println("dayoouuiiuiououo" + mapD);
                //有优惠的订单
                NxCommunityOrdersSubEntity diffSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapD);
                if (diffSubOrderEntity != null) {
                    BigDecimal orderQuantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity());
                    if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                        //1，删除订单
                        nxCommunityOrdersSubService.delete(diffSubOrderEntity.getNxCommunityOrdersSubId());
                    } else {
                        BigDecimal subtract = new BigDecimal(diffSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                        BigDecimal decimal = new BigDecimal(diffSubOrderEntity.getNxCosPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                        diffSubOrderEntity.setNxCosQuantity(subtract.toString());
                        diffSubOrderEntity.setNxCosSubtotal(decimal.toString());
                        nxCommunityOrdersSubService.update(diffSubOrderEntity);
                    }

                }
            }

        } else {

            //common -
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("orderType", orderType);
            NxCommunityOrdersSubEntity commSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
            if (commSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(commSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(commSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal subtract = new BigDecimal(commSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(commSubOrderEntity.getNxCosPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                    commSubOrderEntity.setNxCosQuantity(subtract.toString());
                    commSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    nxCommunityOrdersSubService.update(commSubOrderEntity);
                }
            }

        }


        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        if(nxCommunityOrdersSubEntities.size() > 0){

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
            mapCard.put("type",orderType);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            int hasApply = 0;
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){

                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(userCardEntity.getNxCucaCardId());
                Integer nxCommunityCardId = cardEntity.getNxCommunityCardId();
                for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntityS = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    if(goodsEntityS.getNxCgCardId() != null){
                        if(goodsEntityS.getNxCgCardId().equals(nxCommunityCardId)){
                            hasApply = 1;
                        }
                    }
                }
                if(hasApply == 0){
                    nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                }
            }

        }
        else{
            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
            mapCard.put("type",orderType);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }

        }



        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("orderUserId",orderUserId);
        mapCard.put("type",orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList",  cardEntities);
        return R.ok().put("data",mapData);


    }


    @ResponseBody
    @RequestMapping(value = "/reduceSubOrderRemark", method = RequestMethod.POST)
    public R reduceSubOrderRemark(Integer goodsId, Integer orderUserId, String remark, Integer orderType) {


        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("diffPrice", 0);
        map.put("remark", remark);
        map.put("orderType", orderType);
        System.out.println("reeeeke" + map);
        NxCommunityOrdersSubEntity diffZeroSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(map);
        if (diffZeroSubOrderEntity != null) {
            BigDecimal orderQuantity = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity());
            if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                //1，删除订单
                nxCommunityOrdersSubService.delete(diffZeroSubOrderEntity.getNxCommunityOrdersSubId());
            } else {
                BigDecimal subtract = new BigDecimal(diffZeroSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                BigDecimal decimal = new BigDecimal(diffZeroSubOrderEntity.getNxCosPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                diffZeroSubOrderEntity.setNxCosQuantity(subtract.toString());
                diffZeroSubOrderEntity.setNxCosSubtotal(decimal.toString());
                nxCommunityOrdersSubService.update(diffZeroSubOrderEntity);
            }
        } else {
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("orderUserId", orderUserId);
            mapD.put("goodsId", goodsId);
            mapD.put("status", -1);
            mapD.put("dayuDiffPrice", 0);
            mapD.put("remark", remark);
            mapD.put("orderType", orderType);
            System.out.println("dayoouuiiuiououo" + mapD);
            //有优惠的订单
            NxCommunityOrdersSubEntity diffSubOrderEntity = nxCommunityOrdersSubService.queryChangeSubOrderByParams(mapD);
            if (diffSubOrderEntity != null) {
                BigDecimal orderQuantity = new BigDecimal(diffSubOrderEntity.getNxCosQuantity());
                if (orderQuantity.compareTo(new BigDecimal(1)) == 0) {
                    //1，删除订单
                    nxCommunityOrdersSubService.delete(diffSubOrderEntity.getNxCommunityOrdersSubId());
                } else {
                    BigDecimal subtract = new BigDecimal(diffSubOrderEntity.getNxCosQuantity()).subtract(new BigDecimal(1));
                    BigDecimal decimal = new BigDecimal(diffSubOrderEntity.getNxCosPrice()).multiply(subtract).setScale(1, BigDecimal.ROUND_HALF_UP);
                    diffSubOrderEntity.setNxCosQuantity(subtract.toString());
                    diffSubOrderEntity.setNxCosSubtotal(decimal.toString());
                    nxCommunityOrdersSubService.update(diffSubOrderEntity);
                }

            }
        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("orderType", orderType);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);


        if(nxCommunityOrdersSubEntities.size() > 0){

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
            mapCard.put("type",orderType);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            int hasApply = 0;
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){

                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(userCardEntity.getNxCucaCardId());
                Integer nxCommunityCardId = cardEntity.getNxCommunityCardId();
                for(NxCommunityOrdersSubEntity subEntity: nxCommunityOrdersSubEntities){
                    Integer nxCosCommunityGoodsId = subEntity.getNxCosCommunityGoodsId();
                    NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(nxCosCommunityGoodsId);
                    if(goodsEntity.getNxCgCardId() != null){
                        if(goodsEntity.getNxCgCardId().equals(nxCommunityCardId)){
                            hasApply = 1;
                        }
                    }
                }
                if(hasApply == 0){
                    nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
                }
            }

        }
        else{
            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
            mapCard.put("type",orderType);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }

        }



        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("orderUserId",orderUserId);
        mapCard.put("type",orderType);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList",  cardEntities);
        return R.ok().put("data",mapData);

    }


    @ResponseBody
    @RequestMapping("/saveSubOrderCoupon")
    public R saveSubOrderCoupon(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {
        nxCommunityOrdersSubService.save(nxOrdersSub);
        Integer nxCosCucId = nxOrdersSub.getNxCosCucId();
        NxCustomerUserCouponEntity userCouponEntity = nxCustomerUserCouponService.equalObject(nxCosCucId);
        userCouponEntity.setNxCucStatus(1);
        userCouponEntity.setNxCucSubOrderId(nxOrdersSub.getNxCommunityOrdersSubId());
        nxCustomerUserCouponService.update(userCouponEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/deleteSubOrders")
    public R deleteSubOrders(@RequestBody Integer[] nxOrdersSubIds) {
        for(Integer i : nxOrdersSubIds){
            NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(i);
            if(subEntity.getNxCosCucId() != null){
                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                customerUserCouponEntity.setNxCucSubOrderId(null);
                customerUserCouponEntity.setNxCucStatus(0);
                nxCustomerUserCouponService.update(customerUserCouponEntity);
            }
        }
        nxCommunityOrdersSubService.deleteBatch(nxOrdersSubIds);


        Map<String, Object> mapC = new HashMap<>();
        mapC.put("status", -1);
        mapC.put("type", 0);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
          if(cardEntities.size() > 0){
              for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                  nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
              }
          }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/deleteSubOrdersPindan")
    public R deleteSubOrdersPindan(@RequestBody Integer[] nxOrdersSubIds) {
        for(Integer i : nxOrdersSubIds){
            NxCommunityOrdersSubEntity subEntity = nxCommunityOrdersSubService.queryObject(i);
            if(subEntity.getNxCosCucId() != null){
                NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(subEntity.getNxCosCucId());
                customerUserCouponEntity.setNxCucSubOrderId(null);
                customerUserCouponEntity.setNxCucStatus(0);
                nxCustomerUserCouponService.update(customerUserCouponEntity);
            }
        }
        nxCommunityOrdersSubService.deleteBatch(nxOrdersSubIds);


        Map<String, Object> mapC = new HashMap<>();
        mapC.put("status", -1);
        mapC.put("type", 1);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
        if(cardEntities.size() > 0){
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }
        }
        return R.ok();
    }


}
