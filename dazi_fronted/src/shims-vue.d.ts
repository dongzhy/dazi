declare module '*.vue' {
    import type { DefineComponent } from 'vue';
    // 声明.vue文件的模块类型，匹配Vue组件的类型定义
    const component: DefineComponent<{}, {}, any>;
    export default component;
}