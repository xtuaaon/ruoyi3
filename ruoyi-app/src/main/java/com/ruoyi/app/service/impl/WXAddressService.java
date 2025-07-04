package com.ruoyi.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.app.dto.inputdto.wxadress.addAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.deleteAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.updateAddressInputDto;
import com.ruoyi.app.mapper.WXAddressMapper;
import com.ruoyi.app.service.IWXAddressService;
import com.ruoyi.common.core.domain.entity.WXAddress;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.common.utils.snowflakeId.SnowflakeIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class WXAddressService  extends ServiceImpl<WXAddressMapper, WXAddress> implements IWXAddressService {

    private static final Logger log = LoggerFactory.getLogger(WXAddressService.class);

    @Autowired
    private WXAddressMapper wxaddressMapper;

    @Autowired
    private RedisCache redisCache;

    public List<WXAddress> getAddressList(long userId){

        // 1. 定义Redis缓存key
        String cacheKey = "user:address:list:" + userId;

        try {
            // 2. 先从Redis获取缓存数据
            List<WXAddress> cachedList =  redisCache.getCacheList(cacheKey);

            if (cachedList != null && !cachedList.isEmpty()) {
                // 缓存命中，直接返回
                log.debug("从Redis缓存获取用户{}的地址列表", userId);
                return cachedList;
            }
            // 3. 缓存未命中，从数据库查询
            log.debug("Redis缓存未命中，从数据库获取用户{}的地址列表", userId);

            LambdaQueryWrapper<WXAddress> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(WXAddress::getId, WXAddress::getAddressName, WXAddress::getDetailAddress,
                            WXAddress::getLocation, WXAddress::getContactName, WXAddress::getContactPhone)
                    .eq(WXAddress::getCreator, userId)
                    .orderByDesc(WXAddress::getUpdateTime);
            List<WXAddress> addressList = baseMapper.selectList(queryWrapper);

            // 4. 如果查询到数据，存入Redis缓存
            if (addressList != null && !addressList.isEmpty()) {
                // 设置缓存，过期时间为2小时
                redisCache.setCacheList(cacheKey, addressList, 2, TimeUnit.HOURS);
                log.debug("用户{}的地址列表已缓存到Redis", userId);
            }
            return addressList;
        }
        catch (Exception e) {
            return new ArrayList<WXAddress>();
        }
    }

    public int insertAddress(addAddressInputDto request){
        WXAddress address = new WXAddress();
        BeanUtils.copyProperties(request, address);
        address.setId(SnowflakeIdUtils.getSnowflakeId());
        address.setCreator(SecurityUtils.getUserId());
        address.setCreateTime(new Date());
        int count = baseMapper.insert(address);
        clearAddressListCache(address.getCreator());
        return count;
    }

    public int deleteAddress(deleteAddressInputDto request){
        int count = baseMapper.deleteById(request.getId());
        clearAddressListCache(SecurityUtils.getUserId());
        return count;
    }

    public int updateAddress(updateAddressInputDto request){
        LambdaUpdateChainWrapper<WXAddress> updateChainWrapper = new LambdaUpdateChainWrapper<>(baseMapper)
                .eq(WXAddress::getId, request.getId());
        if (updateChainWrapper == null) {
            return 0; // 记录不存在
        }
        if (request.getAddressName() != null) {
            updateChainWrapper.set(WXAddress::getAddressName, request.getAddressName());
        }
        if (request.getDetailAddress() != null) {
            updateChainWrapper.set(WXAddress::getDetailAddress, request.getDetailAddress());
        }
        if (request.getLocation() != null) {
            updateChainWrapper.set(WXAddress::getLocation, request.getLocation());
        }
        if (request.getContactName() != null) {
            updateChainWrapper.set(WXAddress::getContactName, request.getContactName());
        }
        if (request.getContactPhone() != null) {
            updateChainWrapper.set(WXAddress::getContactPhone, request.getContactPhone());
        }
        updateChainWrapper.set(WXAddress::getUpdateTime, new Date());
        int count =  updateChainWrapper.update() ? 1: 0;
        clearAddressListCache(SecurityUtils.getUserId());
        return count;
    }

    /**
     * 清除用户地址列表缓存
     *
     * @param userId 用户ID
     */
    private void clearAddressListCache(long userId) {
        String cacheKey = "user:address:list:" + userId;
        redisCache.deleteObject(cacheKey);
        log.debug("清除用户{}的地址列表缓存", userId);
    }
}