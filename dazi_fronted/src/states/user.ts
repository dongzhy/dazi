// ../states/user.ts
import type { UserType } from "../models/user.ts";

// 初始化避免undefined
let currentUser: UserType | null = null;

const setCurrentUserState = (user: UserType | null) => {
    currentUser = user;
};

const getCurrentUserState = (): UserType | null => {
    return currentUser;
};

export {
    setCurrentUserState,
    getCurrentUserState
};