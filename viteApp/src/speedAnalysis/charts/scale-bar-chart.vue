<template>
    <div style="height: 500px; width: 700px" :id="elementId"></div>
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

export type scaleBarChartOption = {
    title: string;
    scope: number;
    data: Array<[string, number]>; // ["yyyy-MM-dd hh:mm:ss",data][]
};
export default defineComponent({
    props: {
        option: Object as PropType<scaleBarChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("大数据柱图没有获得参数！");
        }
        console.log(option.value)
        const realOption = computed(() => {
            return {
                title: {
                    text: option!.value!.title,
                    left: "center",
                },
                toolbox: {
                    feature: {
                        dataZoom: {
                            yAxisIndex: false,
                        },
                        saveAsImage: {
                            pixelRatio: 2,
                        },
                    },
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
                        type: "inside",
                    },
                    {
                        type: "slider",
                    },
                ],
                xAxis: {
                    data: option!.value!.data.map((v) => v[0]),
                    silent: false,
                    splitLine: {
                        show: false,
                    },
                    splitArea: {
                        show: false,
                    },
                },
                yAxis: {
                    splitArea: {
                        show: false,
                    },
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
        const { chart, elementId } = useChart([realOption] );

        return {elementId}
    },
});
</script>


<style>
</style>