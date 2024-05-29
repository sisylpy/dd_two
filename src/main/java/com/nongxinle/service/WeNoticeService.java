package com.nongxinle.service;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.nongxinle.entity.SubscribeMessage;
import com.nongxinle.entity.TemplateData;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.MyWxShixianliliPayConfig;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeNoticeService {


    public static void subscribeOrderFinishMessage(String openId, String page, Map<String, TemplateData> map) {
        System.out.println("sussnssnsnnsnsnsnnnnnnsnnsnnsnns" + openId +"===" + page + "pmapp" + map);
        try {

            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            String appId = myAPPIDConfig.getShixianLiliAppId();
            String secret  = myAPPIDConfig.getShixianLiliScreat();


            String templateId = "7x-z_S7NQdD3wAn8qVmQAbAaCbK_j2aA7CRbZWDPGuw";
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            System.out.println("str=====>>>>" + strPhone);
            // 转成Json对象 获取openid
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            System.out.println("jsonObject" + jsonObjectPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            // 拼接数据
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);//事先定义好的模板id
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP).body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue()).execute().body();
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Map<String, TemplateData> map = new HashMap<>();
        map.put("character_string1", new TemplateData(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())));
        map.put("amount2", new TemplateData("12"));
        map.put("phrase3", new TemplateData("已到货"));
        map.put("thing6", new TemplateData("BJYSL-SN-500"));
        map.put("number7", new TemplateData("100"));
       String openId =  "orWDh5HwYg0CefPuW9r5wnVnuZiw";
        subscribeOrderFinishMessage(openId, "/pages/index/index", map);
    }
}
