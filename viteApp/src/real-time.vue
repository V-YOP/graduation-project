<template>
    <el-container style="height: 100%; width: 100%">
        <el-header id="header" height="auto">
            <el-row
                type="flex"
                align="middle"

                style="width: 100%; height: 100%"
            >
                <el-col :span="8">
                    <el-link
                        type="primary"
                        :underline="false"
                        style="font-size: 30px; float: left"
                        >{{ displayed_place_name }}</el-link
                    >
                </el-col>
                <el-col :span="8"> </el-col>
                <el-col :span="8">
                    <div style="float: right">
                        <el-popover
                            placement="bottom"
                            :width="200"
                            trigger="manual"
                            v-model:visible="infoVisible"
                        >
                            <template #reference>
                                <el-button
                                    type="text"
                                    icon="el-icon-info"
                                    circle
                                    style="font-size: 20px"
                                    @click="infoVisible = !infoVisible"
                                    >信息</el-button
                                >
                            </template>
                            <!-- 在这里插入相关信息 -->
                            <h3 style="margin-top: 5px">实时信息</h3>
                            <p>
                                地区线路总数：<el-tag size="mini">{{
                                    lineCount
                                }}</el-tag>
                            </p>
                            <p>
                                地区公交车总数：<el-tag size="mini">{{
                                    busCount
                                }}</el-tag>
                            </p>
                            <p>
                                在线公交车数量：<el-tag size="mini">{{
                                    onlineBusList.length
                                }}</el-tag>
                            </p>
                            <el-space direction="vertical" alignment="left">
                                <el-switch
                                    v-model="displayBusline"
                                    active-text="显示公交线路"
                                >
                                </el-switch>
                                <el-switch
                                    v-model="displayPos"
                                    active-text="显示公交车实时位置"
                                >
                                </el-switch>
                                <el-switch
                                    v-model="displayHeatMap"
                                    active-text="显示超速热力图"
                                >
                                </el-switch>
                            </el-space>
                        </el-popover>
                        <el-button
                            type="text"
                            icon="el-icon-sort"
                            circle
                            style="font-size: 20px"
                            @click="buslineDrawerVisible = true"
                            >线路</el-button
                        >
                        <el-drawer
                            title="线路信息"
                            append-to-body
                            v-model="buslineDrawerVisible"
                            direction="ltr"
                        >
                            <busline-info-handler
                                v-if="busAndLineData"
                                :busAndLineData="busAndLineData"
                                :onlineBusList="onlineBusList"
                                @disable-focus-mode="disableFocusMode"
                                @enable-focus-mode="enableFocusMode"
                            />
                        </el-drawer>
                        <el-button
                            type="text"
                            :icon="darkMode ? `el-icon-moon` : `el-icon-sunny`"
                            @click="darkMode = !darkMode"
                            circle
                            style="font-size: 20px"
                        ></el-button>
                    </div>
                </el-col>
            </el-row>
        </el-header>
        <el-main style="padding: 0" id="main">
            <scene
                :lat="x"
                :lont="y"
                @scene-loaded="scene = $event"
                :darkMode="darkMode"
            />
            <busline-layer
                v-if="scene && busAndLineData"
                :scene="scene"
                :display="displayBusline"
                :busAndLineData="busAndLineData"
                :focusLines="focusBuslines"
            />
            <realtime-layer
                v-if="scene && busIdList"
                :focusBuses="focusBuses"
                :scene="scene"
                :busIdList="busIdList"
                :busAndLineData="busAndLineData"
                :displayPos="displayPos"
                :displayOverspeedPosBy="displayOverspeedPosBy"
                @update-online-bus-list="updateOnlineBusList"
            />
        </el-main>
        <!--  TODO  -->
    </el-container>
</template>

<script lang="ts">
import scene from "./components/scene.vue";
import buslineLayer from "./components/busline-layer.vue";
import realtimeLayer from "./components/realtime-layer.vue";
import busStationInfoHandler from "./components/bus-station-info-handler.vue";
import buslineInfoHandler from "./components/busline-info-handler.vue";
import { computed, defineComponent, provide, Ref, ref, shallowRef, watch } from "vue";
import { useRoute } from "vue-router";
import _ from "lodash";
import { Scene } from "@antv/l7";
import axios from "axios";
import api, { AllInfo, TODO } from "./util/api";
import { BusAndAllNodeAdapter, busAndLine } from "./util/messageAdapter";
import getBusAndLineData from "./composables/getBusAndLineData";
import * as util from "./speedAnalysis/util";

export default defineComponent({
    components: {
        buslineLayer,
        scene,
        realtimeLayer,
        busStationInfoHandler,
        buslineInfoHandler,
    },

    setup() {
        const route = useRoute();
        const scene = shallowRef<Scene>();
        let places: {
            NAME_CHN: string;
            level?: string;
            adcode: number;
            x: number;
            y: number;
        }[] = JSON.parse(decodeURI(route.query.places as string));
        const { x, y, place_name, info } = getBusAndLineData(places, null);
        const onlineBusList = ref<number[]>([]);
        const focusBuslines = ref<number[]>([]);
        const focusMode = ref(false);
        const displayHeatMap= ref(true)

        return {
            focusMode,
            focusBuses : computed(()=>{
                if (info.value === undefined || focusBuslines.value.length === 0)
                    return [];
                let res = _(focusBuslines.value.map(lineId=>info.value!.buslines[lineId].bus_id_list))
                    .flatMap()
                    .uniq()
                    .value();
                console.log("这些公交车！", res)
                return res;
            }),
            focusBuslines,
            darkMode: ref(false),
            displayBusline: ref(true),
            displayPos: ref(true),
            displayHeatMap,
            displayOverspeedPosBy : computed(() => {
                if (displayHeatMap.value)
                    return "HEATMAP";
                return "DENSITY";
            }),
            x,
            y,
            onlineBusList,
            infoVisible: ref(false),
            updateOnlineBusList(res: number[]) {
                onlineBusList.value = res;
            },
            busStationDrawerVisible: ref(false),
            buslineDrawerVisible: ref(false),
            scene,
            place_name,
            busAndLineData: info,
            busIdList: computed(() => {
                if (!info.value) return null;
                return _(info.value.buses).keys().map(Number).value();
            }),
            displayed_place_name: computed(() => {
                let names = places.map((place) => place.NAME_CHN);
                if (names[1] === names[2]) names.splice(2, 1);
                if (names[0] === names[1]) names.splice(1, 1);
                return names.join("-");
            }),
            lineCount: computed(() => {
                if (info.value) return _.size(info.value.buslines);
                return "LOADING";
            }),
            busCount: computed(() => {
                if (info.value) return _.size(info.value.buses);
                return "LOADING";
            }),
            disableFocusMode() {
                focusMode.value = false;

            },
            enableFocusMode(buslineList: number[]) {
                focusMode.value = true;
                console.log("聚焦这些线路！",buslineList);
                focusBuslines.value = buslineList;
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
<style lang="scss">
.el-drawer.ltr {
    overflow: scroll;
}
</style>