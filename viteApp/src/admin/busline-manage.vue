<!-- 
对节点和路径进行管理。
-->

<template>
    <el-container style="height: 100%; width: 100%">
        <el-header style="display: flex; align-items: center">
            <el-space :size="10">
                <el-switch
                    style="display: block; float: right"
                    :disabled="disabledButtons"
                    v-model="displayNode"
                    active-text="显示节点"
                ></el-switch>
                <el-switch
                    style="display: block; float: right"
                    :disabled="disabledButtons"
                    v-model="displayNodepath"
                    active-text="显示路径"
                >
                </el-switch>
                <el-switch
                    style="display: block; float: right"
                    :disabled="disabledButtons"
                    v-model="displayBusline"
                    active-text="显示线路"
                >
                </el-switch>
                <el-button
                    type="success"
                    @click="addBusline"
                    :disabled="disabledButtons"
                    >添加线路</el-button
                >
                <el-button
                    type="warning"
                    @click="editBusline"
                    :disabled="disabledButtons"
                    >编辑线路</el-button
                >
                <el-button
                    type="danger"
                    @click="deleteBusline"
                    :disabled="disabledButtons"
                    >删除线路</el-button
                >
                <el-button
                    type="success"
                    @click="confirm"
                    v-if="disabledButtons"
                    >完成</el-button
                >
                <el-button @click="reset" v-if="disabledButtons"
                    >重置</el-button
                >
            </el-space>
        </el-header>
        <el-main style="padding: 0" v-loading="mapLoading">
            <div
                :id="elementId"
                style="height: 100%; width: 100%; position: relative"
            ></div>
        </el-main>
        <el-footer :height="30" style="display: flex; align-items: center">
            tips:{{ tips }}
        </el-footer>

        <el-dialog :title="confirmDialogTitle" v-model="confirmDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>线路名称</span>
                    <el-input v-model="confirmDialog_line_name"> </el-input>
                </p>
                <p>
                    <span>经过区域：</span>

                    <el-checkbox-group v-model="confirmDialog_adcode">
                        <el-checkbox-button
                            v-for="place in places"
                            :label="place.adcode"
                            :key="place.adcode"
                            >{{ place.name }}</el-checkbox-button
                        >
                    </el-checkbox-group>
                </p>
            </el-space>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="confirmDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmEdit"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>
    </el-container>
</template>

<script lang="ts">
import {
    computed,
    defineComponent,
    inject,
    onMounted,
    PropType,
    provide,
    Ref,
    ref,
    toRefs,
    watch,
    h,
    reactive,
    triggerRef,
} from "vue";
import { AllInfo } from "../util/api";
import { useChart } from "../speedAnalysis/useChart";
import "echarts/extension/bmap/bmap";
import _ from "lodash";
import { EChartsType } from "echarts";
import * as echarts from "echarts";
import {
    DeleteBusFromPlaceOption,
    DeleteBuslineOption,
    DeleteLineFromPlaceOption,
    DeleteNodeOption,
    DeleteNodepathOption,
    InsertBuslineOption,
    InsertLineIntoPlaceOption,
    InsertNodeOption,
    InsertNodepathOption,
    OptionList,
    UpdateBuslineOption,
    UpdateNodeOption,
    UpdateNodepathOption,
} from "../util/adminApi";

import { ElMessage, ElMessageBox } from "element-plus";
import bmapStyle from "./bmapStyle";
import { getPath } from "./graph";
// 用来方便地……麻了
const stor: Record<string, any> = {};

