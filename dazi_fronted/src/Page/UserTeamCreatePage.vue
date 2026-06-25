<script setup lang="ts">
import { useRouter } from "vue-router";
import TeamCardList from "../components/TeamCardList.vue";
import { onMounted, ref } from "vue";
import myAxios from "../plugins/myAxios";
import {showToast} from "vant";
import type { TeamType } from "../models/team.ts";

const router = useRouter();

// 跳转到创建队伍页面（修正方法名语义）
const doCreateTeam = () => {
  router.push({
    path: "/team/add"
  });
};

// 响应式队伍列表（指定类型，避免类型报错）
const teamList = ref<TeamType[]>([]);

/**
 * 加载队伍列表
 * 异步处理 + 异常捕获 + 友好提示
 */
const loadTeamList = async () => {
  try {
    const res = await myAxios.get("/team/list/my/create");
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



</script>

<template>
  <div class="team-page">
    <!-- 创建队伍按钮 -->
    <van-button
        type="primary"
        @click="doCreateTeam"
        class="create-team-btn"
    >
      创建队伍
    </van-button>

    <!-- 队伍列表组件：正确传递Props（短横线命名） -->
    <TeamCardList :team-list="teamList" />
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