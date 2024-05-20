package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 11-30 21:47
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.ParseObject;
import com.nongxinle.utils.WeChatUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityUserService;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.formatWhatDay;


@RestController
@RequestMapping("api/nxcommunityuser")
public class NxCommunityUserController {
	@Autowired
	private NxCommunityUserService nxCommunityUserService;



	@RequestMapping(value = "/updateCustomerUser", method = RequestMethod.POST)
	@ResponseBody
	public R updateCustomerUser (@RequestBody NxCommunityUserEntity customerUser) {
		nxCommunityUserService.update(customerUser);
		return R.ok();
	}



	@RequestMapping(value = "/registerComAdminUser", method = RequestMethod.POST)
	@ResponseBody
	public R registerComAdminUser (@RequestBody NxCommunityUserEntity user ) {
		System.out.println("comusr===" + user);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();

		// 1, 先检查微信号是否以前注册过
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getShixianLiliAppId() + "&secret=" +
				myAPPIDConfig.getShixianLiliScreat() + "&js_code=" + user.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 0);
		NxCommunityUserEntity communityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);
		//2，如果注册过，则返回提示。
		if(communityUserEntity != null){
			return R.error(-1,"微信号已注册!");
		}else {

			user.setNxCouWxOpenId(openid);
			user.setNxCouDeviceId("-1");
			nxCommunityUserService.save(user);

			//3..3 返回用户id
			Integer nxCommunityUserId = user.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", nxCommunityUserId);
			map1.put("roleId", 0);
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);

			return R.ok().put("data", nxCommunityUserEntity1);

		}

	}

	@RequestMapping(value = "/getComUsers/{comId}")
	@ResponseBody
	public R getComUsers(@PathVariable Integer comId) {

		List<NxCommunityUserEntity> userEntities = nxCommunityUserService.getAdmainUserByComId(comId);
		System.out.println( "user-----ssss" + userEntities );
		return R.ok().put("data", userEntities);
	}


	/**
	 * driver员工扫描
	 * 微信小程序扫描二维码校验文件
	 * @return 校验内容
	 */
	@RequestMapping(value = "/pcT8xhlNNF.txt")
	@ResponseBody
	public String driverUserRegist( ) {
		return "82e336d5278050591525a671ae9c050c";
	}


	@RequestMapping(value = "/comUserDriverSave", method = RequestMethod.POST)
	@ResponseBody
	public R comUserDriverSave (@RequestBody NxCommunityUserEntity user) {

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
				myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + user.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openId = jsonObject.get("openid").toString();

		Map<String, Object> map1 = new HashMap<>();
		map1.put("openId", openId);
		map1.put("roleId", user.getNxCouRoleId());
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserByOpenId(map1);
		if(nxCommunityUserEntity != null){
			return R.error(-1,"请直接登陆");
		}else{
			//添加新用户
			user.setNxCouWxOpenId(openId);
			user.setNxCouDeviceId("-1");
			nxCommunityUserService.save(user);
			Integer communityUserId = user.getNxCommunityUserId();
			Map<String, Object> map2 = new HashMap<>();
			map2.put("userId", communityUserId);
			map2.put("roleId", 5 );
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map2);
			return R.ok().put("data",nxCommunityUserEntity1);
		}
	}

	@RequestMapping(value = "/driverUserLogin", method = RequestMethod.POST)
	@ResponseBody
	public R driverLogin (@RequestBody NxCommunityUserEntity communityUserEntity ) {
		System.out.println(communityUserEntity);

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getLiziDriverAppID() + "&secret=" +
				myAPPIDConfig.getLiziDriverScreat() + "&js_code=" + communityUserEntity.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
		map.put("roleId", 5);
		System.out.println(map);
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserByOpenId(map);

		if(nxCommunityUserEntity != null){
			Integer communityUserId = nxCommunityUserEntity.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", communityUserId);
			map1.put("roleId", 5 );
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);

			System.out.println(nxCommunityUserEntity1);
			System.out.println("logingngigign");
			return R.ok().put("data", nxCommunityUserEntity1);
		}else {
			return R.error(-1,"用户不存在");
		}
	}






	/**
	 * 批发商登陆
	 * @param communityUserEntity 批发商
	 * @return 批发商
	 */
	@RequestMapping(value = "/comUserLogin", method = RequestMethod.POST)
	@ResponseBody
	public R comUserLogin (@RequestBody NxCommunityUserEntity communityUserEntity ) {
		System.out.println("afdafaskfjaslkdfj;alsf;alsjf;lasflasjflalogigngignigig");

		MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + myAPPIDConfig.getCommunityAppID() + "&secret=" +
				myAPPIDConfig.getCommunityScreat() + "&js_code=" + communityUserEntity.getNxCouCode() +
				"&grant_type=authorization_code";
		// 发送请求，返回Json字符串
		String str = WeChatUtil.httpRequest(url, "GET", null);
		// 转成Json对象 获取openid
		JSONObject jsonObject = JSONObject.parseObject(str);

		// 我们需要的openid，在一个小程序中，openid是唯一的
		String openid = jsonObject.get("openid").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("openId", openid);
//		map.put("roleId", 0);
		System.out.println(map);
		NxCommunityUserEntity nxCommunityUserEntity = nxCommunityUserService.queryComUserInfo(map);

		if(nxCommunityUserEntity != null){
			Integer communityUserId = nxCommunityUserEntity.getNxCommunityUserId();
			Map<String, Object> map1 = new HashMap<>();
			map1.put("userId", communityUserId);
//			map1.put("roleId", 0);
			System.out.println("ammmda11111" + map1);
			NxCommunityUserEntity nxCommunityUserEntity1 = nxCommunityUserService.queryComUserInfo(map1);
			System.out.println("diididididiidi" + nxCommunityUserEntity1.getNxCommunityEntity().getNxCommunityName());

			return R.ok().put("data", nxCommunityUserEntity1);
		}else {
			return R.error(-1,"用户不存在");
		}
	}



   @RequestMapping(value = "/comUserLoginAndroid/{phone}")
   @ResponseBody
   public R comUserLoginAndroid(@PathVariable String phone) {
	   System.out.println(phone + "=====");
	   NxCommunityUserEntity userEntity = nxCommunityUserService.queryUserByPhone(phone);
	   if (userEntity != null){
		   return R.ok().put("data", userEntity);
	   }else{
		   return R.error(-1, "手机号码错误");

	   }
   }
	
}
