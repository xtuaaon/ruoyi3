package com.ruoyi.app.controller;

import com.ruoyi.app.dto.inputdto.wxLogin.wxGetPhoneInputDto;
import com.ruoyi.app.dto.inputdto.wxLogin.wxLoginInputDto;
import com.ruoyi.app.dto.inputdto.wxLogin.wxRegisterInputDto;
import com.ruoyi.app.service.IWXLoginService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.snowflakeId.SnowflakeIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.Map;

@RestController
public class WXLoginController {
    private static final Logger log = LoggerFactory.getLogger(WXLoginController.class);

    @Autowired
    private IWXLoginService wxloginService;

    @Anonymous
    @PostMapping("/wxlogin")
    public AjaxResult wxlogin(@RequestBody wxLoginInputDto request)
    {
        AjaxResult ajax = AjaxResult.success();
        Map<String, Object> data = wxloginService.login(request.getCode());
        ajax.put("data", data);
        return ajax;
    }


    @GetMapping("/wxgetInfo")
    public AjaxResult wxgetInfo()
    {
        AjaxResult ajax = AjaxResult.success();
        try {
            Object loginUser = SecurityUtils.getAuthentication();

            ajax.put("user", loginUser);
            return ajax;
        }
        catch (Exception e) {
            log.error("用户获取过程中发生异常",e);
            ajax.put("user", e);
            return ajax;
        }
    }

    @Anonymous
    @PostMapping("/wxregister")
    public AjaxResult wxregister(@RequestBody wxRegisterInputDto request)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = new SysUser();
        user.setUserId(SnowflakeIdUtils.getSnowflakeId());
        user.setOpenId(request.getOpenId());
        user.setUserName(request.getNickName());
        user.setNickName(request.getNickName());
        user.setAvatar(request.getAvatarUrl());
        user.setPhonenumber(request.getPhone());
        user.setCreateTime(new Date());
        Map<String, Object> data = wxloginService.register(user);
        ajax.put("data", data);
        return ajax;
    }

    @Anonymous
    @PostMapping("/wxgetPhone")
    public AjaxResult wxgetPhone(@RequestBody wxGetPhoneInputDto request)
    {
        AjaxResult ajax = AjaxResult.success();
        Map<String, Object> data = wxloginService.getPhone(request.getCode());
        ajax.put("data", data);
        return ajax;
    }

}
