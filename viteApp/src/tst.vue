<template>
    <scene :lat="115" :lont="40" @scene-loaded="sceneLoaded" />
</template>

<script lang="ts">
import { PointLayer, Scene } from "@antv/l7";
import { defineComponent, ref,watch } from "vue";
import scene from "./components/scene.vue";
export default defineComponent({
    components: { scene },
    methods: {
        sceneLoaded(scene: Scene) {
            let data  = ref<{lat : number, lont : number}[]>([
                {lat:100,lont:20}
            ]);
            const nodeLayer = new PointLayer({})
                        .source(data.value, {
                            parser: {
                                type: "json",
                                x: "lat",
                                y: "lont",
                            },
                        })
                        .shape("circle")
                        .size(4)
                        .active(true)
                        .color("#0A3663")
                        .style({
                            opacity: 0.5,
                            strokeWidth: 0,
                        });
                    scene.addLayer(nodeLayer);
                watch(data,function(val){
                    nodeLayer.setData(val)
                },{deep:true})
                nodeLayer.on("click", function(v){
                    console.log(v)
                })
                const stationLayer = new PointLayer({})
                        .source(data.value, {
                            parser: {
                                type: "json",
                                x: "lat",
                                y: "lont",
                            },
                        })
                        .shape("circle")
                        .size(4)
                        .active(true)
                        .color("#0A3663")
                        .style({
                            opacity: 0.5,
                            strokeWidth: 0,
                        });
                scene.on("click",function(v){
                    let lat = v.lnglat.lng;
                    let lont = v.lnglat.lat;
                    data.value.push({lat, lont})
                    console.log(data.value)
                })
        },
    },
    setup(props, context) {},
});
</script>


<style>
</style>