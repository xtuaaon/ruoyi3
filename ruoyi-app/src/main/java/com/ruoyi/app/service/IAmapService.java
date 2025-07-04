package com.ruoyi.app.service;

import java.util.List;
import java.util.Map;

public interface IAmapService {
    List<Map<String, Object>> searchAddressByKeyword(String location, String keywords, String city);
    String reverseGeocode(String location);
}
