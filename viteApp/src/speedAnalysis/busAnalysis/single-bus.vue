<template>
    <el-space direction="vertical" alignment="left">
        <p style="display: flex; align-items: center">
            <span>车牌号：</span><el-tag>{{ bus_number }}</el-tag>
        </p>
        <p style="display: flex; align-items: center">
            <span>车型：</span><el-tag>{{ bus_type }}</el-tag>
        </p>
        <p style="display: flex; align-items: center">
            <span>所在公交车线路：</span>
            <el-tag
            v-for="name in buslinesName"
            :key="name"
            >{{name}}</el-tag>
        </p>
        <p style="display: flex; align-items: center">
            <span>{{ useSeconds ? "总超速时间：" : "总超速因子：" }}</span>
            <el-tag>{{ sum }}</el-tag>
            <span v-if="useSeconds">&nbsp;秒</span>
        </p>
        <p style="display: flex; align-items: center">
            <span>{{ useSeconds ? "每日平均超速时间：" : "每日平均超速因子" }}</span>
            <el-tag>{{ average }}</el-tag>
            <span v-if="useSeconds">&nbsp;秒</span>
        </p>
        <!-- 
             一个折线图，表达公交车各小时的超速情况...但还是不要了，和堆叠面积图功能重了
        <basic-line-chart :option="{
            title: useSeconds ? `每日每小时的总超速时间 / 秒` : `每日每小时的总超速因子`,
            scope : scope,
            label : useSeconds ? `秒` : null,
            data : [[bus_number, basicLineChartData]],
            xAxisLabel : selectedHours
        }"/>
        -->
        <!-- 
            一个堆叠面积图，展示各日每个小时该实体的超速情况
        -->
        <new-stacked-area-chart :option="{
            title : useSeconds ? `各日每小时的总超速时间 / 秒` : `各日每小时的总超速因子`,
            scope : scope,
            data : stackedChartData,
            label : useSeconds ? `秒` : null,
            xAxisLabel : selectedHours
        }" />
        <!-- 
            一个排序柱状图，表达公交车在**各路段**上的超速情况。
        -->
        <sorted-bar-chart 
        @clickBar="handleNodepathSortedChartClick"
        :option="{
            title : useSeconds ? `公交车在各路段的总超速时间 / 秒` : `公交车在各路段的总超速因子`,
            scope : scope,
            data : nodepathSortedChartData
        }"/>

        <!-- 
            TODO:地图显示

        -->
    </el-space>
</template>

<script lang="ts">
import _ from "lodash";
import { computed, defineComponent, inject, PropType, ref, Ref, toRefs ,watch} from "vue";
import { overspeedPos } from "../../util/api";
import basicLineChart from "../charts/basic-line-chart.vue";
import { getComputer } from "../util";
import stackedAreaChart from "../charts/stacked-area-chart.vue"
import newStackedAreaChart from "../charts/new-stacked-area-chart.vue"
import sortedBarChart from "../charts/sorted-bar-chart.vue"
import { ElMessage } from "element-plus";
function someInfos(
    overspeedInfo: Ref<overspeedPos[]>,
    bus_id: Ref<number>,
    selectedDates: Ref<string[]>,
    selectedHours: Ref<number[]>,
    computer : Ref<(overspeedPos:overspeedPos)=>number>,
    nodepathInfoGetter : (nodepath_id: string) => {
        street_name: string | null;
        speed_limit: number | null;
    },
    buslineInfoGetter : (line_id: number) => {
        line_name: string | null;
    }
) {
    const dateSet = computed(() => new Set(selectedDates.value));
    const hourSet = computed(() => new Set(selectedHours.value));
    const filtedInfo = computed(()=>{
        return _(overspeedInfo.value)
            .filter(v=>{
                const date = v.update_time.substring(0, 10);
                const hour = Number(v.update_time.substring(11, 13));
                return v.bus_id === bus_id.value
                && dateSet.value.has(date) && hourSet.value.has(hour);
            })
            .value()
    })
    const sum = computed(()=>{
        return _(filtedInfo.value).sumBy(computer.value)
    })
    const average = computed(()=>{
        return sum.value / selectedDates.value.length
    })

    const basicLineChartData = computed(()=>{
        const res = _(filtedInfo.value) // 已经被筛选过了
            .groupBy(v=>v.update_time.substring(11,13))
            .mapValues(v=>_(v).sumBy(computer.value) || 0)
            .value()
        return _.range(0,24).map(i=>[i, res[i] || 0])
    })

    const stackedChartData = computed(()=>{
        return _(filtedInfo.value)
            .groupBy(v=>v.update_time.substring(0, 10))
            .map((v,k)=>{
                const res = _(v)
                    .groupBy(v=>v.update_time.substring(11,13))
                    .mapValues(v=>_(v).sumBy(computer.value) || 0)
                    .value()
                
                return [k, _.range(0,24).map(i=>res[i] || 0)] as [string, (number | null)[]]
            })
            .value()
    })

    const nodepathSortedChartData = computed(()=>{
        return _(filtedInfo.value)
            .groupBy(v=>nodepathInfoGetter(v.nodepath_id+"").street_name)
            .map((v,k)=>[k, _(v).sumBy(computer.value) || 0] as [string,number])
            .sort((a,b)=>a[1]-b[1])
            .value()
    })
    const handleNodepathSortedChartClick = function(name : string) {
        ElMessage.error(`点击${name}，该功能待实现`);
        // TODO
    }
    const buslinesName = computed(()=>{
        return _(filtedInfo.value)
            .map(v=>v.line_id)
            .uniq()
            .map(v=>buslineInfoGetter(v).line_name || ("未找到的ID："+v))
            .value()
    })
    return {
        basicLineChartData,
        stackedChartData,
        nodepathSortedChartData,
        handleNodepathSortedChartClick,
        sum,
        buslinesName,
        average
    }
}

export default defineComponent({
    components: {basicLineChart, newStackedAreaChart, sortedBarChart},
    props: {
        overspeedInfo: Array as PropType<overspeedPos[]>, // 不过滤
        scope : Number,
        selectedDates: Array as PropType<string[]>,
        selectedHours: Array as PropType<number[]>,
        useSeconds: Boolean,
        bus_id: Number,
        OFE: String,
    },
    setup(props, context) {
        const {
            overspeedInfo, 
            selectedDates,
            selectedHours,
            useSeconds,
            OFE,
            scope,
            bus_id,
        } = toRefs(props);
        const busInfoGetter = inject("busInfoGetter") as (bus_id: number) => {
            bus_type: string | null;
            bus_number: string | null;
        };
        const nodepathInfoGetter = inject("nodepathInfoGetter") as (
            nodepath_id: string
        ) => {
            street_name: string | null;
            speed_limit: number | null;
        };
        const buslineInfoGetter = inject("buslineInfoGetter") as ((line_id: number)=>{
                line_name: string | null;
        })
        
        const computer = computed(()=>{
            return useSeconds.value ? _.constant(1) : getComputer(OFE!.value!)
        })

        return {
            busInfoGetter,
            nodepathInfoGetter,
            overspeedInfo,
            selectedDates,
            selectedHours,
            useSeconds,
            OFE,
            bus_id,
            scope,
            computer,
            ...someInfos(overspeedInfo as Ref,
            bus_id as Ref,
            selectedDates as Ref,
            selectedHours as Ref,
             computer,
             nodepathInfoGetter,
             buslineInfoGetter
             ),
            ...busInfoGetter(bus_id!.value!)
        };
    },
});
</script>

<style>
</style>