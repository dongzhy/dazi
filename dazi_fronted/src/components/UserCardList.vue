<script setup lang="ts">
import type {UserType} from "../models/user.ts";

interface UserCardListProps {
  userList: UserType[];
}
const props = defineProps<UserCardListProps>();
</script>

<template>
  <van-card
      v-for="user in props.userList"
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
</template>

<style scoped>

</style>