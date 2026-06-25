import Index from "../Page/Index.vue";
import Team from "../Page/Team.vue";
import User from "../Page/User.vue";
import UserTeamJoinPage from "../Page/UserTeamJoinPage.vue";
import UserTeamCreatePage from "../Page/UserTeamCreatePage.vue";
import Search from "../Page/Search.vue";
import SearchResultPage from "../Page/SearchResultPage.vue";
import UserEdit from "../Page/UserEdit.vue";
import UserLoginPage from "../Page/UserLoginPage.vue";
import TeamAddPage from "../Page/TeamAddPage.vue";
import TeamUpdatePage from "../Page/TeamUpdatePage.vue";
import UserUpdatePage from "../Page/UserUpdatePage.vue";

const routes = [
    { path: '/', component: Index ,name: 'index' ,title: '主页'},
    { path: '/team', component: Team,name: 'team' ,title: '队伍'},
    { path: '/user', component: User,name: 'user' ,title: '个人信息'},
    { path: '/search', component: Search,name: 'search' ,title: '搜索搭子' },
    { path: '/user/list', component: SearchResultPage,name: 'userList',title: '用户列表' },
    { path: '/user/edit', component: UserEdit,name: 'userEdit',title: '用户编辑' },
    { path: '/user/login', component: UserLoginPage,name: 'userLogin' , title:'用户登录页'},
    { path: '/team/add', component: TeamAddPage,name: 'teamAddPage' ,title: '创建队伍列表'},
    { path: '/team/update', component: TeamUpdatePage,name: 'teamUpdatePage' ,title: '更新队伍列表'},
    { path: '/user/update', component: UserUpdatePage,name: 'userUpdatePage',title: '更新用户' },
    { path: '/user/team/join', component: UserTeamJoinPage,name: 'userTeamJoinPage',title: '我加入队伍' },
    { path: '/user/team/create', component: UserTeamCreatePage,name: 'userTeamCreatePage',title: '我创建队伍' },


]

export default routes;