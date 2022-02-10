
<template>
  <el-dialog
    title="请选择要操作的地区"
    v-model="dialogVisible"
    width="30%"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    :destroy-on-close="true"
    append-to-body
  >
    <el-cascader
    :options="options"
    v-model="cascaderValue"
    size="medium"
    :props="{ multiple: true }"
    ref="cascaderRef"
    clearable>
    <template #default="{ node, data }">
    <span>{{ data.label }}</span>
    <span v-if="!node.isLeaf"> ({{ data.children.length }}) </span>
  </template>
    </el-cascader>
    <template #footer>
    <span class="dialog-footer">
      <el-button type="primary" @click="handleClose">确 定</el-button>
    </span>
  </template>
  </el-dialog>
</template>

<script lang="ts">
import axios from 'axios'
import adcodes_ from '../util/adcodes'
import { ComponentInternalInstance, onMounted, ref } from 'vue';
import { ElCascader, ElMessage } from "element-plus";
let adcodes = adcodes_;
let map : Record<number, string> = {};
// @ts-ignore
function getAll(node : {value: string,
    label: string, children? : Node[]}) {
  map[Number(node.value)] = node.label;
  // @ts-ignore
  node.children?.forEach(v=>getAll(v))
}
// @ts-ignore
adcodes.forEach(v=>getAll(v));
export default {
  name: "ask-for-place",
  emits: ["select-places"],
  setup(props, context) {
    const cascaderRef = ref(null);
    const cascaderValue = ref(null);
    const dialogVisible = ref(true)
    return { 
      cascaderValue,
      cascaderRef,
      options : adcodes,
      dialogVisible,
      handleClose() {
        let res = (cascaderValue.value as unknown as [string,string,string][])?.map(lst=>lst.map(v=>Number(v))) as [number,number,number][]
        if (res === null || cascaderValue.value === null) {
          ElMessage.error("请选择要修改的地区！")
          return;
        }
        if (res.length > 5) {
          ElMessage.error("你只能选择小于5个地区！")
          return;
        }
        dialogVisible.value = false;
        context.emit("select-places", res.map(v=>{
          return {adcode : v[2],
          name: v.map(adcode=>map[adcode]).join("/")}
        }));
        console.log(res.map(v=>{
          return {adcode : v[2],
          name: v.map(adcode=>map[adcode]).join("/")}
        }))
        
      }
    }
  },

};
</script>

<style scoped>
</style>