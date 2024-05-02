package com.wayn.common.util;

import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpToolUtil {

    private static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
            .build();

    /**
     * @Description post请求，json格式
     */
    public static Response postJsonRequest(String url, String datJson, Headers headers) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), datJson);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        if (headers != null) {
            Request.Builder newbuilder = request.newBuilder();
            newbuilder.headers(headers);
            request = newbuilder.build();
        }
        return okHttpClient.newCall(request).execute();
    }


    /**
     * @Description post请求，map格式
     */
    public static Response postFormRequest(String url, Map<String, Object> datMap, Headers headers) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        buildFromBody(builder, datMap);
        FormBody formBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        if (headers != null) {
            Request.Builder newbuilder = request.newBuilder();
            newbuilder.headers(headers);
            request = newbuilder.build();
        }
        return okHttpClient.newCall(request).execute();
    }

    public static void buildFromBody(FormBody.Builder builder, Map<String, Object> datMap) {
        for (Map.Entry<String, Object> me : datMap.entrySet()) {
            if (me.getValue() instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) me.getValue();
                Map<String, Object> innerMap = new HashMap<>();
                for (Map.Entry<String, Object> inme : map.entrySet()) {
                    innerMap.put(me.getKey() + "[" + inme.getKey() + "]", inme.getValue());
                }
                buildFromBody(builder, innerMap);
            } else {
                builder.add(me.getKey(), me.getValue() + "");
            }
        }
    }

    /**
     * @Description post请求，json格式
     */
    public static Response postParamAndRequest(String url, Map<String, Object> datMap, Headers headers) throws IOException {
        StringBuffer content = new StringBuffer();
        iter(content, datMap);
        String cont = content.toString();
        cont = cont.substring(1);
        RequestBody requestBody = RequestBody.create(MediaType.get("*/*"), URLEncoder.encode(cont.substring(1)));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        if (headers != null) {
            Request.Builder newbuilder = request.newBuilder();
            newbuilder.headers(headers);
            request = newbuilder.build();
        }
        return okHttpClient.newCall(request).execute();
    }

    private static void iter(StringBuffer content, Map<String, Object> datMap) {
        for (Map.Entry<String, Object> me : datMap.entrySet()) {
            if (me.getValue() instanceof Map) {
                Map<String, Object> map = (Map) me.getValue();
                Map<String, Object> innerMap = new HashMap<>();
                for (Map.Entry<String, Object> inme : map.entrySet()) {
                    innerMap.put(me.getKey() + "[" + inme.getKey() + "]", inme.getValue());
                }
                iter(content, innerMap);
            } else {
                content.append("&" + me.getKey() + "=" + me.getValue());
            }
        }
    }

    public static Response getRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return okHttpClient.newCall(request).execute();
    }
}
