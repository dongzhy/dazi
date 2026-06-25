package com.zhy.dazi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhy.dazi.model.domain.Team;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.dto.TeamQuery;
import com.zhy.dazi.model.request.TeamJoinRequest;
import com.zhy.dazi.model.request.TeamQuitRequest;
import com.zhy.dazi.model.request.TeamUpdateRequest;
import com.zhy.dazi.model.vo.TeamUserVo;

import java.util.List;


/**
* @author 15922
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-01-04 14:04:50
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id,User loginUser);
}
