<!-- 
用于展示各实体间比较的图，这个图将放到总览中，并提供相应的clicker事件来让用户能够去选择实体
-->
<template>
    <div style="height: 500px; width: 600px" :id="elementId"></div>
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

export type sortedBarChartOption = {
    title: string;
    scope: number;
    data: Array<[string, number]>; // ["车牌号之类", value]
};
export default defineComponent({
    props: {
        option: Object as PropType<sortedBarChartOption>,
    },
    emits : [
        "clickBar"
    ],
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("大数据柱图没有获得参数！");
        }
        const realOption = computed(() => {
            return {
                title: {
                    text: option!.value!.title,
                    left: "center",
                },
                tooltip: {
                    trigger: "axis",
                    axisPointer: {
                        type: "shadow",
                    },
                },
                grid: {
                    bottom: 90,
                },
                dataZoom: [
                    {
                        type: "slider",
                        orient : "vertical",
                    },
                    {
                        type: "inside",
                        orient : "vertical",
                    },
                ],
                xAxis: {
                    splitArea: {
                        show: false,
                    },
                },
                yAxis: {
                    data: option!.value!.data.map((v) => v[0]),
                    silent: false,
                    type: 'category'
                },
                
                series: [
                    {
                        type: "bar",
                        data: option!.value!.data.map((v) => v[1] * option!.value!.scope),
                        large: true,
                    },
                ],
            };
        });
        const { chart, elementId } = useChart([realOption],
            (chart)=>{
                chart.on("click", "series.bar", function(param) {
                    if (param)
                        context.emit("clickBar", param.name)
                })
            }
        );
        
        return {
            elementId
        }
    },
});
</script>


<style>
</style>