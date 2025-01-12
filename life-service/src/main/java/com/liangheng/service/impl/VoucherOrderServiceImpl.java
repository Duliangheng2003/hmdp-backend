package com.liangheng.service.impl;

import com.liangheng.dto.Result;
import com.liangheng.entity.LimitVoucher;
import com.liangheng.entity.VoucherOrder;
import com.liangheng.mapper.VoucherOrderMapper;
import com.liangheng.service.ILimitVoucherService;
import com.liangheng.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liangheng.utils.RedisIdWorker;
import com.liangheng.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ILimitVoucherService limitVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;


    @Resource
    private RedissonClient redissonClient;

    @Override
    public Result limitVoucher(Long voucherId) {
        // 查询优惠券
        LimitVoucher voucher = limitVoucherService.getById(voucherId);
        // 判断抢购是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
            return Result.fail("抢购尚未开始");
        }
        // 判断抢购是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
            return Result.fail("抢购已经结束");
        }
        // 判断库存是否充足
        if (voucher.getStock() < 1){
            return Result.fail("库存不足");
        }

        // 获取当前登录用户id
        Long userId = UserHolder.getUser().getId();
        // 创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 获取锁
        boolean isLock = lock.tryLock();
        // 判断是否获取锁成功
        if (!isLock){
            // 获取锁失败，直接返回
            return Result.fail("不允许重复下单");
        }
        try{
            //获取代理对象
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            //释放锁
            lock.unlock();
        }

    }

    @Transactional
    public Result createVoucherOrder(Long voucherId){
        // 查询当前用户的该优惠券下单记录
        Long userId = UserHolder.getUser().getId();
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 判断用户是否已经购买过该优惠券
        if (count > 0){
            return Result.fail("用户已经购买过一次！");
        }
        // 扣减库存
        boolean success = limitVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id",voucherId).gt("stock", 0)
                .update();
        if (!success){
            // 扣减失败
            return Result.fail("库存不足！");
        }

        // 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 生成唯一的订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 设置用户和优惠券id
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        // 返回订单id
        return Result.ok(orderId);
    }


}
