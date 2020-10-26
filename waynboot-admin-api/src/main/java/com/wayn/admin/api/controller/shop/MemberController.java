package com.wayn.admin.api.controller.shop;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.service.shop.IMemberService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@RequestMapping("shop/member")
public class MemberController extends BaseController {

    @Autowired
    private IMemberService iMemberService;

    @GetMapping("list")
    public R list(Member member) {
        Page<Member> page = getPage();
        return R.success().add("page", iMemberService.listPage(page, member));
    }

    @GetMapping("{memberId}")
    public R getMember(@PathVariable Long memberId) {
        return R.success().add("data", iMemberService.getById(memberId));
    }

    @PutMapping
    public R updateMember(@Validated @RequestBody Member member) {
        member.setUpdateTime(new Date());
        return R.result(iMemberService.updateById(member));
    }
}
