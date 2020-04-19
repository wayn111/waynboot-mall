package com.wayn.common.util.http;

import com.wayn.common.constant.Constants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class HttpUtil {

    private static final Logger logger = Logger.getLogger("HttpUtil");

    /**
     * 允许 JS 跨域设置
     *
     * <p>
     * <!-- 使用 nginx 注意在 nginx.conf 中配置 -->
     * <p>
     * http {
     * ......
     * add_header Access-Control-Allow-Origin *;
     * ......
     * }
     * </p>
     *
     * <p>
     * 非 ngnix 下，如果该方法设置不管用、可以尝试增加下行代码。
     * <p>
     * response.setHeader("Access-Control-Allow-Origin", "*");
     * </p>
     *
     * @param response 响应请求
     */
    public static void allowJsCrossDomain(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * <p>
     * 判断请求是否为 AJAX
     * </p>
     *
     * @param request 当前请求
     * @return
     */
    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * <p>
     * AJAX 设置 response 返回状态
     * </p>
     *
     * @param response
     * @param status   HTTP 状态码
     * @param tip
     */
    public static void ajaxStatus(HttpServletResponse response, int status, String tip) {
        try {
            response.setContentType("text/html;charset=" + Constants.UTF_ENCODING);
            response.setStatus(status);
            PrintWriter out = response.getWriter();
            out.print(tip);
            out.flush();
        } catch (IOException e) {
            logger.severe(e.toString());
        }
    }

    /**
     * <p>
     * 获取当前 URL 包含查询条件
     * </p>
     *
     * @param request
     * @param encode  URLEncoder编码格式
     * @return
     * @throws IOException
     */
    public static String getQueryString(HttpServletRequest request, String encode) throws IOException {
        StringBuilder sb = new StringBuilder(request.getRequestURL());
        String query = request.getQueryString();
        if (query != null && query.length() > 0) {
            sb.append("?").append(query);
        }
        return URLEncoder.encode(sb.toString(), encode);
    }

    /**
     * <p>
     * getRequestURL是否包含在URL之内
     * </p>
     *
     * @param request
     * @param url     参数为以';'分割的URL字符串
     * @return
     */
    public static boolean inContainURL(HttpServletRequest request, String url) {
        boolean result = false;
        if (url != null && !"".equals(url.trim())) {
            String[] urlArr = url.split(";");
            StringBuilder reqUrl = new StringBuilder(request.getRequestURL());
            for (String s : urlArr) {
                if (reqUrl.indexOf(s) > 1) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * <p>
     * URLEncoder 返回地址
     * </p>
     *
     * @param url      跳转地址
     * @param retParam 返回地址参数名
     * @param retUrl   返回地址
     * @return
     */
    public static String encodeRetURL(String url, String retParam, String retUrl) {
        return encodeRetURL(url, retParam, retUrl, null);
    }

    /**
     * <p>
     * URLEncoder 返回地址
     * </p>
     *
     * @param url      跳转地址
     * @param retParam 返回地址参数名
     * @param retUrl   返回地址
     * @param data     携带参数
     * @return
     */
    public static String encodeRetURL(String url, String retParam, String retUrl, Map<String, String> data) {
        if (url == null) {
            return null;
        }

        StringBuilder retStr = new StringBuilder(url);
        retStr.append("?");
        retStr.append(retParam);
        retStr.append("=");
        try {
            retStr.append(URLEncoder.encode(retUrl, Constants.UTF_ENCODING));
        } catch (UnsupportedEncodingException e) {
            logger.severe("encodeRetURL error." + url);
            e.printStackTrace();
        }

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                retStr.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return retStr.toString();
    }

    /**
     * <p>
     * URLDecoder 解码地址
     * </p>
     *
     * @param url 解码地址
     * @return
     */
    public static String decodeURL(String url) {
        if (url == null) {
            return null;
        }
        String retUrl = "";

        try {
            retUrl = URLDecoder.decode(url, Constants.UTF_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.severe("encodeRetURL error." + url);
            e.printStackTrace();
        }

        return retUrl;
    }

    /**
     * <p>
     * GET 请求
     * </p>
     *
     * @param request
     * @return boolean
     */
    public static boolean isGet(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod());
    }

    /**
     * <p>
     * POST 请求
     * </p>
     *
     * @param request
     * @return boolean
     */
    public static boolean isPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    /**
     * <p>
     * 请求重定向至地址 location
     * </p>
     *
     * @param response 请求响应
     * @param location 重定向至地址
     */
    public static void sendRedirect(HttpServletResponse response, String location) {
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            logger.severe("sendRedirect location:" + location);
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * 获取Request Playload 内容
     * </p>
     *
     * @param request
     * @return Request Playload 内容
     */
    public static String requestPlayload(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * <p>
     * 获取当前完整请求地址
     * </p>
     *
     * @param request
     * @return 请求地址
     */
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getScheme());
        // 请求协议 http,https
        url.append("://");
        url.append(request.getHeader("host"));// 请求服务器
        url.append(request.getRequestURI());// 工程名
        if (request.getQueryString() != null) {
            // 请求参数
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }

    /**
     * 获取当前项目路径，<br>
     * 例如：http://localhost:8081/crowdfounding
     *
     * @param request
     * @return
     */
    public static String getRequestContext(HttpServletRequest request) {
        // 请求协议 http,https
        return request.getScheme() + "://" +
                request.getHeader("host") +// 请求服务器
                request.getContextPath();
    }

    public static String getValueByCookie(HttpServletRequest request) {
        String value = "";
        Cookie[] cookies = request.getCookies();
        if (Objects.nonNull(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    value = cookie.getValue();
                    break;
                }
            }
        }
        return value;
    }
}