import { ILayer, LineLayer, Scene } from "@antv/l7";
import axios from "axios"
import * as _ from "lodash";
import { readonly, Ref, shallowReadonly, shallowRef } from "vue";
import { BusAndAllNodeAdapter, busAndLine } from "../util/messageAdapter"

function initBuslineLayer(busAndLines: busAndLine) {
    let data = _(busAndLines.nodeIdsOfLine)
        .filter((__, lineId) => busAndLines.lineDisplay[Number(lineId)])
        .map((nodeIds, lineId) => {
            let latlonts = nodeIds.map(nodeId => {
                let node = busAndLines.nodeId2Node[nodeId]
                return [node.lat, node.lont]
            })
            return {lineId, coords: latlonts}
        }).value()
    let layer = new LineLayer()
        .source(data, {
            parser: {
                type: "json",
                coordinates: 'coords'
            }
        })
        .size(3)
        .shape('line')
        .color('rgba(0,0,127,0.3)')
        .animate({
          interval: 1, // 间隔
          duration: 1, // 持续时间，延时
          trailLength: 2 // 流线长度
        });
    return layer;
}

export default function (scene: Scene, place_name: string) {
    // 不需要onMounted了，因为调用这个函数的时候已经mount完了
    let layer = shallowRef<ILayer>();
    let busIdList = shallowRef<number[]>([])
    axios.get("http://localhost:8080/api/getBusAndAllNodeInfo?place_name=" + place_name)
        .then(res => {
            let busAndLineData = BusAndAllNodeAdapter(res.data)
            busIdList.value = Object.keys(busAndLineData.busId2LineId).map(v => Number(v))
            layer.value = initBuslineLayer(busAndLineData)
            scene.addLayer(layer.value)
        }).catch(err => console.log(err));
    return {layer, busIdList};
}
