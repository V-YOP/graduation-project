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
                    公交线路超速分析
                </h1>
                <el-popover
                    placement="top-start"
                    :width="200"
                    trigger="hover"
                    content="公交线路超速分析，查看特定公交车或各公交线路的超速情况。"
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
                    <time-picker v-model="selectedHours" />
                </el-space>
            </el-col>
        </el-row>
    </el-header>
    <el-main id="main">
        <el-tabs v-model="activePane" @tab-remove="removeTab">
            <el-tab-pane name="perspective" :closable="false">
                <template #label>
                    <span>总览</span>
                </template>
                <el-space direction="vertical" alignment="left">
                    <p style="display: flex; align-items: center">
                        <span>线路数量：</span><el-tag>{{ buslineCount }}</el-tag>
                    </p>
                    
                    <p style="display: flex; align-items: center">
                        <span
                            >选择查看线路：</span>
                            <template v-if="selectedBuslines.length===0">
                                <el-tag>无</el-tag>
                            </template>
                            <template v-if="selectedBuslines.length > 0">
                                <el-space wrap>
                            <el-tag
                                closable
                                @close="handleDeleteTag(busline)"
                                v-for="busline in selectedBuslines"
                                :key="busline"
                            >{{busline}}</el-tag
                        >
                        <el-button type="primary"
                        @click="confirmSelectedBusline"
                        >
                        确认
                        </el-button>
                        <el-button
                        @click="selectedBuslines = []"
                        >
                            清空
                        </el-button>
                        </el-space>
                        </template>
                    </p>
                    <p style="display: flex; align-items: center">
                        <item-input-selector :itemList="itemList" @handleSelect="chartClick"/>
                    </p>
                     <el-space  direction="horizontal" alignment="top" :size="10">
                    <sorted-bar-chart
                        :option="sortedBarChartOption"
                        @clickBar="chartClick"
                    />
                    <busline-displayer 
                    :allInfo="mergedInfo"
                    @clickBar="chartClick"
                    :buslineOverspeedValue="buslineOverspeedValue"
                    :label="useSeconds ? `秒` : null"
                    />
                     </el-space>
                </el-space>
            </el-tab-pane>
            <el-tab-pane
                v-for="buslinePane in buslinePanes"
                :key="buslinePane.join(`，`)"
                :label="buslinePane.join(`，`)"
                :name="buslinePane.join(`，`)"
                closable
            >
                <single-busline
                    v-if="buslinePane.length === 1" 
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :line_id="name2idMap[buslinePane[0]]"
                    :useSeconds="useSeconds"
                    :OFE="overspeedFactorExpr"
                />
                <multi-busline
                    v-if="buslinePane.length > 1"
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :line_ids="buslinePane.map(v=>name2idMap[v])"
                    :useSeconds="useSeconds"
                    :OFE="overspeedFactorExpr" 
                />
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
    toRefs,
} from "vue";
import { AllInfo, overspeedPos } from "../util/api";
import timePicker from "./time-picker.vue";
import datePicker from "./date-picker.vue";
import { getComputer, getDiffDate } from "./util";
import sortedBarChart from "./charts/sorted-bar-chart.vue";
import { sortedBarChartOption } from "./charts/sorted-bar-chart.vue";
import _, { isNaN } from "lodash";
import { ElMessageBox } from "element-plus";
import mapDisplayer from "./map-displayer.vue";
import multiBusline from "./buslineAnalysis/multi-busline.vue"
import singleBusline from "./buslineAnalysis/single-busline.vue"
import buslineDisplayer from "./baidu-map-display/busline-displayer.vue"
import itemInputSelector from "../components/item-input-selector.vue"
const useInfos = function (
    overspeedInfo: Ref<overspeedPos[]>,
    selectedDates: Ref<string[]>,
    selectedTimes: Ref<number[]>,
    buslineInfoGetter: (line_id: number)=>{
        line_name: string | null;
    },
    useSeconds: Ref<boolean>,
    dateRange: Ref<[string, string]>,
    scope: Ref<number>,
    OFE: Ref<string>,
    buslinePanes: Ref<string[][]>,
    activePane: Ref<string>
) {
    const dateSet = computed(() => new Set(selectedDates.value));
    const timeSet = computed(() => new Set(selectedTimes.value));
    const buslineCount = computed(() => {
        return _(overspeedInfo!.value!)
            .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const time = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && timeSet.value.has(time);
            })
            .map((v) => v.line_id)
            .uniq()
            .size();
    });
    console.log(buslineCount.value)
    // 平均总的超速时间或超速因子
    const dataSum = computed(() => {
        const res = Math.floor(
            (_(overspeedInfo!.value!)
                .filter((v) => {
                    const date = v.update_time.substring(0, 10);
                    const time = Number(v.update_time.substring(11, 13));
                    return dateSet.value.has(date) && timeSet.value.has(time);
                })
                .map(useSeconds.value ? _.constant(1) : getComputer(OFE.value))
                .sum() *
                scope.value) /
                buslineCount.value
        );
        if (isNaN(res)) return 0;
        return res;
    });
    const idMap = _(overspeedInfo!.value!)
        .map((v) => v.line_id)
        .uniq()
        .map(
            (id) => [id, buslineInfoGetter(id).line_name] as [number, string | null])
        .fromPairs()
        .value();
    const name2idMap: Record<string, number> = 
        _(idMap)
            .map((v, k) => [v , Number(k)] as [string | null, number]) 
            .filter(v=>v[0]!==null)
            .fromPairs()
            .value();
    const sortedBarChartOption = computed<sortedBarChartOption>(() => {
        const computer = useSeconds.value
            ? _.constant(1)
            : getComputer(OFE.value);
        return {
            title: useSeconds.value
                ? "各公交线路总超速时间 / 秒"
                : "各公交线路总超速因子",
            scope: scope!.value,
            data: _(overspeedInfo!.value)
                .filter((v) => {
                    const date = v.update_time.substring(0, 10);
                    const time = Number(v.update_time.substring(11, 13));
                    return dateSet.value.has(date) && timeSet.value.has(time);
                })
                .groupBy((v) => idMap[v.line_id])
                .map((v, k) => {
                    return [k, _(v).map(computer).sum()] as [string, number];
                })
                .sort((a, b) => a[1] - b[1])
                .value(),
        };
    });


    const busOverspeedInfo = computed(() => {
        return _(overspeedInfo!.value)
            .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const time = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && timeSet.value.has(time);
            })
            .groupBy((v) => v.bus_id)
            .value();
            
    });
    const buslineOverspeedValue = computed(()=>{
        const computer = useSeconds.value
            ? _.constant(1)
            : getComputer(OFE.value);
        return _(overspeedInfo!.value)
            .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const time = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && timeSet.value.has(time);
            })
            .groupBy(v=>v.line_id)
            .mapValues(v=>_(v).sumBy(computer) * scope.value)
            .value()
    })

    function useSelector() {
        const selectedBuslines = ref<
            string[]>([]);
        const chartClick = function (name: string) {
            console.log(name)
            if (selectedBuslines.value.indexOf(name) === -1) // 如果没有
                selectedBuslines.value.push(name);
        };
        const handleDeleteTag = function(name : string) {
            let index = -1;
            for (let i = 0; i < selectedBuslines.value.length; i++) {
                if (selectedBuslines.value[i] === name) {
                    index = i;
                    break
                }
            }
            if (index !== -1) {
                selectedBuslines.value.splice(index, 1)
            }
        }
        const confirmSelectedBusline = function() { // 净空，跳转
            const resName = selectedBuslines.value.join("，")
            if (buslinePanes.value.find(v=>v.join("，") === resName)) { // 说明这个已经存在 
                return
            }
            buslinePanes.value.push(selectedBuslines.value)
            selectedBuslines.value = []
            activePane.value = resName
        }
        return {
            selectedBuslines,
            chartClick,
            handleDeleteTag,
            confirmSelectedBusline
         }
    }
    return {
        buslineCount,
        dataSum,
        idMap,
        itemList : _(idMap).map().filter(v=>v!==null).value(),
        sortedBarChartOption,
        busOverspeedInfo,
        ...useSelector(),
        name2idMap,
        buslineOverspeedValue
    };
};

