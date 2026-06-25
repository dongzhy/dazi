<script setup>
import { ref, computed } from 'vue';
import {useRouter} from "vue-router";
const router = useRouter();

// 原始标签列表（只读，不直接修改）
const originTagList = ref([
  {
    text: '性别',
    children: [
      { text: '男', id: '男' },
      { text: '女', id: '女' },
    ],
  },
  {
    text: '年级',
    children: [
      { text: '大一', id: '大一' },
      { text: '大二', id: '大二' },
      { text: '大三', id: '大三' },
      { text: '大四', id: '大四' },
      { text: '研一', id: '研一' },
      { text: '研二', id: '研二' },
    ],
  },
]);

// 搜索文本
const searchText = ref('');

// 已选标签ID和当前激活的主索引
const activeId = ref([]);
const activeIndex = ref(0);

/**
 * 计算属性：根据搜索文本过滤标签列表（无需手动维护，响应式自动更新）
 * 1. 深拷贝原始数据，避免修改原数据
 * 2. 同时匹配父标签名称和子标签名称
 * 3. 过滤后父标签若无子项则不显示
 */
const filteredTagList = computed(() => {
  const keyword = searchText.value.trim().toLowerCase();
  // 深拷贝原始标签列表，防止污染原数据
  const copyTagList = JSON.parse(JSON.stringify(originTagList.value));

  if (!keyword) {
    return copyTagList; // 无搜索关键词时返回原始拷贝
  }

  // 过滤父标签和子标签
  return copyTagList.filter(parentTag => {
    // 父标签名称匹配关键词
    const parentMatch = parentTag.text.toLowerCase().includes(keyword);
    // 过滤子标签，匹配关键词
    parentTag.children = parentTag.children.filter(child =>
        child.text.toLowerCase().includes(keyword)
    );
    // 父标签匹配 或 子标签有匹配结果时，保留该父标签
    return parentMatch || parentTag.children.length > 0;
  });
});

/**
 * 搜索触发函数（可保留，兼容van-search的search事件）
 */
const onSearch = () => {
  // 此处无需额外逻辑，计算属性已自动处理，仅作为事件回调占位
  console.log('搜索关键词：', searchText.value);
};

const doSearchResult = () => {
  router.push({
    path:'/user/list',
    query:{
      tags: activeId.value,
    }

  })

}

/**
 * 取消搜索：清空搜索文本即可，计算属性自动恢复原始列表
 */
const onCancel = () => {
  searchText.value = '';
};

/**
 * 关闭已选标签
 * @param {string} tag - 要关闭的标签ID/文本
 */
const doClose = (tag) => {
  activeId.value = activeId.value.filter(item => item !== tag);
};
</script>

<template>
  <form action="/" @submit.prevent> <!-- 阻止表单默认提交行为 -->
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入搜索标签（支持父/子标签搜索）"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>

  <van-divider content-position="left">已选标签</van-divider>
  <div v-if="activeId.length === 0" style="color: #999; padding: 16px;">请选择标签</div>
  <van-row gutter="16" style="padding: 0 16px;">
    <van-col v-for="tag in activeId" :key="tag"> <!-- 增加key提升性能 -->
      <van-tag
          closeable
          size="small"
          type="primary"
          @close="doClose(tag)"
      >
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>

  <van-divider content-position="left" style="margin-top: 16px;">选择标签</van-divider>
  <van-tree-select
      v-model:main-active-index="activeIndex"
      v-model:active-id="activeId"
      :items="filteredTagList"
  style="padding: 0 16px;"
  />
  <van-button block type="primary"  @click="doSearchResult"
  style="margin: 12px"
  >搜索</van-button>
</template>

<style scoped>
</style>