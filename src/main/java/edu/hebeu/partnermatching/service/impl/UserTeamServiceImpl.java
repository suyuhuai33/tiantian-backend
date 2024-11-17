package edu.hebeu.partnermatching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hebeu.partnermatching.model.domain.UserTeam;
import edu.hebeu.partnermatching.service.UserTeamService;
import edu.hebeu.partnermatching.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author KILL
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-11-05 17:37:14
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




