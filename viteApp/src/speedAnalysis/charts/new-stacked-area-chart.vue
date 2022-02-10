<!-- 
    关于对这个图的使用，值为null的时候表示这个小时不显示
    // 有点麻烦，重写
 -->
<template>
    <div style="height: 600px; width: 1000px" :id="elementId"></div>
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

export type newStackedAreaChartOption = {
    title: string;
    data: [string, [number, number][]][]; // ["日期",[小时,值][]][]
    scope: number;
    xAxisLabel : number[];
    label : "秒" | null
};

export default defineComponent({
    props: {
        option: Object as PropType<newStackedAreaChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("热力图没有获得参数！");
        }
        console.log(option.value)
        function hour2Str(i : number) {
            return `${_.padStart(""+i, 2, "0")}:00-${_.padStart(""+(i+1), 2, "0")}:00`
        }
        const realOption = computed(() => {
            return {
                replaceOption : true,
                title: {
                    text: option.value!.title,
                },
                tooltip: {
                    trigger: "axis",
                    axisPointer: {
                        type: "cross",
                        label: {
                            backgroundColor: "#6a7985",
                        }
                    },
                },
                legend: {
                    data: option.value!.data.map((v) => v[0]).sort().reverse(),
                    right : 0,
                    type : "scroll",
                    orient: 'vertical',
                    selected : 
                        _(option.value!.data)
                            .map(v=>{
                                const notAllEmpty = 
                                    !v[1].every(val=>val[1] === 0)
                                return [v[0], notAllEmpty]
                            })
                            .fromPairs().value()
                },

                grid: {
                    containLabel: true,
                },
                xAxis: [
                    {
                        type: "category",
                        boundaryGap: true,
                        data: option.value!.xAxisLabel.map(hour2Str),
                        name : "小时",
                        axisLabel : {
                            rotate : 45
                        }
                    },
                ],
                yAxis: [
                    {
                        type: "value",
                        name : option.value?.label,
                        axisLabel : {
                            formatter : "{value}"
                        }
                    },
                ],
                series: _(option.value!.data)
                    .sortBy((v) => v[0])
                    .map((v) => {
                        return {
                            id : v[0],
                            name: v[0],
                            type: "line",
                            stack: "总量",
                            areaStyle: {
                            },
                            emphasis: {
                                focus: "series",
                            },
                            data: v[1].map((v,k)=>[hour2Str(k),v])
                        };
                    }).value(),
            };
        });
        watch(realOption, function (val){
            console.log(val)
        })
        const { chart, elementId } = useChart([realOption]);
        return { elementId }
    },
});
</script>


<style>
</style>