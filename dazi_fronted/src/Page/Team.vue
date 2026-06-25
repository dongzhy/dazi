<script setup lang="ts">
import { useRouter } from "vue-router";
import TeamCardList from "../components/TeamCardList.vue";
import { onMounted, ref } from "vue";
import myAxios from "../plugins/myAxios.js";
import {showToast} from "vant";
import type { TeamType } from "../models/team.ts";

const router = useRouter();
const searchText = ref("");

// 跳转到创建队伍页面（修正方法名语义）
const doCreateTeam = () => {
  router.push({
    path: "/team/add"
  });
};

// 响应式队伍列表（指定类型，避免类型报错）
const teamList = ref<TeamType[]>([]);
const myJoinTeamList = ref<TeamType[]>([]);

/**
 * 加载队伍列表
 * 异步处理 + 异常捕获 + 友好提示
 */
const loadTeamList = async (val = '', status = 0) => {
  try {
    const res = await myAxios.get("/team/list",{
      params: {
        searchText: val,
        status: status,
        pageNum: 1,

      }
    });
    if (res && res.code === 0) {
      teamList.value = res.data as TeamType[];
    } else {
      showToast(`加载失败：${res.description || "未知错误"}`);
    }
  } catch (error: any) {
    showToast(`加载失败：${error.description || "网络异常"}`);
  }
};



// 页面挂载时加载列表
onMounted(() => {
  loadTeamList();
});


const onSearch = (val: string) => {
  loadTeamList(val);

}
const active = ref('public');
const onTabChange = (name: string) => {
  if (name === "public") {
    loadTeamList(searchText.value,0);
  }else {
    loadTeamList(searchText.value,2);
  }

}
</script>

<template>
  <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch" />
  <van-tabs v-model:active="active" @change="onTabChange">
    <van-tab title="公开" name="public"/>
    <van-tab title="加密" name="private"/>

  </van-tabs>
  <div class="team-page">
    <!-- 创建队伍按钮 -->
    <van-button
        type="primary"
        @click="doCreateTeam"
        class="app-button"
        icon="plus"
    >
      创建队伍
    </van-button>

    <!-- 队伍列表组件：正确传递Props（短横线命名） -->
    <TeamCardList :team-list="teamList"  />
  </div>
</template>

<style scoped>
.team-page {
  padding: 16px;
}

.create-team-btn {
  margin-bottom: 16px;
  width: 100%;
}
</style>