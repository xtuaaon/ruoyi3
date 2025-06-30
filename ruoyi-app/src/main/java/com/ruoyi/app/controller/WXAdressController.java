package com.ruoyi.app.controller;

import com.ruoyi.app.dto.inputdto.wxadress.addAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.updateAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.wxGetAddressListInputDto;
import com.ruoyi.app.service.IWXAddressService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.WXAddress;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.snowflakeId.SnowflakeIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WXAdressController {
    @Autowired
    private IWXAddressService wxAddressService;

    @PostMapping("/wx/getAddressList")
    public AjaxResult getAddressList(@RequestBody wxGetAddressListInputDto request){
        AjaxResult ajax = AjaxResult.success();
        List<WXAddress> data = wxAddressService.getAddressList(request.getUserId());
        ajax.put("data", data);
        return ajax;
    }

    @PostMapping("/wx/addAddress")
    public AjaxResult addAddress(@RequestBody addAddressInputDto request){
        AjaxResult ajax = AjaxResult.success();
        WXAddress address = new WXAddress();
        address.setId(SnowflakeIdUtils.getSnowflakeId());
        address.setAddressName(request.getAddressName());
        address.setDetailAddress(request.getDetailAddress());
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setOrder(request.getOrder());
        address.setCreator(SecurityUtils.getUserId());
        address.setCreateTime(new Date());
        Map<String, Object> data = wxAddressService.insertAddress(address);
        ajax.put("data", data);
        return ajax;
    }

    @PostMapping("/wx/updateAddress")
    public AjaxResult updateAddress(@RequestBody updateAddressInputDto request){
        AjaxResult ajax = AjaxResult.success();
        // 2. 创建更新对象，只设置需要更新的字段
        WXAddress updateAddress = new WXAddress();
        updateAddress.setId(request.getId());
        // 只更新非null的字段
        if (request.getAddressName() != null) {
            updateAddress.setAddressName(request.getAddressName());
        }
        if (request.getDetailAddress() != null) {
            updateAddress.setDetailAddress(request.getDetailAddress());
        }
        if (request.getLocation() != null) {
            updateAddress.setLocation(request.getLocation());
        }
        if (request.getContactName() != null) {
            updateAddress.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            updateAddress.setContactPhone(request.getContactPhone());
        }
        updateAddress.setUpdateTime(new Date());
        Map<String, Object> data = wxAddressService.updateAddress(updateAddress);
        ajax.put("data", data);
        return ajax;
    }
}
