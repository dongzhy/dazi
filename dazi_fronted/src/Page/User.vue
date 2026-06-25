<script setup>
import { useRoute, useRouter } from "vue-router";
import { onMounted, ref } from "vue";
import { getCurrentUser } from "../services/user.js";

const router = useRouter();
const route = useRoute();
let { tags = [] } = route.query;
if (typeof tags === 'string') {
  tags = tags.includes(',') ? tags.split(',').map(item => item.trim()) : [tags];
} else if (!Array.isArray(tags)) {
  tags = [];
}

const user = ref(null);

// 修复：跳转参数名和修改页保持一致（editKey/editName/currentValue）
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

onMounted(async () => {
  user.value = await getCurrentUser();

});
</script>

<template>
  <div v-if="user">
    <van-cell title="当前用户" :value="user?.username" />

    <van-cell title="修改信息" is-link url="/user/update" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />



  </div>

</template>

<style scoped>
img {
  object-fit: cover;
}
.van-cell-group {
  margin-bottom: 16px;
}
</style>