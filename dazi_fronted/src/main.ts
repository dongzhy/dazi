import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import {Button, CellGroup, Empty, Field, NavBar, Tabbar, TabbarItem, Tag, TimePicker, Toast} from "vant";
import {createRouter, createWebHistory} from 'vue-router'
import routes from "./config/route.ts";
import { TreeSelect } from 'vant';


const app = createApp(App)


app.use(Button);
app.use(NavBar);
app.use(Toast);
app.use(Tabbar);
app.use(Empty);
app.use(Field);
app.use(TimePicker);
app.use(CellGroup);


app.use(TreeSelect);
app.use(TabbarItem);
app.use(Tag);


const router = createRouter({
    history: createWebHistory(),
    routes,
})
app.use(router);
app.mount('#app')