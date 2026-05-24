package com.wayn.mobile.api.controller.member;

import com.wayn.domain.api.trade.entity.Address;
import com.wayn.domain.api.trade.service.IAddressService;
import com.wayn.common.model.request.AddressSaveReqVO;
import com.wayn.common.model.response.AddressResVO;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private IAddressService addressService;

    @Test
    void listShouldReturnAddressResponseVoList() {
        AddressController controller = new AddressController(addressService);
        Address address = new Address();
        address.setId(1L);
        address.setName("张三");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setCounty("南山区");
        address.setAddressDetail("科技园 1 号");
        address.setAreaCode("440305");
        address.setPostalCode("518000");
        address.setTel("13800138000");
        address.setDefault(true);
        when(addressService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<Address>>any()))
                .thenReturn(List.of(address));

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(99L);

            R<List<AddressResVO>> result = controller.list();

            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
            assertEquals(1, result.getData().size());
            AddressResVO resVO = result.getData().get(0);
            assertEquals(address.getId(), resVO.getId());
            assertEquals(address.getName(), resVO.getName());
            assertEquals(address.getProvince(), resVO.getProvince());
            assertEquals(address.getCity(), resVO.getCity());
            assertEquals(address.getCounty(), resVO.getCounty());
            assertEquals(address.getAddressDetail(), resVO.getAddressDetail());
            assertEquals(address.getAreaCode(), resVO.getAreaCode());
            assertEquals(address.getPostalCode(), resVO.getPostalCode());
            assertEquals(address.getTel(), resVO.getTel());
            assertEquals(Boolean.TRUE, resVO.getIsDefault());
        }
    }

    @Test
    void addShouldMapSaveReqVoToEntityAndBindCurrentUser() {
        AddressController controller = new AddressController(addressService);
        AddressSaveReqVO reqVO = new AddressSaveReqVO();
        reqVO.setName("李四");
        reqVO.setProvince("浙江省");
        reqVO.setCity("杭州市");
        reqVO.setCounty("西湖区");
        reqVO.setAddressDetail("文三路 8 号");
        reqVO.setAreaCode("330106");
        reqVO.setPostalCode("310000");
        reqVO.setTel("13900139000");
        reqVO.setIsDefault(true);
        when(addressService.update(any())).thenReturn(true);
        when(addressService.save(any(Address.class))).thenReturn(true);

        try (MockedStatic<MobileSecurityUtils> mockedSecurity = mockStatic(MobileSecurityUtils.class)) {
            mockedSecurity.when(MobileSecurityUtils::getUserId).thenReturn(88L);

            R<Boolean> result = controller.add(reqVO);

            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressService).save(addressCaptor.capture());
            Address saved = addressCaptor.getValue();
            assertEquals(88L, saved.getMemberId());
            assertEquals(reqVO.getName(), saved.getName());
            assertEquals(reqVO.getProvince(), saved.getProvince());
            assertEquals(reqVO.getCity(), saved.getCity());
            assertEquals(reqVO.getCounty(), saved.getCounty());
            assertEquals(reqVO.getAddressDetail(), saved.getAddressDetail());
            assertEquals(reqVO.getAreaCode(), saved.getAreaCode());
            assertEquals(reqVO.getPostalCode(), saved.getPostalCode());
            assertEquals(reqVO.getTel(), saved.getTel());
            assertEquals(true, saved.isDefault());
            assertNotNull(saved.getCreateTime());
            assertEquals(ReturnCodeEnum.SUCCESS.getCode(), result.getCode());
        }
    }
}
