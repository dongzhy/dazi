import myAxios from "../plugins/myAxios";

export const addTeam = async (data: any) => myAxios.post("/team/add", data);
export const deleteTeam = async (id: number) => myAxios.post("/team/delete", { id });
export const getTeamById = async (id: number) => myAxios.get("/team/get", { params: { id } });
export const joinTeam = async (teamId: number, password?: string) => myAxios.post("/team/join", { teamId, password });
export const listTeams = async (params: any) => myAxios.get("/team/list", { params });
export const listMyCreateTeams = async (params?: any) => myAxios.get("/team/list/my/create", { params });
export const listMyJoinTeams = async (params?: any) => myAxios.get("/team/list/my/join", { params });
export const quitTeam = async (teamId: number) => myAxios.post("/team/quit", { teamId });
export const updateTeam = async (data: any) => myAxios.post("/team/update", data);
