<template>
    <el-space direction="vertical" alignment="left">
        <p style="display: flex; align-items: center">
            <span>路段名称：</span><el-tag>{{ street_name }}</el-tag>
        </p>
        <p style="display: flex; align-items: center">
            <span>{{ useSeconds ? "总超速时间：" : "总超速因子" }}</span>
            <el-tag>{{ sum }}</el-tag>
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
            title : `${street_name}` + (useSeconds ? `各日每小时的总超速时间 / 秒` : `各日每小时的总超速因子`),
            scope : scope,
            data : stackedChartData, // [date, [number,number][]][]
            label : useSeconds ? `秒` : null,
            xAxisLabel : selectedHours
        }" />
        <!-- 
            一个排序柱状图，表达该路段上各公交车的超速情况
          -->
        <sorted-bar-chart 
        @clickBar="handleNodepathSortedChartClick"
        :option="{
            title : street_name + (useSeconds ? `上各公交车的总超速时间 / 秒` : `上各公交车的总超速因子`),
            scope : scope,
            data : busSortedChartData
        }"/>
        <!-- 
            一个排序柱状图，表达该路段上各公交车的超速情况
        -->
        <!-- 
            TODO:地图显示

        -->
    </el-space>
</template>

<script lang="ts">
import _ from 'lodash';
import { computed, defineComponent, inject, PropType, Ref, toRefs } from 'vue'
import { overspeedPos } from '../../util/api'
import { getComputer } from '../util';
import newStackedAreaChart from "../charts/new-stacked-area-chart.vue"
import sortedBarChart from "../charts/sorted-bar-chart.vue"
// 接受filted的数据
function useInfos(
    overspeedInfo : Ref<overspeedPos[]>,
     computer : Ref<(overspeedPos: overspeedPos) => number>
     ) {
    const sum = computed(()=>{
        return _(overspeedInfo.value).sumBy(computer.value)
    }) 

    return {
        sum,
    }
}

export default defineComponent({
    components : {
        newStackedAreaChart, sortedBarChart
    },
    props: {
        overspeedInfo: Array as PropType<overspeedPos[]>, // 不过滤
        scope : Number,
        selectedDates: Array as PropType<string[]>,
        selectedHours: Array as PropType<number[]>,
        useSeconds: Boolean,
        street_name: String,
        nodepath_ids : Array as PropType<number[]>,
        OFE: String,
    },
    setup(props, context) {
        const {
            overspeedInfo,
            scope,selectedDates,selectedHours,useSeconds,street_name,nodepath_ids,OFE
        } = toRefs(props);
        const computer = computed(()=>useSeconds.value ? _.constant(1) : getComputer(OFE!.value!))
        const filtedInfo = computed(() => {
            const dateSet = new Set(selectedDates!.value)
            const hourSet = new Set(selectedHours!.value)
            const idSet = new Set(nodepath_ids!.value)
            return _(overspeedInfo!.value)
                .filter((v) => {
                const date = v.update_time.substring(0, 10);
                const hour = Number(v.update_time.substring(11, 13));
                return dateSet.has(date) && hourSet.has(hour) && idSet.has(v.nodepath_id);
            })
            .value()
        })
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
        const busSortedChartData = computed(()=>{
        return _(filtedInfo.value)
            .groupBy(v=>busInfoGetter(v.bus_id).bus_number)
            .map((v,k)=>[k, _(v).sumBy(computer.value) || 0] as [string,number])
            .sort((a,b)=>a[1]-b[1])
            .value()
    })
        return {
            street_name,
            ...useInfos(filtedInfo, computer),
            selectedHours,scope,useSeconds,stackedChartData,busSortedChartData
        }
    },
})
</script>

<style>

</style>