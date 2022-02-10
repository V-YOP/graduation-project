<!-- 将显示buspos的功能同获取公交车实时信息功能分离 -->
<template v-if="true">
<div id="buspos-marker" :style="{ display: (markerVisible ? `block` : `none`) }">
<span style="color:red">
    <el-card class="box-card">
    <el-space direction="vertical" alignment="left" :size="0">
            <div><span>车牌号：</span><el-tag>{{ bus_number }}</el-tag></div><br/>
            <div><span>车型：</span><el-tag> {{ bus_type }} </el-tag></div>
    </el-space>
    </el-card>
</span>
</div>
</template>
<script lang="ts">
// busline-layer可轻易重用，但显示实时位置的layer基本无法重用。
import { Scene } from "@antv/l7-scene";
import { computed, defineComponent, inject, onMounted, PropType, ref, Ref, shallowRef, toRefs, watch } from "vue";
import _ from "lodash";
import { ILayer, Marker, PointLayer } from "@antv/l7";
import {pos} from "../util/api"
import $ from 'jquery'
export default defineComponent({
    name: "buspos-layer",
    props: {
        scene: Scene,
        busposList: Object as PropType<pos[]>,
        display : Boolean,
        infoGetter : Function as PropType<(bus_id: number) => {
    bus_number: string;
    bus_type: string;
}>
    },
    setup(props, context) {
        const {display, busposList, infoGetter} = toRefs(props);
        let layer = shallowRef<ILayer> (
            new PointLayer({
                
            })
                .source(props.busposList!, {
                    parser: {
                        type: "json",
                        x: "lat",
                        y: "lont",
                    },
                })
                .shape("bus")
                .size(10)
        );
        props.scene!.addLayer(layer.value);
        // 这里不能直接监听props.posList，因为从其获取的对象是对象本身而非其proxy
        // 顺带一提，用普通对象居然不报错……are u kidding me？
        watch(busposList!, _.throttle((val)=>{
            layer.value.setData(val);
        }, 1000))
        watch(display, function(val) {
            if (val) 
                layer.value.show();
            else
                layer.value.hide();
        })
        const marker = new Marker({
            offsets:[0,10]
        })

        const markerVisible = ref(false);
        const currentSelectedBusId  = ref<number | null>(null);

        onMounted(()=>{
            marker.setLnglat({lng:0, lat:0})
            marker.setElement($("#buspos-marker").get()[0])
            props.scene!.addMarker(marker)
        })

        function selectItem(lat : number, lont : number, id : number) {
            console.log("选择到了")
            markerVisible.value = true;
            marker.setLnglat({lng : lat, lat : lont}).setElement($("#buspos-marker").get()[0])
            // TODO
        }
        function unselectItem() {
            markerVisible.value = false;
            marker.setLnglat({lng:0, lat:0}).setElement($("#buspos-marker").get()[0])
            currentSelectedBusId.value = null;
            // TODO
        }
        layer.value.on("mousemove",_.throttle((ev)=>{
            // 选择
            console.log("选择！",ev)
            let [lng, lat] : [number,number] = [ev.lngLat.lng, ev.lngLat.lat]
            currentSelectedBusId.value = ev.feature.bus_id as number;
            selectItem(lng, lat, ev.featureId as number)
        },50));
        layer.value.on('unmousemove', _.throttle((ev)=>{
            unselectItem();
        },50)); 
        
        const bus_type = computed (()=>{ 
            return infoGetter!.value!(currentSelectedBusId!.value!).bus_type
        })
        const bus_number = computed (()=>{ 
            return infoGetter!.value!(currentSelectedBusId!.value!).bus_number
        })
        return {
            markerVisible,
            currentSelectedBusId,
            bus_type,bus_number
        }


    },
});




// TODO: 这些代码将来有用
function wsClient(
    busIdList: number[],
    poses: Ref<Record<number, pos[]>>,
    layer: Ref<ILayer | undefined>,
    scene: Scene
) {
    let ws = new WebSocket("ws://localhost:8080/ws/getPos");
    ws.onopen = function () {
        ws.send(JSON.stringify(busIdList));
        console.log("发送busIdList:", JSON.stringify(busIdList));
    };
    ws.onmessage = function (res) {
        let data: pos[] = JSON.parse(res.data);
        if (!data || data.length === 0) return;
        // 根据data刷新poses
        data.filter(
            (v) =>
                !(
                    poses.value[v.bus_id].length > 0 &&
                    _.last(poses.value[v.bus_id])!.update_time === v.update_time
                )
        )
            .sort((a, b) => {
                if (a.update_time > b.update_time) return 1;
                return -1;
            })
            .forEach((v) => poses.value[v.bus_id].push(v));
        // 限制poses各位置数组长度
        _(poses.value).forEach((busPosList) => {
            if (busPosList.length > 100) busPosList.splice(0, 3);
        });
        // 刷新当前位置
        let lastPoses = _(poses.value)
            .map((poses, bus_id) => {
                let { lat, lont } = _.last(poses)!;
                return { bus_id, lat, lont };
            })
            .value();
        if (!layer.value) {
            console.log("her", lastPoses);
            console.log("加载后", scene);
            console.log(layer.value!);
            scene.addLayer(layer.value!);
        }
        layer.value!.setData(lastPoses);
        console.log(lastPoses);
    };
    return ws;
}

</script>


<style>
</style>