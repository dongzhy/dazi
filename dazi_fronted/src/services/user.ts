import myAxios from "../plugins/myAxios";
import type { UserType, UserUpdateRequest, UserTagUpdateRequest } from "../models/user";
import {getCurrentUserState, setCurrentUserState} from "../states/user.ts";

// 增加 forceRefresh 参数，默认为 false
export const getCurrentUser = async (forceRefresh = false) => {
    // 如果不是强制刷新，先读本地缓存
    if (!forceRefresh) {
        const currentUser = getCurrentUserState();
        if (currentUser) {
            return currentUser;
        }
    }

    // 无缓存或强制刷新时，从远程获取
    try {
        const res = await myAxios.get("/user/current");
        if (res.code === 0) {
            setCurrentUserState(res.data); // 存入本地状态
            return res.data;
        }
    } catch (error) {
        console.error("获取当前用户失败：", error);
    }
    return null;
};

/**
 * 用户登录
 */
export const login = async (userAccount: string, userPassword: string) => {
    return myAxios.post("/user/login", { userAccount, userPassword });
};

/**
 * 更新用户信息 (不含标签)
 */
export const updateUser = async (data: UserUpdateRequest) => {
    return myAxios.post("/user/update", data);
};

/**
 * 修改标签 (独立接口)
 */
export const updateUserTags = async (data: UserTagUpdateRequest) => {
    return myAxios.post("/user/update/tags", data);
};

/**
 * 标签匹配用户
 */
export const matchUsers = async (num: number) => {
    return myAxios.get("/user/match", { params: { num } });
};

/**
 * 首页推荐
 */
export const recommendUsers = async (pageSize: number, pageNum: number) => {
    return myAxios.get("/user/recommend", { params: { pageSize, pageNum } });
};

/**
 * 根据标签搜索用户
 */
export const searchUsersByTags = async (tagNameList: string[]) => {
    return myAxios.get("/user/search/tags", { params: { tagNameList } });
};
