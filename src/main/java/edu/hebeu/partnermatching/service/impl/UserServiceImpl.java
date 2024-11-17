package edu.hebeu.partnermatching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.hebeu.partnermatching.common.AlgorithmUtils;
import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.exception.BusinessException;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.service.UserService;
import edu.hebeu.partnermatching.mapper.UserMapper;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.hebeu.partnermatching.constants.UserConstants.ADMIN_ROLE;
import static edu.hebeu.partnermatching.constants.UserConstants.USER_LOGIN_STATE;


/**
* @author su
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-09-21 10:20:52
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{
    //常量
    private static final String SALT = "suyuhuai";

    @Resource
    private UserMapper userMapper;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        //不为空
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //账号长度大于5
        if(userAccount.length() < 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度太短");
        }
        //密码大于8
        if(userPassword.length() < 8 && checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度太短");
        }
        //账号不能包含特殊字符
        String validPattern
         = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].&lt;&gt;/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        //密码和校验密码一致
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码输入不一致");
        }
        //账号不重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_account",userAccount);
        User checkuser = userMapper.selectOne(queryWrapper);
        if(checkuser != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.加入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean res = this.save(user);
        if(!res){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"添加失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        //不为空
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //账号长度大于5
        if(userAccount.length() < 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号太短");
        }
        //密码大于8
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码太短");
        }
        //账号不能包含特殊字符
        String validPattern
                = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].&lt;&gt;/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"包含特殊字符");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("user_password",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null){
            log.info("用户不存在");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        //4.用户脱敏,多次调用单独写
        User safeUser = this.safeUser(user);
        //5记录用户登入态
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        //返回safeUser
        return safeUser;
    }

    @Override
    public User safeUser(User user){
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUserName(user.getUserName());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setPhone(user.getPhone());
        safeUser.setGender(user.getGender());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setTags(user.getTags());
        safeUser.setProfile(user.getProfile());
        return safeUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Deprecated
    @Override
    public List<User> SQLSearchSUsersByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tag);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::safeUser).collect(Collectors.toList());
    }


    @Override
    public List<User> searchUsersByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查出全部的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //筛选
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            //判断标签非空
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            Gson gson = new Gson();
            //转换为gsno
            Set<String> tempTagsSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagsSet = Optional.ofNullable(tempTagsSet).orElse(new HashSet<>());
            //若是不包含则返回false
            for (String tagName:tempTagsSet
                 ) {
                if(!tempTagsSet.contains(tagName))
                    return false;
            }
            return true;
        }).map(this::safeUser).collect(Collectors.toList());
    }

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        if (request == null){
            return null;
        }
        Object userobj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userobj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userobj;
    }

    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        Object objectUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (objectUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户没登陆");
        }
        User user = (User) objectUser;
        return user.getUserRole() == ADMIN_ROLE && user != null;
    }

    /**
     * 判断是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser){
        return loginUser.getUserRole() == ADMIN_ROLE && loginUser != null;
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser){
        //查询用户id是否有效
        long userId = user.getId();
        if(userId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员就可以修改所有的，自己只能修改自己的
        if (!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        //查询用户信息是否存在
        //用户ID有效（即符合系统ID的规则，如非负数）并不意味着对应的用户信息一定存在。可能存在ID被删除或者尚未创建的情况。
        //通过查询数据库，可以确保用户ID对应的用户信息确实存在，避免更新不存在的用户信息导致的错误。
        User userold = userMapper.selectById(userId);
        if(userold == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        //拿到我的标签，json转换成数组
        String loginUserTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> myTagsList = gson.fromJson(loginUserTags, new TypeToken<List<String>>(){}.getType());
        //拿到其他用户列表，（优化点：非空不查、只查需要的数据）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);
        //根据用户列表下标-相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        //算出相似度分数
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签的 或当前用户为自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()){
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            long minDistance = AlgorithmUtils.minDistance(myTagsList, userTagsList);
            list.add(new Pair<User, Long>(user, minDistance));
        }
        //排序和取头num个
        List<Pair<User, Long>> topUserList = list.stream()
                .sorted((a, b) -> (int)(a.getValue() - b.getValue())).
                limit(num)
                .collect(Collectors.toList());
        //取出有序的id
        List<Long> idList = topUserList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        //根据id查询用户完整信息
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idList);
        Map<Long, List<User>>  topUserInfo = this.list(queryWrapper).stream()
                .map(this::safeUser)
                .collect(Collectors.groupingBy(User::getId));

        //查询后列表又变成无序的了，需要再根据有序id添加
        ArrayList<User> finalUserList = new ArrayList<>();
        for (Long id: idList){
            finalUserList.add(topUserInfo.get(id).get(0));
        }
        return finalUserList;
    }
}




