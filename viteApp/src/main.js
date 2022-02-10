import { createApp, h } from 'vue'
import RealTime from './real-time.vue'
import ElementPlus from 'element-plus'
import 'element-plus/lib/theme-chalk/index.css'
import HomePage from "./main.vue"
import SingleDayHistory from "./single-day-history.vue"
import MultiDayHistory from "./multi-day-history.vue"
import tst from "./tst.vue"
const NotFoundComponent = { template: '<p>Page not found</p>' }
const AboutComponent = { template: '<p>about me</p>' }
import * as VueRouter from 'vue-router'
import locale from 'element-plus/lib/locale/lang/zh-cn'
import Admin from './admin.vue'
import moment from 'moment'
moment.locale("zh-cn")
const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    routes: [
        { path: '/', component: HomePage },
        { path: '/admin', component: Admin},
        { path: '/tst', component: tst }, 
        { path: '/real-time', component: RealTime },
        { path: '/single-day-history', component: SingleDayHistory },
        { path: "/multi-day-history", component: MultiDayHistory } 
    ],

})
createApp({}).use(router).use(ElementPlus, {locale}).mount('#app')

/*
    走れ！ハルウララ！
*/

