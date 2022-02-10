<template>
    <div :id="mapId" style="height: 400px;
    width: 400px;
    position: relative;"></div>
</template>

<script lang="ts">
import { ElMessage } from 'element-plus';
import _ from 'lodash';
import { computed, defineComponent, PropType, ref, Ref, toRefs } from 'vue'
import bmapStyle from '../../admin/bmapStyle';
import { AllInfo } from '../../util/api'
import { useChart } from '../useChart';

const useNodeOption = function (
    allInfo: Ref<AllInfo>,
    visibleScope: Ref<{
        latScope: number[];
        lontScope: number[];
    }>
) {
    // 节点的series
    const nodeOption = computed(() => {
        return {};
    });
    const nodeSeries = computed(() => {
        return {
            id: "nodes",
            type: "scatter",
            coordinateSystem: "bmap",
            silent: true,
            symbolSize: 5,
            data: _(allInfo.value.nodes)
                .values()
                .filter((v) => {
                    return (
                        v.lat >= visibleScope.value.latScope[0] &&
                        v.lat <= visibleScope.value.latScope[1] &&
                        v.lont >= visibleScope.value.lontScope[0] &&
                        v.lont <= visibleScope.value.lontScope[1]
                    ) && v.isStation;
                })
                .map((v) => {
                    return {
                        name: "",
                        id: v.node_id,
                        type: "NODE",
                        isStation: v.isStation,
                        value: [v.lat, v.lont, 1],
                    };
                })
                .value(),
        };
    });
    return {
        nodeSeries,
        nodeOption,
    };
};

const useNodepathOption = function (
    allInfo: Ref<AllInfo>,
    visibleScope: Ref<{
        latScope: number[];
        lontScope: number[];
    }>
) {
    const nodepathOption = computed(() => {
        return {};
    });
    const nodepathSeries = computed(() => {
        return {
            id: "nodepaths",
            type: "lines",
            coordinateSystem: "bmap",
            lineStyle: {
                width: 5,
            },
            symbol: ["none", "arrow"],
            symbolSize: 12,
            data: _(allInfo.value.nodepaths)
                .values()
                .filter(v=>{
                    const node1 = allInfo.value.nodes[v.node1_id];
                    const node2 = allInfo.value.nodes[v.node2_id];
                     return node1 && node2
                })
                .filter((v) => {
                    const node1 = allInfo.value.nodes[v.node1_id];
                    const node2 = allInfo.value.nodes[v.node2_id];
                    return (
                        (node1.lat >= visibleScope.value.latScope[0] &&
                            node1.lat <= visibleScope.value.latScope[1] &&
                            node1.lont >= visibleScope.value.lontScope[0] &&
                            node1.lont <= visibleScope.value.lontScope[1]) ||
                        (node2.lat >= visibleScope.value.latScope[0] &&
                            node2.lat <= visibleScope.value.latScope[1] &&
                            node2.lont >= visibleScope.value.lontScope[0] &&
                            node2.lont <= visibleScope.value.lontScope[1])
                    );
                })
                .map((v) => {
                    const node1 = allInfo.value.nodes[v.node1_id];
                    const node2 = allInfo.value.nodes[v.node2_id];
                    return {
                        name: "",
                        id: v.nodepath_id,
                        nodeId1: node1.node_id,
                        nodeId2: node2.node_id,
                        type: "NODEPATH",
                        speedLimit: v.speed_limit,
                        street_name: v.street_name,
                        coords: [
                            [node1.lat, node1.lont],
                            [node2.lat, node2.lont],
                        ],
                    };
                })
                .value(),
        };
    });
    return {
        nodepathOption,
        nodepathSeries,
    };
};

