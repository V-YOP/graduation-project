<!-- 提供一个button，用来呼出地图 -->
<template>
    <el-container style="height: 500px; width: 800px;position:relative;" @touchmove.prevent @scroll.prevent  @mousewheel.prevent>
        <scene :lat="lat" :lont="lont" darkMode @scene-loaded="sceneLoaded" @touchmove.prevent @scroll.prevent   @mousewheel.prevent/>
        <density-layer
            v-if="scene"
            :scene="scene"
            :display="true"
            :overspeedPosList="overspeedPosList"
            @touchmove.prevent @scroll.prevent  @mousewheel.prevent
        />
    </el-container>
</template>

<script lang="ts">
import { Scene } from "@antv/l7";
import {
    computed,
    defineComponent,
    PropType,
    Ref,
    ref,
    shallowRef,
    toRefs,
} from "vue";
import { overspeedPos } from "../util/api";
import scene from "../components/scene.vue";
import densityLayer from "../components/density-layer.vue";
import $ from "jquery"
function useScene(overspeedPoses: Ref<overspeedPos[]>) {
    const lat = ref(overspeedPoses!.value![0].lat);
    const lont = ref(overspeedPoses!.value![0].lont);
    const scene = shallowRef<Scene>();
    const sceneLoaded = function (scene_: Scene, element_id : number) {
        scene.value = scene_;
        scene_.on('zoomstart', () => {

        }); // 缩放开始时触发
        scene_.on('zoomend', () => {

        }); // 缩放停止时触发
    };
    console.log(overspeedPoses.value)
    const overspeedPosList = computed(() => {
      console.log("重新计算超速信息", overspeedPoses!.value!.length)
        return overspeedPoses!.value!.map((v) => {
            return { overspeedPos: v, speed_limit: v.speed_limit };
        });
    });
    
    return {
        lat,
        lont,
        sceneLoaded,
        overspeedPosList,
        scene,
    };
}

export default defineComponent({
    components: {
        scene,
        densityLayer,
    },
    props: {
        overspeedInfo: Array as PropType<overspeedPos[]>,
    },
    setup(props, context) {
        const { overspeedInfo } = toRefs(props);

        return {
            drawer: ref(false),
            ...useScene(overspeedInfo as Ref),
        };
    },
});
</script>


<style>
</style>