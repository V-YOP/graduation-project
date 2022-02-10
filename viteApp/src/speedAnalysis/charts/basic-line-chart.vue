<!-- 展示某实体或多实体的各时间段超速情况的折线图 -->
<template>
    <div style="height: 500px; width: 1100px" :id="elementId"></div>
</template>

<script lang="ts">
import {
    computed,
    defineComponent,
    onMounted,
    PropType,
    Ref,
    ref,
    shallowRef,
    toRefs,
    watch,
} from "vue";
import * as echarts from "echarts";
import _ from "lodash";
import { useChart } from "../useChart";

export type basicLineChartOption = {
    title: string,
    data: [string, [number,number][]][] // x轴是时间段（小时  // TODO
    scope: number,
    label : "秒" | null,
    xAxisLabel : number[],
};

export default defineComponent({
    props: {
        option: Object as PropType<basicLineChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("简单折线图的参数没有得到");
        }
        console.log(option.value)
        const realOption = computed(() => {
            const datas = _(option.value!.data);
            function hour2Str(i : number) {
                return `${_.padStart(""+i, 2, "0")}:00-${_.padStart(""+(i+1), 2, "0")}:00`
            }
            return {
                title: {
                    top: 30,
                    left: "center",
                    text: option.value!.title,
                },
                xAxis: {
                    type: 'category',
                    data: option.value!.xAxisLabel.map(hour2Str),
                    splitArea: {
                        show: true
                    },
                    axisLabel : {
                        rotate : 45
                    }
                },
                yAxis: {
                    type: 'value',
                    name : option!.value!.label
                },
                tooltip : {
                    trigger : "axis"
                },
                legend : {
                    show : true,
                    data : datas.map(v=>v[0]).value()
                },
                series: datas.map(v=>{
                    return {
                        type: 'line',
                        smooth: true,
                        name : v[0],
                        data : v[1].map(v=>[hour2Str(v[0]),Math.floor(v[1]* option.value!.scope!)])
                    }
                }).value()


            } 
        });
        watch(realOption,function (val){console.log(val)})
        const { chart ,elementId } = useChart([realOption]);
        return {elementId}
    },
});
</script>


<style>
</style>