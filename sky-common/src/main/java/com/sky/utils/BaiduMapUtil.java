package com.sky.utils;

import com.sky.properties.BaiduProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class BaiduMapUtil {
    @Autowired
    private BaiduProperties baiduProperties;
    private static String GECODING_URL = "https://api.map.baidu.com/geocoding/v3?";
    private static String ROUTE_URL="https://api.map.baidu.com/directionlite/v1/driving?";





    // 对Map内所有value作utf8编码，拼接返回结果
    public String toQueryString(Map<?, ?> data)
            throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            queryString.append(URLEncoder.encode((String) pair.getValue(),
                    "UTF-8") + "&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    // 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


    public String getGeocodingUrl(String address) throws Exception {
        String strUrl=GECODING_URL;
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", baiduProperties.getAk());
        params.put("callback", "showLocation");
        params.put("sn", getGeocodingSN(address));

        StringBuilder queryString = new StringBuilder();
        queryString.append(strUrl);

        for (Map.Entry<String, String> pair : params.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        return queryString.toString();
    }
    public static String parseResponse(String jsonpResponse) throws Exception {
        // Assuming the format is "showLocation&&showLocation({JSON})"
        int startIndex = jsonpResponse.indexOf("({") + 1;
        int endIndex = jsonpResponse.lastIndexOf("})") + 1;

        if (startIndex == 0 || endIndex == 0) {
            throw new IllegalArgumentException("Invalid JSONP response format");
        }

        return jsonpResponse.substring(startIndex, endIndex);
    }

    public String getRouteUrl(String clientLngLat, String shopLngLat) throws Exception {
        String strUrl=ROUTE_URL;
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("origin", shopLngLat);
        params.put("destination", clientLngLat);
        params.put("ak", baiduProperties.getAk());
        String currentTimestamp =  String.valueOf(System.currentTimeMillis());
        params.put("timestamp", currentTimestamp);
        params.put("sn", getRouteSN(shopLngLat, clientLngLat));

        StringBuilder queryString = new StringBuilder();
        queryString.append(strUrl);
        for (Map.Entry<String, String> pair : params.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        return queryString.toString();

    }
    private String getGeocodingSN(String address) throws UnsupportedEncodingException {
        BaiduMapUtil baiduMapUtil = new BaiduMapUtil();

        // 计算sn跟参数对出现顺序有关，get请求请使用LinkedHashMap保存<key,value>，该方法根据key的插入顺序排序；post请使用TreeMap保存<key,value>，该方法会自动将key按照字母a-z顺序排序。所以get请求可自定义参数顺序（sn参数必须在最后）发送请求，但是post请求必须按照字母a-z顺序填充body（sn参数必须在最后）。以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。

        Map<String, String> paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("address", address);
        paramsMap.put("output", "json");
        paramsMap.put("ak",baiduProperties.getAk());
        paramsMap.put("callback", "showLocation");

        // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
        String paramsStr = baiduMapUtil.toQueryString(paramsMap);

        // 对paramsStr前面拼接上/geocoder/v2/?，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
        String wholeStr = new String("/geocoding/v3?" + paramsStr + baiduProperties.getSk());

        // 对上面wholeStr再作utf8编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 调用下面的MD5方法得到最后的sn签名7de5a22212ffaa9e326444c75a58f9a0
        return baiduMapUtil.MD5(tempStr);
    }
    private String getRouteSN(String origin, String destination) throws Exception {
        Map paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("origin", origin);
        paramsMap.put("destination", destination);
        paramsMap.put("ak", baiduProperties.getAk());
        String currentTimestamp =  String.valueOf(System.currentTimeMillis());
        paramsMap.put("timestamp", currentTimestamp);

        // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
        String paramsStr = toQueryString(paramsMap);

        // 对paramsStr前面拼接上***，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
        String wholeStr = new String("/directionlite/v1/driving?" + paramsStr + baiduProperties.getSk());


        // 对上面wholeStr再作utf8编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 调用下面的MD5方法得到最后的sn签名
        String sn = MD5(tempStr);

        return sn;
    }

}

