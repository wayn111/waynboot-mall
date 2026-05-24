package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.trade.entity.Member;

/**
 * 用户表 Mapper 接口
 *
 * @author wayn
 * @since 2020-07-21
 */
public interface MemberMapper extends BaseMapper<Member> {

    IPage<Member> selectMemberListPage(Page<Member> page, Member member);

}
