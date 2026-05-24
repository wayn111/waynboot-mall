package com.wayn.mobile.api.controller.goods;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.goods.entity.SearchHistory;
import com.wayn.domain.api.goods.service.ISearchHistoryService;
import com.wayn.common.model.request.SearchHistorySaveReqVO;
import com.wayn.common.model.response.SearchHistoryResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史接口
 *
 * @author wayn
 * @since 2020-09-23
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("searchHistory")
public class SearchHistoryController extends BaseController {

    private final ISearchHistoryService iSearchHistoryService;

    /**
     * 用户搜索历史列表
     *
     * @return
     */
    @GetMapping("list")
    public R<List<SearchHistoryResVO>> list() {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("查询搜索历史开始, memberId={}", memberId);
        List<SearchHistoryResVO> historyList = iSearchHistoryService.selectList(memberId)
                .stream()
                .map(this::toSearchHistoryResVO)
                .toList();
        log.info("查询搜索历史完成, memberId={}, count={}", memberId, historyList.size());
        return R.success(historyList);
    }

    /**
     * 添加搜索历史
     *
     * @param searchHistory 搜索历史参数
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody SearchHistorySaveReqVO searchHistory) {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("新增搜索历史开始, memberId={}, keyword={}", memberId, safeKeyword(searchHistory.getKeyword()));
        SearchHistory entity = toSearchHistory(searchHistory);
        entity.setUserId(memberId);
        entity.setCreateTime(LocalDateTime.now());
        boolean saved = iSearchHistoryService.save(entity);
        log.info("新增搜索历史完成, memberId={}, result={}", memberId, saved);
        return R.result(saved);
    }

    /**
     * 删除搜索历史
     *
     * @param id 搜索历史id
     * @return R
     */
    @DeleteMapping("{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("删除搜索历史开始, memberId={}, id={}", memberId, id);
        boolean removed = iSearchHistoryService.remove(new QueryWrapper<SearchHistory>()
                .eq("id", id)
                .eq("user_id", memberId));
        log.info("删除搜索历史完成, memberId={}, id={}, result={}", memberId, id, removed);
        return R.result(removed);
    }

    /**
     * 删除当前用户所有搜索历史
     *
     * @return R
     */
    @DeleteMapping("all")
    public R<Boolean> delete() {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("清空搜索历史开始, memberId={}", memberId);
        boolean removed = iSearchHistoryService.remove(new QueryWrapper<SearchHistory>().eq("user_id", memberId));
        log.info("清空搜索历史完成, memberId={}, result={}", memberId, removed);
        return R.result(removed);
    }

    private SearchHistoryResVO toSearchHistoryResVO(SearchHistory searchHistory) {
        SearchHistoryResVO resVO = new SearchHistoryResVO();
        resVO.setId(searchHistory.getId());
        resVO.setKeyword(searchHistory.getKeyword());
        resVO.setFrom(searchHistory.getFrom());
        resVO.setHasGoods(searchHistory.getHasGoods());
        resVO.setCreateTime(searchHistory.getCreateTime());
        return resVO;
    }

    private SearchHistory toSearchHistory(SearchHistorySaveReqVO reqVO) {
        SearchHistory history = new SearchHistory();
        history.setKeyword(reqVO.getKeyword());
        history.setFrom(reqVO.getFrom());
        history.setHasGoods(reqVO.getHasGoods());
        return history;
    }

    private String safeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword.length() > 20 ? keyword.substring(0, 20) : keyword;
    }

}
