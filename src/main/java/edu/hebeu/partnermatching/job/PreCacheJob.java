package edu.hebeu.partnermatching.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    UserService userService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RedisTemplate redisTemplate;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);


    @Scheduled(cron = "0 51 19 * * *")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("tiantian:precachejob:docache:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                System.out.println("getLock:" + Thread.currentThread().getId());
                for (Long userid: mainUserList
                     ) {
                    //查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20),queryWrapper);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    String redisKey = String.format("tiantian:user:recommend:%s",mainUserList);

                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e){
                        log.error("redis set error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }   finally {
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }



    }
}