function localProvide(name: string, arg: any) {
    stor[name] = arg;
}
function localInject(name: string) {
    return stor[name];
}

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
        const display = localInject("displayNode").value;
        return {
            id: "nodes",
            type: "scatter",
            coordinateSystem: "bmap",
            large: false,
            symbolSize: 5,
            data: !display
                ? []
                : _(allInfo.value.nodes)
                      .values()
                      .filter((v) => {
                          return (
                              v.lat >= visibleScope.value.latScope[0] &&
                              v.lat <= visibleScope.value.latScope[1] &&
                              v.lont >= visibleScope.value.lontScope[0] &&
                              v.lont <= visibleScope.value.lontScope[1]
                          );
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
        const display = localInject("displayNodepath").value;
        return {
            id: "nodepaths",
            type: "lines",
            coordinateSystem: "bmap",
            large: false,
            lineStyle: {
                width: 5,
            },
            symbol: ["none", "arrow"],
            symbolSize: 12,
            data: !display
                ? []
                : _(allInfo.value.nodepaths)
                      .values()
                      .filter(v=>{
                            const node1 = allInfo.value.nodes[v.node1_id];
                            const node2 = allInfo.value.nodes[v.node2_id];
                            return (!!node1 && !!node2)
                        })
                      .filter((v) => {
                          const node1 = allInfo.value.nodes[v.node1_id];
                          const node2 = allInfo.value.nodes[v.node2_id];
                          return (
                              (node1.lat >= visibleScope.value.latScope[0] &&
                                  node1.lat <= visibleScope.value.latScope[1] &&
                                  node1.lont >=
                                      visibleScope.value.lontScope[0] &&
                                  node1.lont <=
                                      visibleScope.value.lontScope[1]) ||
                              (node2.lat >= visibleScope.value.latScope[0] &&
                                  node2.lat <= visibleScope.value.latScope[1] &&
                                  node2.lont >=
                                      visibleScope.value.lontScope[0] &&
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
        const display = localInject("displayBusline").value;
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
            data: !display
                ? []
                : _(lines)
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

export type clickParam = {
    latLont: [number, number];
    target: {
        id: number;
        type: "NODE" | "NODEPATH" | "BUSLINE";
    } | null;
};

const enableClickEvt = function (
    chart: echarts.ECharts,
    allInfo: Ref<AllInfo>,
    optionList: Ref<OptionList>,
    places: Ref<{ adcode: number; name: string }[]>
) {
    // @ts-ignore
    let bmapObject = null;
    const click1 = ref(false);
    const click2 = ref(false);
    const allParam = ref<{ latLont: [number, number]; target: Object }>({
        latLont: [0, 0],
        target: {},
    });
    chart.on("click", (param) => {
        click1.value = true;
        allParam.value.target = param.data;
    });
    chart.getZr().on("click", (params) => {
        // @ts-ignore
        if (!bmapObject) {
            // @ts-ignore
            bmapObject =
                // @ts-ignore
                chart._componentsMap[
                    "_ec_\u0000series\u00000\u00000_bmap"
                ].__model.getBMap();
        }
        // @ts-ignore
        const { lng, lat }: { lng: number; lat: number } =
            // @ts-ignore
            bmapObject.pixelToPoint({
                // @ts-ignore
                x: params.event.offsetX,
                // @ts-ignore
                y: params.event.offsetY,
            });
        const latLont = [lng, lat] as [number, number];
        allParam.value.latLont = latLont;
        click2.value = true;
    });
    watch([click1, click2], (v) => {
        setTimeout(() => {
            const latLont = allParam.value.latLont;
            const target = allParam.value.target;
            if (v[0]) {
                // 点击到对象
                handleClick(
                    {
                        latLont: latLont,
                        target: {
                            // @ts-ignore
                            id: target.id,
                            // @ts-ignore
                            type: target.type,
                        },
                    },
                    allInfo.value,
                    optionList.value
                );
            } else if (v[1]) {
                // 点击到地图
                handleClick(
                    {
                        latLont: latLont,
                        target: null,
                    },
                    allInfo.value,
                    optionList.value
                );
            }
            click1.value = false;
            click2.value = false;
        }, 5);
    });
};

export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        places: Object as PropType<{ adcode: number; name: string }[]>,
    },
    setup(props, context) {
        const { info, places } = toRefs(props);
        localProvide("places", places);
        const optionList = inject("optionList") as Ref<OptionList>;
        const lastSelectedNodeId = ref<number | null>(null);
        localProvide("lastSelectedNodeId", lastSelectedNodeId);
        const mapLoading = ref(false);
        let center = [116, 40];
        const adcode2BuslineIds = inject("adcode2BuslineIds") as Ref<
            Record<number, number[]>
        >;
        watch(
            adcode2BuslineIds,
            (val) => {
                console.log("adcode2BuslineIds重新计算");
            },
            { deep: true }
        );
        localProvide("adcode2BuslineIds", adcode2BuslineIds);
        const buslineId2Adcodes = computed<Record<number, number[]>>(() => {
            console.log("buslineId2Adcodes重新计算");
            const res: Record<number, number[]> = {};
            _(adcode2BuslineIds.value).forEach((busline, adcode) => {
                busline.forEach((line_id) => {
                    if (!res[line_id]) res[line_id] = [];
                    res[line_id].push(Number(adcode));
                });
            });
            return res;
        });
        localProvide("buslineId2Adcodes", buslineId2Adcodes);
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
        const buttonHandler_ = buttonHandler(info as Ref, optionList);
        const viewer = useViewer();

        const { nodeOption, nodeSeries } = useNodeOption(
            info as Ref,
            visibleScope
        );
        const { nodepathOption, nodepathSeries } = useNodepathOption(
            info as Ref,
            visibleScope
        );
        const { buslineOption, buslineSeries } = useBuslineOption(
            info as Ref,
            visibleScope
        );
        const {
            startPointAndEndPointSeries,
            presentSelectedNodeSeries,
            nextBuslineOption,
            nodepathOfLineSeries,
        } = useNextBuslineOption(info as Ref);
        const tooltip = computed(() => {
            return {
                tooltip: {
                    trigger: "item",
                    formatter: function (params: any, p1: any) {
                        if (!params.data) {
                            // 不应当调用
                            return null;
                        }
                        const {
                            id,
                            type,
                        }: {
                            id: number;
                            type: "NODE" | "NODEPATH" | "BUSLINE";
                        } = params.data;
                        if (type === "NODE") {
                            const name = places?.value?.find(
                                (v) =>
                                    v.adcode === info?.value?.nodes[id].adcode
                            )?.name;
                            const {
                                isStation,
                                value,
                            }: { isStation: boolean; value: [number, number] } =
                                params.data;
                            return `
                            节点ID：${
                                id < 0 ? "new-" + Math.abs(id) : id
                            }<br />    
                            经纬度：${value[0]}，${value[1]}<br />
                            是否站点：${isStation ? "是" : "否"}<br />
                            所属地区：${name}
                            `;
                        } else if (type === "NODEPATH") {
                            // 是线路的时候
                            const {
                                speedLimit,
                                street_name,
                            }: { speedLimit: number; street_name: string } =
                                params.data;
                            const name = places?.value?.find(
                                (v) =>
                                    v.adcode ===
                                    info?.value?.nodepaths[id].adcode
                            )?.name;
                            return `
                                路径ID: ${
                                    id < 0 ? "new-" + Math.abs(id) : id
                                }<br />
                                道路名称：${street_name}<br />
                                限速：${speedLimit} km/h<br />
                                所属地区：${name}
                            `;
                        } else if (type === "BUSLINE") {
                            // TODO : 线路
                            const { id }: { id: number } = params.data;
                            const theBusline = info?.value?.buslines[id];
                            const adcodes = localInject("buslineId2Adcodes")
                                .value[theBusline!.line_id] as number[];
                            const names = adcodes.map((v) => {
                                return places?.value?.find(
                                    (j) => j.adcode === v
                                )?.name;
                            });
                            return `
                            线路ID：${
                                theBusline!.line_id < 0
                                    ? `NEW${theBusline!.line_id}`
                                    : theBusline!.line_id
                            }<br />
                            线路名称：${theBusline?.line_name}<br />
                            所属地区：${names.join("，")}<br />
                            `;
                        }
                        return null;
                    },
                },
            };
        });
        const { chart, elementId } = useChart(
            [
                computed(bmapOption),
                tooltip,
                nodeOption,
                nodepathOption,
                buslineOption,
                nextBuslineOption,
                computed(() => {
                    return { series: [nodeSeries.value] };
                }),
                computed(() => {
                    return { series: [nodepathSeries.value] };
                }),
                computed(() => {
                    return { series: [buslineSeries.value] };
                }),
                computed(() => {
                    return { series: [startPointAndEndPointSeries.value] };
                }),
                computed(() => {
                    return { series: [presentSelectedNodeSeries.value] };
                }),
                computed(() => {
                    return { series: [nodepathOfLineSeries.value] };
                }),
            ],
            (chart) => {
                // 监听点击事件
                enableClickEvt(
                    chart,
                    info as Ref,
                    inject("optionList") as Ref,
                    places as Ref
                );
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

                        chart.on("finished", function afterRender() {
                            chart.off("finished", afterRender);
                        });
                    }, 200)
                );
            },
            true
        );
        const tips = ref("");
        localProvide("tips", tips);
        localProvide("triggerRef", () => {
            triggerRef(info as Ref);
        });
        return {
            mapLoading,
            places,
            ...viewer,
            ...buttonHandler_,
            tips,
            elementId,
        };
    },
});

function useViewer() {
    const displayNode = ref(true);
    const displayNodepath = ref(true);
    const displayBusline = ref(true);
    localProvide("displayNode", displayNode);
    localProvide("displayNodepath", displayNodepath);
    localProvide("displayBusline", displayBusline);
    return {
        displayNode,
        displayNodepath,
        displayBusline,
    };
}
function setClicker(
    clicker: (arg: clickParam, allInfo: AllInfo, optionList: OptionList) => void
) {
    localProvide("clicker", clicker);
}

function handleClick(
    arg: clickParam,
    allInfo: AllInfo,
    optionList: OptionList
) {
    // TODO : 干活！
    // TODO : 使用一些按钮来直接选择进行操作，比如点击添加线路，就整个转换到该流程。
    if (localInject("clicker"))
        localInject("clicker")(arg, allInfo, optionList);
}
function useNextBuslineOption(allInfo: Ref<AllInfo>) {
    const lastSelectedNodeId = localInject("lastSelectedNodeId") as Ref<
        number | null
    >;
    const currentBusline = localInject("currentBusline") as Ref<currentBusline>;
    const nextBuslineOption = computed(() => {
        return {};
    });
    const startPointAndEndPointSeries = computed(() => {
        return {
            type: "scatter",
            id: "起始点和终止点",
            name: "起始点和终止点",
            silent: true,
            coordinateSystem: "bmap",
            symbolSize: 20,
            data: _([
                currentBusline.value.start_node_id !== null
                    ? {
                          label: {
                              show: true,
                              formatter: "起始点",
                          },
                          value: [
                              allInfo.value.nodes[
                                  currentBusline.value.start_node_id
                              ].lat,
                              allInfo.value.nodes[
                                  currentBusline.value.start_node_id
                              ].lont,
                          ],
                      }
                    : null,
                currentBusline.value.end_node_id !== null
                    ? {
                          label: {
                              show: true,
                              formatter: "终止点",
                          },
                          value: [
                              allInfo.value.nodes[
                                  currentBusline.value.end_node_id
                              ].lat,
                              allInfo.value.nodes[
                                  currentBusline.value.end_node_id
                              ].lont,
                          ],
                      }
                    : null,
            ])
                .compact()
                .value(),
        };
    });
    const presentSelectedNodeSeries = computed(() => {
        return {
            type: "scatter",
            name: "当前选择节点",
            id: "当前选择节点",
            coordinateSystem: "bmap",
            silent: true,
            symbolSize: 15,
            data:
                lastSelectedNodeId.value === null
                    ? []
                    : [
                          [
                              allInfo.value.nodes[lastSelectedNodeId.value].lat,
                              allInfo.value.nodes[lastSelectedNodeId.value]
                                  .lont,
                          ],
                      ],
        };
    });
    const nodepathOfLineSeries = computed(() => {
        const nodepaths = currentBusline.value.nodepath_id_list.map(
            (id) => allInfo.value.nodepaths[id]
        );
        const coords = nodepaths.map((nodepath) => {
            const node1 = allInfo.value.nodes[nodepath.node1_id];
            const node2 = allInfo.value.nodes[nodepath.node2_id];
            return [
                [node1.lat, node1.lont],
                [node2.lat, node2.lont],
            ] as [[number, number], [number, number]];
        });
        return {
            type: "lines",
            name: "当前路径",
            id: "当前路径",
            coordinateSystem: "bmap",
            silent: true,
            lineStyle: {
                width: 5,
                color: "rgb(25,25,175)",
            },
            effect: {
                constantSpeed: 20,
                show: true,
                trailLength: 0.1,
                symbolSize: 1.5,
            },
            data: coords.map((coord) => {
                return {
                    coords: coord,
                };
            }),
        };
    });

    return {
        startPointAndEndPointSeries,
        presentSelectedNodeSeries,
        nextBuslineOption,
        nodepathOfLineSeries,
    };
}
type currentBusline = {
    line_id: number | null; // 一个id，标识是新添加还是编辑
    line_name: string | null;
    start_node_id: number | null;
    nodepath_id_list: number[];
    end_node_id: number | null;
    adcodes: number[];
};
function buttonHandler(allInfo: Ref<AllInfo>, optionList: Ref<OptionList>) {
    function selectTheFirstNode(
        arg: clickParam,
        allInfo: AllInfo,
        optionList: OptionList
    ) {
        if (!arg.target) {
            return;
        }
        const selectedNodeId = arg.target.id;
        currentBusline.value.start_node_id = selectedNodeId;
        setClicker(selectTheEndNode);
        ElMessage.info("请设置路径的终点");
    }
    function selectTheEndNode(
        arg: clickParam,
        allInfo: AllInfo,
        optionList: OptionList
    ) {
        if (!arg.target) {
            return;
        }
        const selectedNodeId = arg.target.id;
        currentBusline.value.end_node_id = selectedNodeId;
        setClicker(selectNodepath);
        ElMessage.info(
            "请选择路径，操作与路网管理中编辑路径操作相同，完成路径设置后请点击完成"
        );
        localInject("tips").value = "请选择节点";
        localInject("displayNodepath").value = true;
    }

    function selectNodepath(
        arg: clickParam,
        allInfo: AllInfo,
        optionList: OptionList
    ) {
        if (lastSelectedNodeId.value === null) {
            if (!arg.target || arg.target.type === "NODEPATH") {
                // 没有点击到点
                return;
            }
            lastSelectedNodeId.value = arg.target.id;
            localInject("tips").value = "请选择路径";
            return;
        }
        if (!arg.target || arg.target.type === "NODE") {
            // 没有点击到路径，重置
            lastSelectedNodeId.value = null;
            localInject("tips").value = "请选择节点";
            return;
        }
        let selectedNodepathId = arg.target.id;
        // 检查这个路径是否和nodeId相连
        if (
            allInfo.nodepaths[selectedNodepathId].node1_id !==
            lastSelectedNodeId.value
        ) {
            if (
                allInfo.nodepaths[selectedNodepathId].node2_id !==
                lastSelectedNodeId.value
            ) {
            }
            // 说明选择的不是这个方向的路径，遍历所有nodepath，获取反向的路径
            const realNodepath = _(allInfo.nodepaths).find((v) => {
                return (
                    v.node1_id ===
                        allInfo.nodepaths[selectedNodepathId].node2_id &&
                    v.node2_id ===
                        allInfo.nodepaths[selectedNodepathId].node1_id
                );
            });
            if (realNodepath) {
                selectedNodepathId = realNodepath.nodepath_id;
            } else {
                ElMessage.error("选择的路径不以选择节点为起始点！");
                lastSelectedNodeId.value = null;
                localInject("tips").value = "请选择节点";
                return;
            }
        }
        // 检查这个线路是否在线路里，如果在则将其删除，否则插入
        const theIndex =
            currentBusline.value.nodepath_id_list.indexOf(selectedNodepathId);
        if (theIndex === -1) {
            currentBusline.value.nodepath_id_list.push(selectedNodepathId);
        } else {
            currentBusline.value.nodepath_id_list.splice(theIndex, 1);
        }
        lastSelectedNodeId.value = null;
        localInject("tips").value = "请选择节点";
    }

    const disabledButtons = ref(false);
    localProvide("disabledButtons", disabledButtons);
    const lastSelectedNodeId = ref<number | null>(null);
    localProvide("lastSelectedNodeId", lastSelectedNodeId);
    let currentBusline: Ref<currentBusline> = ref({
        line_id: null,
        line_name: null,
        start_node_id: null,
        nodepath_id_list: [],
        end_node_id: null,
        adcodes: [],
    });
    function setToOri() {
        currentBusline.value.line_name = null;
        currentBusline.value.start_node_id = null;
        currentBusline.value.nodepath_id_list = [];
        currentBusline.value.end_node_id = null;
        currentBusline.value.line_id = null;
        currentBusline.value.adcodes = [];
    }
    localProvide("currentBusline", currentBusline);
    function addBusline() {
        setToOri();
        disabledButtons.value = true;
        localInject("displayNode").value = true;
        localInject("displayNodepath").value = false;
        localInject("displayBusline").value = false;
        ElMessage.info("请设置路径的起始点");
        setClicker(selectTheFirstNode);
    }
    function editBusline() {
        // TODO
        setToOri();
        localInject("displayNodepath").value = false;
        localInject("displayNode").value = false;
        disabledButtons.value = true;
        ElMessage.info("请选择要修改的路径");
        setClicker((arg, allInfo, optionList) => {
            if (!arg.target) {
                return;
            }
            const busline_id = arg.target.id;
            const busline = allInfo.buslines[busline_id];
            currentBusline.value.line_id = busline_id;
            currentBusline.value.line_name = busline.line_name;
            currentBusline.value.start_node_id = busline.start_node_id;
            currentBusline.value.end_node_id =
                allInfo.nodepaths[_.last(busline.nodepath_id_list)!].node2_id;
            currentBusline.value.nodepath_id_list = _.cloneDeep(
                busline.nodepath_id_list
            );
            currentBusline.value.adcodes = _.cloneDeep(
                localInject("buslineId2Adcodes").value[busline_id]
            );
            localInject("displayNodepath").value = true;
            localInject("displayNode").value = true;
            localInject("displayBusline").value = false;
            ElMessage.info("请选择起始点或直接点击完成以编辑线路信息");
            setClicker(selectTheFirstNode);
        });
    }
    function deleteBusline() {
        setToOri();
        localInject("displayNodepath").value = false;
        localInject("displayNode").value = false;
        ElMessage.info("请选择要删除的路径");
        setClicker((arg, allInfo_, optionList) => {
            if (!arg.target) {
                return;
            }
            const busline_id = arg.target.id;
            ElMessageBox.confirm("确认要删除吗？", {
                callback(action: string) {
                    if (action === "confirm") {
                        // 从地区中删除
                        const adcode2BuslineIds =
                            localInject("adcode2BuslineIds");
                        const adcodes = localInject("buslineId2Adcodes").value[
                            busline_id
                        ] as number[];
                        adcodes.forEach((adcode) => {
                            adcode2BuslineIds.value[adcode].splice(
                                adcode2BuslineIds.value[adcode].indexOf(
                                    busline_id
                                ),
                                1
                            );
                            const deleteBusFromPlaceOption: DeleteLineFromPlaceOption =
                                {
                                    target: "BUSLINE_OF_PLACE",
                                    type: "DELETE",
                                    data: { line_id: busline_id, adcode },
                                };
                            optionList.push(deleteBusFromPlaceOption);
                        });

                        delete allInfo_.buslines[busline_id];
                        const deleteBuslineOption: DeleteBuslineOption = {
                            target: "BUSLINE",
                            type: "DELETE",
                            data: { line_id: busline_id },
                        };
                        triggerRef(allInfo);
                        localInject("disabledButtons").value = false;
                        optionList.push(deleteBuslineOption);
                        // TODO 删除公交线路的时候，检查其上的公交车列表，进行操作
                        setClicker(() => {});
                    } else {
                    }
                },
            });
        });
    }
    function confirm() {
        const currentBusline: currentBusline =
            localInject("currentBusline").value;
        if (
            currentBusline.start_node_id === null ||
            currentBusline.end_node_id === null ||
            currentBusline.nodepath_id_list.length === 0
        ) {
            ElMessage.error("起点、终点或路径未给定！");
            return;
        }
        const paths = getPath(
            currentBusline.start_node_id,
            currentBusline.end_node_id,
            currentBusline.nodepath_id_list.map((id) => {
                const { node1_id, node2_id } = allInfo.value.nodepaths[id]!;
                return [node1_id, node2_id];
            })
        );
        if (paths.length === 0) {
            ElMessage.error("起点和终点未相连！");
            return;
        }
        if (paths.length > 1) {
            ElMessage.error("起点和终点之间有多条路径！");
            return;
        }
        const path = paths[0];
        const finalNodepathIdList: number[] = [];
        const coord2nodepathId: Record<string, number> = {};
        currentBusline.nodepath_id_list!.forEach((id) => {
            const { node1_id, node2_id } = allInfo.value.nodepaths[id]!;
            coord2nodepathId[`${node1_id}-${node2_id}`] = id;
        });
        _.zip(_.dropRight(path, 1), _.drop(path, 1)).map(
            ([node1_id, node2_id]) => {
                const nodepathId = coord2nodepathId[`${node1_id}-${node2_id}`];
                finalNodepathIdList.push(nodepathId);
            }
        );
        if (
            currentBusline.nodepath_id_list.length !==
            finalNodepathIdList.length
        ) {
            ElMessage.error("存在多余路径！");
            return;
        }
        localInject("callConfirmDialog")(
            currentBusline,
            allInfo.value,
            optionList.value
        );
    }
    function reset() {
        setClicker(() => {});
        setToOri();
        disabledButtons.value = false;
        localInject("displayNode").value = true;
        localInject("displayNodepath").value = true;
        localInject("displayBusline").value = true;
    }
    return {
        disabledButtons,
        addBusline,
        editBusline,
        deleteBusline,
        reset,
        confirm,
        ...useConfirmEditDialog(),
    };
}

function useConfirmEditDialog() {
    const confirmDialogVisible = ref(false);
    const confirmDialogTitle = ref("");
    const confirmDialog_line_name = ref("");
    const confirmDialog_adcode = ref<number[]>([]);
    let resBox: {
        value: {
            currentBusline: currentBusline;
            allInfo: AllInfo;
            optionList: OptionList;
        } | null;
    } = { value: null };
    localProvide(
        "callConfirmDialog",
        function (
            currentBusline: currentBusline,
            allInfo: AllInfo,
            optionList: OptionList
        ) {
            if (currentBusline.line_id === null) {
                // 说明是添加
                confirmDialogTitle.value = "添加公交线路";
                confirmDialog_line_name.value = "";
                confirmDialog_adcode.value = [];
            } else {
                confirmDialogTitle.value = "编辑公交线路";
                confirmDialog_line_name.value = currentBusline.line_name!;
                confirmDialog_adcode.value = currentBusline.adcodes;
            }
            resBox.value = { currentBusline, allInfo, optionList };
            confirmDialogVisible.value = true;
        }
    );
    const confirmEdit = function () {
        const currentBusline = resBox.value!.currentBusline!;
        const adcode2BuslineIds = localInject("adcode2BuslineIds") as Ref<
            Record<number, number[]>
        >;
        if (currentBusline.line_id === null) {
            // 添加
            const maybeMinimumId = _(resBox.value!.allInfo.buslines)
                .map((v) => v.line_id)
                .min();
            const nextlineId =
                maybeMinimumId && maybeMinimumId <= -1
                    ? maybeMinimumId - 1
                    : -1;
            resBox.value!.allInfo.buslines[nextlineId] = {
                line_id: nextlineId,
                line_name: confirmDialog_line_name.value,
                start_node_id: currentBusline.start_node_id!,
                bus_id_list: [],
                nodepath_id_list: _.cloneDeep(currentBusline.nodepath_id_list),
            };
            const insertBuslineOption: InsertBuslineOption = {
                target: "BUSLINE",
                type: "INSERT",
                data: _.cloneDeep(resBox.value!.allInfo.buslines[nextlineId]),
            };
            resBox.value!.optionList.push(insertBuslineOption);
            confirmDialog_adcode.value.forEach((adcode) => {
                adcode2BuslineIds.value[adcode].push(nextlineId);
                const insertLineIntoPlaceOption: InsertLineIntoPlaceOption = {
                    target: "BUSLINE_OF_PLACE",
                    type: "INSERT",
                    data: { adcode, line_id: nextlineId },
                };
                resBox.value!.optionList.push(insertLineIntoPlaceOption);
            });
        } else {
            // 修改
            resBox.value!.allInfo.buslines[currentBusline.line_id].line_name =
                confirmDialog_line_name.value;
            resBox.value!.allInfo.buslines[
                currentBusline.line_id
            ].start_node_id = currentBusline.start_node_id!;
            resBox.value!.allInfo.buslines[
                currentBusline.line_id
            ].nodepath_id_list = _.cloneDeep(currentBusline.nodepath_id_list);
            console.log(resBox.value!.allInfo.buslines[currentBusline.line_id]);
            const updateBuslineOption: UpdateBuslineOption = {
                target: "BUSLINE",
                type: "UPDATE",
                data: _.cloneDeep(
                    resBox.value!.allInfo.buslines[currentBusline.line_id]
                ),
            };
            resBox.value!.optionList.push(updateBuslineOption);
            // 将bus插入和删除到地点中
            const places = localInject("places");
            const beforeAdcodes =
                localInject("buslineId2Adcodes").value[currentBusline.line_id];
            const afterAdcodes = confirmDialog_adcode.value;
            // 获取原数据与后数据引用的adcode的差集，从这些地点中删除数据
            const adcodeBeDeleted = _.without(beforeAdcodes, ...afterAdcodes);
            const adcodeBeAdded = _.without(afterAdcodes, ...beforeAdcodes);
            adcodeBeDeleted.forEach((adcode) => {
                adcode2BuslineIds.value[adcode].splice(
                    adcode2BuslineIds.value[adcode].indexOf(
                        currentBusline.line_id!
                    ),
                    1
                );
                const deleteBusFromPlaceOption: DeleteLineFromPlaceOption = {
                    target: "BUSLINE_OF_PLACE",
                    type: "DELETE",
                    data: { line_id: currentBusline.line_id!, adcode },
                };
                resBox.value!.optionList.push(deleteBusFromPlaceOption);
            });
            adcodeBeAdded.forEach((adcode) => {
                adcode2BuslineIds.value[adcode].push(currentBusline.line_id!);
                const insertBuslineIntoPlaceOption: InsertLineIntoPlaceOption =
                    {
                        target: "BUSLINE_OF_PLACE",
                        type: "INSERT",
                        data: { line_id: currentBusline.line_id!, adcode },
                    };
                resBox.value!.optionList.push(insertBuslineIntoPlaceOption);
            });
        }
        localInject("triggerRef")();
        localInject("disabledButtons").value = false;
        // 重设
        currentBusline.line_name = null;
        currentBusline.start_node_id = null;
        currentBusline.nodepath_id_list = [];
        currentBusline.end_node_id = null;
        currentBusline.line_id = null;
        currentBusline.adcodes = [];
        confirmDialogVisible.value = false;
    };
    return {
        confirmDialogVisible,
        confirmEdit,
        confirmDialog_line_name,
        confirmDialog_adcode,
        confirmDialogTitle,
    };
}
</script>

