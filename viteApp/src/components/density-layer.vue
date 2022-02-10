<!-- 使用这个图替代一下热力图试试  -->
<template>
  
</template>

<script lang="ts">

import { Scene, PointLayer } from "@antv/l7";
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
        const data = computed(()=>{
            return overspeedPosList.value!.map(v=>{
                return {lat : v.overspeedPos.lat, lont : v.overspeedPos.lont, speed : v.overspeedPos.speed-v.speed_limit}
            })
        })
        const layer = shallowRef(
            new PointLayer(
                {
                    blend : "normal"
                }
            )
                .source(data.value, {
                    parser: {
                        type: "json",
                        x : "lat",
                        y : "lont"
                    }
                })
                .shape("dot")
                .size("speed", [0.2, 1])
                .color('speed',  [
                            'rgba(100,0,2)',
                            'rgb(150,4,9)',
                            'rgb(170,10,20)',
                            'rgb(250,98,69)',
                            'rgba(252,129,97,0.6)',
                            'rgba(252,152,121,0.8)',
                            'rgba(254,231,220,1)',
                        ])
                .style({
                    opacity: 1,
                })
        );
        watch(data, _.throttle((val)=>{
            console.log("density-layer重设")
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