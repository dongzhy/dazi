<script setup>
import { useRoute } from "vue-router";
import { onMounted, ref } from "vue";
import MyAxios from "../plugins/myAxios.js";
import { showToast } from "vant";
import qs from 'qs';

// 获取路由参数，规范化tags为数组格式（解决路由参数可能是字符串/空的问题）
const route = useRoute();
let tags = route.query.tags || [];
// 处理路由参数格式：如果是字符串（如"java,python"或单个标签），转为数组
if (typeof tags === 'string') {
  // 两种常见场景：逗号分隔字符串 / 单个标签字符串
  tags = tags.includes(',') ? tags.split(',') : [tags];
}

// 响应式用户列表，初始为模拟数据（可保留，也可设为空）
const userList = ref([]);

// 组件挂载后发起接口请求（统一用try/catch替代await+then/catch混合，避免数据流转异常）
onMounted(async () => {
  try {
    // 发起axios请求（无需混合then/catch，用try/catch处理错误）
    const response = await MyAxios.get('/user/search/tags', {
      params: {
        tagNameList: tags // 确保传递给后端的是数组格式
      },
      paramsSerializer: params => {
        // 确保数组参数序列化格式符合后端要求（如tagNameList=java&tagNameList=python）
        return qs.stringify(params, { indices: false });
      }
    });

    // 1. 验证后端返回数据（方便调试）
    console.log('后端原始返回数据：', response);
    // 2. 提取有效数据（根据后端实际返回结构调整，确保拿到真实数据列表）
    const userListData = response.data || [];
    console.log('提取后的用户数据：', userListData);

    if (userListData.length > 0) {
      // 3. 处理用户tags字段（JSON字符串转数组，增加异常捕获避免报错）
      const formattedUserList = userListData.map(user => {
        try {
          // 若tags是JSON字符串则解析，否则保持原格式（兼容后端可能的返回格式）
          user.tags = typeof user.tags === 'string' ? JSON.parse(user.tags) : (user.tags || []);
        } catch (e) {
          console.warn(`用户${user.id}的tags解析失败：`, e);
          user.tags = []; // 解析失败时设为空数组，避免模板报错
        }
        return user;
      });

      // 4. 关键：将处理后的数据赋值给响应式变量（原代码缺失这一步，导致模板无数据）
      userList.value = formattedUserList;
    }

    showToast("请求成功，获取到" + userListData.length + "条用户数据");

  } catch (error) {
    // 错误详情打印，方便排查问题
    console.error('请求失败详情：', error);
    console.error('响应数据：', error.response?.data); // 打印后端返回的错误响应
    showToast("请求失败，请稍后重试");
    // 异常时清空列表，避免残留旧数据
    userList.value = [];
  }
});
</script>

<template>
  <!-- 优化：key优先用唯一标识，若id可能重复可拼接其他字段；优化空状态判断 -->
  <van-card
      v-for="user in userList"
      :key="`user-${user.id}`"
      :desc="user.profile || '暂无个人简介'"
      :title="`${user.username || '未知用户'} (${user.planetCode || '暂无星球码'})`"
      :thumb="user.avatarUrl || '默认头像图片地址'"
  >
    <template #tags>
      <van-tag
          plain
          type="danger"
          v-for="tag in user.tags"
          :key="`tag-${tag}`"
          style="margin-right: 8px; margin-top: 8px"
      >
        {{ tag }}
      </van-tag>
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
  <!-- 优化空状态判断，更严谨 -->
  <van-empty v-if="userList.length === 0" description="搜索结果为空"></van-empty>
</template>

<style scoped>
/* 可根据需要添加自定义样式 */
</style>