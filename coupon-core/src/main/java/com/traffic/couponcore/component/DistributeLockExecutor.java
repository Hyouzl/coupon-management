package com.traffic.couponcore.component;


import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class DistributeLockExecutor {

    private final RedissonClient redissonClient;

    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logic.run();

    }

}
