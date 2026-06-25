<template>
  <van-nav-bar
      left-arrow
      fixed
      safe-area-inset-top
      @click-right="onClickRight"
      :title="title"
  >
    <template #right>
      <van-icon name="search" size="18"/>
    </template>
  </van-nav-bar>
  <div id="content">
    <router-view />
  </div>

  <van-tabbar route @change="onChange" v-model="active">
    <van-tabbar-item replace to="/" icon="home-o" name="index">主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="friends-o" name="team">队伍</van-tabbar-item>
    <van-tabbar-item to="/user" icon="setting-o" name="user">个人</van-tabbar-item>
  </van-tabbar>
</template>

<script>
import { showToast } from 'vant';
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import routes from "../config/route.js"; // 引入路由配置

export default {
  setup() {
    const DEFAULT_TITLE = '伙伴匹配';
    const router = useRouter();
    const route = useRoute(); // 获取当前路由实例
    const title = ref(DEFAULT_TITLE);
    const active = ref("index");

    // 核心逻辑：根据当前路由路径更新标题
    const updateTitle = () => {
      // 从路由配置中找到匹配当前路径的路由项
      const matchedRoute = routes.find(item => item.path === route.path);
      // 如果找到对应的title则使用，否则用默认标题
      title.value = matchedRoute?.title ?? DEFAULT_TITLE;
      // 同步更新tabbar的active值（根据路由name）
      if (matchedRoute?.name) {
        active.value = matchedRoute.name;
      }
    };

    // 监听路由变化，更新标题
    watch(
        () => route.path, // 监听路由路径变化
        () => {
          updateTitle();
        },
        { immediate: true } // 立即执行一次，初始化标题
    );

    // 左侧返回按钮事件
    const onClickLeft = () => {
      router.back();
    };

    // 右侧搜索按钮事件
    const onClickRight = () => {
      router.push('/search');
    };


    return {
      active,
      onClickLeft,
      onClickRight,
      title
    };
  },
};
</script>

<style scoped>
#content {
  padding-bottom: 50px;
  padding-top: 46px; /* 给fixed的导航栏留出顶部空间 */
}
</style>