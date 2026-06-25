<script setup lang="ts">
import { useRoute } from "vue-router";
import { onMounted, ref, watch } from "vue";
import { matchUsers, recommendUsers } from "../services/user";
import { showToast } from "vant";
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const isMatchMode = ref<boolean>(false);
const userList = ref<any[]>([]);

const formatUserTags = (userListData: any[]) => {
  return userListData.map(user => {
    try {
      user.tags = typeof user.tags === 'string' ? JSON.parse(user.tags) : (user.tags || []);
    } catch (e) {
      user.tags = [];
    }
    return user;
  });
};

const loadData = async () => {
  userList.value = [];
  try {
    let response;
    const num = 10;
    if (isMatchMode.value) {
      response = await matchUsers(num);
    } else {
      response = await recommendUsers(8, 1);
    }

    if (response.code === 0) {
      const userListData = response.data.records || response.data || [];
      userList.value = formatUserTags(userListData);
      showToast(`加载成功，共${userListData.length}条数据`);
    } else {
      showToast(`加载失败：${response.message || "未知错误"}`);
    }
  } catch (error) {
    showToast('数据加载失败，请稍后重试');
    userList.value = [];
  }
};

onMounted(() => loadData());
watch(isMatchMode, () => loadData());
</script>
<!-- template 部分保持不变 -->


<template>
  <van-cell center title="心动模式">
    <template #right-icon>
      <van-switch v-model="isMatchMode" />
    </template>
  </van-cell>
  <!-- 加载中可增加loading状态，提升体验 -->
  <van-loading v-if="userList.length === 0 && !$refs.loaded" vertical>加载中...</van-loading>
  <user-card-list :user-list="userList" v-else></user-card-list>
  <van-empty v-if="userList.length === 0" description="数据为空"></van-empty>
</template>

<style scoped>
/* 可根据需要添加自定义样式 */
.van-loading {
  margin: 20px 0;
  text-align: center;
}
</style>