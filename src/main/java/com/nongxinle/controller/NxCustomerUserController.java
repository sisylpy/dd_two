package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 19:11:55
 */

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.NxCommunityCouponService;
import com.nongxinle.service.NxCustomerService;
import com.nongxinle.service.NxCustomerUserCouponService;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.UploadFile;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCustomerUserService;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.afterWhatDay;
import static com.nongxinle.utils.DateUtils.getHowManyDaysInPeriod;


@RestController
@RequestMapping("api/nxcustomeruser")
public class NxCustomerUserController {
	@Autowired
	private NxCustomerUserService nxCustomerUserService;

	@Autowired
	private NxCustomerService nxCustomerService;
	@Autowired
	private NxCommunityCouponService nxCommunityCouponService;
	@Autowired
	private NxCustomerUserCouponService nxCustomerUserCouponService;





	@RequestMapping(value = "/getDayCustomer", method = RequestMethod.POST)
	@ResponseBody
	public R getDayCustomer(Integer commId, String date) {

		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
		map.put("date", date);
		System.out.println("dateeee" + map);
		List<NxCustomerUserEntity> userEntities = nxCustomerUserService.queryCustomerByParams(map);

		return R.ok().put("data", userEntities);
	}




	@RequestMapping(value = "/getCustomerEveryDay", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> getCustomerEveryDay(String startDate, String stopDate, Integer commId) {

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
				String substring = whichDay.substring(8, 10);
				dateList.add(substring);

//				String dailyFresh = "0";
				Integer integer = nxCustomerUserService.queryCustomerUserCount(map);
//				if (integer > 0) {
////					double subtotal = nxCustomerUserService.query(map);
////					dailyFresh = new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
//
//				}
				totalList.add(integer.toString());
				Map<String, Object> mapItem = new HashMap<>();
				mapItem.put("day", whichDay);
				mapItem.put("value", integer);
				itemList.add(mapItem);
				mapR.put("date", dateList);
				mapR.put("list", totalList);
				mapR.put("arr", itemList);

			}

		}
		return R.ok().put("data", mapR);

	}



	@RequestMapping(value = "/commSearchCustomer", method = RequestMethod.POST)
	@ResponseBody
	public R commSearchCustomer (Integer commId, String phone) {

		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
		map.put("phone", phone);
		System.out.println("searcusut" + map);
		List<NxCustomerUserEntity> userEntities =  nxCustomerUserService.queryCustomerByParams(map);
	    return R.ok().put("data", userEntities);
	}



	@RequestMapping(value = "/updateCustomerUser", method = RequestMethod.POST)
	@ResponseBody
	public R updateCustomerUser (@RequestBody NxCustomerUserEntity user) {
	    nxCustomerUserService.update(user);
	    return R.ok();
	}

	@RequestMapping(value = "/updateCustomerUserWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCustomerUserWithFile(@RequestParam("file") MultipartFile file,
									@RequestParam("userId") Integer userId,
									HttpSession session) {
		//1,上传图片
		String newUploadName = "userImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(userId);
		if (userEntity.getNxCuWxAvatarUrl() != null) {
			ServletContext servletContext = session.getServletContext();
			String realPath1 = servletContext.getRealPath(userEntity.getNxCuWxAvatarUrl());
			File file1 = new File(realPath1);
			if (file1.exists() && !realPath1.equals("userImage/myUrl.png")) {
				file1.delete();
			}
		}

		userEntity.setNxCuWxAvatarUrl(filePath);
		nxCustomerUserService.update(userEntity);
		return R.ok();
	}



	@RequestMapping(value = "/deleteCustomerUser/{id}")
	@ResponseBody
	public R deleteCustomerUser(@PathVariable Integer id) {
		NxCustomerUserEntity userEntity = nxCustomerUserService.queryObject(id);
		Integer nxCuCustomerId = userEntity.getNxCuCustomerId();
		nxCustomerService.delete(nxCuCustomerId);
		nxCustomerUserService.delete(id);
		return R.ok();
	}





	@RequestMapping(value = "/customerUserLogin/{code}")
	@ResponseBody
	public R customerUserLogin(@PathVariable String code) {

		System.out.println("customerUserLogincodee" + code);
		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String liancaiKufangAppId = myAPPIDConfig.getShixianLiliAppId();
		String liancaiKufangScreat = myAPPIDConfig.getShixianLiliScreat();

		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + liancaiKufangAppId + "&secret=" +
				liancaiKufangScreat + "&js_code=" + code +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);
		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();
		if (openId != null) {
			System.out.println("ageopddid" + openId);
			NxCustomerUserEntity userEntity = nxCustomerUserService.queryUserByOpenId(openId);
			if (userEntity != null) {
			Map<String, Object> stringObjectMap = nxCustomerUserService.queryCustomerUserInfo(userEntity.getNxCuUserId());
				System.out.println("whsiisiiwiwiw" + stringObjectMap);
				Map<String, Object> map = new HashMap<>();
				map.put("commId", userEntity.getNxCuCommunityId());
				map.put("status", 0);
				NxCommunityCouponEntity communityCouponEntity =  nxCommunityCouponService.queryCustomerShowCoupon(map);
				if(communityCouponEntity != null){
					Map<String, Object> mapIF = new HashMap<>();
					mapIF.put("coupId", communityCouponEntity.getNxCommunityCouponId());
					mapIF.put("userId", userEntity.getNxCuUserId());
					List<NxCustomerUserCouponEntity> nxCustomerUserCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(mapIF);
					if(nxCustomerUserCouponEntities.size() == 0){
						stringObjectMap.put("coupon", communityCouponEntity);
					}else{
						stringObjectMap.put("coupon", null);
					}
				}else{
					stringObjectMap.put("coupon", null);
				}

				return R.ok().put("data", stringObjectMap);
			} else {
				return R.error(-1, openId);
			}

		} else {
			return R.error(-1, openId);
		}
	}






	
}
