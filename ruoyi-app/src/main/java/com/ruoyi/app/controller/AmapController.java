package com.ruoyi.app.controller;

import com.ruoyi.app.dto.inputdto.amap.reverseGeocodeInputDto;
import com.ruoyi.app.dto.inputdto.amap.searchAddressByKeywordInputDto;
import com.ruoyi.app.service.IAmapService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AmapController {
    @Autowired
    private IAmapService AmapService;

    @PostMapping("/amap/searchAddressByKeyword")
    public AjaxResult searchAddressByKeyword(@RequestBody searchAddressByKeywordInputDto request){
        AjaxResult ajax = AjaxResult.success();
        List<Map<String, Object>> data = AmapService.searchAddressByKeyword(request.getLocation(),request.getKeywords(),request.getCity());
        ajax.put("data", data);
        return ajax;
    }

    @PostMapping("/amap/reverseGeocode")
    public AjaxResult reverseGeocode(@RequestBody reverseGeocodeInputDto request){
        AjaxResult ajax = AjaxResult.success();
        String data = AmapService.reverseGeocode(request.getLocation());
        ajax.put("data", data);
        return ajax;
    }
}
