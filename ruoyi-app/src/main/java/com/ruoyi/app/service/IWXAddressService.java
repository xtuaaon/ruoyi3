package com.ruoyi.app.service;

import com.ruoyi.app.dto.inputdto.wxadress.addAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.deleteAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.updateAddressInputDto;
import com.ruoyi.common.core.domain.entity.WXAddress;

import java.util.List;
import java.util.Map;

public interface IWXAddressService {
    List<WXAddress> getAddressList(long userId);
    int insertAddress(addAddressInputDto request);
    int deleteAddress(deleteAddressInputDto request);
    int updateAddress(updateAddressInputDto request);
}
