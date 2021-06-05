package com.wayn.common.base.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.constant.Constants;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 基础控制器类，获得通用的参数封装、返回值封装方法
 */
@Slf4j
public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HttpSession session;

    @Autowired
    protected ServletContext application;

    /**
     * 是否为 post 请求
     */
    protected boolean isPost() {
        return HttpUtil.isPost(request);
    }

    /**
     * 是否为 get 请求
     */
    protected boolean isGet() {
        return HttpUtil.isGet(request);
    }

    /**
     * 获取分页对象
     * @param <T>
     * @return 返回分页对象
     */
    protected <T> Page<T> getPage() {
        //设置通用分页
        try {
            Integer pageNumber = ServletUtils.getParameterToInt(Constants.PAGE_NUMBER);
            Integer pageSize = ServletUtils.getParameterToInt(Constants.PAGE_SIZE);
            String sortName = ServletUtils.getParameter(Constants.SORT_NAME);
            String sortOrder = ServletUtils.getParameter(Constants.SORT_ORDER);
            Page<T> tPage = new Page<>(pageNumber, pageSize);
            if (StringUtils.isNotEmpty(sortName)) {
                String[] split = sortName.split(",");
                for (String s : split) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setColumn(s.replaceAll("[A-Z]", "_$0").toLowerCase());
                    orderItem.setAsc(sortOrder == null || !sortOrder.startsWith(Constants.ORDER_DESC));
                    tPage.addOrder(orderItem);
                }
            }
            return tPage;
        } catch (Exception e) {
            // log.error(e.getMessage(), e);
            return getPage(1, 10);
        }
    }


    /**
     * 获取分页对象
     * @param pageNumber 当前页
     * @param <T>
     * @return 返回分页对象
     */
    protected <T> Page<T> getPage(int pageNumber) {
        return getPage(pageNumber, 15);
    }

    /**
     * <p>
     * 获取分页对象
     * </p>
     *
     * @param pageNumber  当前页
     * @param pageSize    分页数
     * @param <T>
     * @return 返回分页对象
     */
    protected <T> Page<T> getPage(int pageNumber, int pageSize) {
        return new Page<>(pageNumber, pageSize);
    }

    /**
     * 格式化分页对象
     *
     * @param iPage 分页对象
     * @param list  格式化列表
     * @return 转换后的分页对象
     */
    protected <E, T> IPage<T> formatPage(IPage<E> iPage, List<T> list) {
        Page<T> formatPage = getPage();
        formatPage.setRecords(list);
        formatPage.setTotal(iPage.getTotal());
        formatPage.setCurrent(iPage.getCurrent());
        formatPage.setPages(iPage.getPages());
        formatPage.setSize(iPage.getSize());
        return formatPage;
    }

    /**
     * 重定向至地址 url
     *
     * @param url 请求地址
     * @return 重定向后地址
     */
    protected String redirectTo(String url) {
        return "redirect:" + url;
    }

    /**
     * 返回 JSON 格式对象
     *
     * @param object 转换对象
     * @return json字符串
     */
    protected String toJson(Object object) {
        return JSON.toJSONString(object, SerializerFeature.BrowserCompatible);
    }

    /**
     * 返回 JSON 格式对象
     *
     * @param object 转换对象
     * @param format 序列化特点
     * @return json字符串
     *
     */
    protected String toJson(Object object, String format) {
        if (format == null) {
            return toJson(object);
        }
        return JSON.toJSONStringWithDateFormat(object, format, SerializerFeature.WriteDateUseDateFormat);
    }

}
