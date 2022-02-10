<!-- 
一个输入组件，在输入时给与输入建议，回车确定，调用事件
-->
<template>
       <el-autocomplete
      v-model="inputValue"
      :fetch-suggestions="querySearch"
      placeholder="请输入对象名称"
      :trigger-on-focus="false"
      @select="confirm"
    ></el-autocomplete>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, toRefs } from 'vue'
import _ from 'lodash'
export default defineComponent({
    props : {
        itemList : Array as PropType<string[]>
    },
    setup(props, context) {
        const { itemList } = toRefs(props)
        if (!itemList || itemList.value === undefined) {
            throw new Error("粗事了!")
        }
        const inputValue = ref("")
        const querySearch = (queryString : string , cb : ( res : any[] ) => void) => {
            // 匹配首部元素
            console.log("执行")
            const res = 
                _(itemList!.value!)
                    .filter(item=>{
                        return item !== null && item.toUpperCase().indexOf(queryString.toUpperCase()) === 0 // 为0说明匹配首部
                    })
                    .take(10).map(v=>{return {value:v}}).value()
            console.log(res)
            cb(res)
        }
        const confirm = function(param : {value: string}) {
            context.emit("handleSelect", param.value)
            inputValue.value = ""
        }
        return {
            confirm,
            querySearch,
            inputValue
        }
    },
})
</script>


<style>

</style>