package com.wayn.mobile.api.controller.goods;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.model.request.SearchRequestVO;
import com.wayn.common.model.response.HotKeywordsResVO;
import com.wayn.common.model.response.SearchGoodsItemResVO;
import com.wayn.mobile.api.service.SearchApplicationService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 搜索接口
 *
 * @author wayn
 * @since 2020-09-23
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("search")
public class SearchController extends BaseController {

    private final SearchApplicationService searchApplicationService;

    /**
     * 商城搜索建议
     *
     * @param searchRequestVO 搜索参数
     * @return R
     */
    @GetMapping("sugguest")
    public R<List<String>> sugguest(SearchRequestVO searchRequestVO) throws IOException {
        String keyword = searchRequestVO.getKeyword();
        log.info("查询搜索建议开始, keyword={}", safeKeyword(keyword));
        List<String> list = searchApplicationService.suggest(searchRequestVO);
        log.info("查询搜索建议完成, keyword={}, count={}", safeKeyword(keyword), list.size());
        return R.success(list);
    }

    /**
     * 商城搜索结果
     *
     * @param searchRequestVO 搜索参数
     * @return R
     */
    @GetMapping("result")
    public R<List<SearchGoodsItemResVO>> result(SearchRequestVO searchRequestVO) throws IOException {
        Long memberId = MobileSecurityUtils.getUserId();
        String keyword = searchRequestVO.getKeyword();
        Page<SearchRequestVO> page = getPage();
        log.info("查询搜索结果开始, userId={}, keyword={}, pageNum={}, pageSize={}, filterNew={}, filterHot={}, isNew={}, isHot={}, isPrice={}, isSales={}, orderBy={}",
                memberId, safeKeyword(keyword), page.getCurrent(), page.getSize(),
                Boolean.TRUE.equals(searchRequestVO.getFilterNew()), Boolean.TRUE.equals(searchRequestVO.getFilterHot()),
                Boolean.TRUE.equals(searchRequestVO.getIsNew()), Boolean.TRUE.equals(searchRequestVO.getIsHot()),
                Boolean.TRUE.equals(searchRequestVO.getIsPrice()), Boolean.TRUE.equals(searchRequestVO.getIsSales()),
                searchRequestVO.getOrderBy());
        List<SearchGoodsItemResVO> returnGoodsList = searchApplicationService.searchResult(searchRequestVO, page, memberId);
        log.info("查询搜索结果完成, userId={}, keyword={}, count={}", memberId, safeKeyword(keyword), returnGoodsList.size());
        return R.success(returnGoodsList);
    }

    /**
     * 热门搜索词
     *
     * @return R
     */
    @GetMapping("hotKeywords")
    public R<HotKeywordsResVO> hotKeywords() {
        log.info("查询热门搜索词开始");
        HotKeywordsResVO resVO = searchApplicationService.hotKeywords();
        log.info("查询热门搜索词完成, hotCount={}, hasDefault={}", resVO.getHotStrings().size(),
                resVO.getDefaultSearch() != null);
        return R.success(resVO);
    }

    /**
     * 缩短日志里的搜索词，避免长关键词污染日志。
     *
     * @param keyword 搜索词
     * @return 截断后的搜索词
     */
    private String safeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return StringUtils.abbreviate(keyword, 20);
    }

}
