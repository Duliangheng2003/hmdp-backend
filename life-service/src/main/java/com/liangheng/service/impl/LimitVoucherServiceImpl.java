package com.liangheng.service.impl;

import com.liangheng.entity.LimitVoucher;
import com.liangheng.mapper.SeckillVoucherMapper;
import com.liangheng.service.ILimitVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
@Service
public class LimitVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, LimitVoucher> implements ILimitVoucherService {

}
