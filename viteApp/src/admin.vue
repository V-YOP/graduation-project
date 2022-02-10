<!-- 提供管理功能的页面
在编写的时候别忘了用组合式API！
公交客车超速分析系统的设计与实现
 -->
<template>
    <el-container style="height: 100%; width: 100%">
        <ask-for-place v-if="isLogined" @select-places="selectPlaces" />
        <el-aside>
            <el-menu
                :default-active="presentIndex"
                style="height: 100%; width: inherit !important; position: fixed"
                @select="handleSelect"
            >
                <!-- 
      @open="handleOpen"
      @close="handleClose" -->
                <el-menu-item index="1">
                    <i class="el-icon-info"></i>
                    <template #title>信息总览</template>
                    <!-- 线路管理是最复杂的地方……应该吧 -->
                </el-menu-item>
                <el-menu-item index="2"
                    ><i class="el-icon-location"></i>路网管理</el-menu-item
                >

                <el-menu-item index="3">
                    <i class="el-icon-info"></i>
                    <template #title>公交车管理</template>
                </el-menu-item>
                <el-menu-item index="4">
                    <i class="el-icon-sort"></i>
                    <template #title>线路管理</template>
                    <!-- 线路管理是最复杂的地方……应该吧 -->
                </el-menu-item>
                <el-menu-item index="5">
                    <i class="el-icon-user-solid"></i>
                    <template #title>个人信息</template>
                </el-menu-item>
                <el-menu-item index="6">
                    <i class="el-icon-s-management"></i>
                    <template #title>操作列表</template>
                    <!-- 所有修改将聚集在这里，等待一次全部提交……（先不写能够删除其中单个的功能，只允许清空或提交 -->
                </el-menu-item>
            </el-menu>
        </el-aside>
        <!-- 这里的内容随着选择而改变 -->
        <el-container style="height: 100%; width: 100%" direction="vertical">
            <div
                v-if="!presentIndex"
                style="height: 100%; width: 100%; overflow: hidden"
            >
                <el-skeleton :rows="180" animated />
            </div>
            <component
                v-if="presentIndex"
                :is="componentName"
                :info="info"
                :places="places"
            />
            <!-- 使用动态组件解决路由问题 -->
        </el-container>
    </el-container>
</template>


<script lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import {
    computed,
    defineComponent,
    onMounted,
    provide,
    Ref,
    ref,
    shallowRef,
    watch,
} from "vue";
import { useRouter } from "vue-router";
import adminApi, { OptionList } from "./util/adminApi";
import { getLoginData, isLogined as useLogin } from "./admin/useAdmin";
import askForPlace from "./components/ask-for-place.vue";
import busManage from "./admin/bus-manage.vue";
import buslineManage from "./admin/busline-manage.vue";
import mapManage from "./admin/map-manage.vue";
import optionList from "./admin/option-list.vue";
import personalInfo from "./admin/personal-info.vue";
import simpleInfoPerspective from "./admin/simple-info-perspective.vue";
import api, { AllInfo } from "./util/api";
import _ from "lodash";
import axios from "axios";

const stor: Record<string, any> = {};

function localProvide(name: string, arg: any) {
    stor[name] = arg;
}
function localInject(name: string) {
    return stor[name];
}

// 处理根据事件获取信息的逻辑
function useSelectPlaces() {
    const info = ref<AllInfo>(); // 获取的信息
    const places = ref<{ adcode: number; name: string }[]>(); // 获取的地点
    const ready = ref(false);

    return {
        adcodeMessageHandler(places_: { adcode: number; name: string }[]) {
            // 该函数应当作为获取要查看的地区的回调
            console.log(places);
            places.value = places_;
            api.getInfos(
                places_.map((v) => v.adcode),
                (data) => {
                    info.value = data;
                    console.log("获取到所有地区信息！", data);
                    ready.value = true;
                    localInject("usePlaceData")(places_);
                }
            );
        },
        info,
        places,
        ready,
    };
}

function useMyRouter(ready: Ref<boolean>) {
    const index2Component: Record<string, string> = {
        "1": "simpleInfoPerspective",
        "2": "mapManage",
        "3": "busManage",
        "4": "buslineManage",
        "5": "personalInfo",
        "6": "optionList",
    };
    const component2Index = _.invert(index2Component);

    const presentIndex = ref<string | null>(null);
    // 等待ready后初始化
    watch(
        ready,
        (val) =>
            val &&
            (presentIndex.value = component2Index["simpleInfoPerspective"])
    );
    return {
        presentIndex,
        componentName: computed(() => {
            console.log("更改component");
            return index2Component[presentIndex.value!];
        }),
        handleSelect(index: string) {
            presentIndex.value = index;
        },
    };
}

// 获取公交车所处地点的信息……

export default defineComponent({
    components: {
        askForPlace,
        busManage,
        buslineManage,
        mapManage,
        optionList,
        personalInfo,
        simpleInfoPerspective,
    },
    
    setup(props, context) {
        // 检查是否登录并获取
        const adcode2BusIds = ref<Record<number, number[]> | null>(null);
        const adcode2BuslineIds = ref<Record<number, number[]> | null>(null);
        provide("adcode2BusIds", adcode2BusIds);
        provide("adcode2BuslineIds", adcode2BuslineIds);
        const { isLogined } = useLogin();
        const { info, places, adcodeMessageHandler, ready } = useSelectPlaces();
        const { presentIndex, componentName, handleSelect } =
            useMyRouter(ready);
        const optionList = ref<OptionList>([]);
        localProvide(
            "usePlaceData",
            function usePlaceData(places: { adcode: number; name: string }[]) {
                axios
                    .get(
                        `http://localhost:8080/api/getAdcode2busIdsMap?adcodes=${places
                            .map((v) => v.adcode)
                            .join(",")}`
                    )
                    .then((res) => {
                        adcode2BusIds.value = res.data;
                        console.log("获取到地区的公交车ID")
                    })
                    .catch((e) => console.log(e));

                axios
                    .get(
                        `http://localhost:8080/api/getAdcode2buslineIdsMap?adcodes=${places
                            .map((v) => v.adcode)
                            .join(",")}`
                    )
                    .then((res) => {
                        adcode2BuslineIds.value = res.data;
                        console.log("获取到地区的公交线路ID")
                    })
                    .catch((e) => console.log(e));
            }
        );
        provide("optionList", optionList);
        return {
            isLogined,
            optionList,
            selectPlaces: adcodeMessageHandler,
            info,
            places,
            presentIndex,
            ready,
            componentName,
            handleSelect,
        };
    },
});
</script>


<style>
</style>