<!-- 查询单日信息的功能页面 -->
<template>
    <el-container style="height: 100%; width: 100%">
        <el-dialog
            title="更换查看日期"
            v-model="selectDateDialogVisible"
            width="30%"
            append-to-body
        >
            <!-- 在这里插入内容 -->
            <br />
            <el-radio-group
                v-model="selectType"
                style="margin-top: 10px; margin-bottom: 10px"
            >
                <el-radio-button label="实时信息"></el-radio-button>
                <el-radio-button label="历史信息"></el-radio-button>
                <el-radio-button label="超速分析信息"></el-radio-button>
            </el-radio-group>
            <el-date-picker
                v-if="selectType === `历史信息`"
                v-model="singleDayValue"
                type="date"
                :disabled-date="disabledDate"
                placeholder="选择日期"
            >
            </el-date-picker>
            <el-date-picker
                v-if="selectType === `超速分析信息`"
                v-model="multiDayValue"
                type="daterange"
                :disabled-date="disabledDate"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
            >
            </el-date-picker>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="selectDateDialogVisible = false"
                        >返 回</el-button
                    >
                    <el-button type="primary" @click="handleClick"
                        >确 认</el-button
                    >
                </span>
            </template>
        </el-dialog>
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
                <el-col :span="10">
                    <el-button-group style="float: right">
                        <el-button
                            type="primary"
                            icon="el-icon-arrow-left"
                            @click="goBeforeDay"
                            >前一日</el-button
                        >
                        <el-button
                            type="primary"
                            icon="el-icon-date"
                            @click="selectDateDialogVisible = true"
                            >{{ date }}</el-button
                        >
                        <el-button
                            type="primary"
                            @click="goNextDay"
                            :disabled="!nextDayValid()"
                            >后一日<i
                                class="el-icon-arrow-right el-icon--right"
                            ></i
                        ></el-button>
                    </el-button-group>
                </el-col>
                <el-col :span="6">
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
                            <el-space direction="vertical" alignment="left">
                                <el-switch
                                    v-model="displayBusline"
                                    active-text="显示公交线路"
                                >
                                </el-switch>
                                <el-switch
                                    v-model="displayHeatMap"
                                    active-text="显示热力图"
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
            <el-affix
                :offset="100"
                style="position: fixed; margin-left: 60px; z-index: 1000"
            >
                <el-popover placement="bottom" width="auto" trigger="click">
                    <template #reference>
                        <el-button type="primary" icon="el-icon-date">{{
                            timeRangeDisplay
                        }}</el-button>
                    </template>
                    <el-time-picker
                        is-range
                        v-model="timeRange"
                        :clearable="false"
                        :editable="false"
                        format="HH:mm"
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                        placeholder="选择时间范围"
                        @change="changeTimeRange"
                        >
                    </el-time-picker>
                </el-popover>
            </el-affix>
            <scene :lat="x" :lont="y" 
            @scene-loaded="sceneLoaded" 
            :darkMode="darkMode"
            />
            <busline-layer
                v-if="scene && busAndLineData"
                :scene="scene"
                :display="displayBusline"
                :busAndLineData="busAndLineData"
                :focusLines="focusBuslines"
            />
            <history-layer
                v-if="scene && busIdList"
                :focusBuses="focusBuses"
                :scene="scene"  
                :busIdList="busIdList"
                :adcode="adcode"
                :busAndLineData="busAndLineData"
                :displayOverspeedPosBy="displayOverspeedPosBy"
                :timeRange="realTimeRange"
            /> 
        </el-main>
        <!--  TODO  -->
    </el-container>
</template>

