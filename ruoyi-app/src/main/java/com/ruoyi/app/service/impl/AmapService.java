package com.ruoyi.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.app.service.IAmapService;
import com.ruoyi.common.core.domain.model.LocationPoint;
import com.ruoyi.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.util.*;

@Service
public class AmapService implements IAmapService {
    @Value("${amap.amapKey}")
    private String amapKey;

    private static final Logger log = LoggerFactory.getLogger(AmapService.class);

    @Override
    public List<Map<String, Object>> searchAddressByKeyword(String location, String keywords, String city){

        List<Map<String, Object>> tipsWithDistance = new ArrayList<>();

        String url = "https://restapi.amap.com/v3/assistant/inputtips";

        try {
            String param = "key=" + URLEncoder.encode(amapKey, "UTF-8") +
                    "&keywords=" + URLEncoder.encode(keywords, "UTF-8") +
                    "&city=" + URLEncoder.encode(city, "UTF-8") +
                    "&output=JSON";
            log.info("url:{}", url);
            // 发送HTTP GET请求到微信服务器
            String responseJson = HttpUtils.sendGet(url,param);

            // 使用fastjson解析
            JSONObject jsonObject = JSON.parseObject(responseJson);

            // 检查是否有错误
            if (jsonObject != null && jsonObject.containsKey("errcode")) {
                log.error("微信API返回错误: {} - {}",
                        jsonObject.getIntValue("errcode"),
                        jsonObject.getString("errmsg"));
                throw new RuntimeException("微信API错误: " + jsonObject.getString("errmsg"));
            }

            JSONArray tipsArray = null;
            if (jsonObject != null) {
                tipsArray = jsonObject.getJSONArray("tips");
            }

            // 使用空间向量法计算距离并添加到tips中
            if (tipsArray != null) {
                for (int i = 0; i < tipsArray.size(); i++) {
                    JSONObject tip = tipsArray.getJSONObject(i);
                    tip.put("name", tip.getString("name"));
                    tip.put("address", tip.getString("district")+tip.getString("address"));
                    String locationStr = tip.getString("location");
                    tip.put("location", locationStr);
                    if (locationStr != null && !locationStr.isEmpty()) {
                        LocationPoint tipPoint = parseLocation(locationStr);
                        double distance = calculateDistanceVector(parseLocation(location), tipPoint);
                        tip.put("distance", Math.round(distance)); // 四舍五入到整数米
                    } else {
                        tip.put("distance", -1); // 无位置信息
                    }
                    tipsWithDistance.add(tip);
                }
            }

            // 按距离排序
            tipsWithDistance.sort(Comparator.comparingDouble(tip ->
                    ((Number)((JSONObject)tip).getDoubleValue("distance")).doubleValue()));

            return tipsWithDistance;
        }
        catch (Exception e)
        {
            log.error("请求微信API时发生错误", e);
            return tipsWithDistance;
        }
    }

    @Override
    public String reverseGeocode(String location){

        String addressdesc = null;
        // API地址
        String url = "https://restapi.amap.com/v3/geocode/regeo";

        try {
            // 构建参数字符串
            String paramBuilder = "key=" + URLEncoder.encode(amapKey, "UTF-8") +
                    "&location=" + URLEncoder.encode(location, "UTF-8") +
                    "&output=JSON";
            // 发送HTTP GET请求到微信服务器
            String responseJson = HttpUtils.sendGet(url, paramBuilder);

            log.info("data:{}", responseJson);

            // 使用fastjson解析
            JSONObject jsonObject = JSON.parseObject(responseJson);

            log.info("jsonObject:{}", jsonObject);


            if (jsonObject != null) {
                JSONObject regeocode = jsonObject.getJSONObject("regeocode");
                addressdesc = regeocode.getString("formatted_address");
            }
            log.info("addressdesc:{}", addressdesc);
            return addressdesc;
        }
        catch (Exception e)
        {
            log.error("请求微信API时发生错误", e);
            return addressdesc;
        }
    }


    private static LocationPoint parseLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) {
            return null;
        }

        String[] parts = locationStr.split(",");
        if (parts.length != 2) {
            return null;
        }

        try {
            double longitude = Double.parseDouble(parts[0]);
            double latitude = Double.parseDouble(parts[1]);
            return new LocationPoint(longitude, latitude);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 使用空间向量计算两点之间的距离(米)
     */
    private static double calculateDistanceVector(LocationPoint point1, LocationPoint point2) {
        if (point1 == null || point2 == null) {
            return -1;
        }

        // 地球平均半径(米)
        final double EARTH_RADIUS = 6371000;

        // 转换为笛卡尔坐标
        double[] v1 = point1.toCartesian();
        double[] v2 = point2.toCartesian();

        // 计算向量间的欧几里得距离
        double dx = v1[0] - v2[0];
        double dy = v1[1] - v2[1];
        double dz = v1[2] - v2[2];
        double chord = Math.sqrt(dx*dx + dy*dy + dz*dz);

        // 弦长转换为弧长
        double angle = 2 * Math.asin(chord / 2);

        return EARTH_RADIUS * angle;
    }


}
