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
                    公交车超速分析
                </h1>
                <el-popover
                    placement="top-start"
                    :width="200"
                    trigger="hover"
                    content="公交车超速分析，查看特定公交车或所有公交车的超速情况。"
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
                        <span>公交车数量：</span><el-tag>{{ busCount }}</el-tag>
                    </p>

                    <p style="display: flex; align-items: center">
                        <span
                            >{{
                                useSeconds ? "每辆公交车平均超速时间" : "每辆公交车平均超速因子"
                            }}：</span
                        ><el-tag>{{ dataSum }}</el-tag
                        ><span v-if="useSeconds">&nbsp;秒</span>
                    </p>
                    
                    <p style="display: flex; align-items: center">
                        <span
                            >选择查看公交车：</span>
                            <template v-if="selectedBuses.length===0">
                                <el-tag>无</el-tag>
                            </template>
                            <template v-if="selectedBuses.length>0">
                                <el-space wrap>
                            <el-tag
                                closable
                                @close="handleDeleteTag(bus.bus_id)"
                                v-for="bus in selectedBuses"
                                :key="bus.bus_id"
                                
                            >{{bus.bus_number}}</el-tag
                        >
                        <el-button type="primary"
                        @click="confirmSelectedBus"
                        >
                        确认
                        </el-button>
                        <el-button
                        @click="selectedBuses = []"
                        >
                            清空
                        </el-button>
                        </el-space>
                        </template>
                    </p>
                    <p style="display: flex; align-items: center">
                        <item-input-selector :itemList="itemList" @handleSelect="chartClick"/>
                    </p>
                    <!-- 用一个el-input来输入？ -->
                    <sorted-bar-chart
                        :option="sortedBarChartOption"
                        @clickBar="chartClick"
                    />
                </el-space>
            </el-tab-pane>
            <el-tab-pane
                v-for="busPane in busPanes"
                :key="busPane.bus_number.join(`，`)"
                :label="busPane.bus_number.join(`，`)"
                :name="busPane.bus_number.join(`，`)"
                closable
            >
                <single-bus 
                    v-if="busPane.bus_id.length === 1" 
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :bus_id="busPane.bus_id[0]"
                    :useSeconds="useSeconds"
                    :OFE="overspeedFactorExpr"
                />
                <multi-bus 
                    v-if="busPane.bus_id.length > 1"
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :bus_ids="busPane.bus_id"
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
import { overspeedPos } from "../util/api";
import timePicker from "./time-picker.vue";
import datePicker from "./date-picker.vue";
import { getComputer, getDiffDate } from "./util";
import sortedBarChart from "./charts/sorted-bar-chart.vue";
import { sortedBarChartOption } from "./charts/sorted-bar-chart.vue";
import _, { isNaN } from "lodash";
import { ElMessageBox } from "element-plus";
import mapDisplayer from "./map-displayer.vue";
import singleBus from "./busAnalysis/single-bus.vue"
import multiBus from "./busAnalysis/multi-bus.vue"
import itemInputSelector from "../components/item-input-selector.vue"
const useInfos = function (
    overspeedInfo: Ref<overspeedPos[]>,
    selectedDates: Ref<string[]>,
    selectedTimes: Ref<number[]>,
    busInfoGetter: (bus_id: number) => {
        bus_type: string | null;
        bus_number: string | null;
    },
    useSeconds: Ref<boolean>,
    dateRange: Ref<[string, string]>,
    scope: Ref<number>,
    OFE: Ref<string>,
    busPanes: Ref<
        {
            bus_number: string[];
            bus_id: number[];
        }[]
    >,
    activePane: Ref<string>
) {
    const dateSet = computed(() => new Set(selectedDates.value));
    const timeSet = computed(() => new Set(selectedTimes.value));
    const busCount = computed(() => {
        return _(overspeedInfo!.value!)
            .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const time = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && timeSet.value.has(time);
            })
            .map((v) => v.bus_id)
            .uniq()
            .size();
    });
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
                busCount.value
        );
        if (isNaN(res)) return 0;
        return res;
    });

    const idMap = _(overspeedInfo!.value!)
        .map((v) => v.bus_id)
        .uniq()
        .map(
            (id) => {
                return [id, busInfoGetter(id)] as [
                    number,
                    { bus_type: string | null; bus_number: string | null }
                ]
            }
                
        )
        .fromPairs()
        .value();
    const name2idMap: Record<string, number> = _(idMap)
        .toPairs()
        .map((v) => [v[1].bus_number!, Number(v[0])])
        .fromPairs()
        .value();
    const sortedBarChartOption = computed<sortedBarChartOption>(() => {
        const computer = useSeconds.value
            ? _.constant(1)
            : getComputer(OFE.value);
        return {
            title: useSeconds.value
                ? "各公交车总超速时间 / 秒"
                : "各公交车总超速因子",
            scope: scope!.value,
            data: _(overspeedInfo!.value)
                .filter((v) => {
                    const date = v.update_time.substring(0, 10);
                    const time = Number(v.update_time.substring(11, 13));
                    return dateSet.value.has(date) && timeSet.value.has(time);
                })
                .groupBy((v) => idMap[v.bus_id].bus_number)
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
    const busDataMap = computed(() => {
        return _(busOverspeedInfo.value)
            .mapValues((v) => {
                const computer = useSeconds.value
                    ? _.constant(1)
                    : getComputer(OFE.value);
                return _(v).map(computer).sum() * scope.value;
            })
            .value();
    });

    function useSelector() {
        const selectedBuses = ref<
            {
                bus_id: number;
                bus_number: string}[]>([]);
        const chartClick = function (name: string) {
        // id就是id，name是车牌号
            const id = name2idMap[name];
            console.log(selectedBuses.value)
            if (!selectedBuses.value.find(v=>v.bus_id === id))
                selectedBuses.value.push({bus_id : id, bus_number : name});
        };
        const handleDeleteTag = function(bus_id : number) {
            console.log(bus_id)
            let index = -1;
            for (let i = 0; i < selectedBuses.value.length; i++) {
                if (selectedBuses.value[i].bus_id === bus_id) {
                    index = i;
                    break
                }
            }
            if (index !== -1) {
                selectedBuses.value.splice(index, 1)
            }
        }
        const confirmSelectedBus = function() { // 净空，跳转
            const resName = selectedBuses.value.map(v=>v.bus_number).join("，")
            if (busPanes.value.find(v=>v.bus_number.join("，") === resName)) { // 说明这个已经存在 
                return
            }
            busPanes.value.push({
                bus_id : selectedBuses.value.map(v=>v.bus_id), 
                bus_number : selectedBuses.value.map(v=>v.bus_number)
            })
            selectedBuses.value = []
            activePane.value = resName
        }
        return {
            selectedBuses,
            chartClick,
            handleDeleteTag,
            confirmSelectedBus
         }
    }
    return {
        busCount,
        dataSum,
        idMap,
        itemList : _(idMap).map(v=>v.bus_number).value(),
        sortedBarChartOption,
        busDataMap,
        busOverspeedInfo,
        ...useSelector(),
    };
};

const useTabs = function () {
    const busPanes = ref<
        {
            bus_id: number[];
            bus_number: string[];
        }[]
    >([]);

    const activePane = ref("perspective");
    return {
        busPanes,
        activePane,
        removeTab(name: string) {
            // 根据name找到index，使用splice删除
            let index = -1;
            for (let i = 0; i < busPanes.value.length; i++) {
                if (busPanes.value[i].bus_number.join(`，`) === name) {
                    index = i;
                    break;
                }
            }
            if (index === -1) return;
            busPanes.value.splice(index, 1);
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
    components: { timePicker, datePicker, sortedBarChart, mapDisplayer ,singleBus,multiBus, itemInputSelector},
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

        const { busPanes, activePane, removeTab } = useTabs();
        return {
            dateRange,
            selectedDates,
            selectedHours,
            busPanes,
            activePane,
            removeTab,
            overspeedInfo,
            overspeedFactorExpr,
            ...useInfos(
                overspeedInfo as Ref,
                selectedDates,
                selectedHours,
                busInfoGetter,
                useSeconds,
                dateRange,
                scope as Ref,
                overspeedFactorExpr as Ref,
                busPanes,
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