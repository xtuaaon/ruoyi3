package com.ruoyi.common.utils.http;

import com.alibaba.fastjson2.JSON;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    private static final CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(HttpConfig.MAX_TOTAL_CONN);
        cm.setDefaultMaxPerRoute(HttpConfig.MAX_ROUTE_CONN);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(HttpConfig.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .setResponseTimeout(HttpConfig.SO_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(HttpConfig.RETRY_COUNT, TimeValue.ofSeconds(1)))
                .setKeepAliveStrategy((response, context) -> TimeValue.ofMilliseconds(HttpConfig.KEEP_ALIVE_TIMEOUT))
                .build();
    }

    public static String sendGet(String url) {
        return sendGet(url, "");
    }

    public static String sendGet(String url, String param) {
        String urlWithParams = param.isEmpty() ? url : url + "?" + param;
        HttpGet httpGet = new HttpGet(urlWithParams);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        try{
            return httpClient.execute(httpGet, response -> {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("Get Response: {}", responseBody);
                return responseBody;
            });
        }
        catch (IOException e) {
            log.error("Error in sendGet, url={}, param={}", url, param, e);
            return null;
        }
    }

    public static String sendPost(String url, Object data, String contentType) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        // 根据数据类型和contentType设置实体
        if (data instanceof String) {
            httpPost.setEntity(new StringEntity((String) data, ContentType.create(contentType, StandardCharsets.UTF_8)));
        } else if (data instanceof Map && "application/json".equals(contentType)) {
            String jsonStr = JSON.toJSONString(data);
            httpPost.setEntity(new StringEntity(jsonStr, ContentType.APPLICATION_JSON));
        } else if (data instanceof Map) {
            // 默认为表单格式
            @SuppressWarnings("unchecked")
            Map<String, String> params = (Map<String, String>) data;
            List<NameValuePair> formParams = new ArrayList<>();
            params.forEach((key, value) -> formParams.add(new BasicNameValuePair(key, value)));
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + data.getClass());
        }

        try {
            return httpClient.execute(httpPost, response -> {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("POST Response: {}", responseBody);
                return responseBody;
            });
        } catch (IOException e) {
            log.error("Error in sendPost, url={}, data={}", url, data, e);
            return null;
        }
    }

    // 便捷方法
    public static String sendPostForm(String url, Map<String, String> params) {
        return sendPost(url, params, "application/x-www-form-urlencoded");
    }

    public static String sendPostJson(String url, Map<String, String> params) {
        return sendPost(url, params, "application/json");
    }
}