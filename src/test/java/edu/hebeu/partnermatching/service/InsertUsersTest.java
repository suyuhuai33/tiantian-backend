package edu.hebeu.partnermatching.service;

import edu.hebeu.partnermatching.mapper.UserMapper;
import edu.hebeu.partnermatching.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 普通插入1k = 2798
     * 批量插入   = 1468
     * 批量并发   = 1323
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++){
            User user = new User();
            user.setUserName("天天");
            user.setUserAccount("tiantian");
            user.setAvatarUrl("https://iconfont.alicdn.com/p/illus/preview_image/pMhdd5wW6xfB/a1988b80-b515-41c6-aa2f-4494c2089485.png");
            user.setGender(0);
            user.setUserPassword("160325");
            user.setEmail("qq.com");
            user.setUserStatus(0);
            user.setPhone("577955");
            user.setUserRole(0);
            user.setTags("[]");
            user.setProfile("我是大王");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doInsertUsersPro(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++){
            User user = new User();
            user.setUserName("天天");
            user.setUserAccount("tiantian");
            user.setAvatarUrl("https://iconfont.alicdn.com/p/illus/preview_image/pMhdd5wW6xfB/a1988b80-b515-41c6-aa2f-4494c2089485.png");
            user.setGender(0);
            user.setUserPassword("160325");
            user.setEmail("qq.com");
            user.setUserStatus(0);
            user.setPhone("577955");
            user.setUserRole(0);
            user.setTags("[]");
            user.setProfile("我是大王");
            userList.add(user);
        }
        userService.saveBatch(userList,100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doConcurrencyInsertUse(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        int j = 0;
        int batchSize = 200;
        //分5个线程一个200,批量一次50
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM/200; i++){
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUserName("天天");
                user.setUserAccount("tiantian");
                user.setAvatarUrl("https://iconfont.alicdn.com/p/illus/preview_image/pMhdd5wW6xfB/a1988b80-b515-41c6-aa2f-4494c2089485.png");
                user.setGender(0);
                user.setUserPassword("160325");
                user.setEmail("qq.com");
                user.setUserStatus(0);
                user.setPhone("577955");
                user.setUserRole(0);
                user.setTags("[]");
                user.setProfile("我是大王");
                userList.add(user);
                if (j % batchSize == 0 ){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


}
