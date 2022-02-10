<template>
    <el-space direction="vertical" alignment="left">
        <p style="display: flex; align-items: center">
            <span>线路名称：</span><el-tag>{{ line_name }}</el-tag>
        </p>
      
        <p style="display: flex; align-items: center">
            <span>{{ useSeconds ? "总超速时间：" : "总超速因子" }}</span>
            <el-tag>{{ dataSum  }}</el-tag>
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
            data : stackedChartData, // [date, [number,number][]][]
            label : useSeconds ? `秒` : null,
            xAxisLabel : selectedHours
        }" />
        <!-- 
            一个排序柱状图，表达该公交线路上各公交车的超速情况
        -->
        <sorted-bar-chart 
        @clickBar="handleNodepathSortedChartClick"
        :option="{
            title : useSeconds ? `线路上各公交车的总超速时间 / 秒` : `线路上各公交车的总超速因子`,
            scope : scope,
            data : busSortedChartData
        }"/>
        <!-- 
            一个排序柱状图，表达该公交线路上各路段的超速情况
        -->
        <sorted-bar-chart 
        @clickBar="handleNodepathSortedChartClick"
        :option="{
            title : useSeconds ? `线路上各路段的总超速时间 / 秒` : `路段上各路段的总超速因子`,
            scope : scope,
            data : nodepathSortedChartData
        }"/>
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
        line_id : Number,
        OFE: String,
    },
    setup(props, context) {
        const {
            overspeedInfo,
            scope,selectedDates,selectedHours,useSeconds,line_id,OFE
        } = toRefs(props);

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

        const computer = computed(()=>useSeconds.value ? _.constant(1) : getComputer(OFE!.value!))
        const filtedInfo = computed(() => {
            const dateSet = new Set(selectedDates!.value)
            const hourSet = new Set(selectedHours!.value)
            return _(overspeedInfo!.value)
                .filter((v) => {
                    const date = v.update_time.substring(0, 10);
                    const hour = Number(v.update_time.substring(11, 13));
                    return dateSet.has(date) && hourSet.has(hour) && v.line_id === line_id!.value
            })
            .value()
        })

        const dataSum = computed( () => {
            return _(filtedInfo.value)
                .sumBy(computer.value) * scope!.value!
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
        const busSortedChartData = computed(()=>{
            return _(filtedInfo.value)
                .groupBy(v=>busInfoGetter(v.bus_id).bus_number)
                .map((v,k)=>[k, _(v).sumBy(computer.value) || 0] as [string,number])
                .sort((a,b)=>a[1]-b[1])
                .value()               
        })
        const nodepathSortedChartData = computed(()=> {
            return _(filtedInfo.value)
                .groupBy(v=>nodepathInfoGetter(v.nodepath_id).street_name)
                .map((v,k)=>[k, _(v).sumBy(computer.value) || 0] as [string,number])
                .sort((a,b)=>a[1]-b[1])
                .value()               
        })

        return {
            selectedHours,
            scope,
            useSeconds,
            stackedChartData,
            busSortedChartData,
            line_name : buslineInfoGetter(line_id!.value!).line_name,
            nodepathSortedChartData,
            dataSum
        }
    },
})
</script>

<style>

</style>