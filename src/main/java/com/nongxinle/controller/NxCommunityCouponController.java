package com.nongxinle.controller;

/**
 * 
 *
 * @author lpy
 * @date 05-15 08:33
 */

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxCommunityGoodsService;
import com.nongxinle.service.NxCustomerUserCardService;
import com.nongxinle.service.NxCustomerUserCouponService;
import com.nongxinle.utils.UploadFile;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxCommunityCouponService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("api/nxcommunitycoupon")
public class NxCommunityCouponController {
	@Autowired
	private NxCommunityCouponService nxCommunityCouponService;
	@Autowired
	private NxCommunityGoodsService nxCommunityGoodsService;
	@Autowired
	private NxCustomerUserCouponService nxCustomerUserCouponService;


	@RequestMapping(value = "/comGetCouponDetail/{id}")
	@ResponseBody
	public R comGetCouponDetail(@PathVariable Integer id) {
	    Map<String, Object> map = new HashMap<>();
	    map.put("id", id);
		NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryCouponDetail(map);
		System.out.println("edeetktkkt" + communityCouponEntity);
		return R.ok().put("data", communityCouponEntity);
	}

//	@RequestMapping(value = "/comSaveOneCoupon", method = RequestMethod.POST)
//	@ResponseBody
//	public R comSaveOneCoupon (@RequestBody NxCommunityCouponEntity  coupon) {
//		System.out.println("safnfnff" + coupon);
//		coupon.setNxCpStatus(0);
//		nxCommunityCouponService.save(coupon);
//		return R.ok();
//	}


	@RequestMapping(value = "/delComCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R delComCoupon (Integer id,HttpSession session) {

		Map<String, Object> map = new HashMap<>();
		map.put("coupId", id);
		List<NxCustomerUserCouponEntity> nxCustomerUserCouponEntities = nxCustomerUserCouponService.queryUserCouponListByParams(map);
		if(nxCustomerUserCouponEntities.size() > 0){
			return R.error(-1,"有用户已购买，不能删除");
		}else{
			NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryObject(id);
			Integer nxCpCgGoodsId = communityCouponEntity.getNxCpCgGoodsId();
			NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(nxCpCgGoodsId);
			if (nxCommunityGoodsEntity.getNxCgNxGoodsFilePath() != null) {
				ServletContext servletContext = session.getServletContext();
				String realPath1 = servletContext.getRealPath(nxCommunityGoodsEntity.getNxCgNxGoodsFilePath());
				File file1 = new File(realPath1);
				if (file1.exists()) {
					file1.delete();
				}
			}
			nxCommunityGoodsService.delete(nxCpCgGoodsId);
			nxCommunityCouponService.delte(id);
			return R.ok();
		}

	}

	@RequestMapping(value = "/comGetConponList", method = RequestMethod.POST)
	@ResponseBody
	public R comGetConponList(Integer commId, Integer status) {
		Map<String, Object> map = new HashMap<>();
		map.put("commId", commId);
//		map.put("status", 0);
	    List<NxCommunityCouponEntity> communityCouponEntities =  nxCommunityCouponService.queryCouponListByParams(map);
	    return R.ok().put("data", communityCouponEntities);
	}

	@RequestMapping(value = "/comUpdateCoupon", method = RequestMethod.POST)
	@ResponseBody
	public R comUpdateCoupon(@RequestBody NxCommunityCouponEntity goodsCoupon) {

		NxCommunityGoodsEntity nxCommunityGoodsEntity = goodsCoupon.getNxCommunityGoodsEntity();
		goodsCoupon.setNxCommunityCouponName(nxCommunityGoodsEntity.getNxCgGoodsName());

//        String nxCpStopTimeZone = "2024-05-01-00-00-00";
		String startDate =  goodsCoupon.getNxCpStartDate();
		String stopDate =  goodsCoupon.getNxCpStopDate();
		String couponStartTime =  goodsCoupon.getNxCpStartTime();
		String couponStopTime =  goodsCoupon.getNxCpStopTime();
//
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
		goodsCoupon.setNxCpPrice(nxCommunityGoodsEntity.getNxCgGoodsPrice());
		goodsCoupon.setNxCpOriginalPrice(nxCommunityGoodsEntity.getNxCgGoodsHuaxianPrice());
		goodsCoupon.setNxCpQuantity(nxCommunityGoodsEntity.getNxCgGoodsHuaxianQuantity());
		nxCommunityCouponService.update(goodsCoupon);

		nxCommunityGoodsService.update(nxCommunityGoodsEntity);

		return R.ok().put("data", goodsCoupon);
	}


	@RequestMapping(value = "/updateCouponWithFile", method = RequestMethod.POST)
	@ResponseBody
	public R updateCouponWithFile(@RequestParam("file") MultipartFile file,
									   @RequestParam("id") Integer id,
									   HttpSession session) {
		//1,上传图片
		String newUploadName = "goodsImage";
		String realPath = UploadFile.upload(session, newUploadName, file);

		String filename = file.getOriginalFilename();
		String filePath = newUploadName + "/" + filename;

		System.out.println("nefifiififiififpapapa" + filePath);


		NxCommunityCouponEntity communityCouponEntity = nxCommunityCouponService.queryObject(id);
		if (communityCouponEntity.getNxCpFilePath() != null) {
			ServletContext servletContext = session.getServletContext();
			String realPath1 = servletContext.getRealPath(communityCouponEntity.getNxCpFilePath());
			File file1 = new File(realPath1);
			if (file1.exists()) {
				file1.delete();
			}
		}

		communityCouponEntity.setNxCpFilePath(filePath);

		nxCommunityCouponService.update(communityCouponEntity);

		return R.ok().put("data", communityCouponEntity);
	}


	
}
