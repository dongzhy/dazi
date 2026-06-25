package com.zhy.dazi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhy.dazi.common.ErrorCode;
import com.zhy.dazi.exception.BusinessException;
import com.zhy.dazi.mapper.TeamMapper;
import com.zhy.dazi.model.Enum.TeamStatusEnum;
import com.zhy.dazi.model.domain.Team;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.domain.UserTeam;
import com.zhy.dazi.model.dto.TeamQuery;
import com.zhy.dazi.model.request.TeamJoinRequest;
import com.zhy.dazi.model.request.TeamQuitRequest;
import com.zhy.dazi.model.request.TeamUpdateRequest;
import com.zhy.dazi.model.vo.TeamUserVo;
import com.zhy.dazi.model.vo.UserVo;
import com.zhy.dazi.service.TeamService;
import com.zhy.dazi.service.UserService;
import com.zhy.dazi.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 队伍服务实现
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();

        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(3);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "人数不符合要求（需1-20人）");
        }

        String name = team.getName();
        if (name == null || name.isEmpty() || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求（不能为空且长度≤20）");
        }

        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长（长度≤512）");
        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不满足要求（不能为空且长度≤32）");
            }
        }

        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能早于当前时间");
        }

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能创建5个队伍");
        }

        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        Long teamId = team.getId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            this.removeById(teamId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败（关联用户失败）");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }

            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }

            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null) {
                queryWrapper.eq("maxNum", maxNum);
            }

            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }

            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            // 恢复权限控制：非管理员无法查看私有队伍
            if (!isAdmin && TeamStatusEnum.PRIVATE.equals(statusEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "无权限查看私有队伍");
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }

        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            if (user != null) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Long userId = loginUser.getId();
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID非法");
        }

        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态非法");
        }
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "禁止加入私有队伍");
        }

        String inputPassword = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(inputPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍需要输入密码");
            }
            String teamPassword = team.getPassword();
            if (!inputPassword.equals(teamPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }

        // 修复分布式锁：移除死循环
        RLock lock = redissonClient.getLock("dazi:join_team");
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(0, 30000, TimeUnit.MILLISECONDS);
            if (!tryLock) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前加入队伍人数过多，请稍后再试");
            }

            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("userId", userId);
            long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
            if (hasJoinNum >= 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能创建/加入5个队伍");
            }

            userTeamQueryWrapper.clear();
            userTeamQueryWrapper.eq("userId", userId).eq("teamId", teamId);
            long hasUserTeam = userTeamService.count(userTeamQueryWrapper);
            if (hasUserTeam > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "你已加入该队伍");
            }

            Integer maxNum = team.getMaxNum();
            if (maxNum == null) {
                maxNum = 10;
            }
            userTeamQueryWrapper.clear();
            userTeamQueryWrapper.eq("teamId", teamId);
            long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
            if (teamHasJoinNum >= maxNum) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满，无法加入");
            }

            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            return userTeamService.save(userTeam);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (tryLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();

        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }

        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if (teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            if (team.getUserId().equals(userId)) {
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId)
                        .orderByAsc("joinTime") // 修复：按加入时间排序，而非ID
                        .last("limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserLeadTeam = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserLeadTeam);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        Team team = getTeamById(id);
        long teamId = team.getId();
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无访问权限");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        return this.removeById(teamId);
    }

    private Team getTeamById(Long teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    private long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}