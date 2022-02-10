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
                    路段超速分析
                </h1>
                <el-popover
                    placement="top-start"
                    :width="200"
                    trigger="hover"
                    content="路段超速分析，查看所有路段或特定路段的超速情况。"
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
        <el-tabs  v-model="activePane" @tab-remove="removeTab">
            <el-tab-pane name="perspective" :closable="false">
                <template #label>
                    <span>总览</span>
                </template>
                <el-space direction="vertical" alignment="left">

                    <p style="display: flex; align-items: center">
                        <span>存在超速的路段数：</span><el-tag>{{nodepathCount}}</el-tag>
                    </p>
                    <p style="display: flex; align-items: center">
                        <span
                            >选择查看的路段：</span>
                            <template v-if="selectedNodepaths.length===0">
                                <el-tag>无</el-tag>
                            </template>
                            <template v-if="selectedNodepaths.length>0">
                                <el-space wrap>
                            <el-tag
                                closable
                                @close="handleDeleteTag(street_name)"
                                v-for="street_name in selectedNodepaths"
                                :key="street_name"
                                
                            >{{street_name}}</el-tag
                        >
                        <el-button type="primary"
                        @click="confirmSelectedNodepath"
                        >
                        确认
                        </el-button>
                        <el-button
                        @click="selectedNodepaths = []"
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
                    <nodepath-station-displayer 
                    :allInfo="mergedInfo"
                    @clickBar="chartClick"
                    :nodepathOverspeedValue="nodepathOverspeedValue"
                    :label="useSeconds ? `秒` : null"
                     />
                    
                     </el-space>
                </el-space>
            </el-tab-pane>
            <el-tab-pane
                v-for="nodepathPane in nodepathPanes"
                :key="nodepathPane.join(`，`)"
                :label="nodepathPane.join(`，`)"
                :name="nodepathPane.join(`，`)"
                closable
            >
                <single-nodepath 
                    v-if="nodepathPane.length === 1" 
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :street_name="nodepathPane[0]"
                    :nodepath_ids="name2IdMap[nodepathPane[0]]"
                    :useSeconds="useSeconds"
                    :OFE="overspeedFactorExpr"
                />
                <multi-nodepath 
                    v-if="nodepathPane.length > 1"
                    :overspeedInfo="overspeedInfo"
                    :scope="scope"
                    :selectedDates="selectedDates"
                    :selectedHours="selectedHours"
                    :street_names="nodepathPane"
                    :nodepath_ids="nodepathPane.map(v=>[v,name2IdMap[v]])"
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
import sortedBarChart from "./charts/sorted-bar-chart.vue"
import {sortedBarChartOption} from  "./charts/sorted-bar-chart.vue"
import singleNodepath from "./nodepathAnalysis/single-nodepath.vue"
import multiNodepath from "./nodepathAnalysis/multi-nodepath.vue"
import nodepathStationDisplayer from "./baidu-map-display/nodepath-station-displayer.vue"
import _ from "lodash";
import itemInputSelector from "../components/item-input-selector.vue"
const useInfos = function (
    overspeedInfo: Ref<overspeedPos[]>,
    selectedDates: Ref<string[]>,
    selectedTimes: Ref<number[]>,
    nodepathInfoGetter: (bus_id: number) => {
        street_name: string | null;
        speed_limit: number | null;
    },
    useSeconds : Ref<boolean>,
    dateRange : Ref<[string, string]>,
    scope : Ref<number>,
    OFE : Ref<string>,
    nodepathPanes: Ref<
        string[][]
    >,
    activePane: Ref<string>
) {
    const dateSet = computed(()=>new Set(selectedDates.value));
    const timeSet = computed(()=>new Set(selectedTimes.value));
    const filtedInfo = computed(()=>{
        return _(overspeedInfo!.value!)
            .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const time = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && timeSet.value.has(time);
            })
            .value()
    })
    const nodepathId2NameMap = computed(() => {
        return _(filtedInfo.value)
            .map(v=>v.nodepath_id)
            .uniq()
            .map(id=>[id,nodepathInfoGetter(id).street_name] as [number,string])
            .fromPairs()
            .value()
    })
    // 通过street_name获取nodepath_ids的Record
    const name2IdMap = computed(()=>{
        return _(filtedInfo.value)
            .map(v=>v.nodepath_id)
            .uniq()
            .map(v=>[v, nodepathId2NameMap.value[v]] as [number,string])
            .groupBy(v=>v[1])
            .mapValues(v=>v.map(j=>j[0]))
            .value()
    })
    const nodepathCount = computed(() => {
        return _(filtedInfo.value)
            .map(v=>nodepathId2NameMap.value[v.nodepath_id])
            .uniq()
            .size()
    });
    const computer = computed(()=>useSeconds.value ? _.constant(1) : getComputer(OFE.value));
    // 平均总的超速时间或超速因子
    const sortedBarChartOption = computed<sortedBarChartOption>(()=>{
        return {
            title : useSeconds.value ? "各路段总超速时间 / 秒" : "各路段总超速因子",
            scope : scope!.value,
            data : _(filtedInfo.value)
                .groupBy(v=>nodepathId2NameMap.value[v.nodepath_id])
                .map((v,k)=>{
                    return [k,_(v).sumBy(computer.value)] as [string,number]
                })
                .sort((a,b)=>a[1]-b[1])
                .value()
        }
    })
    
    function useSelector() {
        const selectedNodepaths= ref<string[]>([]);
        const chartClick = function (name: string) {
        // name是路径名称，应当根据路径名称获取所有id并返回
            console.log(name)
            if (selectedNodepaths.value.indexOf(name) === -1)
                selectedNodepaths.value.push(name);
        };
        const handleDeleteTag = function(street_name : string) {
            let index = -1;
            for (let i = 0; i < selectedNodepaths.value.length; i++) {
                if (selectedNodepaths.value[i] === street_name) {
                    index = i;
                    break
                }
            }
            if (index !== -1) {
                selectedNodepaths.value.splice(index, 1)
            }
        }
        const confirmSelectedNodepath = function() { // 净空，跳转
            /*
            if (busPanes.value.find(v=>v.bus_number.join("，") === resName)) { // 说明这个已经存在 
                return
            }
            busPanes.value.push({
                bus_id : selectedBuses.value.map(v=>v.bus_id), 
                bus_number : selectedBuses.value.map(v=>v.bus_number)
            })
            selectedBuses.value = []
            activePane.value = resName
            */
           const resName = selectedNodepaths.value.join("，")
            if (nodepathPanes.value.find(v=>v.join("，") === resName)) { // 说明这个已经存在 
                return
            }
            nodepathPanes.value.push(selectedNodepaths.value)
            console.log(nodepathPanes)
            selectedNodepaths.value = []
            activePane.value = resName
        }
        return {
            selectedNodepaths,
            chartClick,
            handleDeleteTag,
            confirmSelectedNodepath,
            name2IdMap,
            itemList : _(nodepathId2NameMap.value).map().filter(v=>v!==null).value()
         }
    }
    const nodepathOverspeedValue = computed(()=>{
            return _(filtedInfo.value)
                .groupBy(v=>v.nodepath_id)
                .mapValues(v=>_(v).sumBy(computer.value) * scope.value)
                .value()
        })
    return {
        nodepathCount, 
    sortedBarChartOption,
    ...useSelector(),
    nodepathOverspeedValue,

    };
};

