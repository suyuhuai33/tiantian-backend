package edu.hebeu.partnermatching.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
public class RedissionTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedission(){
        //list数据存储在JVM内存中
        List<String> list = new ArrayList<>();
        list.add("帅");
        System.out.println("list:" + list.get(0));

        //redis作为list使用
        RList<String> rList = redissonClient.getList("test-list");
      //  rList.add("可爱");
        System.out.println("rlist:" + rList.get(0));

        //Map
        HashMap<String, Integer> map = new HashMap<>();
        map.put("帅哥的座号",33);
        map.get("帅哥的座号");

        RMap<String, Integer> rMap = redissonClient.getMap("map-test");
      //  rMap.put("美女的",48);
        rMap.get("美女的");


        rMap.remove("美女的");
        rList.remove(0);

    }
}
