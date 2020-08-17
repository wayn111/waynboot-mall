package com.wayn.mobile.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;

public interface IHomeService {

    /**
     * 获取首页数据（bannerList，category List，newGoodsList，hotGoodsList）， <br>
     * 采用Future方式，可以异步计算返回结果，但是对于结果的获取却是很不方便，只能通过阻塞或者轮询的方式得到任务的结果， <br>
     * 区别详情见下方链接
     *
     * @return r
     * @see <a href="https://www.cnblogs.com/cjsblog/p/9267163.html">https://www.cnblogs.com/cjsblog/p/9267163.html</a>
     */
    R getHomeIndexData();

    /**
     * 获取首页数据（bannerList，category List，newGoodsList，hotGoodsList） <br>
     * 采用CompletableFuture方式
     *
     * @return r
     * @see <a href="https://www.cnblogs.com/cjsblog/p/9267163.html">https://www.cnblogs.com/cjsblog/p/9267163.html</a>
     */
    R getHomeIndexDataCompletableFuture();

    /**
     * 获取商品分页列表
     * @param page 分页对象
     * @return r
     */
    R listGoodsPage(Page<Goods> page);

}
