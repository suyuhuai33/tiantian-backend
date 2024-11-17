package edu.hebeu.partnermatching.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class testWatchDog {
    @Resource
    RedissonClient redissonClient;

    @Test
    public void watchdogTest(){
        RLock lock = redissonClient.getLock("tiantian:precachejob:docache:lock");
        try {
            if(lock.tryLock(0, -1 , TimeUnit.MILLISECONDS)){
                //要运行的程序
                Thread.sleep(300000);
                System.out.println("getLock:" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println("getLockError:" + e);
        } finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
