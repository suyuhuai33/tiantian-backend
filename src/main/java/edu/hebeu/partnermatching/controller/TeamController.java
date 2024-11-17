package edu.hebeu.partnermatching.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.hebeu.partnermatching.common.BaseResponse;
import edu.hebeu.partnermatching.common.DeleteRequest;
import edu.hebeu.partnermatching.common.ErrorCode;
import edu.hebeu.partnermatching.common.ResultUtils;
import edu.hebeu.partnermatching.exception.BusinessException;
import edu.hebeu.partnermatching.model.domain.Team;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.model.domain.UserTeam;
import edu.hebeu.partnermatching.model.domain.request.*;
import edu.hebeu.partnermatching.model.domain.vo.TeamUserVO;
import edu.hebeu.partnermatching.service.TeamService;
import edu.hebeu.partnermatching.service.UserService;
import edu.hebeu.partnermatching.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"})
@Slf4j
public class TeamController {
    @Resource
    TeamService teamService;

    @Resource
    UserService userService;

    @Resource
    UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        if(teamId < 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody  DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(deleteRequest, loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest team, HttpServletRequest request){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(team, loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long teamId){
        if(teamId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if(teamQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        long loginUserId = userService.getLoginUser(request).getId();
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest, isAdmin);
        //查询用户是否加入队伍 ==> 查询关联表有没有这个队伍和你的数据
        List<Long> idList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.in("team_id", idList);
        //通过拿到所有已加入队伍的集合，再拿到其id，最后为hasJoin字段赋值提供给前端
        List<UserTeam> joinTeamList = userTeamService.list(queryWrapper);
        Set<Long> joinIdSet = joinTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamList.forEach(teamUserVO -> {
            Boolean HasJoin = joinIdSet.contains(teamUserVO.getId());
            teamUserVO.setHasJoin(HasJoin);
        });
        //查询队伍加入人数 ==> 把上面的队伍idList查出所有需要的关联信息最后分组。根据每组的大小给其赋值
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("team_id", idList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getUserId));
        teamList.forEach(teamUserVO -> teamUserVO.setHasJoinNum(listMap.getOrDefault(teamUserVO.getId(), new ArrayList<>())
                .size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listCreateTeams( TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if(teamQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 加入的队伍查询（包含自己创建的）
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listJoinTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //过滤重复的队伍
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> teamIdList = new ArrayList<>(listMap.keySet());
        teamQueryRequest.setIdList(teamIdList);
        List<TeamUserVO> teamListVO = teamService.listTeams(teamQueryRequest, true);
        return ResultUtils.success(teamListVO);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(@RequestBody TeamQueryRequest teamQueryRequest){
        if(teamQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryRequest, team);
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQueryRequest.getPageNum(),teamQueryRequest.getPageSize());
        Page<Team> teamPage = teamService.page(page, teamQueryWrapper);
        return ResultUtils.success(teamPage);
    }

    @GetMapping("/join")
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result =  teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/quit")
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }



}
