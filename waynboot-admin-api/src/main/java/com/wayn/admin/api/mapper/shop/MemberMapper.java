package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.admin.api.domain.shop.Goods;
import com.wayn.admin.api.domain.shop.Member;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
