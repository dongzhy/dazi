<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import myAxios from "../plugins/myAxios";
import { showToast } from "vant";

const router = useRouter();
const route = useRoute();

// 初始化响应式数据
const addTeamData = ref({
  id: 0,
  name: "",
  description: "",
  expireTime: null as [number, number, number, number, number] | null,
  status: 0,
  password: "",
});

const showPicker = ref(false);

// 兼容的时间转换函数：后端字符串 -> 前端数组
const parseExpireTime = (timeStr: string | null | undefined) => {
  if (!timeStr) return null;
  try {
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return null;
    return [
      date.getFullYear(),
      date.getMonth() + 1,
      date.getDate(),
      date.getHours(),
      date.getMinutes()
    ] as [number, number, number, number, number];
  } catch (e) {
    console.error("时间转换失败：", e);
    return null;
  }
};

const result = ref("请选择过期时间");

// 格式化时间：前端数组 -> 前端显示文本
const formatDisplayDate = (arr: [number, number, number, number, number] | null) => {
  if (!arr) return "请选择过期时间";
  const [y, m, d, h, i] = arr;
  const date = new Date(y, m - 1, d, h, i);
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
};

// 新增：前端数组 -> 后端需要的 ISO 字符串
const formatApiDate = (arr: [number, number, number, number, number] | null) => {
  if (!arr) return null;
  const [y, m, d, h, i] = arr;
  const date = new Date(y, m - 1, d, h, i);
  return date.toISOString();
};

// 页面挂载加载数据
onMounted(async () => {
  const teamId = route.query.id || route.params.id;
  if (!teamId) {
    showToast("缺少队伍ID，请从队伍列表进入编辑");
    return;
  }

  try {
    const res = await myAxios.get("/team/get", { params: { id: teamId } });

    if (res.code !== 0) {
      showToast(res.description || "获取队伍数据失败");
      return;
    }

    const teamData = res.data;
    if (!teamData) {
      showToast("未查询到该队伍数据");
      return;
    }

    // 映射字段 + 适配后端枚举值
    addTeamData.value = {
      id: teamData.id || 0,
      name: teamData.name || "",
      description: teamData.description || "",
      status: Number(teamData.status) || 0,
      password: teamData.password || "",
      expireTime: parseExpireTime(teamData.expireTime)
    };

    result.value = formatDisplayDate(addTeamData.value.expireTime);
  } catch (err) {
    showToast("网络异常，加载失败");
    console.error("加载失败原因：", err);
  }
});

// 时间选择器确认事件
const onConfirm = ({ selectedValues }: { selectedValues: number[] }) => {
  if (selectedValues.length < 5) return;
  addTeamData.value.expireTime = selectedValues as [number, number, number, number, number];
  // 同步显示文本
  result.value = formatDisplayDate(addTeamData.value.expireTime);
  showPicker.value = false;
};

const openPicker = () => {
  showPicker.value = true;
};

// 提交表单
const doSubmit = async () => {
  try {
    const teamData = addTeamData.value;
    if (!teamData.name.trim()) {
      showToast("请填写队伍名");
      return;
    }
    if (teamData.status === 2 && !teamData.password.trim()) {
      showToast("加密队伍必须填写密码");
      return;
    }

    // 构造符合后端 TeamUpdateRequest 结构的对象
    const postData = {
      id: teamData.id,
      name: teamData.name,
      description: teamData.description,
      status: Number(teamData.status),
      // 关键修复：将数组转换为后端能识别的 ISO 时间字符串
      expireTime: formatApiDate(teamData.expireTime),
      // 如果不是加密队伍，不传密码（或传空，视后端具体校验逻辑定，通常不传更安全）
      password: teamData.status === 2 ? teamData.password : undefined,
    };

    const res = await myAxios.post("/team/update", postData);

    // 关键修复：拦截器已经返回了 response.data，所以直接判断 res.code
    if (res.code === 0) {
      showToast("更新成功");
      await router.push({ path: "/team", replace: true });
    } else {
      showToast(res.description || "更新失败");
    }
  } catch (error) {
    console.error("提交失败：", error);
    showToast("网络异常，提交失败");
  }
};
</script>

<template>
  <van-form @submit="doSubmit">
    <van-cell-group inset>
      <!-- 队伍名 -->
      <van-field
          v-model="addTeamData.name"
          name="name"
          label="队伍名"
          placeholder="请输入队伍名"
          :rules="[{ required: true, message: '请填写队伍名' }]"
      />

      <!-- 队伍描述 -->
      <van-field
          v-model="addTeamData.description"
          rows="3"
          autosize
          label="队伍描述"
          type="textarea"
          placeholder="请输入队伍描述"
      />

      <!-- 过期时间 -->
      <van-field
          v-model="result"
          is-link
          readonly
          label="过期时间"
          placeholder="点击选择过期时间"
          @click="openPicker"
      />

      <!-- 日期选择器弹窗 -->
      <van-popup v-model:show="showPicker" position="bottom">
        <van-date-picker
            v-model="addTeamData.expireTime"
            type="datetime"
            title="选择过期时间"
            :min-date="new Date()"
            @confirm="onConfirm"
            @cancel="showPicker = false"
        />
      </van-popup>

      <!-- 队伍状态 -->
      <van-radio-group v-model="addTeamData.status">
        <van-radio :name="0">公开</van-radio>
        <van-radio :name="1">私有</van-radio>
        <van-radio :name="2">加密</van-radio>
      </van-radio-group>

      <!-- 加密密码 -->
      <van-field
          v-if="addTeamData.status === 2"
          v-model="addTeamData.password"
          type="password"
          label="密码"
          placeholder="请输入队伍密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
    </van-cell-group>

    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<style scoped>
.van-radio-group {
  padding: 10px 0;
}
</style>
