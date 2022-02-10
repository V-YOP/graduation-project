<template>
    <el-space direction="vertical" alignment="left">
        <!-- 
            一个折线图，表达各公交车各小时的超速情况
        -->
        <basic-line-chart :option=" {
            title: useSeconds ? `每日每小时的总超速时间 / 秒` : `每日每小时的总超速因子`,
            scope : scope,
            label : useSeconds ? `秒` : null,
            data : basicLineChartData.map(([id, datas])=> {
                return [busInfoGetter(id).bus_number, datas]
            }),
            xAxisLabel : selectedHours
        }
        "/>
    </el-space>
</template>

<script lang="ts">
import _ from "lodash";
import { computed, defineComponent, inject, PropType, ref, Ref, toRefs ,watch} from "vue";
import { overspeedPos } from "../../util/api";
import basicLineChart from "../charts/basic-line-chart.vue";
import { getComputer } from "../util";

function someInfos(
    overspeedInfo: Ref<overspeedPos[]>,
    bus_ids: Ref<number[]>,
    selectedDates: Ref<string[]>,
    selectedHours: Ref<number[]>,
    computer : Ref<(overspeedPos:overspeedPos)=>number>
) {
    const dateSet = computed(() => new Set(selectedDates.value));
    const hourSet = computed(() => new Set(selectedHours.value));
    const busIdSet = computed(() => new Set(bus_ids.value));
    const filtedInfo = computed(()=>{
        return _(overspeedInfo.value)
            .filter(v=>{
                const date = v.update_time.substring(0, 10);
                const hour = Number(v.update_time.substring(11, 13));
                return dateSet.value.has(date) && hourSet.value.has(hour) && busIdSet.value.has(v.bus_id);
            })
            .value()
    })

    const basicLineChartData = computed(()=>{
        function getAdata(overspeedPos: overspeedPos[]) {
            const res = _(overspeedPos) // 已经被筛选过了
            .groupBy(v=>v.update_time.substring(11,13))
            .mapValues(v=>_(v).sumBy(computer.value) || 0)
            .value()
            return _.range(0,24).map(i=>[i, res[i] || 0])
        }
        return  _(filtedInfo.value)
            .groupBy(v=>v.bus_id)
            .map((v,k)=>[Number(k), getAdata(v)] as [number, number[][]])
            .value()

    })
    watch(basicLineChartData,(val)=>{console.log(val)})
    return {
        basicLineChartData
    }
}

export default defineComponent({
    components: {basicLineChart},
    props: {
        overspeedInfo: Array as PropType<overspeedPos[]>, // 不过滤
        scope : Number,
        selectedDates: Array as PropType<string[]>,
        selectedHours: Array as PropType<number[]>,
        useSeconds: Boolean,
        bus_ids: Array as PropType<number[]>,
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
            bus_ids,
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
            bus_ids,
            scope,
            computer,
            ...someInfos(overspeedInfo as Ref,
            bus_ids as Ref,
            selectedDates as Ref,
            selectedHours as Ref,
             computer
             ),
        };
    },
});
</script>

<style>
</style>