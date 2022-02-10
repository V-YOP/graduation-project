<!-- TODO: 切换显示公交车线路的功能 -->
<template>
<div id="busline-marker" :style="{ display: (markerVisible ? `block` : `none`) }">
<span style="color:red">
    <el-card class="box-card">
    <el-space direction="vertical" alignment="left" :size="0">
            <div><span>线路ID：</span><el-tag>{{ currentSelectedLineId }}</el-tag></div><br/>
            <div><span>线路名称：</span               
            ><el-tag>{{ markerBuslineName }}</el-tag></div>
    </el-space>
    </el-card>
</span>
</div>
</template>
<script lang="tsx">
import { ILayer, LineLayer, Marker, Scene } from "@antv/l7";
import axios from "axios";
import {
    computed,
    defineComponent,
    onMounted,
    PropType,
    ref,
    Ref,
    shallowRef,
    toRef,
    toRefs,
    watch
} from "vue";
import $ from 'jquery'
import { BusAndAllNodeAdapter, busAndLine } from "../util/messageAdapter";
import _, { throttle,  } from "lodash";
import { AllInfo } from "../util/api";
import { ElMessageBox } from "element-plus";
import message from "element-plus/lib/el-message/src/message";

export default defineComponent({

    props: {
        scene: Scene,
        busAndLineData: Object as PropType<AllInfo>,
        display: Boolean,
        focusLines: Array as PropType<number[]>,
    },

    emits: ["layer-loaded"],
    setup(props, context) {
        // 使用computed，考虑busAndLineData更新后直接重设
        const markerVisible = ref(false);
        const { busAndLineData, scene, display, focusLines } = toRefs(props);
        if (
            busAndLineData === undefined ||
            busAndLineData.value === undefined
        ) {
            throw Error("需要传入！");
            return;
        }
        const buslineData = computed(() => {
            let values = _(busAndLineData.value!.buslines).values();
            if (focusLines!.value!.length !== 0)
                values = values.filter(
                    (line) =>
                        !!_(focusLines!.value!).find((v) => v === line.line_id)
                );
            console.log(
                "这些公交线路！",
                values.value(),
                _(busAndLineData.value!.buslines)
                    .values()
                    .map((v) => v.line_id)
                    .value(),
                focusLines!.value!
            );
            return values
                .map((busline) => {
                    let firstNode = busAndLineData!.value!.nodes[
                        busline.start_node_id
                    ];
                    const coords =  (firstNode ? [[firstNode.lat, firstNode.lont]] : []).concat(
                            _(busline.nodepath_id_list).map((nodepath_id) => {
                                if (busAndLineData.value!.nodepaths[nodepath_id] === undefined) {
                                    return null
                                }
                                let node = busAndLineData.value!.nodes[
                                        busAndLineData.value!.nodepaths[nodepath_id]
                                            .node2_id
                                    ];
                                return [node.lat, node.lont];
                            }).compact().value()
                        )
                    return {
                        lineId: busline.line_id,
                        coords
                    };
                })
                .value();
        });
        let buslineLayer = shallowRef<ILayer>(
            new LineLayer({
                    pickingBuffer: 20,
                })
                .source(buslineData.value, {
                    parser: {
                        type: "json",
                        coordinates: "coords",
                    },
                })
                .size(2)
                .shape("line")
                //.select({color: "rgba(0,0,255,0.5)" })
                .active( {color:"rgba(255,127,0,0.7)" })
                .color("rgba(20,20,127,0.3)")
                .animate({
                    interval: 1, // 间隔
                    duration: 1, // 持续时间，延时
                    trailLength: 2, // 流线长度
                })
        );
        const marker = new Marker({
            offsets:[0,10]
        })
        const currentSelectedLineId  = ref<number | null>(null);
        onMounted(()=>{
            marker.setLnglat({lng:0, lat:0})
            marker.setElement($("#busline-marker").get()[0])
            props.scene!.addMarker(marker)
        })
        function selectItem(lat : number, lont : number, id : number) {
            console.log("选择到了")
            markerVisible.value = true;
            marker.setLnglat({lng : lat, lat : lont}).setElement($("#busline-marker").get()[0])
            // TODO
        }
        function unselectItem() {
            markerVisible.value = false;
            marker.setLnglat({lng:0, lat:0}).setElement($("#busline-marker").get()[0])
            currentSelectedLineId.value = null;
            // TODO
        }
        buslineLayer.value.on("mousemove",_.throttle((ev)=>{
            // 选择
            let [lng, lat] : [number,number] = [ev.lngLat.lng, ev.lngLat.lat]
            currentSelectedLineId.value = ev.feature.lineId as number;
            selectItem(lng, lat, ev.featureId as number)
        },50));
        buslineLayer.value.on('unmousemove', _.throttle((ev)=>{
            unselectItem();
        },50));
        props.scene!.addLayer(buslineLayer.value);

        const busStationData = computed(() => {
            // TODO
        });
        watch(display, function (val) {
            if (val) {
                buslineLayer.value.show();
            } else {
                buslineLayer.value.hide();
            }
        });
        watch(buslineData, function (val) {
            buslineLayer.value.setData(val);
        });
        return { 
            markerVisible,
            currentSelectedLineId,
            busAndLineData,
            markerBuslineName : computed(()=>{
                if (!busAndLineData.value || !currentSelectedLineId.value)
                    return "LOADING"
                return busAndLineData.value.buslines[currentSelectedLineId.value].line_name
            })
        }
    },
});

// * 此函数弃用
function initBuslineLayer(busAndLines: busAndLine): any {
    let data = _(busAndLines.nodeIdsOfLine)
        .filter((_, lineId) => busAndLines.lineDisplay[Number(lineId)])
        .map((nodeIds, lineId) => {
            let latlonts = nodeIds.map((nodeId) => {
                let node = busAndLines.nodeId2Node[nodeId];
                return [node.lat, node.lont];
            });
            return { lineId, coords: latlonts };
        })
        .value();
    let buslineLayer = new LineLayer({
        pickingBuffer: 100,
    })
        .source(data, {
            parser: {
                type: "json",
                coordinates: "coords",
            },
        })
        .size(2)
        .shape("line")
        .color("rgba(0,0,127,0.5)")
        .active({
            color: "red",
        })
        .animate({
            interval: 1, // 间隔
            duration: 1, // 持续时间，延时
            trailLength: 2, // 流线长度
        });

    return buslineLayer;
}
</script>

<style>
</style>