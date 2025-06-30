package com.ruoyi.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.app.service.IWXLoginService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class WXLoginService implements IWXLoginService {

    private static final Logger log = LoggerFactory.getLogger(WXLoginService.class);

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Override
    public Map<String, Object> login(String code){
        log.info("打印code");
        log.info("code:{}", code);
        log.info("appId:{}", appId);
        log.info("appSecret:{}", appSecret);

        String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid=" + appId +
                "&secret=" + appSecret +
                "&js_code=" + code +
                "&grant_type=authorization_code";

        try {
            // 发送HTTP GET请求到微信服务器
            String responseJson = HttpUtils.sendGet(url);

            log.info("data:{}", responseJson);

            // 使用fastjson解析
            JSONObject jsonObject = JSON.parseObject(responseJson);

            log.info("jsonObject:{}", jsonObject);

            // 检查是否有错误
            if (jsonObject != null && jsonObject.containsKey("errcode")) {
                log.error("微信API返回错误: {} - {}",
                        jsonObject.getIntValue("errcode"),
                        jsonObject.getString("errmsg"));
                throw new RuntimeException("微信API错误: " + jsonObject.getString("errmsg"));
            }

            String openid = null;
            if (jsonObject != null) {
                openid = jsonObject.getString("openid");
            }

            log.info("openid:{}", openid);

            // 查找用户是否已注册
            SysUser user = userService.selectUserByOpenId(openid);

            Map<String, Object> result = new HashMap<>();

            if (user == null)
            {
                // 用户未注册
                result.put("isRegistered", false);
                result.put("openid", openid);
            }
            else
            {
                // 创建LoginUser对象
                LoginUser loginUser = new LoginUser();
                loginUser.setUserId(user.getUserId());
                loginUser.setOpenId(user.getOpenId());
                loginUser.setUser(user);
                loginService.recordLoginInfo(user.getUserId());
                log.info("生成token");
                String token = tokenService.createWXToken(loginUser);
                result.put("token", token);
                result.put("isRegistered", true);
                result.put("message", "登录成功");
            }
            return result;
        }
        catch (Exception e)
        {
            log.error("请求微信API时发生错误", e);
            // 处理请求异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "请求微信API时发生错误: " + e);
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> register(SysUser user){
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("user:{}", JSON.toJSONString(user));
            int count = userService.insertUser(user);
            log.info("插入成功");

            if (count > 0) {
                // 插入成功
                // 创建LoginUser对象
                LoginUser loginUser = new LoginUser();
                loginUser.setUserId(user.getUserId());
                loginUser.setOpenId(user.getOpenId());
                loginUser.setUser(user);
                loginService.recordLoginInfo(user.getUserId());
                log.info("生成token");
                // 生成token
                String token = tokenService.createWXToken(loginUser);
                result.put("token", token);
                result.put("isRegistered", true);
                result.put("message", "注册成功");
            }
            else {
                // 插入失败，但没有抛出异常
                result.put("isRegistered", false);
                result.put("message", "注册失败，请稍后重试");
                // 可以记录日志
                log.warn("用户注册失败，影响行数为0: {}", user);
            }
        }
        catch (Exception e) {
            // 捕获异常
            result.put("isRegistered", false);
            result.put("message", "系统错误，请稍后重试");
            // 记录异常日志
            log.error("用户注册过程中发生异常: {}", user, e);
        }
        return result;
    }

    @Override
    public Map<String, Object> getPhone(String code){

        try{
            String TokenRTC = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                    "&appid=" + appId +
                    "&secret=" + appSecret;

            String tokenResponseJson = HttpUtils.sendGet(TokenRTC);
            log.info("tokenResponseJson:{}", tokenResponseJson);
            JSONObject tokenJsonObject = JSON.parseObject(tokenResponseJson);
            log.info("tokenJsonObject:{}", tokenJsonObject);
            String access_token = tokenJsonObject.getString("access_token");

            String PhoneRTC = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?" +
                    "&access_token=" + access_token;
            // 创建请求参数
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("code", code);
            String phoneResponseJson = HttpUtils.sendPostJson(PhoneRTC,requestParams);
            log.info("phoneResponseJson:{}", phoneResponseJson);
            JSONObject phoneJsonObject = JSON.parseObject(phoneResponseJson);
            log.info("phoneJsonObject:{}", phoneJsonObject);
            String phoneNumber = phoneJsonObject.getString("phone_info");

            Map<String, Object> result = new HashMap<>();
            result.put("phoneNumber", phoneNumber);
            return result;
        }
        catch (Exception e)
        {
            log.error("请求微信API时发生错误", e);
            // 处理请求异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "请求微信API时发生错误: " + e);
            return errorResult;
        }
    }

}
