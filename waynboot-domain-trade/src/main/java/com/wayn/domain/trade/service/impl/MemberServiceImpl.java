package com.wayn.domain.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.trade.entity.Member;
import com.wayn.domain.api.trade.mapper.MemberMapper;
import com.wayn.domain.api.trade.service.IMemberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户表 服务实现类
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
        return memberMapper.selectMemberListPage(page, member);
    }
}
