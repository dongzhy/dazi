export interface TeamType {
    id: number;
    name?: string;
    description?: string;
    maxNum?: number;
    expireTime?: string;
    userId?: number;
    status?: number;
    password?: string;
    createTime?: string;
    updateTime?: string;
    hasJoin?: boolean;
    hasJoinNum?: number;
    createUser?: any; // 可进一步定义为 UserVo
}
