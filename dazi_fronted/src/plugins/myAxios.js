import axios from "axios";
const isDev = process.env.NODE_ENV === "development";
// 1. 创建自定义Axios实例
const MyAxios = axios.create({
    baseURL: isDev ? 'http://localhost:8080/api' :'线上环境',
    // timeout: 1000,
    // headers: {'X-Custom-Header': 'foobar'}
    withCredentials: true
});

// 2. 拦截器绑定到MyAxios实例上（而非全局axios）
// 添加请求拦截器
MyAxios.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么（此时该逻辑会对MyAxios的所有请求生效）
    console.log('我要发起请求啦', config);
    return config;
}, function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
MyAxios.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数（仅对MyAxios的响应生效）
    console.log('我收到你的响应啦', response.data);
    if (response?.data?.code === 40100) {
        const redirectUrl =window.location.href;
        window.location.href =`/user/login?redirectUrl=${redirectUrl}`;
    }
    return response.data;
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数
    return Promise.reject(error);
});

// 导出自定义实例
export default MyAxios;