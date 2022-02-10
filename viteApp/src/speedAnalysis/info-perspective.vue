<template>
    <el-dialog
        title="修改超速因子"
        v-model="editOverspeecFactorExprDialogVisible"
        width="30%"
        @open="openEditOFEDialog"
    >
        <el-space direction="vertical" alignment="left" :size="1">
            <p>新的超速因子表达式：</p>
            <el-input v-model="newOFE"> </el-input>
        </el-space>
        <template #footer>
            <span class="dialog-footer">
                <el-button type="warning" @click="toDefaultOFE"
                    >默 认</el-button
                >
                <el-button @click="editOverspeecFactorExprDialogVisible = false"
                    >取 消</el-button
                >
                <el-button type="primary" @click="confirmChangeOFE"
                    >确 定</el-button
                >
            </span>
        </template>
    </el-dialog>

    <el-header style="display: flex; align-items: center;border:solid 1px #e6e6e6;">
        <h1 style="font-size: 30px">信息总览</h1>
    </el-header>
    <el-main id="main">
        <el-space direction="vertical" alignment="left">
            <p style="display: flex; align-items: center">
                <span>查看地区：</span><el-tag>{{ place_name }}</el-tag>
            </p>
            <p style="display: flex; align-items: center">
                <span>查看日期：</span
                ><el-tag>{{ dateRange[0] + "~" + dateRange[1] }}</el-tag>
            </p>
            <p style="display: flex; align-items: center">
                <span>超速因子表达式：</span
                ><el-tag>
                    {{ overspeedFactorExpr }}
                </el-tag>
                <el-popover
                    placement="top-start"
                    :width="200"
                    trigger="hover"
                    content="该表达式为计算超速因子的数学表达式，其中V为车速，LIMIT为路段限速，A为加速度。"
                >
                    <template #reference>
                        <el-button
                            type="text"
                            icon="el-icon-info"
                            style="font-size: 20px; margin-left: 10px"
                        ></el-button>
                    </template>
                </el-popover>

                <el-button
                    type="text"
                    style="font-size: 15px; margin-left: 10px"
                    @click="editOverspeecFactorExprDialogVisible = true"
                    >更改</el-button
                >
            </p>
            <p style="display: flex; align-items: center">
                <span>接收到的超速信息数量：</span
                ><el-tag>{{ overspeedInfo.length }}</el-tag>
                <el-button
                    type="text"
                    style="font-size: 15px; margin-left: 10px"
                    icon="el-icon-download"
                    @click="downloadData"
                    >下载</el-button
                >
            </p>
            <p style="display: flex; align-items: center">
                <span>该日期段超速信息数量估值：</span
                ><el-tag>{{ allLength }} </el-tag>
            </p>
            <el-switch
                style="display: block"
                v-model="useSeconds"
                active-text="超速时间"
                inactive-text="超速因子"
            ></el-switch>
            <calandarChart :option="calandarChartOption" />
            <anotherCalandarChart :option="anotherCalandarChartOption" />

        </el-space>
    </el-main>
</template>

<script lang="ts">
import {
    computed,
    defineComponent,
    inject,
    PropType,
    Ref,
    ref,
    SetupContext,
    toRefs,
    useContext,
    watch,
} from "vue";
import { overspeedPos } from "../util/api";
import calandarChart from "./charts/calandar-chart.vue";
import scaleBarChart from "./charts/scale-bar-chart.vue"
import anotherCalandarChart from "./charts/another-calandar-chart.vue"
import {scaleBarChartOption} from "./charts/scale-bar-chart.vue"
import {anotherCalandarChartOption} from "./charts/another-calandar-chart.vue"
import { countByDays, getComputer, countByWeekDayAndHour } from "./util";
import _ from "lodash";
import * as math from "mathjs";
import { ElLoading, ElMessage } from "element-plus";
import moment from "moment";
import * as echarts from "echarts";

function createAndDownloadFile(fileName : string, content : any) {
    var aTag = document.createElement('a');
    var blob = new Blob([content]);
    aTag.download = fileName;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    // @ts-ignore
    URL.revokeObjectURL(blob);
}

