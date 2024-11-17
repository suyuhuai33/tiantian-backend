package edu.hebeu.partnermatching.service;

import edu.hebeu.partnermatching.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author SU
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-09-21 10:20:52
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return Long
     * @Author SU
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登入
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     * @Author SU
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User safeUser(User user);

    /**
     * 用户登出
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据用户标签查询用户
     * @param tagNameList
     * @return
     */
    @Deprecated
    List<User> SQLSearchSUsersByTags(List<String> tagNameList);

    User getLoginUser(HttpServletRequest request);

    /**
     * 根据用户标签查询用户
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    int updateUser(User user, User loginUser);

    List<User> matchUsers(long num, User loginUser);

}
