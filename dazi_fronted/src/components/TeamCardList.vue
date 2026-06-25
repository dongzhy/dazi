<script setup lang="ts">
// 类型导入用 import type
import type { TeamType } from "../models/team.ts";
import { teamStatusEnum } from "../constants/team.ts";
import { joinTeam, quitTeam, deleteTeam } from "../services/team";
import { showToast } from "vant";
import { onMounted, ref } from "vue";
import { getCurrentUser } from "../services/user";
import myAxios from "../plugins/myAxios";
import {useRouter} from "vue-router";

// Props 定义
interface TeamCardListProps {
  teamList: TeamType[];
}
const props = withDefaults(defineProps<TeamCardListProps>(), {
  teamList: () => []
});

// 响应式数据
const showPasswordDialog = ref(false); // 密码弹窗显示状态
const password = ref(''); // 输入的密码
const joinTeamId = ref(0); // 要加入的队伍ID
const currentUser = ref(); // 当前登录用户
const router = useRouter();

// 页面挂载时获取当前用户信息
onMounted(async () => {
  currentUser.value = await getCurrentUser();
});

/**
 * 加入队伍方法
 * @param inputPassword 输入的队伍密码（可选）
 */
const doJoinTeam = async (inputPassword?: string) => {
  try {
    // 校验队伍ID
    if (!joinTeamId.value) {
      showToast("请选择要加入的队伍");
      return;
    }

    // 确定最终使用的密码（弹窗传参或直接使用响应式密码）
    const finalPassword = inputPassword || password.value;

    // 调用接口加入队伍
    const res = await myAxios.post("/team/join", {
      teamId: joinTeamId.value,
      password: finalPassword
    });

    if (res.code === 0) {
      showToast("加入队伍成功");
      showPasswordDialog.value = false; // 关闭密码弹窗
      // 可选：刷新队伍列表（可根据实际需求添加）
      // emit('refreshTeamList');
    } else {
      showToast(`加入失败：${res.description || "未知错误"}`);
    }
  } catch (error: any) {
    showToast(`加入失败：${error.message || error.description || "网络异常"}`);
  }
};

/**
 * 打开密码弹窗并设置要加入的队伍ID
 * @param id 队伍ID
 */
const openPasswordDialog = (id: number) => {
  joinTeamId.value = id; // 设置要加入的队伍ID
  password.value = ''; // 清空之前输入的密码
  showPasswordDialog.value = true; // 打开密码弹窗
};

/**
 * 跳转到队伍更新页面
 * @param id 队伍ID
 */
const doUpdateTeam = async (id: number) => {
  router.push({
    path: '/team/update',
    query: { id }
  });
};

/**
 * 退出队伍
 * @param id 队伍ID
 */
const doQuitTeam = async (id: number) => {
  try {
    const res = await myAxios.post("/team/quit", { teamId: id });
    if (res.code === 0) {
      showToast("退出队伍成功");
      // 可选：刷新队伍列表
    } else {
      showToast(`退出队伍失败：${res.description || "未知错误"}`);
    }
  } catch (error: any) {
    // 修复：提示文案错误
    showToast(`退出队伍失败：${error.message || error.description || "网络异常"}`);
  }
};

/**
 * 解散队伍
 * @param id 队伍ID
 */
const doDeleteTeam = async (id: number) => {
  try {
    const res = await myAxios.post("/team/delete", { id });
    if (res.code === 0) {
      showToast("解散队伍成功");
      // 可选：刷新队伍列表
    } else {
      showToast(`解散队伍失败：${res.description || "未知错误"}`);
    }
  } catch (error: any) {
    showToast(`解散队伍失败：${error.message || error.description || "网络异常"}`);
  }
};

const doJoinCancel=()=>{
  joinTeamId.value=0;
  password.value = '';
}

// 新增：封装公开队伍直接加入的方法，让模板更简洁
const joinPublicTeam = (id: number) => {
  joinTeamId.value = id;
  doJoinTeam();
};
</script>

<template>
  <div class="team-card-list">
    <!-- 队伍卡片列表 -->
    <van-card
        v-for="team in props.teamList"
        :key="`team-${team.id}`"
        :title="team.name"
        :desc="team.description || '暂无队伍描述'"
        class="team-card"
    >
      <!-- 队伍状态标签 -->
      <template #tags>
        <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
          {{ teamStatusEnum[team.status] || "未知状态" }}
        </van-tag>
      </template>

      <!-- 队伍基础信息   最大人数：{{ team.maxNumber || 3 }}-->
      <template #bottom>
        <div class="team-info-item">{{ `队伍人数: ${team.hasJoinNum}/${team.maxNum}` }}</div>
        <div v-if="team.expireTime" class="team-info-item">
          过期时间：{{ team.expireTime }}
        </div>
        <div class="team-info-item">创建时间：{{ team.createTime }}</div>
      </template>

      <!-- 操作按钮区 -->
      <template #footer>
        <!-- 队伍创建者可见：更新、解散按钮 -->
        <van-button
            v-if="team.userId === currentUser?.id"
            size="small"
            plain
            @click="doUpdateTeam(team.id)"
        >
          更新队伍
        </van-button>
        <van-button
            v-if="team.userId === currentUser?.id"
            size="small"
            type="danger"
            plain
            @click="doDeleteTeam(team.id)"
        >
          解散队伍
        </van-button>

        <!-- 已加入队伍：退出按钮 -->
        <van-button
            v-else-if="team.hasJoin"
            size="small"
            type="primary"
            plain
            @click="doQuitTeam(team.id)"
        >
          退出队伍
        </van-button>

        <!-- 未加入队伍：加入按钮 -->
        <van-button
            v-else
            size="small"
            type="primary"
            plain
        @click="team.status === 0 ? joinPublicTeam(team.id) : openPasswordDialog(team.id)"
        >
        加入队伍
        </van-button>
      </template>
    </van-card>

    <!-- 空状态提示 -->
    <div v-if="props.teamList.length === 0" class="empty-tip">
      暂无可用队伍，快去创建吧～
    </div>
  </div>

  <!-- 加入加密队伍的密码弹窗 -->
  <van-dialog
      v-model:show="showPasswordDialog"
      title="请输入队伍密码"
      show-cancel-button
      @confirm="() => doJoinTeam(password.value)"
      @cancel="doJoinCancel"
  >
    <van-field
        v-model="password"
        type="password"
        placeholder="请输入队伍密码"
        clearable
    ></van-field>
  </van-dialog>
</template>

<style scoped>
.team-card-list {
  width: 100%;
}

.team-card {
  margin-bottom: 12px;
}

.team-info-item {
  margin-bottom: 4px;
  font-size: 14px;
  color: #666;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 16px;
}
</style>