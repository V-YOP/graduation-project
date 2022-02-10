<!-- 
超速分析信息的功能页面
这个功能才是最重要的
 -->
<template>
    <el-container style="height: 100%; width: 100%">
        <el-aside width="250px">
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
                </el-menu-item>

                <el-menu-item index="3-0">
                    <i class="el-icon-discover"></i>
                    <template #title>地区超速分析</template>
                </el-menu-item>
                <el-menu-item index="3-1"
                    ><i class="el-icon-mobile-phone"></i>
                    <template #title>公交车超速分析</template></el-menu-item
                >
                <el-menu-item index="3-2"
                    ><i class="el-icon-position"></i>
                    <template #title>路径超速分析</template></el-menu-item
                >
                <el-menu-item index="3-3"
                    ><i class="el-icon-sort"></i>
                    <template #title>公交线路超速分析</template></el-menu-item
                >
            </el-menu>
        </el-aside>
        <!-- 这里的内容随着选择而改变 -->
        <el-container
            style="height: 100%; width: 100%"
            direction="vertical"
            v-loading="infoGetting"
            element-loading-text="加载超速信息中……"
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
        >
            <component
                v-if="!infoGetting"
                :is="componentName"
                :overspeedInfo="overspeedInfo"
                :scope="scope"
                :infoGetter="infoGetter"
                :overspeedFactorExpr="overspeedFactorExpr"
                @changeOverspeedFactorExpr="changeOverspeedFactorExpr"
            />
        </el-container>
    </el-container>
</template>

<script lang="ts">
// TODO
import scene from "./components/scene.vue";
import buslineLayer from "./components/busline-layer.vue";
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
import { useRoute } from "vue-router";
import _ from "lodash";
import { Scene } from "@antv/l7";
import axios from "axios";
import { BusAndAllNodeAdapter, busAndLine } from "./util/messageAdapter";
import api, { AllInfo, overspeedPos } from "./util/api";
import * as util from "./speedAnalysis/util";
import busAnalysis from "./speedAnalysis/bus-analysis.vue";
import buslineAnalysis from "./speedAnalysis/busline-analysis.vue";
import infoPerspective from "./speedAnalysis/info-perspective.vue";
import nodepathAnalysis from "./speedAnalysis/nodepath-analysis.vue";
import placeAnalysis from "./speedAnalysis/place-analysis.vue";
import { ElMessageBox } from "element-plus";
function getQuerys() {
    const route = useRoute();
    const places: {
        NAME_CHN: string;
        level?: string;
        adcode: number;
        x: number;
        y: number;
    }[] = JSON.parse(decodeURI(route.query.places as string));
    const { x, y, adcode } = _.last(places)!;
    const place_name = places.map((place) => place.NAME_CHN);
    if (place_name[0] === place_name[1]) place_name[1] = "市辖区";
    return {
        lat: x,
        lont: y,
        adcode,
        place_name: place_name.join("/"),
        startDate: route.query.date0 as string,
        endDate: route.query.date1 as string,
    };
}

function useMyRouter() {
    const index2Component: Record<string, string> = {
        "1": "infoPerspective",
        "2": "mapDisplay",
        "3-0": "placeAnalysis",
        "3-1": "busAnalysis",
        "3-2": "nodepathAnalysis",
        "3-3": "buslineAnalysis",
    };
    const component2Index = _.invert(index2Component);
    const presentIndex = ref<string>(component2Index["infoPerspective"]);
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

function getInfo(adcode: number, startDate: string, endDate: string) {
    const infoGetting = ref<boolean>(true);
    const overspeedInfo = shallowRef<overspeedPos[]>();
    const scope = ref<number>();
    const startInfo = ref<AllInfo>();
    const endInfo = ref<AllInfo>();
    provide("busInfoGetter", function(bus_id:number) {
        return util.getBusInfo(bus_id,
            startInfo.value as AllInfo,
                endInfo.value as AllInfo)
    })
    provide("nodepathInfoGetter", function(nodepath_id : number) {
        return util.getNodepathInfo(nodepath_id,
            startInfo.value as AllInfo,
            endInfo.value as AllInfo)
    })
    provide("buslineInfoGetter", function(line_id : number) {
        return util.getBuslineInfo(line_id,
            startInfo.value as AllInfo,
            endInfo.value as AllInfo)
    })
    
    const mergedInfo = ref<AllInfo>({buses:{},nodepaths:{},buslines:{},nodes:{}})
    watch([startInfo,endInfo], function(val) {
        if (startInfo.value && endInfo.value) {
            mergedInfo.value = util.getMergedInfo(
            startInfo.value as AllInfo,
            endInfo.value as AllInfo)
        }      
        
    })
    provide("mergedInfo", mergedInfo)

    const infoGetter = function (overspeedPos: overspeedPos) {
        if (!startInfo.value || !endInfo.value) {
            throw new Error("还未加载数据！");
            return;
        }
        return util.getRelativeInfo(
            overspeedPos,
            startInfo.value,
            endInfo.value
        );
    };
    // 获取超速信息
    axios
        .get(
            `http://localhost:8080/api/getRandomOverspeedPosesByAdcodeBetweenTest?adcode=${adcode}&startDate=${startDate}&endDate=${endDate}`
        )
        .catch((e) => console.log(e))
        .then((res) => {
            if (!res) throw new Error("出了点情况");
            const data: { overspeedPoses: overspeedPos[]; scope: number } =
                res.data;
            overspeedInfo.value = data.overspeedPoses;
            scope.value = data.scope;
            console.log("获取超速信息成功");
            if (!overspeedInfo.value || overspeedInfo.value.length === 0) {
                alert("该日期区间没有数据！")
                document.location.href="/" // 直接跳转到主页，因为没有数据
            }
        });
    api.getAllInfo(adcode, startDate, (data) => {
        startInfo.value = data;
        console.log("获取开始时间信息成功");
        console.log(startInfo.value)
    });
    api.getAllInfo(adcode, endDate, (data) => {
        endInfo.value = data;
        console.log("获取终止时间信息成功");
        console.log(endInfo.value)
    });
    watch(overspeedInfo, (info) => {
        if (info) {
            infoGetting.value = false;
            console.log("接收到信息");
        }
    });
    return { overspeedInfo, scope, infoGetter, infoGetting };
}

export default defineComponent({
    components: {
        busAnalysis,
        buslineAnalysis,
        infoPerspective,
        nodepathAnalysis,
        placeAnalysis,
    },
    provide: {},
    setup() {
        const { lat, lont, adcode, place_name, startDate, endDate } =
            getQuerys();
        const myRouter = useMyRouter();
        provide("place", place_name);
        provide("dateRange", [startDate, endDate]);
        const infos = getInfo(adcode, startDate, endDate);
        const overspeedFactorExpr = ref("(V - LIMIT) * 0.7 + A * 2");

        return {
            ...infos,
            ...myRouter,
            overspeedFactorExpr,
            changeOverspeedFactorExpr(newExpr: string) {
                overspeedFactorExpr.value = newExpr;
            },
        };
    },
});
</script>


<style scoped>
</style>

<style>
/*
包裹全局的div
*/
html,
body {
    height: 100%;
    width: 100%;
    padding: 0;
    border: 0;
    margin: 0;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
        "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}
#app {
    height: 100%;
    width: 100%;
}
</style>