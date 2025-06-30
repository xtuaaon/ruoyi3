package com.ruoyi.app.service;

import com.ruoyi.common.core.domain.entity.WXAddress;

import java.util.List;
import java.util.Map;

public interface IWXAddressService {
    List<WXAddress> getAddressList(long userId);
    Map<String, Object> insertAddress(WXAddress address);
    Map<String, Object> updateAddress(WXAddress address);
}
