<!-- ! 本文件为展示实时公交车位置的组件，其不保存历史信息 -->
<template>
    <!-- layer由buspos-layer维护 -->
    <buspos-layer :scene="scene" :busposList="filtedBusPosList" :display="displayPos" :infoGetter="infoGetter"/>
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
// busline-layer可轻易重用，但显示实时位置的layer基本无法重用。
import { Scene } from "@antv/l7-scene";
import {
    computed,
    defineComponent,
    PropType,
    ref,
    Ref,
    shallowRef,
    toRefs,
    watch,
} from "vue";
import _ from "lodash";
import { ILayer, PointLayer } from "@antv/l7";
import busposLayer from "./buspos-layer.vue";
import overspeedHeatmapLayer from "./overspeed-heatmap-layer.vue";
import densityLayer from "./density-layer.vue";
import * as config from "../config";
import { AllInfo, overspeedPos, pos } from "../util/api";
import api from "../util/api";

export default defineComponent({
    components: { busposLayer, overspeedHeatmapLayer, densityLayer },
    props: { 
        busIdList: Object as PropType<number[]>, 
        scene: Scene, 
        displayOverspeedPosBy : String as PropType<"HEATMAP" | "DENSITY">,
        displayPos : Boolean, 
        focusBuses : Array as PropType<number[]>, 
        busAndLineData: Object as PropType<AllInfo>, 
        
    },
    emits: ["update-online-bus-list"],
    setup(props, context) {
        const {displayPos, displayOverspeedPosBy, focusBuses, busAndLineData} = toRefs(props);
        // * 每个公交车的位置
        // 使用ref似乎会造成一定的性能损耗
        let busposMap = ref<Record<number, pos[]>>({});
        let overspeedPosMap = ref<Record<number, overspeedPos[]>>({});
        const busPosList = computed(() => {
            return _(busposMap.value)
                .map(poses=>_.last(poses))
                .filter((k,v)=>!!k)
                .value() as pos[];
        });
        const filtedBusPosList = computed(()=>{
            if (focusBuses!.value!.length === 0) return busPosList.value;
            return busPosList.value.filter(pos=>!!_(focusBuses!.value!).find(v=>v===pos.bus_id))
        })
        watch(
            busPosList,
            _.throttle((val:pos[], oldval:pos[]) => {
                    // 如果val和oldval中bus_id完全相同，则不做操作
                    if (_.difference(_.map(val,v=>v.bus_id), _.map(oldval,v=>v.bus_id)).length!==0)
                        context.emit("update-online-bus-list", _(val).map(v=>v.bus_id).value());
                }, 100));

        let ws = api.posInfoGetterWS(
            props.busIdList!,
            function onMessage(poses:pos[], overspeedPoses:overspeedPos[]) {
                poses
                    .sort((a, b) => {
                        if (a.update_time > b.update_time) return 1;
                        return -1;
                    })
                    .forEach((pos) => {
                        if (!busposMap.value[pos.bus_id])
                            busposMap.value[pos.bus_id] = [];
                        let {update_time} = pos;
                        if (busposMap.value[pos.bus_id].length !== 0 &&
                        update_time <= _.last(busposMap.value[pos.bus_id])!.update_time) {
                            // * 这时候是非最新数据，删除
                            return;
                        }
                        if (busposMap.value[pos.bus_id].length >= 1) { // 只留一个！
                            busposMap.value[pos.bus_id][0] = pos
                        } 
                        else {
                            busposMap.value[pos.bus_id].push(pos);
                        }   
                    });
                overspeedPoses
                    .sort((a, b) => {
                        if (a.update_time > b.update_time) return 1;
                        return -1;
                    })
                    .forEach((overspeedPos) => {
                        if (!overspeedPosMap.value[overspeedPos.bus_id])
                            overspeedPosMap.value[overspeedPos.bus_id] = [];
                        if (overspeedPosMap.value[overspeedPos.bus_id].length !== 0 &&
                        overspeedPos.update_time <= _.last(overspeedPosMap.value[overspeedPos.bus_id])!.update_time) {
                            return;
                        }
                        overspeedPosMap.value[overspeedPos.bus_id].push(overspeedPos);
                        if (overspeedPosMap.value[overspeedPos.bus_id].length > 600)
                            overspeedPosMap.value[overspeedPos.bus_id].splice(0,50);
                    });
                console.log(`超速量前后：${overspeedPoses.length},`,_(overspeedPosMap.value).values().flatMap().value())
            }
        );
        const infoGetter = function(bus_id : number) {
            const res = busAndLineData?.value?.buses[bus_id]
            return {
                bus_number : res?.bus_number, 
                bus_type : res?.bus_type
            }
        }
        return { displayPos, 
        busAndLineData,
        infoGetter,
            displayHeatMap : computed(()=>{
                return (displayOverspeedPosBy!.value!) === "HEATMAP"
            }),
            displayDensityMap: computed(()=>{
                return (displayOverspeedPosBy!.value!) === "DENSITY"
            }),
            busPosList, overspeedPosMap, 
            filtedBusPosList,
            overspeedPosList: computed(()=>{
                console.log(overspeedPosMap.value)
                let res =  _(overspeedPosMap.value)
                                .values()
                                .flatten();
                if (focusBuses!.value!.length !== 0)
                    res = res.filter(pos=>!!_(focusBuses!.value!).find(v=>v===pos.bus_id))
                return res.map(v=>{return {overspeedPos:v, speed_limit:busAndLineData?.value?.nodepaths[v.nodepath_id].speed_limit}})
                .value()
            })
        }
    },
});
</script>


<style>
</style>


