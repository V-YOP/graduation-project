import { Scene } from "@antv/l7"
import { GaodeMap } from "@antv/l7-maps";
import { markRaw, onMounted, shallowRef } from "vue"

/**
 * 初始化scene到某个全局变量。
 * @param id 要mount到的element的id
 * @param varName 将赋值到的全局变量的名称，即@code()
 * @param click 
 * @param afterLoaded 
 */
export default function (id: string, click?: (lat: number, lont: number) => void, afterLoaded?: (instance: Scene) => void) {
    // 浅层引用，以防止性能损失
    let scene = shallowRef<Scene>()
    onMounted(() => {
        scene.value = new Scene({
            id,
            map: new GaodeMap({
                style: "light",
                center: [116.770672, 40.159869],
                pitch: 20,
                token: "ad894ea468b09ac3d309ffd294bcea7c",
            }),
        });
        scene.value.on("loaded", () => {
            if (!scene.value) return
            scene.value.on("click", function (ev) {
                if (!click) return;
                click(ev.lnglat.lng, ev.lnglat.lat);
            })
            afterLoaded && afterLoaded(scene.value);
        })
    })
    return scene;
}