const useTabs = function () {
    const nodepathPanes = ref<
        string[][]
    >([]);
    const activePane = ref("perspective");
    return {
        nodepathPanes,
        activePane,
        removeTab(name: string) {
            // 根据name找到index，使用splice删除
            let index = -1;
            for (let i = 0; i < nodepathPanes.value.length; i++) {
                if (nodepathPanes.value[i].join(`，`) === name) {
                    index = i;
                    break;
                }
            }
            if (index === -1) return;
            nodepathPanes.value.splice(index, 1);
            if (activePane.value === name) {
                // 关闭的恰好是当前页，回到主页
                activePane.value = "perspective";
            }
        },
    };
};


export default defineComponent({
    components: { timePicker, datePicker, sortedBarChart, singleNodepath, multiNodepath, nodepathStationDisplayer ,itemInputSelector },
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
        const { overspeedInfo, scope, infoGetter, overspeedFactorExpr } =
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
        const buslineInfoGetter = inject("buslineInfoGetter") as ((line_id: number)=>{
                line_name: string | null;
        })
        const mergedInfo = inject("mergedInfo") as Ref<AllInfo>
        const {activePane, nodepathPanes, removeTab} = useTabs()

        
        return {
            mergedInfo,
            dateRange,
            selectedDates,
            selectedHours,
            ...useInfos(
                overspeedInfo as Ref,
                selectedDates,
                selectedHours,
                nodepathInfoGetter,
                useSeconds,
                dateRange,
                scope as Ref,
                overspeedFactorExpr as Ref,
                nodepathPanes,
                activePane
            ),
            useSeconds,
            overspeedInfo,
            activePane, nodepathPanes, removeTab
        };
    },
});
</script>


<style scoped>
#main {
    padding-left: 50px;
}
</style>