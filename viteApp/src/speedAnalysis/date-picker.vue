<!-- 用一个时间段，呼出一个dialog，通过这个dialog筛选查看时间，提供每日，全选，全不选，每周一二三四五六七，然后抛出事件来更新时间 -->
<template>
  <el-button type="primary" @click="callDialog">修改查看日期</el-button>
  <el-dialog
        title="修改查看日期"
        v-model="dialogVisible"
        append-to-body
  >
  <el-space direction="vertical" alignment="left">
  <el-button-group>
  <el-button  @click="selectAll" size="mini">全选</el-button>
    <el-button  @click="selectReverse"  size="mini">反选</el-button>
    <el-button  @click="selectNone"  size="mini">全不选</el-button>
</el-button-group>
 <el-button-group>
  <el-button  @click="selectWeekday[0]()"  size="mini">星期一</el-button>
    <el-button  @click="selectWeekday[1]()"  size="mini">星期二</el-button>
    <el-button  @click="selectWeekday[2]()"  size="mini">星期三</el-button>
    <el-button  @click="selectWeekday[3]()"  size="mini">星期四</el-button>
    <el-button  @click="selectWeekday[4]()"  size="mini">星期五</el-button>
    <el-button  @click="selectWeekday[5]()"  size="mini">星期六</el-button>
    <el-button  @click="selectWeekday[6]()"  size="mini">星期日</el-button>
</el-button-group>
    
  <el-checkbox-group v-model="selectedDates">
      <el-checkbox-button v-for="date in allDate" :label="date" :key="date">{{date}}</el-checkbox-button>
    </el-checkbox-group>
    </el-space>     
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">确 定</el-button>
    </span>
  </template>
</el-dialog>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, Ref, SetupContext, toRefs } from 'vue'
import _ from 'lodash';
import { getDiffDate, getWeekday2Dates } from './util';
function useDialog(
    dateRange : Ref<[string, string]>,
    modelValue : Ref<string[]>,
    context : SetupContext
) {
    const dialogVisible = ref(false)
    const selectedDates = ref<string[]>([])
    const allDate = getDiffDate(...dateRange.value)
    function callDialog () {
        selectedDates.value = _.cloneDeep(modelValue.value) // 保证不会联动改变
        dialogVisible.value = true
    }
    function confirm() {
        context.emit("update:modelValue", _.cloneDeep(selectedDates.value))
        console.log("改变啦")
        dialogVisible.value = false
    }
    function selectAll() {
        selectedDates.value = _.cloneDeep(allDate)
    }
    function selectNone() {
        selectedDates.value = []
    }
    function selectReverse() {
        allDate.forEach(date=>{
            const index = selectedDates.value.indexOf(date)
            if (index === -1) { // 日期不在区间里，push它
                selectedDates.value.push(date)
            }
            else {
                selectedDates.value.splice(index, 1)
            }
        })
    }
    const selectWeekday : (()=>void)[] = []
    const weekday2Dates = getWeekday2Dates(allDate)
    console.log(weekday2Dates)
    _.range(0,7).forEach(day=>{
        selectWeekday.push(()=>{
            // 首先判断是否全为-1，如果全为-1则将这些全部都push，如果全不为-1则全部都splice
            const indexes = weekday2Dates[day].map(date=>selectedDates.value.indexOf(date))
            if (indexes.every(v=>v===-1)) { // 全选
                weekday2Dates[day].forEach(date=>selectedDates.value.push(date))
                return;
            }
            if (indexes.every(v=>v!==-1)) { // 全不选
                indexes.sort((a,b)=>b-a).forEach(index=>selectedDates.value.splice(index, 1))
                return;
            }
            weekday2Dates[day].forEach(date=>{
                if (selectedDates.value.indexOf(date) === -1) { // 如果没有
                    selectedDates.value.push(date)
                }
                else {
                    return;
                }
            })
        })
    })
    return {dialogVisible, selectedDates,callDialog,confirm, allDate,selectAll,selectNone,selectReverse, selectWeekday}
}

export default defineComponent({
    props : {
        dateRange : Object as PropType<[string,string]>,
        modelValue : Array as PropType<string[]>, // 当前选择的日期
    },
    setup(props, context) {
        const {dateRange, modelValue} = toRefs(props);
        console.log(props.dateRange)
        return {
            ...useDialog(dateRange as Ref, modelValue as Ref, context)
        }
    },
})
</script>

<style>

</style>