package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-24 01:00
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.NxCustomerUserCouponEntity;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.NxCustomerUserCardEntity;
import com.nongxinle.service.NxCustomerUserCardService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxcustomerusercard")
public class NxCustomerUserCardController {
	@Autowired
	private NxCustomerUserCardService nxCustomerUserCardService;

	@RequestMapping(value = "/userGetCards/{id}")
	@ResponseBody
	public R userGetCards (@PathVariable Integer id) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", id);
		map.put("status", 1);
		System.out.println("getupodddi");
		List<NxCustomerUserCardEntity> cardEntities = nxCustomerUserCardService.queryUserCardByParams(map);
		return R.ok().put("data", cardEntities);
	}
	

	
}
