package edu.hebeu.partnermatching.service;

import edu.hebeu.partnermatching.model.domain.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void addUser() {
        User user = new User();
        user.setUserName("溯");
        user.setUserAccount("33");
        user.setAvatarUrl("https://spring.io/");
        user.setGender(0);
        user.setUserPassword("123");
        user.setEmail("33");
        user.setUserStatus(0);
        user.setPhone("177");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        user.setUserRole(1);
        boolean res = userService.save(user);
        Assertions.assertTrue(res);

    }

    @Test
    void userRegister() {
       String userAccount = "suyuhuai";
        String userPassword = "";
        String checkPassword = "123456";
        long res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);

        userAccount = "su";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);

        userPassword = "12345678";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);

        userAccount = "suyuhuai*@#";
        checkPassword = "12345678";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);

        userAccount = "suyuhuai";
        userPassword = "123456789";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);

        checkPassword = "123456789";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertTrue(res > 0);

        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,res);




    }

    @Test
    void searchUsersByTags() {
        List<String> tagList = Arrays.asList("java");
        List<User> userList = userService.SQLSearchSUsersByTags(tagList);
        Assertions.assertNotNull(userList);
    }


}