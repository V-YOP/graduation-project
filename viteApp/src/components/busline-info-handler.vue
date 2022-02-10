<template>
    <!--
    <el-container style="height: 80vh; width: 100%;max-height:100%">
        <el-main style="height: 100%; width: 100%">
            <el-scrollbar height="100%">

    <p class="item" v-for="item in 200">{{ item }}</p>
        </el-scrollbar>
        </el-main>
    </el-container>
    -->
<el-container>
    <el-main>
        <el-row
        type="flex"
        align="middle"
        justify="space-between"
        >
    <el-button round @click="clearSelect" :disabled="inFocusMode">全不选</el-button>
    <el-button type="primary" round @click="inFocusMode ? unFocus() : focus()">{{inFocusMode ? "取消聚焦" : "聚焦"}}</el-button>
        </el-row>
        <el-table
    :data="tableData"
    stripe
    height="600"
    style="width: 100%">

    <el-table-column
      :label="`选择(${selectCount})`"
      >
      <template #default="scope">
        <el-switch v-model="selectBusline[scope.row.lineId]" :disabled="inFocusMode"> </el-switch>
      </template>
    </el-table-column>
    <el-table-column
      prop="lineName"
      label="路线名">
    </el-table-column>
    <el-table-column
    v-if="!emptyBusList"
      prop="busCount"
      label="在线公交车数量"
      sortable
      >
    </el-table-column>
  </el-table>
    </el-main>
</el-container>



</template>

<script lang="ts">

import { computed, defineComponent, PropType, ref, shallowRef, toRefs, watch } from 'vue'
import { AllInfo ,TODO } from '../util/api'
import * as _ from "lodash";
export default defineComponent({
    props: {busAndLineData : Object as PropType<AllInfo> , onlineBusList : Object as PropType<number[]>},
    emits: ["enable-focus-mode","disable-focus-mode"],
    setup(props, context) {
        const {onlineBusList} = toRefs(props);
        if (!props.busAndLineData || !props.onlineBusList)
            throw new Error("需要给定数据！")
        const emptyBusList = computed(()=>{
            return onlineBusList!.value?.length === 0
        })
        const buslines = shallowRef(_(props.busAndLineData.buslines).values().value());
        const lineBusCount  = computed<Record<number,number>>(()=>{
            let res : Record<number,number> = {}
            let busIdSet = new Set(onlineBusList!.value)
            buslines.value.forEach(busline=>{
                if (!res[busline.line_id])
                    res[busline.line_id] = 0;
                busline.bus_id_list.forEach(busId=>{
                    if (busIdSet.has(busId))
                        res[busline.line_id]++
                })
            })
            return res;
        })
        const tableData = computed<{lineName:string,busCount:number,lineId:number}[]>(()=>{
            return buslines.value.map(({line_id, line_name})=>{
                let busCount = lineBusCount.value[line_id];
                return {lineName:line_name, busCount:busCount, lineId:line_id}
            })
        })
        console.log(tableData.value)
        const tmp : Record<number, boolean> = {}
        buslines.value.forEach(busline=> tmp[busline.line_id] = false)
        const selectBusline  = ref<Record<number, boolean>>(tmp)
        const selectCount = computed(()=>_(selectBusline.value).values().filter(v=>v).size())
        const inFocusMode = ref(false)
        return {buslines, lineBusCount, tableData, selectBusline, selectCount,inFocusMode,
            focus: function() {
                const busList = _(selectBusline.value)// 发送事件，告诉父组件进入该模式。
                    .entries()
                    .filter((pair)=>pair[1])
                    .map((pair)=>Number(pair[0])).value()
                context.emit("enable-focus-mode", busList);
                inFocusMode.value = true
            },
            unFocus() {
                _(selectBusline.value).forEach((v,k,map)=>{map[Number(k)] = false});
                const busList = _(selectBusline.value)// 发送事件，告诉父组件进入该模式。
                    .entries()
                    .filter((pair)=>pair[1])
                    .map((pair)=>Number(pair[0])).value()
                context.emit("enable-focus-mode", busList);
                inFocusMode.value = false
            },
            clearSelect(){
                _(selectBusline.value).forEach((v,k,map)=>{map[Number(k)] = false});
            },
            emptyBusList
        }
    }
})
</script>


<style scoped>
.item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50px;
    margin: 10px;
    text-align: center;
    border-radius: 4px;
    background: #ecf5ff;
    color: #409eff;
}

.card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
  }
    
  .text {
    font-size: 14px;
  }

  .item {
    margin-bottom: 18px;
  }


</style>

