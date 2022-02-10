<template>
    <div :id="mapId" style="height: 500px;
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

const useBuslineOption = function (
    allInfo: Ref<AllInfo>,
    visibleScope: Ref<{
        latScope: number[];
        lontScope: number[];
    }>
) {
    const buslineOption = computed(() => {
        return {};
    });
    const buslineSeries = computed(() => {
        // TODO
        const lines = _(allInfo.value.buslines)
            .map((v) => {
                if (
                    v.nodepath_id_list.some((v) => {
                        const nodepath = allInfo.value.nodepaths[v];
                        return !nodepath;
                    }) ||
                    v.nodepath_id_list
                        .map((id) => allInfo.value.nodepaths[id])
                        .some((v) => {
                            const node1 = allInfo.value.nodes[v.node1_id];
                            const node2 = allInfo.value.nodes[v.node2_id];
                            return !node1 || !node2;
                        })
                ) {
                    return null;
                }
                return {
                    line_id: v.line_id,
                    line_name: v.line_name,
                    startNodeId: v.start_node_id,
                    nodepaths: v.nodepath_id_list
                        .map((id) => allInfo.value.nodepaths[id])
                        .filter(v=>{
                            const node1 = allInfo.value.nodes[v.node1_id];
                            const node2 = allInfo.value.nodes[v.node2_id];
                            return !!node1 && !!node2
                        })
                        .filter((v) => {
                            const node1 = allInfo.value.nodes[v.node1_id];
                            const node2 = allInfo.value.nodes[v.node2_id];
                            return (
                                (node1.lat >= visibleScope.value.latScope[0] &&
                                    node1.lat <=
                                        visibleScope.value.latScope[1] &&
                                    node1.lont >=
                                        visibleScope.value.lontScope[0] &&
                                    node1.lont <=
                                        visibleScope.value.lontScope[1]) ||
                                (node2.lat >= visibleScope.value.latScope[0] &&
                                    node2.lat <=
                                        visibleScope.value.latScope[1] &&
                                    node2.lont >=
                                        visibleScope.value.lontScope[0] &&
                                    node2.lont <=
                                        visibleScope.value.lontScope[1])
                            );
                        })
                        .map((nodepath) => {
                            const node1 =
                                allInfo.value.nodes[nodepath.node1_id];
                            const node2 =
                                allInfo.value.nodes[nodepath.node2_id];
                            return {
                                nodepath_id: nodepath.nodepath_id,
                                coord: [
                                    [node1.lat, node1.lont],
                                    [node2.lat, node2.lont],
                                ] as [[number, number], [number, number]],
                            };
                        }),
                };
            })
            .compact()
            .value();
        return {
            id: "buslines",
            type: "lines",
            coordinateSystem: "bmap",
            polyline: true,
            zlevel: 1000,
            z: 1000,
            lineStyle: {
                width: 3,
                color: "rgba(25,25,125,0.5)",
            },
            data: _(lines)
                      .map((line) => {
                          const coords = _(line.nodepaths)
                              .map((v) => v.coord)
                              .flatten()
                              .value();
                          if (coords.length === 0) return null;
                          return {
                              name: "",
                              id: line.line_id,
                              startNodeId: line.startNodeId,
                              type: "BUSLINE",
                              coords: _(line.nodepaths)
                                  .map((v) => v.coord)
                                  .flatten()
                                  .value(),
                          };
                      })
                      .compact()
                      .value(),

            // TODO
        };
    });
    return { buslineSeries, buslineOption };
};

export default defineComponent({
    props : {
        allInfo : Object as PropType<AllInfo>,
        buslineOverspeedValue : Object as PropType<Record<number, number | null>>, // id对值，值有可能是空
        label : String as PropType<"秒" | null>
    },
    setup(props, context) {
        const {allInfo, buslineOverspeedValue, label} = toRefs(props);
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
                        const theBusline = allInfo?.value?.buslines[id];
                
                            return `
                            线路ID：${
                                theBusline!.line_id < 0
                                    ? `NEW${theBusline!.line_id}`
                                    : theBusline!.line_id
                            }<br />
                            线路名称：${theBusline?.line_name}<br />
                            ${!label ||  label.value === null  ? "总超速因子" : "总超速时间"}：${buslineOverspeedValue!.value![id] || 0}${!label ||  label.value === null ? "" : "秒"}
                        `;
                    },
                },
            };

        });
        const {buslineSeries} = useBuslineOption(allInfo as Ref, visibleScope)
        const { chart , elementId} = useChart(
            [
                computed(bmapOption),
                tooltip,
                computed(() => {
                    return { series: [buslineSeries.value] };
                }),
            ],
            (chart) => {        
                // 监听点击事件
                chart.on("click", (param) => {
                    console.log("ori", param);
                    if (param.seriesId !== "buslines") {
                        // 只允许点击线路
                        return
                    }
                    // 如果点击的线路没有超速，则提示并返回
                    // @ts-ignore 明明有，为什么说没有？
                    const id = param.data.id as number;
                    if (!buslineOverspeedValue!.value![id] || buslineOverspeedValue!.value![id] === 0) {
                        ElMessage.error("所选线路上没有超速！")
                        return;        
                    }
                    console.log(id)
                    const theBusline = allInfo.value?.buslines[id]
                    context.emit("clickBar", theBusline?.line_name)
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