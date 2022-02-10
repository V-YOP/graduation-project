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

export type anotherCalandarChartOption = {
    title: string,
    data: [number,number,number][]
    scope: number,
};

export default defineComponent({
    props: {
        option: Object as PropType<anotherCalandarChartOption>,
    },
    setup(props, context) {
        const { option } = toRefs(props);
        if (!option || !option.value) {
            throw new Error("热力图没有获得参数！");
        }
        const realOption = computed(() => {
            const datas = _(option.value!.data);
            const values = datas.map((v) => v[2]);
            const maxValue = values.max()! * option.value!.scope!;
            const minValue = values.min()! * option.value!.scope!;
            console.log("最大值", maxValue, "最小值", minValue);
            console.log(option.value?.data.map(v=>[v[1],v[0],v[2] * option.value!.scope!]),)
            function hour2Str(i : number) {
                return `${_.padStart(""+i, 2, "0")}:00-${_.padStart(""+(i+1), 2, "0")}:00`
            }
            return {
                title: {
                    top: 30,
                    left: "center",
                    text: option.value!.title,
                },
                tooltip: {
                    position: 'top',
                    trigger : "item",
                    formatter : function(param:any) {
                        const [hour, weekday, value] : [number,number,number] = param.data
                        const days = ["星期一","星期二","星期三","星期四","星期五","星期六","星期日"]
                        const hourStr = _.padStart(""+hour,2,"0")+":00~"+_.padStart(""+(hour+1),2,"0")+":00"
                        const over = _.nth(option.value!.title, -1)==="秒" ? "秒" : ""
                        return `${days[weekday]} ${hourStr}：${value} ${over}`
                    }
                },
                grid: {
                    height: '50%',
                    top: 80,
                },
                xAxis: {
                    type: 'category',
                    data: _.range(0,24).map(hour2Str),
                    splitArea: {
                        show: true
                    },
                    
                    axisLabel : {
                        rotate : 45
                    }
                },
                yAxis: {
                    type: 'category',
                    data: ["星期一","星期二","星期三","星期四","星期五","星期六","星期日"],
                    splitArea: {
                        show: true
                    }
                },
                visualMap: {
                    min: minValue,
                    max: maxValue,
                    calculable: true,
                    orient: 'horizontal',
                    left: 'right',
                    top : 0
                },
                series: [{
                    name: '',
                    type: 'heatmap',
                    data: option.value?.data.filter(v=>v[2]!==0).map(v=>[v[1],v[0],Math.floor(v[2] * option.value!.scope!)]),
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }]
            }
        });
        const { chart ,elementId} = useChart([realOption]);
        return {elementId}
    },
});
</script>


<style>
</style>