const useTabs = function () {
    const buslinePanes = ref<
        string[][]
    >([]);

    const activePane = ref("perspective");
    return {
        buslinePanes,
        activePane,
        removeTab(name: string) {
            // 根据name找到index，使用splice删除
            let index = -1;
            for (let i = 0; i < buslinePanes.value.length; i++) {
                if (buslinePanes.value[i].join(`，`) === name) {
                    index = i;
                    break;
                }
            }   
            if (index === -1) return;
            buslinePanes.value.splice(index, 1);
            if (activePane.value === name) {
                // 关闭的恰好是当前页，回到主页
                activePane.value = "perspective";
            }
        },
    };
};

const usePanes = function() {
    
}

export default defineComponent({
    components: { timePicker, datePicker, sortedBarChart, mapDisplayer ,multiBusline,singleBusline, buslineDisplayer, itemInputSelector},
    props: {
        overspeedInfo: Object as PropType<overspeedPos[]>,
        scope: Number,
        overspeedFactorExpr: String,
        infoGetter: Function as PropType<
            (overspeedPos: overspeedPos) => {
                street_name: string | null;
                speed_limit: number | null;
                bus_number: string | null;
                bus_type: string | null;
            }
        >,
    },
    setup(props, context) {
        const { overspeedInfo, scope, overspeedFactorExpr, } =
            toRefs(props);
        const dateRange = ref(inject("dateRange") as [string, string]);
        const selectedDates = ref<string[]>(getDiffDate(...dateRange.value));
        const selectedHours = ref(_.range(0, 24));
        const useSeconds = ref(true);

        const busInfoGetter = inject("busInfoGetter") as (bus_id: number) => {
            bus_type: string | null;
            bus_number: string | null;
        };
        const nodepathInfoGetter = inject("nodepathInfoGetter") as (nodepath_id: number) => {
            street_name: string | null;
            speed_limit: number | null;
        };
        const buslineInfoGetter = inject("buslineInfoGetter") as (line_id: number)=>{
                line_name: string | null;
        }

        const { buslinePanes, activePane, removeTab } = useTabs();
        const mergedInfo = inject("mergedInfo") as Ref<AllInfo>
        return {
            mergedInfo,
            dateRange,
            selectedDates,
            selectedHours,
            buslinePanes,
            activePane,
            removeTab,
            overspeedInfo,
            overspeedFactorExpr,
            ...useInfos(
                overspeedInfo as Ref,
                selectedDates,
                selectedHours,
                buslineInfoGetter,
                useSeconds,
                dateRange,
                scope as Ref,
                overspeedFactorExpr as Ref,
                buslinePanes,
                activePane
            ),
            useSeconds,
        };
    },
});
</script>


<style scoped>
#main {
    padding-left: 50px;
}
</style>