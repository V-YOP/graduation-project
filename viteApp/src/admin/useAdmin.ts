import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import adminApi from "../util/adminApi";

export function getLoginData() {
    let loginData = adminApi.loginCheck()
    return loginData;
}
export function isLogined() {
    const isLogined = ref(false);
    if (!getLoginData()) {
        let router = useRouter();
        ElMessageBox.alert("请先登录！").then(() => {
            // 路由到主界面
            router.push({
                path: "/",
            });
        });
    } else {
        isLogined.value = true
    }
    // 调试用
    isLogined.value = true
    return { isLogined };
}
