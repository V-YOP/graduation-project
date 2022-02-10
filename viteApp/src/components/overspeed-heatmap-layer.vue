<!-- 本文件用于展示热力图 -->
<!-- 发现亮度图更加漂亮…… -->
<template></template>

<script lang="ts">
import { Scene, HeatmapLayer } from "@antv/l7";
import { computed, defineComponent, PropType, shallowRef, toRefs,watch } from "vue";
import { overspeedPos, TODO } from "../util/api";
import _ from 'lodash';
export default defineComponent({
    props: {
        scene: Scene,
        overspeedPosList: Object as PropType<{overspeedPos:overspeedPos, speed_limit:number}[]>, 
        display : Boolean
    },
    setup(props, context) {
        const { scene, overspeedPosList, display } = toRefs(props);
        if (!overspeedPosList || !scene ) {
            throw new Error("需要初始化所有props！")
        }
        // 考虑
        // TODO ： 根据给定的公交线路，等信息进行进一步的筛选。
        const data = computed(()=>{
            console.log("来了来了！",overspeedPosList.value!)
            return overspeedPosList.value!.map(v=>{
                return {lat : v.overspeedPos.lat, lont : v.overspeedPos.lont, speed : v.overspeedPos.speed-v.speed_limit}
            })
        })
        const layer = shallowRef(
            new HeatmapLayer()
                .source(data.value, {
                    parser: {
                        type: "json",
                        x : "lat",
                        y : "lont"
                    }
                })
                .size("speed" ,[0.1, 2]) // weight映射通道
                .style({
                    intensity: 1,
                    radius: 10,
                    rampColors: {
                        colors: [
                            'rgba(170,10,20, 0.95)',
                            'rgba(220,41,36,0.7)',
                            'rgba(245,68,50,0.6)',
                            'rgba(250,98,69,0.6)',
                            'rgba(252,129,97,0.5)',
                            'rgba(252,152,121,0.5)',
                            'rgba(252,185,159,0.2)',
                            'rgba(253,211,193,0.2)',
                            'rgba(254,231,220,0.1)',
                            'rgba(255,255,255,0.1)',
                        ].reverse(),
                        positions: [0 , 0.1, 0.2,0.3, 0.4,0.5, 0.6,0.7, 0.8, 0.9,1.0],
                    },
                })
        );
        watch(data, _.throttle((val)=>{
            layer.value.setData(data.value);
        },1000))
        scene.value!.addLayer(layer.value);
    
        watch(display, function(val) {
            if (val) 
                layer.value.show();
            else
                layer.value.hide();
        }, {immediate:true})

    },
});
</script>


<style>
</style>