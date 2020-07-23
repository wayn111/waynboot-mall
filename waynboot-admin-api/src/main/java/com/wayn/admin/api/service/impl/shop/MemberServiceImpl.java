package com.wayn.admin.api.service.impl.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.shop.Member;
import com.wayn.admin.api.mapper.shop.MemberMapper;
import com.wayn.admin.api.service.shop.IMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public IPage<Member> listPage(Page<Member> page, Member member) {
        return memberMapper.selectMemberListPage(page,member);
    }
}
