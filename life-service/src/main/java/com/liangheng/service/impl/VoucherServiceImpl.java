package com.liangheng.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liangheng.dto.Result;
import com.liangheng.entity.Voucher;
import com.liangheng.mapper.VoucherMapper;
import com.liangheng.entity.LimitVoucher;
import com.liangheng.service.ILimitVoucherService;
import com.liangheng.service.IVoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ILimitVoucherService limitVoucherService;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addLimitVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存限时优惠券信息
        LimitVoucher limitVoucher = new LimitVoucher();
        limitVoucher.setVoucherId(voucher.getId())
                .setStock(voucher.getStock())
                .setBeginTime(voucher.getBeginTime())
                .setEndTime(voucher.getEndTime());
        limitVoucherService.save(limitVoucher);
    }
}
