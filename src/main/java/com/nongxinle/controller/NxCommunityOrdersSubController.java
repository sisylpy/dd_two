
package com.nongxinle.controller;

/**
 * @author lpy
 * @date 2020-03-22 18:07:28
 */


import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIAttribute;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.getNowMinute;


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
        map.put("time", formatWhatDayMinute(50));
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


    @RequestMapping(value = "/updateSplicingOrder/{id}")
    @ResponseBody
    public R updateSplicingOrder(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("dayuType", 3);
        List<NxCommunityOrdersSubEntity> subEntities = nxCommunityOrdersSubService.querySubOrdersByParams(map);
        BigDecimal total = new BigDecimal(0);
        BigDecimal youhuiTotal = new BigDecimal(0);
        if (subEntities.size() > 0) {
            for (NxCommunityOrdersSubEntity subEntity : subEntities) {
                BigDecimal subtotal = new BigDecimal(subEntity.getNxCosSubtotal());
                total = total.add(subtotal);
                if (!subEntity.getNxCosHuaxianDifferentPrice().equals("0")) {
                    BigDecimal diffTotal = new BigDecimal(subEntity.getNxCosQuantity()).multiply(new BigDecimal(subEntity.getNxCosHuaxianDifferentPrice()));
                    BigDecimal dif = diffTotal.setScale(1, BigDecimal.ROUND_HALF_UP);
                    youhuiTotal = youhuiTotal.add(dif);
                }
            }
        }
        NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(id);
        splicingOrdersEntity.setNxCsoTotal(total.toString());
        splicingOrdersEntity.setNxCsoYouhuiTotal(youhuiTotal.toString());
        nxCommSplicingOrdersService.update(splicingOrdersEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping("/saveSubOrderPindan")
    public R saveSubOrderPindan(@RequestBody NxCommunityOrdersSubEntity nxOrdersSub) {
        nxCommunityOrdersSubService.save(nxOrdersSub);
        Integer nxCosOrdersId = nxOrdersSub.getNxCosOrdersId();

        NxCommunitySplicingOrdersEntity splicingOrdersEntity = nxCommSplicingOrdersService.queryObject(nxCosOrdersId);
        BigDecimal decimal = new BigDecimal(splicingOrdersEntity.getNxCsoTotal());
        BigDecimal decimal1 = new BigDecimal(nxOrdersSub.getNxCosSubtotal());
        BigDecimal total = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
        splicingOrdersEntity.setNxCsoTotal(total.toString());

        if (!nxOrdersSub.getNxCosHuaxianDifferentPrice().equals("0")) {
            BigDecimal diffTotal = new BigDecimal(nxOrdersSub.getNxCosQuantity()).multiply(new BigDecimal(nxOrdersSub.getNxCosHuaxianDifferentPrice()));
            BigDecimal decimal2 = diffTotal.setScale(1, BigDecimal.ROUND_HALF_UP);
            BigDecimal add = new BigDecimal(splicingOrdersEntity.getNxCsoYouhuiTotal()).add(decimal2);
            splicingOrdersEntity.setNxCsoYouhuiTotal(add.toString());
        }

        splicingOrdersEntity.setNxCsoStatus(1);
        nxCommSplicingOrdersService.update(splicingOrdersEntity);

        return R.ok().put("data", nxOrdersSub);
    }








    @ResponseBody
    @RequestMapping(value = "/saveSubOrder", method = RequestMethod.POST)
    public R saveSubOrder(Integer goodsId, Integer orderUserId, Integer pindanId) {

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
        if(pindanId != -1){
            subEntity.setNxCosPindanOrdersId(pindanId);
        }
        nxCommunityOrdersSubService.save(subEntity);

        //判断是否是会员卡商品
        if (goodsEntity.getNxCgCardId() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("cardId", goodsEntity.getNxCgCardId());
            map.put("stopTime", formatWhatDay(0));
            map.put("userId", orderUserId);
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
            if (cardEntities.size() == 0) {
                NxCommunityCardEntity cardEntity = nxCommunityCardService.queryObject(goodsEntity.getNxCgCardId());
                NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
                userCardEntity.setNxCucaStatus(-1);
                userCardEntity.setNxCucaCustomerUserId(orderUserId);
                userCardEntity.setNxCucaStartDate(formatWhatDay(0));
                userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
                userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
                userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
                userCardEntity.setNxCucaIsSelected(1);
                nxCustomerUserCardService.save(userCardEntity);
            }
        }

        return R.ok().put("data", subEntity);

    }


    @RequestMapping(value = "/memberSaveMemberOrderRemark", method = RequestMethod.POST)
    @ResponseBody
    public R memberSaveMemberOrderRemark (Integer goodsId, Integer orderUserId, String remark) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> mapC = new HashMap<>();
        mapC.put("orderUserId", orderUserId);
        mapC.put("goodsId", goodsId);
        mapC.put("status", -1);
        mapC.put("diffPrice", 0);
        mapC.put("remark", remark);
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
                        System.out.println("whgoodsnewCommmonssssss" + goodsEntity.getNxCgGoodsName());
                        System.out.println("idigyigeeiieaaaa");

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
                        nxCommunityOrdersSubService.save(subEntity);
                    }
                }
            }else{
                //diyige
                System.out.println("idigyigeeiie111111111");
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
                nxCommunityOrdersSubService.save(subEntity);
            }

        }
        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemark", method = RequestMethod.POST)
    public R saveSubOrderRemark(Integer goodsId, Integer orderUserId, String remark) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
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
                            nxCommunityOrdersSubService.save(subEntity);
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
                            subEntity.setNxCosRemark(remark);
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
            nxCommunityOrdersSubService.save(subEntity);

            NxCustomerUserCardEntity userCardEntity = new NxCustomerUserCardEntity();
            userCardEntity.setNxCucaStatus(-1);
            userCardEntity.setNxCucaCustomerUserId(orderUserId);
            userCardEntity.setNxCucaStartDate(formatWhatDay(0));
            userCardEntity.setNxCucaStopDate(formatWhatDay(Integer.valueOf(cardEntity.getNxCcEffectiveDays())));
            userCardEntity.setNxCucaCardId(cardEntity.getNxCommunityCardId());
            userCardEntity.setNxCucaCommunityId(cardEntity.getNxCcCommunityId());
            userCardEntity.setNxCucaIsSelected(1);
            System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
            nxCustomerUserCardService.save(userCardEntity);

        }


        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("xiaoyuGoodsType", 4);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        return R.ok().put("data", nxCommunityOrdersSubEntities);

    }


    @ResponseBody
    @RequestMapping(value = "/saveSubOrderRemarkHuaxian", method = RequestMethod.POST)
    public R saveSubOrderRemarkHuaxian(Integer goodsId, Integer orderUserId, String remark) {
        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        //一，如果已经有会员卡，则按照优惠订单保存

        Map<String, Object> map = new HashMap<>();
        map.put("cardId", goodsEntity.getNxCgCardId());
        map.put("stopTime", formatWhatDay(0));
        map.put("userId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
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
                            nxCommunityOrdersSubService.save(subEntity);
                        } else {

                            System.out.println("jianchaputoggnfndndndndn");
                            Map<String, Object> mapS = new HashMap<>();
                            mapS.put("orderUserId", orderUserId);
                            mapS.put("goodsId", goodsId);
                            mapS.put("status", -1);
                            mapS.put("remark", remark);
                            mapS.put("diffPrice", 0);
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
            System.out.println("nimeiieyeoeueoueoeu" + userCardEntity.getNxCucaIsSelected());
            nxCustomerUserCardService.save(userCardEntity);


        }


        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/addGoodsOrder", method = RequestMethod.POST)
    public R addGoodsOrder(Integer goodsId, Integer orderUserId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianPrice() == null) {
            //commAdd
            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
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
                        nxCommunityOrdersSubService.save(subEntity);
                    }

                }
            }
        }

        //giveapply
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("orderUserId", orderUserId);
        mapA.put("status", -1);
        mapA.put("xiaoyuGoodsType", 4);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);


        return R.ok().put("data", nxCommunityOrdersSubEntities);


    }


    @ResponseBody
    @RequestMapping(value = "/reduceGoodsOrder", method = RequestMethod.POST)
    public R reduceGoodsOrder(Integer goodsId, Integer orderUserId) {

        NxCommunityGoodsEntity goodsEntity = nxCommunityGoodsService.queryObject(goodsId);
        if (goodsEntity.getNxCgGoodsHuaxianPrice() != null) {

            Map<String, Object> map = new HashMap<>();
            map.put("orderUserId", orderUserId);
            map.put("goodsId", goodsId);
            map.put("status", -1);
            map.put("diffPrice", 0);
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
        mapA.put("xiaoyuGoodsType", 4);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);

        if(nxCommunityOrdersSubEntities.size() > 0){

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
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
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }

        }



        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("orderUserId",orderUserId);
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);

        Map<String, Object> mapData = new HashMap<>();
        mapData.put("arr", nxCommunityOrdersSubEntities);
        mapData.put("cardList",  cardEntities);
        return R.ok().put("data",mapData);


    }


    @ResponseBody
    @RequestMapping(value = "/reduceSubOrderRemark", method = RequestMethod.POST)
    public R reduceSubOrderRemark(Integer goodsId, Integer orderUserId, String remark) {


        Map<String, Object> map = new HashMap<>();
        map.put("orderUserId", orderUserId);
        map.put("goodsId", goodsId);
        map.put("status", -1);
        map.put("diffPrice", 0);
        map.put("remark", remark);
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
        mapA.put("xiaoyuGoodsType", 4);
        System.out.println("apappapap" + mapA);
        List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities = nxCommunityOrdersSubService.querySubOrdersByParams(mapA);


        if(nxCommunityOrdersSubEntities.size() > 0){

            Map<String, Object> mapCard = new HashMap<>();
            mapCard.put("status", -1);
            mapCard.put("orderUserId",orderUserId);
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
            List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapCard);
            for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
            }

        }



        Map<String, Object> mapCard = new HashMap<>();
        mapCard.put("status", -1);
        mapCard.put("orderUserId",orderUserId);
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
        List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(mapC);
          if(cardEntities.size() > 0){
              for(NxCustomerUserCardEntity userCardEntity: cardEntities){
                  nxCustomerUserCardService.delete(userCardEntity.getNxCustomerUserCardId());
              }
          }
        return R.ok();
    }



}
