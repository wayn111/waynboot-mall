package com.wayn.mobile.api.controller.member;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wayn.domain.api.trade.entity.Address;
import com.wayn.domain.api.trade.service.IAddressService;
import com.wayn.common.model.request.AddressSaveReqVO;
import com.wayn.common.model.response.AddressResVO;
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

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用户地址接口
 *
 * @author wayn
 * @since 2020-08-03
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("address")
public class AddressController {

    private final IAddressService iAddressService;

    /**
     * 地址列表
     *
     * @return R
     */
    @GetMapping("list")
    public R<List<AddressResVO>> list() {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("查询地址列表开始, memberId={}", memberId);
        List<AddressResVO> addressList = iAddressService.list(new QueryWrapper<Address>().eq("member_id", memberId))
                .stream()
                .map(this::toAddressResVO)
                .toList();
        log.info("查询地址列表完成, memberId={}, count={}", memberId, addressList.size());
        return R.success(addressList);
    }

    /**
     * 添加地址
     *
     * @return R
     */
    @PostMapping
    public R<Boolean> add(@RequestBody AddressSaveReqVO reqVO) {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("保存地址开始, memberId={}, addressId={}, isDefault={}", memberId, reqVO.getId(), Boolean.TRUE.equals(reqVO.getIsDefault()));
        if (Boolean.TRUE.equals(reqVO.getIsDefault())) {
            iAddressService.update(new UpdateWrapper<Address>()
                    .eq("member_id", memberId)
                    .set("is_default", false));
        }
        Address address = toAddress(reqVO);
        boolean result;
        if (Objects.nonNull(reqVO.getId())) {
            address.setUpdateTime(new Date());
            result = iAddressService.update(address, new UpdateWrapper<Address>()
                    .eq("id", reqVO.getId())
                    .eq("member_id", memberId));
            log.info("更新地址完成, memberId={}, addressId={}, result={}", memberId, reqVO.getId(), result);
            return R.result(result);
        }
        address.setMemberId(memberId);
        address.setCreateTime(new Date());
        result = iAddressService.save(address);
        log.info("新增地址完成, memberId={}, result={}", memberId, result);
        return R.result(result);
    }

    /**
     * 删除地址
     *
     * @return R
     */
    @DeleteMapping("{addressId}")
    public R<Boolean> delete(@PathVariable Long addressId) {
        Long memberId = MobileSecurityUtils.getUserId();
        log.info("删除地址开始, memberId={}, addressId={}", memberId, addressId);
        boolean removed = iAddressService.remove(new QueryWrapper<Address>()
                .eq("id", addressId)
                .eq("member_id", memberId));
        log.info("删除地址完成, memberId={}, addressId={}, result={}", memberId, addressId, removed);
        return R.result(removed);
    }

    private AddressResVO toAddressResVO(Address address) {
        AddressResVO resVO = new AddressResVO();
        resVO.setId(address.getId());
        resVO.setName(address.getName());
        resVO.setProvince(address.getProvince());
        resVO.setCity(address.getCity());
        resVO.setCounty(address.getCounty());
        resVO.setAddressDetail(address.getAddressDetail());
        resVO.setAreaCode(address.getAreaCode());
        resVO.setPostalCode(address.getPostalCode());
        resVO.setTel(address.getTel());
        resVO.setIsDefault(address.isDefault());
        return resVO;
    }

    private Address toAddress(AddressSaveReqVO reqVO) {
        Address address = new Address();
        address.setId(reqVO.getId());
        address.setName(reqVO.getName());
        address.setProvince(reqVO.getProvince());
        address.setCity(reqVO.getCity());
        address.setCounty(reqVO.getCounty());
        address.setAddressDetail(reqVO.getAddressDetail());
        address.setAreaCode(reqVO.getAreaCode());
        address.setPostalCode(reqVO.getPostalCode());
        address.setTel(reqVO.getTel());
        address.setDefault(Boolean.TRUE.equals(reqVO.getIsDefault()));
        return address;
    }
}
