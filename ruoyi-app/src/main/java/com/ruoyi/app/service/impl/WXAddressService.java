package com.ruoyi.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.app.mapper.WXAddressMapper;
import com.ruoyi.app.service.IWXAddressService;
import com.ruoyi.common.core.domain.entity.WXAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WXAddressService  extends ServiceImpl<WXAddressMapper, WXAddress> implements IWXAddressService {

    private static final Logger log = LoggerFactory.getLogger(WXAddressService.class);

    @Autowired
    private WXAddressMapper wxaddressMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<WXAddress> getAddressList(long userId){

        // 1. 定义Redis缓存key
        String cacheKey = "user:address:list:" + userId;

        try {
            // 2. 先从Redis获取缓存数据
            List<WXAddress> cachedList = (List<WXAddress>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedList != null && !cachedList.isEmpty()) {
                // 缓存命中，直接返回
                log.debug("从Redis缓存获取用户{}的地址列表", userId);
                return cachedList;
            }
            // 3. 缓存未命中，从数据库查询
            log.debug("Redis缓存未命中，从数据库获取用户{}的地址列表", userId);

            LambdaQueryWrapper<WXAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WXAddress::getCreator, userId)
                    .orderByDesc(WXAddress::getOrder);
            List<WXAddress> addressList = baseMapper.selectList(queryWrapper);

            // 4. 如果查询到数据，存入Redis缓存
            if (addressList != null && !addressList.isEmpty()) {
                // 设置缓存，过期时间为2小时
                redisTemplate.opsForValue().set(cacheKey, addressList, 2, TimeUnit.HOURS);
                log.debug("用户{}的地址列表已缓存到Redis", userId);
            }
            return addressList;
        }
        catch (Exception e) {
            return new ArrayList<WXAddress>();
        }
    }

    public Map<String, Object> insertAddress(WXAddress address){
        Map<String, Object> result = new HashMap<>();
        try {
            int count = baseMapper.insert(address);
            clearAddressListCache(address.getCreator());
            result.put("message", "success");
            return result;
        }
        catch (Exception e) {
            result.put("message", "新增地址失败");
            log.error("新增地址发生异常: {}",address,e);
            return result;
        }
    }

    public Map<String, Object> updateAddress(WXAddress address){
        Map<String, Object> result = new HashMap<>();
        try {
            int count = baseMapper.updateById(address);
            clearAddressListCache(address.getCreator());
            result.put("message", "success");
            return result;
        }
        catch (Exception e) {
            result.put("message", "修改地址失败");
            log.error("修改地址发生异常: {}",address,e);
            return result;
        }
    }

    /**
     * 清除用户地址列表缓存
     *
     * @param userId 用户ID
     */
    private void clearAddressListCache(long userId) {
        String cacheKey = "user:address:list:" + userId;
        redisTemplate.delete(cacheKey);
        log.debug("清除用户{}的地址列表缓存", userId);
    }
}