function getCalandarChartOptionWithSecs(
    overspeedInfo: overspeedPos[],
    scope: number,
    dateRange: [string, string]
) {
    if (!overspeedInfo) throw new Error("问题来了");
    return {
        title: "各日总超速时间 / 秒",
        data: countByDays(overspeedInfo, (v) => 1),
        scope,
        range: dateRange,
    };
}
function getCalandarChartOptionWithFactor(
    overspeedInfo: overspeedPos[],
    scope: number,
    dateRange: [string, string],
    computer: (overspeedPos: overspeedPos) => number
) {
    if (!overspeedInfo) throw new Error("问题来了");
    return {
        title: "各日总超速因子",
        data: countByDays(overspeedInfo, computer),
        scope,
        range: dateRange,
    };
}
function useEditOverspeedFactorDialog(context: SetupContext, OFE : Ref<string>) {
    const editOverspeecFactorExprDialogVisible = ref(false);
    const newOFE = ref("");
    const confirmChangeOFE = () => {
        function illegal() {
            let illegal = false
            // 第一次判断，判断是否是合法表达式
            try {
                math.evaluate(newOFE.value, {
                    A : 1,
                    LIMIT : 2,
                    V : 3
                })
                eval(`
                    (function(){
                        const A = 1;
                        const LIMIT = 2;
                        const V = 3;
                        return ${newOFE.value}
                    })()
                `)
            }
            catch(e) {
                illegal = true
                ElMessage.error("请输入合法表达式！")
            }
            function Calcu(scope : Object) {
                return math.evaluate(newOFE.value, scope)
            }
            function checkConstant() {
                function generateRandom() {
                    return Calcu({
                        A : (Math.random()-0.5)*100000,
                        LIMIT : (Math.random()-0.5)*100000,
                        V : (Math.random()-0.5)*100000,
                    })
                }
                const a = generateRandom() 
                const b = generateRandom() 
                return a === b
            }
            
            if (checkConstant()) {
                ElMessage.error("表达式不能为常量！")
                illegal = true
            }
            return illegal
        }
        if  (illegal()) {
            return;
        }
        context.emit("changeOverspeedFactorExpr", newOFE.value);
        editOverspeecFactorExprDialogVisible.value = false;
    };
    const toDefaultOFE = () => {
        context.emit("changeOverspeedFactorExpr", "(V-LIMIT)*0.7+A*2");
        editOverspeecFactorExprDialogVisible.value = false;
    };
    const openEditOFEDialog = ()=>{
        newOFE.value = OFE.value
    }
    return {
        editOverspeecFactorExprDialogVisible,
        openEditOFEDialog,
        newOFE,
        confirmChangeOFE,
        toDefaultOFE,
    };
}


// 生成相关Option
function useScaleBarChart(dateRange : Ref<[string, string]>, overspeedInfo: Ref<overspeedPos[]>, scope : Ref<number>, useSeconds : Ref<boolean>, computer: (overspeedPos: overspeedPos) => number) {
    function generateOriData() {
        const startTime = moment(dateRange.value[0])
        const endTime = moment(dateRange.value[1])
        const dayCount = endTime.diff(startTime, "days")
        function format2(i : number){
            if (i < 10) return "0"+i;
            return ""+i;
        }
        const res : string[] = []
        _.range(0, dayCount).map((i)=>{
            return startTime.add(i, "days").format("YYYY-MM-DD")
        }).forEach(v=>{
            for (let h = 0;h<24;h++)
                for(let m = 0;m < 60;m++)
                    for (let s = 0;s < 60;s++)
                        res.push(`${v}\n${format2(h)}:${format2(m)}:${format2(s)}`)
        })
        return res;
        
    }

    function getData() {
        const theComputer = useSeconds.value ? (thing : any)=>{return 1} : computer;
        const title = useSeconds.value ? "超速时间柱图 / 秒" : "超速因子柱图"
        const bucket : Record<string ,number> = {}
        /*
        generateOriData().forEach(v=>{
            bucket[v] = 0;
        })
        */
        overspeedInfo.value.forEach((v)=>{
            const index = v.update_time
            if (bucket[index])
                bucket[index] += theComputer(v)
            else bucket[index] = theComputer(v)
        })
        const data = _(bucket).toPairs().map(v=>{
                return [v[0].substring(0,10)+"\n"+v[0].substring(11,19),v[1]]
            }).sortBy(v=>v[0]).value();
        return {
            title, scope:scope.value,data : (data as [string,number][])
        }
    }
    return { 
        scaleBarChartOption : computed<scaleBarChartOption>(getData)
    }
    
}

