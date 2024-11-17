package edu.hebeu.partnermatching.service;


import com.baomidou.mybatisplus.extension.service.IService;
import edu.hebeu.partnermatching.common.DeleteRequest;
import edu.hebeu.partnermatching.model.domain.Team;
import edu.hebeu.partnermatching.model.domain.User;
import edu.hebeu.partnermatching.model.domain.request.TeamJoinRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamQueryRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamQuitRequest;
import edu.hebeu.partnermatching.model.domain.request.TeamUpdateRequest;
import edu.hebeu.partnermatching.model.domain.vo.TeamUserVO;

import java.util.List;

/**
* @author KILL
* @description 针对表【team】的数据库操作Service
* @createDate 2024-11-05 17:39:42
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQueryRequest
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin);

    /**
     * 修改队伍信息
     * @param team
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    Boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(DeleteRequest deleteRequest, User loginUser);
}
