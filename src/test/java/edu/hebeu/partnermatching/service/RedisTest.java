package edu.hebeu.partnermatching.service;

import edu.hebeu.partnermatching.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    @Resource
    RedisTemplate redisTemplate;

    @Test
    public void test(){
        //插入
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("String","tiantian");
        valueOperations.set("Int",33);
        valueOperations.set("double",33.3);
        User user = new User();
        user.setUserName("tiantian");
        user.setUserAccount("32156");
        valueOperations.set("User",user);

        //查询
        Object tiantian = valueOperations.get("String");
        Assertions.assertTrue("tiantian".equals((String) tiantian));
        tiantian = valueOperations.get("Int");
        Assertions.assertTrue(33 == (Integer) tiantian);
        tiantian = valueOperations.get("double");
        Assertions.assertTrue(33.3 == (double) tiantian);
        tiantian = valueOperations.get("User");
        System.out.println(tiantian);

        //删

    }
}
