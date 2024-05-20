package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:30
 */

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCommunityCouponEntity;
import com.nongxinle.service.NxCommunityCouponService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.service.NxCustomerUserCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcustomerusercoupon")
public class NxCustomerUserCouponController {
	@Autowired
	private NxCustomerUserCouponService nxCustomerUserCouponService;
	@Autowired
	private NxCommunityCouponService nxCommunityCouponService;



	//
	@RequestMapping(value = "/receiveSubOrderCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R receiveSubOrderCoupon (Integer userId, Integer coupId, Integer shareUserId) {
		System.out.println("soudfid" + coupId);
		NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.equalObject(coupId);
		customerUserCouponEntity.setNxCucCustomerUserId(userId);
		customerUserCouponEntity.setNxCucShareUserId(shareUserId);
		customerUserCouponEntity.setNxCucStatus(0);
		nxCustomerUserCouponService.update(customerUserCouponEntity);
		return R.ok();
	}

	@RequestMapping(value = "/getCoupDetail/{id}")
	@ResponseBody
	public R getCoupDetail(@PathVariable Integer id) {

		NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.queryUserCouponDetail(id);
		return R.ok().put("data", customerUserCouponEntity);
	}


	@RequestMapping(value = "/shareCoupon/{id}")
	@ResponseBody
	public R shareCoupon(@PathVariable Integer id) {
		NxCustomerUserCouponEntity customerUserCouponEntity = nxCustomerUserCouponService.queryUserCouponDetail(id);
		customerUserCouponEntity.setNxCucStatus(-1);
		nxCustomerUserCouponService.update(customerUserCouponEntity);
		return R.ok().put("data", customerUserCouponEntity);

	}


	@RequestMapping(value = "/userGetCouponPick", method = RequestMethod.POST)
	@ResponseBody
	public R userGetCouponPick (Integer userId, Integer type) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("type", type );
		map.put("xiaoyuStatus", 1);
		System.out.println("getupodddi");
	   List<NxCustomerUserCouponEntity> couponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(map);
		return R.ok().put("data", couponEntities);
	}
	@RequestMapping(value = "/customerUserSaveCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R customerUserSaveCoupon (Integer userId, Integer couId, Integer commId) {
		NxCustomerUserCouponEntity customerUserCouponEntity = new NxCustomerUserCouponEntity();
		customerUserCouponEntity.setNxCucCommunityId(commId);
		customerUserCouponEntity.setNxCucCustomerUserId(userId);
		customerUserCouponEntity.setNxCucCouponId(couId);
		customerUserCouponEntity.setNxCucStatus(0);
		customerUserCouponEntity.setNxCucType(0);
		nxCustomerUserCouponService.save(customerUserCouponEntity);

	    return R.ok();
	}




	@RequestMapping(value = "/userGetCoupous/{id}")
	@ResponseBody
	public R userGetCoupons(@PathVariable Integer id) {

//		String nxCpStopTimeZone = coupon.getNxCpStopTimeZone();
		String nxCpStopTimeZone = "2024-05-01-00-00-00";
		String[] split = nxCpStopTimeZone.split("-");

		int year = Integer.parseInt(split[0]);
		int month = Integer.parseInt(split[1]);
		int day = Integer.parseInt(split[2]);
		int hour = Integer.parseInt(split[3]);
		int minute = Integer.parseInt(split[4]);
		int haomiao = Integer.parseInt(split[5]);
		LocalDateTime beginTime = LocalDateTime.of(year, month, day, hour, minute, haomiao);
		LocalDateTime now = LocalDateTime.now();
		if(now.isAfter(beginTime)){
			System.out.println("1ok");
		}else{
			System.out.println("nook");
		}
		return R.ok();
	}



}
