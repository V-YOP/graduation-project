<!-- 
整个地区信息的展示，
-->
<template>
    <el-header style="border: solid 1px #e6e6e6; padding: 0; margin: 0">
        <el-row
            type="flex"
            style="width: 100%; height: 100%; align-items: center"
        >
            <el-col :span="12" style="display: flex; align-items: center">
                <h1
                    style="
                        font-size: 30px;
                        padding: 0;
                        margin: 0;
                        padding-left: 20px;
                    "
                >
                    地区超速分析
                </h1>
                <el-popover
                    placement="top-start"
                    :width="200"
                    trigger="hover"
                    content="地区超速分析页面，查看整个地区各时间段的超速情况"
                >
                    <template #reference>
                        <el-button
                            type="text"
                            icon="el-icon-info"
                            style="font-size: 20px; margin-left: 10px"
                        ></el-button>
                    </template>
                </el-popover>
            </el-col>
            <el-col :span="2"> </el-col>
            <el-col :span="10">
                <el-space>
                    <el-switch
                        style="display: block; float: right"
                        v-model="useSeconds"
                        active-text="超速时间"
                        inactive-text="超速因子"
                    ></el-switch>
                    <date-picker
                        :dateRange="dateRange"
                        v-model="selectedDates"
                    />
                    <time-picker v-model="selectedTimes" />
                </el-space>
            </el-col>
        </el-row>
    </el-header>
    <el-main id="main">
        <el-tabs>
            <el-tab-pane>
                <template #label>
                    <span>总览</span>
                </template>
                <el-space direction="vertical" alignment="left">
                    <p style="display: flex; align-items: center">
                        <span
                            >{{
                                useSeconds ? "总超速时间" : "总超速因子"
                            }}：</span
                        ><el-tag>{{ allDataSum }}</el-tag
                        ><span v-if="useSeconds">&nbsp;秒</span>
                    </p>
                </el-space>
                <stacked-area-chart :option="stackedAreaChartOption" />
                       
            </el-tab-pane>
            <el-tab-pane label="地图展示" name="second" lazy>
                <map-displayer :overspeedInfo="overspeedInfo" />
            </el-tab-pane>
        </el-tabs>
    </el-main>
</template>

<script lang="ts">
import {
    computed,
    defineComponent,
    inject,
    PropType,
    Ref,
    ref,
    shallowRef,
    toRefs,
} from "vue";
import { overspeedPos } from "../util/api";
import { countByDayAndHour, getComputer, getDiffDate } from "./util";
import stackedAreaChart, {
    stackedAreaChartOption,
} from "./charts/stacked-area-chart.vue";
import _ from "lodash";
import datePicker from "./date-picker.vue";
import timePicker from "./time-picker.vue";
import mapDisplayer from "./map-displayer.vue"
import { Scene } from "@antv/l7";
function useStackedAreaChartOption(
    overspeedInfo: Ref<overspeedPos[]>,
    scope: Ref<number>,
    dateRange: Ref<[string, string]>,
    selectedDates: Ref<string[]>,
    selectedTimes : Ref<number[]>,
    useSeconds: Ref<boolean>,
    OFE: Ref<string>
) {
    return {
        stackedAreaChartOption: computed<stackedAreaChartOption>(() => {
            const computer = useSeconds.value
                ? _.constant(1)
                : getComputer(OFE.value);
            const dateSet = new Set(selectedDates.value)
            const timeSet = new Set(selectedTimes.value)
            return {
                title: useSeconds.value
                    ? "各日中各小时超速时间堆叠面积图 / 秒"
                    : "各日中各小时总超速因子堆叠面积图",
                scope: scope.value,
                data: _(
                    countByDayAndHour(
                        overspeedInfo.value,
                        dateRange.value,
                        computer
                    )
                )
                    .toPairs()
                    .filter((v) => dateSet.has(v[0]))
                    .map(v=>{ // 筛选hours
                        const newSeries = v[1].map((val,index)=>{
                            if (timeSet.has(index)) {
                                return val
                            }
                            return null
                        })
                        console.log(newSeries)
                        return [v[0], newSeries] as [string, (number | null)[]]
                    })
                    .value(),
            };
        }),
    };
}

function useInfos(
    overspeedInfo_: Ref<overspeedPos[]>,
     scope: Ref<number>,
         selectedDates: Ref<string[]>,
    selectedTimes : Ref<number[]>,
     useSeconds: Ref<boolean>,
    OFE: Ref<string>,
) {
     const allDataSum = computed(()=>{
         const computer = useSeconds.value
                ? _.constant(1)
                : getComputer(OFE.value);
            const dateSet = new Set(selectedDates.value)
            const timeSet = new Set(selectedTimes.value)
        return Math.floor(_(overspeedInfo_.value)
            .filter((v)=>{
                const date = v.update_time.substring(0,10)
                const hour = Number(v.update_time.substring(11,13))
                return dateSet.has(date) && timeSet.has(hour)
            })
            .sumBy(computer) * scope.value)
     })
    const overspeedInfo = computed(()=>{
        const dateSet = new Set(selectedDates.value)
        const timeSet = new Set(selectedTimes.value)
        return overspeedInfo_!.value
            .filter((v)=>{
                const date = v.update_time.substring(0,10)
                const hour = Number(v.update_time.substring(11,13))
                return dateSet.has(date) && timeSet.has(hour)
            })
    })
     return {
         allDataSum,
         overspeedInfo
     }
}

export default defineComponent({
    components: {
        stackedAreaChart,
        datePicker,
        timePicker,
        mapDisplayer
    },
    props: {
        overspeedInfo: Object as PropType<overspeedPos[]>,
        scope: Number,
        overspeedFactorExpr: String,
        infoGetter: Function as PropType<
            (overspeedPos: overspeedPos) => {
                street_name: string;
                speed_limit: number;
                bus_number: string;
                bus_type: string;
            }
        >,
    },
    setup(props, context) {
        const { overspeedInfo, scope, infoGetter, overspeedFactorExpr } =
            toRefs(props);
        const dateRange = ref(inject("dateRange") as [string, string]);
        const selectedDates = ref<string[]>(getDiffDate(...dateRange.value));
        const selectedTimes = ref(_.range(0,24))
        const useSeconds = ref(true);
        return {
            useSeconds,
            selectedDates,
            dateRange,
            selectedTimes,
            ...useStackedAreaChartOption(
                overspeedInfo as Ref,
                scope as Ref,
                dateRange,
                selectedDates,
                selectedTimes,
                useSeconds,
                overspeedFactorExpr as Ref
            ),
            ...useInfos(
                overspeedInfo as Ref,
                scope as Ref,
                selectedDates,
                selectedTimes,
                useSeconds,
                overspeedFactorExpr as Ref
            ),
        };
    },
});
</script>


<style scoped>
#main {
    padding-left: 50px;
}
</style>