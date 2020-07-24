package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Member;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-07-21
 */
public interface MemberMapper extends BaseMapper<Member> {

    IPage<Member> selectMemberListPage(Page<Member> page, Member member);

}
