package com.liangheng.service;

import com.liangheng.dto.Result;
import com.liangheng.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result limitVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);
}
