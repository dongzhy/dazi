<script setup lang="ts">
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import MyAxios from "../plugins/myAxios";
import { showToast } from "vant";

const router = useRouter();

const addTeamData = ref({
  name: "",
  description: "",
  expireTime: null as [number, number, number, number, number] | null,
  maxNum: 3,
  password: "",
  status: 0,
});

const showPicker = ref(false);
const result = ref("");

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

const formatApiDate = (arr: [number, number, number, number, number] | null) => {
  if (!arr) return null;
  const [y, m, d, h, i] = arr;
  return new Date(y, m - 1, d, h, i).toISOString();
};

watch(
    () => addTeamData.value.expireTime,
    (newVal) => {
      result.value = formatDisplayDate(newVal);
    },
    { immediate: true }
);

const onConfirm = ({ selectedValues }: { selectedValues: number[] }) => {
  if (selectedValues.length < 5) return;
  addTeamData.value.expireTime = selectedValues as [number, number, number, number, number];
  showPicker.value = false;
};

const openPicker = () => {
  showPicker.value = true;
};

const doSubmit = async () => {
  try {
    if (!addTeamData.value.name.trim()) {
      showToast("请填写队伍名");
      return;
    }
    if (addTeamData.value.status === 2 && !addTeamData.value.password.trim()) {
      showToast("加密队伍必须填写密码");
      return;
    }
    const postData = {
      ...addTeamData.value,
      status: Number(addTeamData.value.status),
      expireTime: formatApiDate(addTeamData.value.expireTime),
    };
    const res = await MyAxios.post("/team/add", postData);
    if (res.code === 0) {
      showToast("添加成功");
      await router.push({ path: "/team", replace: true });
    } else {
      showToast(res.message || "添加失败");
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

      <!-- 过期时间选择（核心修复：点击事件改为打开弹窗） -->
      <van-field
          v-model="result"
          is-link
          readonly
          label="过期时间"
          placeholder="点击选择过期时间"
          @click="openPicker"
          style="pointer-events: auto;"
      />

      <!-- 日期选择器弹窗（修复层级和按钮属性） -->
      <van-popup
          v-model:show="showPicker"
          position="bottom"
          style="z-index: 9999;"
      >
        <van-date-picker
            v-model="addTeamData.expireTime"
            type="datetime"
            title="选择过期时间"
            :min-date="new Date()"
            @confirm="onConfirm"
            @cancel="showPicker = false"
            :cancel-button-text="'取消'"
            :confirm-button-text="'确认'"
            style="z-index: 10000;"
        />
      </van-popup>

      <!-- 最大人数 -->
      <van-field name="stepper" label="最大人数">
        <template #input>
          <van-stepper v-model="addTeamData.maxNum" min="1" max="20" />
        </template>
      </van-field>

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

/* 确保弹窗按钮可点击 */
:deep(.van-picker__action) {
  z-index: 10001 !important;
}

/* 避免父容器遮挡弹窗 */
:deep(.van-cell-group) {
  overflow: visible !important;
}
</style>