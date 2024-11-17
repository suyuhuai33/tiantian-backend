package edu.hebeu.partnermatching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hebeu.partnermatching.common.DeleteRequest;
import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.exception.BusinessException;
import edu.hebeu.partnermatching.model.domain.Team;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.model.domain.UserTeam;
import edu.hebeu.partnermatching.model.domain.request.TeamJoinRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamQueryRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamQuitRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamUpdateRequest;
import edu.hebeu.partnermatching.model.domain.vo.TeamUserVO;
import edu.hebeu.partnermatching.model.domain.vo.UserVO;
import edu.hebeu.partnermatching.service.TeamService;
import edu.hebeu.partnermatching.mapper.TeamMapper;
import edu.hebeu.partnermatching.service.UserService;
import edu.hebeu.partnermatching.service.UserTeamService;
import edu.hebeu.partnermatching.enums.TeamStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author TianTina
* @description 针对表【team】的数据库操作Service实现
* @createDate 2024-11-05 17:39:42
*/
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    UserTeamService userTeamService;

    @Resource
    UserService userService;

    @Resource
    RedissonClient redissonClient;

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登入，未登入不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3.校验信息
        //队伍吗不超过20
        String teamName = team.getTeamName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名字过长");
        }
        //描述不超过520
        String teamDescription = team.getTeamDescription();
        if(StringUtils.isBlank(teamDescription) || teamDescription.length() > 520){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
        //队伍人数>1 <20,默认5个
        int num = Optional.ofNullable(team.getMaxNum()).orElse(5);
        if(num < 1 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "人数不符合要求");
        }
        //过期时间小于当前时间---默认永不过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null) {
            if(new Date().after(expireTime)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
            }
        }
        //状态是否公开，不传默认为0
        Integer status = Optional.ofNullable(team.getTeamStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态有误会");
        }
        //如果状态为加密，必须要有密码 密码长度<20
        String teamPassword = team.getTeamPassword();
        if(TeamStatusEnum.Encrypt.equals(teamStatusEnum)){
            if(StringUtils.isBlank(teamPassword) || teamPassword.length() > 20){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过长");
            }
        }
        //用户最多创建五个用户--查队伍表的创建人
        long userId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long count = this.count(queryWrapper);
        if(count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍过多");
        }
        //4.插入队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍创建失败");
        }
        //5.插入队伍-用户关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍关系插入失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if(teamQueryRequest != null){
            //id
            Long id = teamQueryRequest.getId();
            if(id != null && id > 0){
                queryWrapper.eq("id", id);
            }
            //user_id列表
            List<Long> idList = teamQueryRequest.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id", idList);
            }
            //队伍名
            String teamName = teamQueryRequest.getTeamName();
            if (StringUtils.isNotBlank(teamName)){
                queryWrapper.like("team_name", teamName);
            }
            //描述
            String teamDescription = teamQueryRequest.getTeamDescription();
            if (StringUtils.isNotBlank(teamDescription)){
                queryWrapper.like("team_description", teamDescription);
            }
            //最大人数
            Integer maxNum = teamQueryRequest.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("max_num", maxNum);
            }
            //创建人id
            Long userId = teamQueryRequest.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("user_id", userId);
            }
            //状态
            Integer teamStatusValue = teamQueryRequest.getTeamStatus();
            if (teamStatusValue == null) {
                teamStatusValue = 0;
            }
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamStatusValue);
            if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum) && !isAdmin){
                throw new BusinessException(ErrorCode.NOT_AUTH, "非管理员无法访问他人私密房间");
            }
            queryWrapper.eq("team_status",teamStatusEnum.getValue());
            String searchText = teamQueryRequest.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("team_name", searchText).or().like("team_description", searchText));
            }
        }
        //不展示已过期的队伍
        //.gt方法：SQL 条件将会是 fieldName > value 的形式。
        queryWrapper.and(qw -> qw.gt("expire_time", new Date()).or().isNull("expire_time"));
        //查询队伍列表
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team: teamList
             ) {
            //队伍的脱敏
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            //用户的脱敏
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                //把创建人信息加入队伍
                teamUserVO.setCreatUserVO(userVO);
            }
            //把脱敏后的队伍信息含创建人加入队伍信息列表里
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long teamId = team.getId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(teamId);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //只有用户创建者和管理员才有资格更改队伍
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTH, "无权限");
        }
        //如果状态变为加密，则必须有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getTeamStatus());
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有该权限");
        }
        if(TeamStatusEnum.Encrypt.equals(teamStatusEnum)){
            if (StringUtils.isBlank(team.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须有密码");
            }
        }
        //更改队伍
        Team updateTeam = new Team();
        BeanUtils.copyProperties(team, updateTeam);
        boolean res = this.updateById(updateTeam);
        return res;
    }

    /**
     * （加分布式锁--会有死锁问题可能？）
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getId();
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不存在" + teamId);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //过期队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        //无法加入私密的队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getTeamStatus());
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法加入私密队伍");
        }
        //如果是加密队伍，则要正确的密码
        String password = teamJoinRequest.getTeamPassword();
        if (TeamStatusEnum.Encrypt.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || !team.getTeamPassword().equals(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码有问题");
            }
        }
        RLock lock = redissonClient.getLock("tiantian:join_team");
        while (true){
            try {
                if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    //查询用户自己加入超5个
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    long userId = loginUser.getId();
                    queryWrapper.eq("user_id", userId);
                    long userTeamNum = userTeamService.count(queryWrapper);
                    if(userTeamNum >= 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户队伍过多");
                    }
                    //不可重复加入队伍
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId);
                    queryWrapper.eq("team_id", teamJoinRequest.getId());
                    long hasJoinSameTeam = userTeamService.count(queryWrapper);
                    if (hasJoinSameTeam > 0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    //查询队伍是否满
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("team_id", teamId);
                    long teamNum = userTeamService.count(queryWrapper);
                    if (teamNum >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //增加user——team表
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    boolean result = userTeamService.save(userTeam);
                    return result;
                }
            } catch (InterruptedException e) {
                log.error("doCacheRecommendUser error", e);
                return false;
            }   finally {
                if (lock.isHeldByCurrentThread()) {
                    System.out.println("unLock: " + Thread.currentThread().getId());
                    lock.unlock();
                }
            }
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不存在");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long hasInTeam = userTeamService.count(queryWrapper);
        if (hasInTeam == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long userNum = this.countTeamUserNumByTeamId(teamId);
        if(userNum == 1){
            //解散队伍了
            this.removeById(teamId);
        }   else {
            //是队长的情况下，将队长转移给第二个加入的
            if(userId == team.getUserId()){
                //查关联表的两条数据
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("team_id", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                //拿到userId
                UserTeam newLeaderUserTeam = userTeamList.get(1);
                Long newLeaderUserId = newLeaderUserTeam.getUserId();
                //修改队伍的userId<--update<--new UpdateUserTeam
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(newLeaderUserId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
                }
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(DeleteRequest deleteRequest, User loginUser) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //检查队伍是否存在
        Long teamId = deleteRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不存在");
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //检查你是不是队长
        if (team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_AUTH, "你不是队长");
        }
        //删除关联表所有信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        //删除队伍
        return this.removeById(teamId);
    }

    public long countTeamUserNumByTeamId(long teamId){
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        long userNum = userTeamService.count(queryWrapper);
        return userNum;
    }
}




