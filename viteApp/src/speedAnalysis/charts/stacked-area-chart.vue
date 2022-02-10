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

export type stackedAreaChartOption = {
    title: string;
    data: [string, (number | null)[]][]; // ["日期", 代表24小时情况的数组，null表示不显示
    scope: number;
};

export default defineComponent({
    props: {
        option: Object as PropType<stackedAreaChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("热力图没有获得参数！");
        }
        function hour2Str(i : number) {
                return `${_.padStart(""+i, 2, "0")}:00-${_.padStart(""+(i+1), 2, "0")}:00`
            }
        const realOption = computed(() => {
            console.log("重新计算opt", option.value)
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
                            .map(v=>[v[0], !(v[1].every(val=>(val===0 || val === null)))])
                            .fromPairs().value()
                },

                grid: {
                    containLabel: true,
                },
                xAxis: [
                    {
                        type: "category",
                        boundaryGap: true,
                        data: _(option.value!.data[0][1]) // 用第一个数据做“标本”生成X轴
                            .map((v,i)=>{
                                if (v === null)
                                    return null
                                return i
                            })
                            .filter(v=>v!==null) // compact会把0也干掉，？？？
                            .map(hour2Str)
                            .value(),
                        name : "小时",
                        axisLabel : {
                            rotate : 45
                        }
                    },
                ],
                yAxis: [
                    {
                        type: "value",
                        name : _.last(option.value!.title) === "秒" ? "秒" : "",
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
                            data: _(v[1])
                                .filter((dat)=>dat!==null)
                                .map((dat) => dat! * option.value!.scope!)
                                .value(),
                        };
                    }).value(),
            };
        });
        watch(realOption, function (val){
            
        })
        const { chart, elementId } = useChart([realOption]);
        return { elementId }
    },
});
</script>


<style>
</style>