function useAnotherCalandarChart(overspeedInfo: Ref<overspeedPos[]>, scope : Ref<number>, useSeconds : Ref<boolean>, overspeedFactorExpr :Ref<string> ) {
    return {
        anotherCalandarChartOption : computed<anotherCalandarChartOption>(()=>{
            const computer : (overspeedPos: overspeedPos) => number = getComputer(overspeedFactorExpr.value)
            return {
                scope : scope.value,
                data : countByWeekDayAndHour(overspeedInfo.value, useSeconds.value ? _.constant(1) : computer),
                title : useSeconds.value ? "每周各小时的总超速时间 / 秒" : "每周各小时的总超速因子"
            }
        })
    }
}

export default defineComponent({
    components: { calandarChart , scaleBarChart, anotherCalandarChart},
    props: {
        overspeedInfo: Object as PropType<overspeedPos[]>,
        scope: Number,
        overspeedFactorExpr: String,
        infoGetter: Function as PropType<
            (overspeedPos: overspeedPos) => {
                street_name: string;
                speed_limit: number;
                bus_number: string;
                bus_type: string;
            }
        >,
    },
    setup(props, context) {
        const { overspeedInfo, scope, infoGetter, overspeedFactorExpr } =
            toRefs(props);
        if (
            !overspeedInfo ||
            !overspeedInfo.value ||
            !scope ||
            !scope.value ||
            !infoGetter ||
            !infoGetter.value
        ) {
            throw new Error("没有拿到数据，怪");
        }
        countByWeekDayAndHour(overspeedInfo.value, ()=>1)
        const dateRange = ref(inject("dateRange") as [string, string]);
        const useSeconds = ref(true);
        const calandarChartOption = computed(() => {
            console.log("重计算");
            if (useSeconds.value)
                return getCalandarChartOptionWithSecs(
                    overspeedInfo.value!,
                    scope.value!,
                    dateRange.value
                );
            else
                return getCalandarChartOptionWithFactor(
                    overspeedInfo.value!,
                    scope.value!,
                    dateRange.value,
                    getComputer(overspeedFactorExpr!.value!)
                );
        });
        const editOverspeedFactorDialog = useEditOverspeedFactorDialog(context, overspeedFactorExpr as Ref<string>);
        const place_name = ref(inject("place") as string)
        console.log(scope.value!)
        return {
            ...editOverspeedFactorDialog,
            ...useAnotherCalandarChart(overspeedInfo as Ref,scope as Ref,useSeconds, overspeedFactorExpr as Ref),
            overspeedFactorExpr,
            useSeconds,
            calandarChartOption,
            overspeedInfo,
            place_name,
            dateRange,
            allLength: computed(() =>
                Math.floor(overspeedInfo.value!.length * scope.value!)
            ),
            scope,
            infoGetter,
            downloadData:()=>{
                const loadingInstance = ElLoading.service();
                setTimeout(()=>{
                    const data = JSON.stringify({
                    scope:scope.value!,
                    info : overspeedInfo.value!.map(v=>{
                        const otherInfo = infoGetter!.value!(v)
                        return {...v, ...otherInfo }
                    })
                 })
                createAndDownloadFile(`${place_name.value}-${dateRange.value.join("~")}.json`, data)
                loadingInstance.close()
                },200)
            }
        };
    },
});
</script>

<style scoped>
#main {
    padding-left: 50px;
}
</style>