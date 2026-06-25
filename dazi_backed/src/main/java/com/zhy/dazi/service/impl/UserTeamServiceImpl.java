package com.zhy.dazi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhy.dazi.mapper.UserTeamMapper;
import com.zhy.dazi.model.domain.UserTeam;
import com.zhy.dazi.service.UserTeamService;

import org.springframework.stereotype.Service;

/**
* @author 15922
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2026-01-04 14:05:32
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




