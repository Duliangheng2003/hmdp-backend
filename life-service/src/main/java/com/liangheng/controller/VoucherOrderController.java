package com.liangheng.controller;


import com.liangheng.dto.Result;
import com.liangheng.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 优惠券订单功能模块
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    // 抢购优惠券
    @PostMapping("seckill/{id}")
    public Result limitVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.limitVoucher(voucherId);
    }
}
