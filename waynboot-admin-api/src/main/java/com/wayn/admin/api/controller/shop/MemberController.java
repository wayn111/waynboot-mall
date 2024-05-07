package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Member;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 会员管理
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("shop/member")
public class MemberController extends BaseController {

    private IMemberService iMemberService;

    /**
     * 会员列表
     *
     * @param member
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:member:list')")
    @GetMapping("list")
    public R<IPage<Member>> list(Member member) {
        Page<Member> page = getPage();
        return R.success(iMemberService.listPage(page, member));
    }

    /**
     * 获取会员信息
     *
     * @param memberId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:member:info')")
    @GetMapping("{memberId}")
    public R<Member> getMember(@PathVariable Long memberId) {
        return R.success(iMemberService.getById(memberId));
    }

    /**
     * 修改会员信息
     *
     * @param member
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:member:update')")
    @PutMapping
    public R<Boolean> updateMember(@Validated @RequestBody Member member) {
        member.setUpdateTime(new Date());
        return R.result(iMemberService.updateById(member));
    }
}
