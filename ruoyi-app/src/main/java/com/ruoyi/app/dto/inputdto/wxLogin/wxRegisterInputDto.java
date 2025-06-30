package com.ruoyi.app.dto.inputdto.wxLogin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class wxRegisterInputDto {
    private String openId;
    private String nickName;
    private String avatarUrl;
    private String phone;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
