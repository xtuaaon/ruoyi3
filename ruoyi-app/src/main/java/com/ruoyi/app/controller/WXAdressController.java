package com.ruoyi.app.controller;

import com.ruoyi.app.dto.inputdto.wxadress.addAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.deleteAddressInputDto;
import com.ruoyi.app.dto.inputdto.wxadress.updateAddressInputDto;
import com.ruoyi.app.service.IWXAddressService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.WXAddress;
import com.ruoyi.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class WXAdressController {

    private static final Logger log = LoggerFactory.getLogger(WXAdressController.class);

    @Autowired
    private IWXAddressService wxAddressService;

    @GetMapping("/wx/getAddressList")
    public AjaxResult getAddressList(){
        AjaxResult ajax = AjaxResult.success();
        long userId = SecurityUtils.getUserId();
        log.info("userId:{}", userId);
        List<WXAddress> data = wxAddressService.getAddressList(userId);
        ajax.put("data", data);
        return ajax;
    }

    @PostMapping("/wx/addAddress")
    public AjaxResult addAddress(@RequestBody addAddressInputDto request){
        try {
            AjaxResult ajax = AjaxResult.success();
            int data = wxAddressService.insertAddress(request);
            ajax.put("data", "success");
            return ajax;
        }
        catch (Exception e) {
            log.error("添加地址失败", e);
            return AjaxResult.error("添加地址失败：" + e.getMessage());
        }
    }

    @PostMapping("/wx/deleteAddress")
    public AjaxResult deleteAddress(@RequestBody deleteAddressInputDto request){
        try {
            AjaxResult ajax = AjaxResult.success();
            int count = wxAddressService.deleteAddress(request);
            log.info("count:{}", count);
            if (count==0){
                return AjaxResult.error("删除地址失败");
            }
            ajax.put("data", "success");
            return ajax;
        }
        catch (Exception e) {
            log.error("删除地址失败", e);
            return AjaxResult.error("删除地址失败");
        }
    }

    @PostMapping("/wx/updateAddress")
    public AjaxResult updateAddress(@RequestBody updateAddressInputDto request){
        try {
            AjaxResult ajax = AjaxResult.success();
            int data = wxAddressService.updateAddress(request);
            ajax.put("data", "success");
            return ajax;
        }
        catch (Exception e) {
            log.error("更新地址失败", e);
            return AjaxResult.error("更新地址失败：" + e.getMessage());
        }
    }
}
