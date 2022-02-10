<!-- 历史信息所使用的layer，包括pos-layer和density-layer -->
<template>
   <overspeed-heatmap-layer
        :scene="scene"
        :overspeedPosList="overspeedPosList"
        :display="displayHeatMap"
    />
    <density-layer
        :scene="scene"
        :overspeedPosList="overspeedPosList"
        :display="displayDensityMap"
    />
</template>

<script lang="ts">
import { Scene } from '@antv/l7'
import { computed, defineComponent, PropType, ref, toRefs, watch } from 'vue'
import { AllInfo, overspeedPos, pos } from '../util/api'
import api from "../util/api"
import overspeedHeatmapLayer from "./overspeed-heatmap-layer.vue";
import busposLayer from "./buspos-layer.vue";
import densityLayer from "./density-layer.vue";
import moment from "moment"
import _ from 'lodash';
import { ElLoading, ElMessage } from 'element-plus';
export default defineComponent({
    components: { busposLayer, densityLayer,overspeedHeatmapLayer },
    props: {
        busIdList: Object as PropType<number[]>, 
        scene: Scene, 
        displayOverspeedPosBy : String as PropType<"HEATMAP" | "DENSITY">,
        focusBuses : Array as PropType<number[]>, 
        busAndLineData: Object as PropType<AllInfo>, 
        timeRange : Array as PropType<Date[]>,
        adcode : Number
    },
    setup(props, context) {
        const {displayOverspeedPosBy, focusBuses, busAndLineData, busIdList, timeRange, adcode, scene} = toRefs(props);
        const poses = ref<overspeedPos[]>([]); // poses是这段时间的位置
        watch(timeRange!, function(val) {
            if (!val) return;
            // 根据timeRange获取……获取
            let [startTime, endTime] = [val[0],val[1]] as [Date,Date]
            let formatter = function(date:Date){
                return moment(date).format("YYYY-MM-DD%20HH:mm:ss")
            }
            console.log(adcode!.value)
            console.log(formatter(startTime))
            // * 根据startTime，endTime获取相关信息，然后设置
            const loadingInstance = ElLoading.service()
            api.getoverspeedPosesByAdcodeBetween(adcode!.value!, formatter(startTime), formatter(endTime),(overspeedPos)=>{
                poses.value = overspeedPos
                if (overspeedPos.length === 0) {
                    ElMessage.warning("该时间段没有数据！")
                }
                loadingInstance.close()
            })
        },{deep:true, immediate:true})
        return {
            displayHeatMap : computed(() => {
                return (displayOverspeedPosBy!.value!) === "HEATMAP"
            }),
            displayDensityMap: computed(() => {
                return (displayOverspeedPosBy!.value!) === "DENSITY"
            }),
            overspeedPosList: computed(() => {
                if (!focusBuses!.value || focusBuses!.value!.length === 0)
                    return poses.value.map(v=>{return {overspeedPos:v, speed_limit:busAndLineData?.value?.nodepaths[v.nodepath_id].speed_limit}})
                return poses.value
                    .filter(pos=>!!_(focusBuses!.value!).find(v=>v===pos.bus_id))
                    .map(v=>{return {overspeedPos:v, speed_limit:busAndLineData?.value?.nodepaths[v.nodepath_id].speed_limit}})
            })
        }
    },
})
</script>


<style>

</style>