export default defineComponent({
    props : {
        allInfo : Object as PropType<AllInfo>,
        nodepathOverspeedValue : Object as PropType<Record<number, number | null>>, // id对值，值有可能是空
        label : String as PropType<"秒" | null>
    },
    setup(props, context) {
        const {allInfo, nodepathOverspeedValue, label} = toRefs(props);
        if (!allInfo || !allInfo.value) {
            throw new Error("没有获得啊，信息！")
        }
        // 用随机的一个啊，节点的位置，啊，作为啊，中心，啊
        const {lat, lont} = _(allInfo!.value?.nodes).sample()!
        let center = [116, 40];
        const visibleScope = ref({
            latScope: [-1000, 1000],
            lontScope: [-1000, 1000],
        });
        let zoom = 14;
        const bmapOption = () => {
            return {
                bmap: {
                    center: [center[0], center[1]],
                    zoom: zoom,
                    roam: true,
                    mapStyle: {
                        styleJson: bmapStyle,
                    },
                },
            };
        };
        const { nodeOption, nodeSeries } = useNodeOption(
            allInfo as Ref,
            visibleScope
        );
        const { nodepathOption, nodepathSeries } = useNodepathOption(
            allInfo as Ref,
            visibleScope
        );
        const tooltip = computed(() => {
            return {
                tooltip: {
                    trigger: "item",
                    formatter: function (params: any) {
                        if (!params.data) {
                            // 不应当调用
                            return null;
                        }
                        const {
                            id,
                            type,
                        }: { id: number; type: "NODE" | "NODEPATH" } =
                            params.data;
                        if (type === "NODE") {
                            return null;
                        }
                        // 是线路的时候
                        const {
                            speedLimit,
                            street_name,
                        }: { speedLimit: number; street_name: string } =
                            params.data;
                        
                        return `
                            路径ID: ${id < 0 ? "new-" + Math.abs(id) : id}<br />
                            道路名称：${street_name}<br />
                            限速：${speedLimit} km/h<br />
                            ${!label ||  label.value === null  ? "总超速因子" : "总超速时间"}：${nodepathOverspeedValue!.value![id] || 0}${!label ||  label.value === null ? "" : "秒"}
                        `;
                    },
                },
            };

        });
    
        const { chart , elementId} = useChart(
            [
                computed(bmapOption),
                tooltip,
                nodeOption,
                nodepathOption,
                computed(() => {
                    return { series: [nodeSeries.value] };
                }),
                computed(() => {
                    return { series: [nodepathSeries.value] };
                }),
            ],
            (chart) => {
                // 监听点击事件
                chart.on("click", (param) => {
                    console.log("ori", param);
                    if (param.seriesId !== "nodepaths") {
                        // 只允许点击线路
                        return
                    }
                    // 如果点击的线路没有超速，则提示并返回
                    // @ts-ignore 明明有，为什么说没有？
                    const id = param.data.id as number;
                    if (!nodepathOverspeedValue!.value![id] || nodepathOverspeedValue!.value![id] === 0) {
                        ElMessage.error("所选线路上没有超速！")
                        return;
                    }
                    console.log(id)
                    const theNodepath = allInfo.value?.nodepaths[id]
                    context.emit("clickBar", theNodepath?.street_name)
                    // TODO 干点什么！
                });
                // @ts-ignore
                let bmapObject = null;
                chart.on("finished", function mapInited() {
                    // @ts-ignore
                    if (
                        !chart.getOption() ||
                        // @ts-ignore
                        !chart._componentsMap[
                            "_ec_\u0000series\u00000\u00000_bmap"
                        ]
                    )
                        return;
                    // 似乎一定能成功获取到
                    // @ts-ignore 获得bmap对象
                    bmapObject =
                        // @ts-ignore
                        chart._componentsMap[
                            "_ec_\u0000series\u00000\u00000_bmap"
                        ].__model.getBMap();
                    const leftTop = bmapObject.pixelToPoint({ x: 0, y: 0 });
                    const currentSize = bmapObject.getSize();
                    const rightBottom = bmapObject.pixelToPoint({
                        x: currentSize.width,
                        y: currentSize.height,
                    });
                    visibleScope.value = {
                        latScope: [
                            Math.min(leftTop.lng, rightBottom.lng),
                            Math.max(leftTop.lng, rightBottom.lng),
                        ],
                        lontScope: [
                            Math.min(leftTop.lat, rightBottom.lat),
                            Math.max(leftTop.lat, rightBottom.lat),
                        ],
                    };
                    chart.off("finished", mapInited);
                });
                chart.on(
                    "bmapRoam",
                    _.debounce((ev) => {
                        console.log("fuckme!");
                        // @ts-ignore
                        const currentCenter = chart.getOption().bmap[0]
                            .center as [number, number];
                        // @ts-ignore
                        const currentZoom = chart.getOption().bmap[0]
                            .zoom as number;
                        center[0] = currentCenter[0];
                        center[1] = currentCenter[1];
                        zoom = currentZoom;
                        // @ts-ignore
                        const leftTop = bmapObject.pixelToPoint({ x: 0, y: 0 });
                        // @ts-ignore
                        const currentSize = bmapObject.getSize();
                        // @ts-ignore
                        const rightBottom = bmapObject.pixelToPoint({
                            x: currentSize.width,
                            y: currentSize.height,
                        });
                        visibleScope.value = {
                            latScope: [
                                Math.min(leftTop.lng, rightBottom.lng),
                                Math.max(leftTop.lng, rightBottom.lng),
                            ],
                            lontScope: [
                                Math.min(leftTop.lat, rightBottom.lat),
                                Math.max(leftTop.lat, rightBottom.lat),
                            ],
                        };
                    }, 200)
                );
            },
            true
        );
    
        return {
            mapId : elementId
        }
    },
})
</script>


<style>

</style>