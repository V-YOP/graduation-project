<!-- 一个普通的包裹scene的类，-->
<template>
    <div :id="element_id" style="overflow:hidden;
    height: 100%;
    width: 100%;
    position: relative;"></div>
</template>

<script lang="ts">
import axios from "axios";
import { computed, defineComponent, onMounted, PropType, ref, shallowRef, toRefs, watch } from "vue";
import { ILayer, LineLayer, Scene ,GaodeMap, IPopup, Popup , Marker, MarkerLayer, Mapbox} from "@antv/l7";
import $ from 'jquery';
import _ from "lodash";
import { BusAndAllNodeAdapter, busAndLine } from "../util/messageAdapter"

function randomString(e?:number) {  
  e = e || 32;
  let t = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz1234567890",
  a = t.length,
  n = "";
  for (let i = 0; i < e; i++) n += t.charAt(Math.floor(Math.random() * a));
  return n
}

export default defineComponent({
    emits: ["scene-loaded"],
    props: { lat: Number, lont: Number, darkMode: Boolean },
    setup(props, context) {
        let scene = shallowRef<Scene>();
        const {darkMode} = toRefs(props);
        watch(darkMode!, function(val){
            scene.value!.setMapStyle(mapStyle.value)
        })
        const mapStyle = computed(()=>{
            if (darkMode.value) return "dark";
            return "light"
        })

        const element_id = ref("scene-container" + randomString(10))
        onMounted(() => {
            scene.value = new Scene({
                id: element_id.value,

                map: new GaodeMap({
                    style: mapStyle.value,
                    center: [props.lat || 116.770672, props.lont || 40.159869],
                    pitch: 20,
                    token: "ad894ea468b09ac3d309ffd294bcea7c",
                })
               /*
                map : new Mapbox({
                    style: mapStyle.value,
                    center: [props.lat || 116.770672, props.lont || 40.159869],
                    pitch: 4.00000000000001,
                    zoom: 10.210275860702593,
                    defaultLanguage: "zh",
                    token: "pk.eyJ1Ijoib2d1cmF0b21vbm9yaSIsImEiOiJja25mbWZmMmsyMWl1MnZwanhobHU0ejY2In0.3DqrLcglfqzDZ64BkYWXXA"
                })
                */
            });
            scene.value.on("loaded", () => {
                // 关闭滚动条，配置其属性
                scene.value!.addImage(
                    "bus",
                    "https://cdn.jsdelivr.net/gh/AOYMYKN/AOYMYKN.github.io@origin/master/img/bus.svg"
                );
                // @ts-ignore
                document.body.parentNode.style.overflow = "hidden";
                // 待emit成功则返回scene实例到其父组件
                context.emit("scene-loaded", scene.value, "map-container");
                scene.value!.on("click", ( res )=>{
                    // 这个事件在点击layer的时候同样会抛出，很麻烦。
                    // console.log("点击到了scene",res)
                })
                // 给scene添加一个marker-layer，以供后续使用
            });
            
        });
        return {scene, element_id}
    },
});
</script>

