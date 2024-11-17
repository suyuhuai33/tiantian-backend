package edu.hebeu.partnermatching.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.hebeu.partnermatching.common.BaseResponse;
import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.common.ResultUtils;
import edu.hebeu.partnermatching.exception.BusinessException;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.model.domain.request.UserDeleteRequest;
import edu.hebeu.partnermatching.model.domain.request.UserLoginRequest;
import edu.hebeu.partnermatching.model.domain.request.UserRegisterRequest;
import edu.hebeu.partnermatching.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static edu.hebeu.partnermatching.constants.UserConstants.USER_LOGIN_STATE;
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"})
public class UserController {
    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR );
        }
        long id = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(id);

    }

    /**
     * 登入
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);

    }

    /**
     * 根据姓名查询用户
     * @param userName
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUusers(String userName, HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTH,"不是管理员");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(userName)){
            queryWrapper.like("user_name",userName);
        }
        //用到了java8的api对所有查询的用户脱敏
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.safeUser(user)).collect(Collectors.toList());
         return ResultUtils.success(list);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object objectUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) objectUser;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userService.getById(currentUser.getId());
        User safeUser = userService.safeUser(user);
        return ResultUtils.success(safeUser);
    }

    /**
     * 刪除用戶
     * @param userDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NOT_AUTH,"不是管理员");
        }
        if(userDeleteRequest.getId() <= 0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = userDeleteRequest.getId();
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int id = userService.userLogout(request);
        return ResultUtils.success(id);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.SQLSearchSUsersByTags(tagList);
        return ResultUtils.success(userList);
    }

    /**
     * 更新用户
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUser(long pageSize,long pageNum,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("tiantian:user:recommend:%s",loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        //如果有缓存直接使用缓存
        if (userPage != null){
            return ResultUtils.success(userPage);
        }
        //如果没有则查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         userPage = userService.page(new Page<>(pageNum,pageSize), queryWrapper);
         //写缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        }   catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        if(num <0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }
}
