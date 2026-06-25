package com.zhy.dazi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhy.dazi.common.BaseResponse;
import com.zhy.dazi.common.DeleteRequest;
import com.zhy.dazi.common.ErrorCode;
import com.zhy.dazi.common.ResultUtils;
import com.zhy.dazi.exception.BusinessException;
import com.zhy.dazi.model.domain.Team;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.domain.UserTeam;
import com.zhy.dazi.model.dto.TeamQuery;
import com.zhy.dazi.model.request.TeamAddRequest;
import com.zhy.dazi.model.request.TeamJoinRequest;
import com.zhy.dazi.model.request.TeamQuitRequest;
import com.zhy.dazi.model.request.TeamUpdateRequest;
import com.zhy.dazi.model.vo.TeamUserVo;
import com.zhy.dazi.service.TeamService;
import com.zhy.dazi.service.UserService;
import com.zhy.dazi.service.UserTeamService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@Api(tags = "队伍接口")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team byId = teamService.getById(id);
        if (byId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(byId);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> list = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = list.stream().map(TeamUserVo::getId).collect(Collectors.toList());

        // 判断当前用户是否已加入队伍
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> userTeamqueryWrapper = new QueryWrapper<>();
        userTeamqueryWrapper.eq("userId", loginUser.getId());
        userTeamqueryWrapper.in("teamId", teamIdList);
        List<UserTeam> list1 = userTeamService.list(userTeamqueryWrapper);
        Set<Long> hasJoinTeamIdSet = list1.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        list.forEach(team -> team.setHasJoin(hasJoinTeamIdSet.contains(team.getId())));

        // 查询队伍人数（修复变量复用BUG）
        QueryWrapper<UserTeam> userTeamJoinqueryWrapper = new QueryWrapper<>();
        userTeamJoinqueryWrapper.in("teamId", teamIdList);
        List<UserTeam> list2 = userTeamService.list(userTeamJoinqueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = list2.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        list.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));

        return ResultUtils.success(list);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            // 修复：源对象、目标对象顺序颠倒
            BeanUtils.copyProperties(teamQuery, team);
        } catch (BeansException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean delete = teamService.deleteTeam(id, loginUser);
        if (!delete) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取我创建的队伍
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }
}