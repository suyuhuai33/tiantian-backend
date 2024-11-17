package edu.hebeu.partnermatching.service;

import edu.hebeu.partnermatching.common.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具类测试
 */
@SpringBootTest
public class AlgorithmUtilsTest {
    @Test
    void test(){
        String s1 = new String("我爱吃饭");
        String s2 = new String("我超级爱吃饭");
        String s3 = new String("我不爱吃饭");

        int distance = AlgorithmUtils.minDistance(s1, s2);
        int distance1 = AlgorithmUtils.minDistance(s1, s3);
        System.out.println(distance);
        System.out.println(distance1);
    }

    @Test
    void testTags(){
        List<String> s1 = Arrays.asList("吃饭", "睡觉", "看电影");
        List<String> s2 = Arrays.asList("不吃饭", "不喜欢睡觉", "不看电影");
        List<String> s3 = Arrays.asList("吃饭", "睡觉", "洗衣服");

        int distance = AlgorithmUtils.minDistance(s1, s2);
        int distance1 = AlgorithmUtils.minDistance(s1, s3);
        System.out.println(distance);
        System.out.println(distance1);
    }

}
