package com.ruoyi.app.dto.inputdto.amap;

public class searchAddressByKeywordInputDto {
    private String location;
    private String keywords;
    private String city;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
