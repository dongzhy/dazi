<script setup>
import { useRoute, useRouter } from "vue-router";
import { onMounted, ref } from "vue";
import { showToast } from "vant";
import { getCurrentUser } from "../services/user.js";

const router = useRouter();
const route = useRoute();

const user = ref(null);

// 跳转到编辑页
const toEdit = (editKey, editName, currentValue) => {
  router.push({
    path: "/user/edit",
    query: {
      editKey,
      editName,
      // 处理数字类型值，转为字符串（路由参数只能是字符串）
      currentValue: currentValue != null ? String(currentValue) : ""
    }
  });
};

const formatGender = (gender) => {
  if (gender === undefined || gender === null) return "未知";
  return gender === 1 ? "女" : (gender === 0 ? "男" : "未知");
};

const formatDate = (dateStr) => {
  if (!dateStr) return "——";
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return "无效时间";
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 新增：格式化标签显示，将 JSON 字符串转为逗号分隔文本
const formatTags = (tags) => {
  if (!tags) return "未设置";
  try {
    const tagList = typeof tags === 'string' ? JSON.parse(tags) : tags;
    if (Array.isArray(tagList)) {
      return tagList.join(', ') || "未设置";
    }
    return "未设置";
  } catch (e) {
    return tags || "未设置";
  }
};

onMounted(async () => {
  try {
    showToast({ type: "loading", message: "加载中...", forbidClick: true });
    const currentUser = await getCurrentUser();
    if (currentUser && typeof currentUser === 'object') {
      user.value = currentUser;
      showToast({ type: "success", message: "获取用户信息成功" });
    } else {
      showToast({ type: "fail", message: "获取信息失败" });
    }
  } catch (error) {
    console.error("接口请求异常：", error);
    showToast({ type: "fail", message: "网络异常，请稍后重试" });
  } finally {
    showToast.clear();
  }
});
</script>

<template>
  <div v-if="user">
    <van-cell title="昵称" is-link :value="user.username || '未设置'" @click="toEdit('username','昵称',user.username)" />
    <van-cell title="账户" :value="user.userAccount || '——'" />
    <van-cell title="头像" is-link @click="toEdit('avatarUrl','头像',user.avatarUrl)">
      <img style="height: 48px; border-radius: 50%;" :src="user.avatarUrl || 'https://img.yzcdn.cn/vant/cat.jpeg'" alt="用户头像" />
    </van-cell>
    <van-cell title="性别" is-link :value="formatGender(user.gender)" @click="toEdit('gender','性别',user.gender)" />
    <van-cell title="电话" is-link :value="user.phone || '未绑定'" @click="toEdit('phone','电话',user.phone)" />
    <van-cell title="邮箱" is-link :value="user.email || '未绑定'" @click="toEdit('email','邮箱',user.email)" />
    <!-- 新增：标签修改入口 -->
    <van-cell title="标签" is-link :value="formatTags(user.tags)" @click="toEdit('tags','标签',formatTags(user.tags))" />
    <van-cell title="星球编号" :value="user.planetCode || '——'" />
    <van-cell title="注册时间" :value="formatDate(user.createTime)" />
  </div>
  <van-empty v-else description="暂无用户信息" />
</template>

<style scoped>
img { object-fit: cover; }
.van-cell-group { margin-bottom: 16px; }
</style>

