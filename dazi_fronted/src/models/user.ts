export interface UserType {
    id: number;
    username?: string;
    userAccount?: string;
    avatarUrl?: string;
    gender?: number;
    phone?: string;
    email?: string;
    planetCode?: string;
    profile?: string;
    tags?: string[]; // 前端展示用数组
    userRole?: number;
    userStatus?: number;
    createTime?: string;
    updateTime?: string;
}

// 更新用户请求体
export interface UserUpdateRequest {
    id?: number;
    username?: string;
    avatarUrl?: string;
    gender?: number;
    phone?: string;
    email?: string;
    // tags 不再通过这个接口更新
}

// 修改标签请求体
export interface UserTagUpdateRequest {
    tags: string[];
}