<script lang="ts">
// TODO
import scene from "./components/scene.vue";
import buslineLayer from "./components/busline-layer.vue";
import historyLayer from "./components/history-layer.vue";
import { computed, defineComponent, Ref, ref, shallowRef } from "vue";
import { useRoute, useRouter } from "vue-router";
import _ from "lodash";
import { Scene } from "@antv/l7";
import axios from "axios";
import { BusAndAllNodeAdapter, busAndLine } from "./util/messageAdapter";
import api from "./util/api";
import getBusAndLineData from "./composables/getBusAndLineData";
import { ElLoading, ElMessage } from "element-plus";
import buslineInfoHandler from "./components/busline-info-handler.vue";
function toyyyyMMdd(date: Date) {
    let format = function (num: number) {
        return _.padStart("" + num, 2, "0");
    };
    return `${date.getFullYear()}-${format(date.getMonth() + 1)}-${format(
        date.getDate()
    )}`;
}
export default defineComponent({
    components: { buslineLayer, scene, historyLayer, buslineInfoHandler },
    watch: {
        $route(to, from) {
            // data数据操作
            location.reload();
        },
    },
    setup() {
        // 用户给定查询日期
        const route = useRoute();
        const router = useRouter();
        let loading = ElLoading.service();
        const scene = shallowRef<Scene>();
        const singleDayValue = ref();
        const multiDayValue = ref();
        const placeStr = decodeURI(route.query.places as string);
        const places: {
            NAME_CHN: string;
            level?: string;
            adcode: number;
            x: number;
            y: number;
        }[] = JSON.parse(placeStr);
        const date = ref(route.query.date as string);
        const selectDateDialogVisible = ref(false);
        const focusBuslines = ref<number[]>([]);
        const { x, y, place_name, info } = getBusAndLineData(
            places,
            date.value
        );
        let presentDate = new Date(date.value);
        let [year, month, day] = [
            presentDate.getFullYear(),
            presentDate.getMonth(),
            presentDate.getDate(),
        ];
        const selectType = ref("实时信息");
        const focusMode = ref(false);
        // timeRange改变是在更改而非确实选择时间范围的时候触发的，这还得经历再一次的包装。
        const timeRange = ref([new Date(year, month, day, 6, 0), new Date(year, month, day, 6, 5)])
        const realTimeRange = ref([new Date(year, month, day, 6, 0), new Date(year, month, day, 6, 5)])
        const displayHeatMap = ref(true)
        return {
            darkMode: ref(false),
            adcode : _.last(places)!.adcode,
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
            timeRange,
            realTimeRange,
            changeTimeRange(){
                let [startTime, endTime] = timeRange.value;
                realTimeRange.value = [startTime, endTime];
                console.log("更改！")
            },
            timeRangeDisplay: computed(() => {
                let fn = (n : number) => _.padStart(n+"",2,'0')
                let [startTime, endTime] = timeRange.value;
                return `${fn(startTime.getHours())}:${fn(startTime.getMinutes())}~${fn(endTime.getHours())}:${fn(endTime.getMinutes())}`
            }),
            selectType,
            handleClick() {
                let type = selectType.value;
                let placeStr = encodeURI(
                    JSON.stringify(
                        places.map(({ NAME_CHN, level, adcode, x, y }) => {
                            return { NAME_CHN, level, adcode, x, y };
                        })
                    )
                );
                if (type === "实时信息") {
                    selectDateDialogVisible.value = false;
                    router.push({
                        path: "/real-time",
                        query: {
                            places: placeStr,
                        },
                    });
                    return;
                }

                console.log(singleDayValue.value, multiDayValue.value);
                if (
                    (type === "历史信息" && !singleDayValue.value) ||
                    (type === "超速分析信息" && !multiDayValue.value)
                ) {
                    ElMessage("请选择查看日期！");
                    return;
                }

                // 给月份和日期前补0
                let format = function (num: number) {
                    return _.padStart("" + num, 2, "0");
                };
                if (type === "历史信息") {
                    // * 说明选择的是单日
                    let date: Date = singleDayValue.value;
                    router.push({
                        path: "single-day-history",
                        query: {
                            places: placeStr,
                            date: `${date.getFullYear()}-${format(
                                date.getMonth() + 1
                            )}-${format(date.getDate())}`,
                        },
                    });
                } else {
                    let date: [Date, Date] = multiDayValue.value;
                    router.push({
                        path: "multi-day-history",
                        query: {
                            places: placeStr,
                            date0: `${date[0].getFullYear()}-${format(
                                date[0].getMonth() + 1
                            )}-${format(date[0].getDate())}`,
                            date1: `${date[1].getFullYear()}-${format(
                                date[1].getMonth() + 1
                            )}-${format(date[1].getDate())}`,
                        },
                    });
                }
            },

            x,
            lineCount: computed(() => {
                if (!info.value) return "LOADING";
                return _.size(info.value.buslines);
            }),
            busCount: computed(() => {
                if (!info.value) return "LOADING";
                return _.size(info.value.buses);
            }),
            y,
            scene,
            place_name,
            displayHeatMap,
            infoVisible: ref(false),
            displayBusline: ref(true),
            busAndLineData: info,
            displayOverspeedPosBy : computed(() => {
                if (displayHeatMap.value)
                    return "HEATMAP";
                return "DENSITY";
            }),
            busIdList: computed(() => {
                if (!info.value) return null;
                return _(info.value.buses).keys().map(Number).value();
            }),
            date,
            selectDateDialogVisible,
            handleSelectDateBtn() {
                if (!singleDayValue.value) {
                    ElMessage("请选择查看日期！");
                    return;
                }
                // 给月份和日期前补0
                let format = function (num: number) {
                    return _.padStart("" + num, 2, "0");
                };

                // * 说明选择的是单日
                let date: Date = singleDayValue.value;
                router.push({
                    path: "single-day-history",
                    query: {
                        places: encodeURI(placeStr),
                        date: `${date.getFullYear()}-${format(
                            date.getMonth() + 1
                        )}-${format(date.getDate())}`,
                    },
                });
                // 需要手动刷新

                selectDateDialogVisible.value = false;
            },
            disabledDate(time: Date) {
                // * 这里的代码是从main复制过来的
                // TODO 记得改
                // let minus = 60 * 60 * 24 * 1000;
                let minus = 0;
                return time.getTime() > Date.now() - minus;
            },
            singleDayValue,
            multiDayValue,
            goBeforeDay() {
                let beforeDay = new Date(
                    new Date(date.value).getTime() - 24 * 60 * 60 * 1000
                );
                router.push({
                    path: "single-day-history",
                    query: {
                        places: encodeURI(placeStr),
                        date: toyyyyMMdd(beforeDay),
                    },
                });
            },
            goNextDay() {
                let nextDay = new Date(
                    new Date(date.value).getTime() + 24 * 60 * 60 * 1000
                );
                router.push({
                    path: "single-day-history",
                    query: {
                        places: encodeURI(placeStr),
                        date: toyyyyMMdd(nextDay),
                    },
                });
            },
            nextDayValid() {
                let nextDay = new Date(
                    new Date(date.value).getTime() + 24 * 60 * 60 * 1000
                );
                // 如果下一天的日期大于昨天
                // ! 这里和main页面中设置是否合法日期时不一样，毕竟这里没必要做测试
                if (
                    new Date(
                        toyyyyMMdd(
                            new Date(new Date().getTime() - 24 * 60 * 60 * 1000)
                        )
                    ).getTime() < new Date(toyyyyMMdd(nextDay)).getTime()
                )
                    return false;
                return true;
            },
            sceneLoaded(scene_: Scene) {
                scene.value = scene_;
                loading.close();
            },
            displayed_place_name: computed(() => {
                let names = places.map((place) => place.NAME_CHN);
                if (names[1] === names[2]) names.splice(2, 1);
                if (names[0] === names[1]) names.splice(1, 1);
                return names.join("-");
            }),
            disableFocusMode() {
                focusMode.value = false;

            },
            enableFocusMode(buslineList: number[]) {
                focusMode.value = true;
                console.log("聚焦这些线路！",buslineList);
                focusBuslines.value = buslineList;
            },
            buslineDrawerVisible : ref(false),
            onlineBusList : ref([])
        };
    },
});
</script>


<style scoped>
#aside {
    padding-left: 10px;
    padding-right: 10px;
    overflow: scroll;
    max-height: 100%;
}
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
#header {
    border-bottom: 1px solid #dcdfe6;
}
</style>