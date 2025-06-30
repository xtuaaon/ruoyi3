package com.ruoyi.app.service;

import com.ruoyi.common.core.domain.entity.SysUser;

import java.util.Map;

public interface IWXLoginService {
    Map<String, Object> login(String code);
    Map<String, Object> register(SysUser user);
    Map<String, Object> getPhone(String code);
}
