import { ILayer, LineLayer, PointLayer, Scene } from "@antv/l7";
import axios from "axios"
import * as _ from "lodash";
import { readonly, ref, Ref, shallowReadonly, shallowRef } from "vue";
import { BusAndAllNodeAdapter, busAndLine } from "../util/messageAdapter"
export type pos = {
    bus_id: number,
    update_time: string,
    speed: string,
    lat: number,
    lont: number
}
function wsClient(busIdList: number[], poses: Record<number, pos[]>, layer: Ref<ILayer | undefined>, scene: Scene) {
    let ws = new WebSocket("ws://localhost:8080/ws/getPos");
    ws.onopen = function () {
        ws.send(JSON.stringify(busIdList));
        console.log("发送busIdList:", JSON.stringify(busIdList));
    };
    ws.onmessage = function (res) {
        let data: pos[] = JSON.parse(res.data)
        if (!data || data.length === 0) return
        // 根据data刷新poses
        data.filter((v) => !(
                poses[v.bus_id].length > 0 &&
                _.last(poses[v.bus_id])!.update_time === v.update_time
            ))
            .sort((a, b) => {
                if (a.update_time > b.update_time)
                    return 1;
                return -1;
            })
            .forEach(v => poses[v.bus_id].push(v))
        // 限制poses各位置数组长度
        _(poses).forEach((busPosList)=>{
            if (busPosList.length > 100)
                busPosList.splice(0,3);
        })
        // 刷新当前位置
        let lastPoses = _(poses)
            .map((poses, bus_id)=>{
                let {lat, lont} = _.last(poses)!
                return {bus_id , lat, lont}
            })
            .value()
        if (!layer.value) {
            console.log("her", lastPoses)
            layer.value = new PointLayer()
                .source([], {
                        parser: {
                            type: "json",
                            x: "lat",
                            y: "lont"
                        }
                    })
                .shape( 'bus')
                .size(10);

            console.log("加载后",scene)
            console.log(layer.value!)
            scene.addLayer(layer.value!)

        }
        layer.value!.setData(lastPoses)
        console.log(lastPoses);
        // 刷新当前热力图
        // TODO
    };
    return ws;
}

export default function (scene: Scene, busIdList: number[]) {
    scene.addImage(
        'bus',
        "https://cdn.jsdelivr.net/gh/AOYMYKN/AOYMYKN.github.io@origin/master/img/bus.svg"
      );
    // 不需要onMounted了，因为调用这个函数的时候已经mount完了
    let layer = shallowRef<ILayer>();
    // bus_id -> poses
    let poses : Ref<Record<number, pos[]>> = ref({})
    busIdList.forEach(v=>poses.value[v]=[])
    let ws = wsClient(busIdList, poses.value, layer, scene)
    return {layer, poses};
}
