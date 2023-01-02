package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Member;
import com.wayn.common.core.mapper.shop.MemberMapper;
import com.wayn.common.core.service.shop.IMemberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-07-21
 */
@Service
@AllArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {

    private MemberMapper memberMapper;

    @Override
    public IPage<Member> listPage(Page<Member> page, Member member) {
        return memberMapper.selectMemberListPage(page,member);
    }
}
