<template>
    <div style="height: 300px; width: 700px" :id="elementId"></div>
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

export type calandarChartOption = {
    title: string,
    data: Array<[string, number]>, // ["YYYY-MM-dd", data][]
    scope: number,
    range: [string, string]
};

export default defineComponent({
    props: {
        option: Object as PropType<calandarChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("热力图没有获得参数！");
        }
        const realOption = computed(() => {
            const datas = _(option.value!.data);
            const values = datas.map((v) => v[1]);
            const maxValue = values.max()! * option.value!.scope!;
            const minValue = values.min()! * option.value!.scope!;
            console.log("最大值", maxValue, "最小值", minValue);
            let toolTipOver : string = ""
            if (_.nth(option.value!.title,-1)==="秒") {
                toolTipOver = "秒"
            }
            return {
                title: {
                    top: 30,
                    left: "center",
                    text: option.value!.title,
                },
                tooltip : {
                    trigger : "item",
                    formatter : function(param : any) {
                        const [date, value] : [string, number]= param.data
                        return `${date}: ${Math.floor(value)} ${toolTipOver}`
                    }
                },
                visualMap: {
                    min: minValue,
                    max: maxValue,
                    calculable: true,
                    orient: "horizontal",
                    left: "right",
                    top: 65,
                },
                calendar: {
                    top: 150,
                    left: 30,
                    right: 30,
                    range: option.value!.range,
                    itemStyle: {
                        borderWidth: 0.5,
                    },
                    yearLabel: { show: false },
                    dayLabel: { nameMap: "cn" },
                    monthLabel: { nameMap: "cn" },
                },
                series: {
                    type: "heatmap",
                    coordinateSystem: "calendar",
                    data: option.value!.data.map((v) => [
                        v[0],
                        v[1] * option.value!.scope,
                    ]),
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                },
            };
        });
        const { chart, elementId } = useChart( [realOption]);
        return {elementId}
    },
});
</script>


<style>
</style>