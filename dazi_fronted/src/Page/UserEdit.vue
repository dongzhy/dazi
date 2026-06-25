<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { getCurrentUser } from "../services/user.ts";
import myAxios from "../plugins/myAxios";
import { showToast } from "vant";

const route = useRoute();
const router = useRouter();

// 编辑字段类型
interface EditUserInfo {
  editKey: string;
  currentValue: string;
  editName: string;
}

// 初始化数据
const editUser = ref<EditUserInfo>({
  editKey: (route.query.editKey as string) || "",
  currentValue: (route.query.currentValue as string) || "",
  editName: (route.query.editName as string) || "",
});

// 核心提交方法
const handleSubmit = async () => {
  const { editKey, currentValue, editName } = editUser.value;

  // 基础校验
  if (!editKey) {
    showToast("编辑字段异常，请返回重试");
    return;
  }
  const trimValue = currentValue.trim();
  if (!trimValue) {
    showToast(`请输入${editName || "内容"}`);
    return;
  }

  showToast({ type: "loading", message: "提交中...", forbidClick: true, duration: 0 });

  try {
    // 获取登录用户
    const currentUser = await getCurrentUser();
    if (!currentUser?.id) {
      showToast("请先登录");
      router.push("/user/login");
      return;
    }

    let res;

    // 根据接口文档：如果是修改标签，走独立接口 /user/update/tags
    if (editKey === "tags") {
      const tagList = trimValue.split(",").filter(item => item.trim() !== "").map(item => item.trim());
      res = await myAxios.post("/user/update/tags", { tags: tagList });
    } else {
      // 其他字段走 /user/update 接口
      let submitValue: string | number = trimValue;

      // 性别转为数字
      if (editKey === "gender") {
        const num = Number(trimValue);
        if (isNaN(num) || ![0, 1].includes(num)) {
          showToast("性别仅支持 0(男) / 1(女)");
          return;
        }
        submitValue = num;
      }

      const postData = { id: currentUser.id, [editKey]: submitValue };
      res = await myAxios.post("/user/update", postData);
    }

    // 关键修复：拦截器已经返回了 response.data，所以 res 就是 { code: 0, data: 1, ... }
    // 直接判断 res.code 即可，不要再用 res.data.code
    if (res.code === 0) {
      showToast("修改成功");
      // 强制刷新本地缓存的用户信息（传入 true 强制请求后端）
      await getCurrentUser(true);
      router.back();
    } else {
      // 接口文档中错误描述字段为 description
      showToast(res.description || "修改失败");
    }
  } catch (err: any) {
    console.error("提交异常：", err);
    showToast(err?.response?.data?.description || err?.message || "请求异常，请稍后重试");
  }
};
</script>

<template>
  <!-- 移除原生 form 提交逻辑，改用普通布局 + 按钮点击事件，彻底解决点击无响应 -->
  <van-cell-group inset>
    <van-field
        v-model="editUser.currentValue"
        :name="editUser.editKey"
        :label="editUser.editName"
        :placeholder="editUser.editKey === 'tags' ? '多个标签请用英文逗号分隔' : `请输入${editUser.editName || '内容'}`"
        :disabled="!editUser.editKey"
        rows="3"
        type="textarea"
    />
  </van-cell-group>
  <div style="margin: 16px">
    <!-- 直接绑定 click 事件，不再使用表单 submit -->
    <van-button round block type="primary" @click="handleSubmit">
      提交保存
    </van-button>
  </div>
</template>

<style scoped></style>
