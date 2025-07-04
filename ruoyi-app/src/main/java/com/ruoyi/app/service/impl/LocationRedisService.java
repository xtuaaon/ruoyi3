package com.ruoyi.app.service.impl;

import com.ruoyi.app.dto.outputdto.MasterLocationDTO;
import com.ruoyi.common.constant.RedisKeyConstants;
import com.ruoyi.common.core.domain.entity.WXMaster;
import com.ruoyi.common.core.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationRedisService {

    private static final Logger log = LoggerFactory.getLogger(LocationRedisService.class);

    @Autowired
    private RedisCache redisCache;

    public List<MasterLocationDTO> getOnlineList(double longitude, double latitude, int distance){
        List<MasterLocationDTO> mastersNearby = new ArrayList<>();

        try {
            GeoResults<RedisGeoCommands.GeoLocation<String>> masterList = getMasterKeysInRange(longitude,latitude,distance);

            // 提取所有masterKey和距离信息
            List<String> masterKeys = new ArrayList<>();
            Map<String, Distance> distanceMap = new HashMap<>();

            for (GeoResult<RedisGeoCommands.GeoLocation<String>> master : masterList.getContent()) {
                String masterKey = master.getContent().getName();
                masterKeys.add(masterKey);
                distanceMap.put(masterKey, master.getDistance());
            }
            // 批量获取所有master信息
            List<Map<String, Object>> masterMaps = getCacheMapBatchWithLua(masterKeys);

            for (int i = 0; i < masterKeys.size(); i++) {
                String masterKey = masterKeys.get(i);
                Map<String, Object> masterMap = masterMaps.get(i);

                if (masterMap != null && !masterMap.isEmpty()) {
                    // 获取距离信息
                    Distance distanceData = distanceMap.get(masterKey);
                    double distanceValue = distanceData != null ? distanceData.getValue() : 0.0;
                    String distanceUnit = distanceData != null ? distanceData.getUnit() : null;

                    MasterLocationDTO dto = new MasterLocationDTO();

                    // 从masterMap中提取字段并设置到DTO中
                    // 1. 设置评分 (rating)
                    if (masterMap.containsKey("rating")) {
                        try {
                            Object ratingObj = masterMap.get("rating");
                            if (ratingObj != null) {
                                if (ratingObj instanceof Number) {
                                    dto.setRating(((Number) ratingObj).doubleValue());
                                } else {
                                    dto.setRating(Double.parseDouble(ratingObj.toString()));
                                }
                            }
                        } catch (NumberFormatException e) {
                            // 设置默认值或记录日志
                            dto.setRating(0.0);
                            log.warn("无法解析master评分: {}", masterMap.get("rating"));
                        }
                    }

                    // 2. 设置专长 (specialty)
                    if (masterMap.containsKey("specialty")) {
                        Object specialtyObj = masterMap.get("specialty");
                        dto.setSpecialty(specialtyObj != null ? specialtyObj.toString() : null);
                    }

                    // 3. 设置服务次数 (serviceCount)
                    if (masterMap.containsKey("serviceCount")) {
                        try {
                            Object countObj = masterMap.get("serviceCount");
                            if (countObj != null) {
                                if (countObj instanceof Number) {
                                    dto.setServiceCount(((Number) countObj).intValue());
                                } else {
                                    dto.setServiceCount(Integer.parseInt(countObj.toString()));
                                }
                            }
                        } catch (NumberFormatException e) {
                            // 设置默认值或记录日志
                            dto.setServiceCount(0);
                            log.warn("无法解析master服务次数: {}", masterMap.get("serviceCount"));
                        }
                    }

                    // 4. 设置在线状态 (onlineStatus)
                    if (masterMap.containsKey("onlineStatus")) {
                        Object statusObj = masterMap.get("onlineStatus");
                        if (statusObj != null) {
                            if (statusObj instanceof Boolean) {
                                dto.setOnlineStatus((Boolean) statusObj);
                            } else if (statusObj instanceof Number) {
                                dto.setOnlineStatus(((Number) statusObj).intValue() > 0);
                            } else {
                                String statusStr = statusObj.toString().toLowerCase();
                                dto.setOnlineStatus("true".equals(statusStr) || "1".equals(statusStr) || "yes".equals(statusStr));
                            }
                        }
                    }
                    dto.setDistance(distanceValue);
                    dto.setDistanceUnit(distanceUnit != null ? distanceUnit : "m");

                    mastersNearby.add(dto);
                }
            }
        }
        catch (Exception e) {
            // 处理异常
            log.error("Error fetching online masters within geo fence", e);
        }
        return mastersNearby;
    }

    /**
     * 从Redis GEO中查询指定范围内的人员key
     * @param longitude 经度
     * @param latitude 纬度
     * @param distance 距离（公里）
     * @return 人员key列表
     */
    private GeoResults<RedisGeoCommands.GeoLocation<String>> getMasterKeysInRange(double longitude, double latitude, int distance) {
        // 创建中心点
        Point center = new Point(longitude, latitude);

        // 创建距离对象（将公里转换为米）
        Distance radius = new Distance(distance, Metrics.KILOMETERS);

        // 创建Circle对象表示搜索范围
        Circle circle = new Circle(center, radius);

        // 设置GEO查询参数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()      // 包含距离信息
                .includeCoordinates()   // 包含坐标信息
                .sortAscending();       // 按距离升序排序

        // 执行GEORADIUS查询
        return redisCache.getCacheGEO(RedisKeyConstants.Master_Location_GEO, circle, args);
    }

    /**
     * 缓存师傅位置信息
     * @param masterId 师傅ID
     * @param longitude 经度
     * @param latitude 纬度
     */
    public void cacheMasterLocation(String masterId, double longitude, double latitude) {
        Point point = new Point(longitude, latitude);
        redisCache.setCacheGEO(RedisKeyConstants.Master_Location_GEO, point, masterId);
    }

    /**
     * 缓存师傅个人信息
     * @param master 师傅个人信息
     */
    public void cacheMasterInfo(WXMaster master) {
        String redisKey = RedisKeyConstants.Master_Info_Hash + master.getId();
        Map<String, Object> fieldMap = new HashMap<>();
        BeanMap beanMap = BeanMap.create(master);
        for (Object key : beanMap.keySet()) {
            fieldMap.put(key.toString(), beanMap.get(key));
        }
        redisCache.setCacheMap(redisKey, fieldMap);
    }


    public List<Map<String, Object>> getCacheMapBatchWithLua(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        // Lua脚本: 批量获取多个hash的所有数据
        String script =
                "local result = {}\n" +
                        "for i, key in ipairs(KEYS) do\n" +
                        "    result[i] = redis.call('HGETALL', key)\n" +
                        "end\n" +
                        "return result";

        // 执行Lua脚本
        List<Object> results = redisCache.executeLua(script, List.class, keys);

        // 处理结果
        List<Map<String, Object>> mapResults = new ArrayList<>(keys.size());
        for (Object result : results) {
            if (result instanceof List) {
                List<Object> hashEntries = (List<Object>) result;
                Map<String, Object> hashMap = new HashMap<>();

                // 将Redis返回的列表转换为Map
                for (int i = 0; i < hashEntries.size(); i += 2) {
                    String field = (String) hashEntries.get(i);
                    Object value = hashEntries.get(i + 1);
                    hashMap.put(field, value);
                }

                mapResults.add(hashMap);
            } else {
                mapResults.add(new HashMap<>());
            }
        }

        return mapResults;
    }



}
