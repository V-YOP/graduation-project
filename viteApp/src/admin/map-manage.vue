<!-- 
对节点和路径进行管理。
-->

<template>
    <el-container style="height: 100%; width: 100%">
        <el-header style="display: flex; align-items: center">
            <el-space :size="10">
                <el-radio-group v-model="currentOptionType">
                    <el-radio-button label="ROAM">移动</el-radio-button>
                    <el-radio-button label="DISPLAY_MESSAGE"
                        >查看</el-radio-button
                    >
                </el-radio-group>
                <el-radio-group v-model="currentOptionType">
                    <el-radio-button label="ADD_NODE">添加节点</el-radio-button>
                    <el-radio-button label="UPDATE_NODE"
                        >编辑节点</el-radio-button
                    >
                    <el-radio-button label="MOVE_NODE"
                        >移动节点</el-radio-button
                    >
                    <el-radio-button label="DELETE_NODE"
                        >删除节点</el-radio-button
                    >
                </el-radio-group>
                <el-radio-group v-model="currentOptionType">
                    <el-radio-button label="ADD_NODEPATH"
                        >添加路径</el-radio-button
                    >
                    <el-radio-button label="EDIT_NODEPATH"
                        >编辑路径</el-radio-button
                    >
                    <el-radio-button label="DELETE_NODEPATH"
                        >删除路径</el-radio-button
                    >
                </el-radio-group>

                <el-popover placement="bottom" :width="400" trigger="hover">
                    <template #reference>
                        <el-button
                            type="text"
                            icon="el-icon-info"
                            style="font-size: 20px; margin-left: 10px"
                        ></el-button>
                    </template>
                    <p>
                        <b>移动</b
                        >：采用降采样策略以提升绘制效率，但是无法显示标注和线路的方向。该项之外的选项在移动时可能会有卡顿和刷新延迟。<br />
                    </p>
                    <p><b>查看</b>：查看节点以及线路的信息。<br /></p>
                    <p>
                        <b>添加节点</b
                        >：点击地图空白处添加节点或者点击路径以拆分路径。<br />
                    </p>
                    <p><b>编辑节点</b>：点击节点以进行编辑。<br /></p>
                    <p>
                        <b>删除节点</b
                        >：点击没有同路径相连的节点以删除节点。<br />
                    </p>
                    <p>
                        <b>添加路径</b
                        >：按道路方向依序点击两节点以添加路径。<br />
                    </p>
                    <p><b>删除路径</b>：点击路径和其起始点以删除路径。<br /></p>
                    <p><b>编辑路径</b>：点击路径和其起始点以编辑路径。<br /></p>
                </el-popover>
            </el-space>
        </el-header>
        <el-main style="padding: 0" v-loading="mapLoading">
            <div :id="mapId" style="height: 100%;
    width: 100%;
    position: relative;"></div>
        </el-main>
        <el-footer :height="30" style="display: flex; align-items: center">
            tips:{{ tip }}
        </el-footer>

        <el-dialog title="添加节点" v-model="addNodeDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>经纬度：</span
                    ><el-tag>{{ addNodeDialog_latLont }}</el-tag>
                </p>
                <p>
                    <span>是否站点：</span>
                    <el-radio-group v-model="addNodeDialog_isStation">
                        <el-radio-button :label="false">否</el-radio-button>
                        <el-radio-button :label="true">是</el-radio-button>
                    </el-radio-group>
                </p>
                <template v-if="!addNodeDialog_splitNodepath">
                    <p>
                        <span>所处区域：</span>
                        <el-radio-group v-model="addNodeDialog_selectAdcode">
                            <el-radio-button
                                :key="place.adcode"
                                v-for="place in places"
                                :label="place.adcode"
                                >{{ place.name }}</el-radio-button
                            >
                        </el-radio-group>
                    </p>
                </template>
                <template v-if="addNodeDialog_splitNodepath">
                    <p>
                        <span>拆分路径ID：</span
                        ><el-tag>{{ addNodeDialog_splitNodepathId }}</el-tag>
                    </p>
                </template>
            </el-space>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="addNodeDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmAddNode"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>

        <el-dialog title="更新节点" v-model="updateNodeDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>经纬度：</span
                    ><el-tag>{{ updateNodeDialog_latLont }}</el-tag>
                </p>
                <p>
                    <span>是否站点：</span>
                    <el-radio-group v-model="updateNodeDialog_isStation">
                        <el-radio-button :label="false">否</el-radio-button>
                        <el-radio-button :label="true">是</el-radio-button>
                    </el-radio-group>
                </p>
                <p>
                    <span>所处区域：</span>
                    <el-radio-group v-model="updateNodeDialog_selectAdcode">
                        <el-radio-button
                            :key="place.adcode"
                            v-for="place in places"
                            :label="place.adcode"
                            >{{ place.name }}</el-radio-button
                        >
                    </el-radio-group>
                </p>
            </el-space>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="updateNodeDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmUpdateNode"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>

        <el-dialog title="添加路径" v-model="addNodepathDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>道路名称：</span>
                    <el-input v-model="addNodepathDialog_street_name">
                    </el-input>
                </p>
                <p>
                    <span>道路限速：</span
                    ><el-input-number
                        v-model="addNodepathDialog_speed_limit"
                        :min="20"
                        :max="120"
                    ></el-input-number>
                </p>
                <p>
                    <span>所处区域：</span>
                    <el-radio-group
                        v-model="addNodepathDialog_adcode"
                        :disabled="addNodepathDialog_disableSelectAdcode"
                    >
                        <el-radio-button
                            :key="place.adcode"
                            v-for="place in places"
                            :label="place.adcode"
                            >{{ place.name }}</el-radio-button
                        >
                    </el-radio-group>
                </p>
            </el-space>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="addNodepathDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmAddNodepath"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>
        <el-dialog title="更新路径" v-model="updateNodepathDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>道路名称：</span>
                    <el-input v-model="updateNodepathDialog_street_name">
                    </el-input>
                </p>
                <p>
                    <span>道路限速：</span
                    ><el-input-number
                        v-model="updateNodepathDialog_speed_limit"
                        :min="20"
                        :max="120"
                    ></el-input-number>
                </p>
                <p>
                    <span>所处区域：</span>
                    <el-radio-group
                        v-model="updateNodepathDialog_adcode"
                        :disabled="updateNodepathDialog_disableSelectAdcode"
                    >
                        <el-radio-button
                            :key="place.adcode"
                            v-for="place in places"
                            :label="place.adcode"
                            >{{ place.name }}</el-radio-button
                        >
                    </el-radio-group>
                </p>
            </el-space>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="updateNodepathDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmUpdateNodepath"
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
    SetupContext,
} from "vue";
import { AllInfo } from "../util/api";
import { useChart } from "../speedAnalysis/useChart";
import "echarts/extension/bmap/bmap";
import _ from "lodash";
import { EChartsType } from "echarts";
import * as echarts from "echarts";
import {
    DeleteNodeOption,
    DeleteNodepathOption,
    InsertNodeOption,
    InsertNodepathOption,
    OptionList,
    UpdateBuslineOption,
    UpdateNodeOption,
    UpdateNodepathOption,
} from "../util/adminApi";
import { ElMessage } from "element-plus";
import bmapStyle from "./bmapStyle";
// 用来方便地……麻了
const stor: Record<string, any> = {};

function localProvide(name: string, arg: any) {
    stor[name] = arg;
}
function localInject(name: string) {
    return stor[name];
}

export type optionType =
    | "ROAM"
    | "DISPLAY_MESSAGE"
    | "ADD_NODE"
    | "DELETE_NODE"
    | "UPDATE_NODE"
    | "MOVE_NODE"
    | "ADD_NODEPATH"
    | "DELETE_NODEPATH"
    | "EDIT_NODEPATH";

const useNodeOption = function (
    allInfo: Ref<AllInfo>,
    optionType: Ref<optionType>,
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
            large: optionType.value === "ROAM",
            
            symbolSize: 5,
            data: _(allInfo.value.nodes)
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
    optionType: Ref<optionType>,
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
            large: optionType.value === "ROAM",
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

export type clickParam = {
    latLont: [number, number];
    target: {
        id: number;
        type: "NODE" | "NODEPATH";
    } | null;
};

function useAddNodeDialog(places: Ref<{ adcode: number; name: string }[]>) {
    const addNodeDialogVisible = ref(false);
    const addNodeDialog_latLont = ref<[number, number] | null>(null);
    const addNodeDialog_splitNodepath = ref<boolean | null>(null);
    const addNodeDialog_splitNodepathId = ref<number | null>(null);
    const addNodeDialog_selectAdcode = ref<number | null>(null);
    const addNodeDialog_isStation = ref<boolean | null>(null);

    const newId = ref<number>(0);
    let resBox: {
        value: {
            allInfo: AllInfo;
            optionList: OptionList;
        } | null;
    } = { value: null };
    localProvide(
        "callAddNodeDialog",
        function (arg: clickParam, allInfo: AllInfo, optionList: OptionList) {
            if (arg.target && arg.target.type === "NODE") {
                // 点击到路径，什么都不做
                return;
            }
            resBox.value = { allInfo, optionList };
            // 使用给定的玩意对dialog进行初始化
            addNodeDialog_latLont.value = arg.latLont;
            if (arg.target && arg.target.type === "NODEPATH") {
                // 点击到路径
                addNodeDialog_splitNodepath.value = true;
                addNodeDialog_splitNodepathId.value = arg.target!.id;
                addNodeDialog_selectAdcode.value =
                    allInfo.nodepaths[arg.target!.id].adcode;
            } else {
                // 点击空白处
                addNodeDialog_splitNodepath.value = false;
            }
            addNodeDialogVisible.value = true;
        }
    );
    const confirmAddNode = () => {
        // 校验是否选择了区域和站点
        if (
            addNodeDialog_selectAdcode.value === null ||
            addNodeDialog_isStation.value === null
        ) {
            ElMessage.error("信息未选择完整！");
            return;
        }
        // 插入节点
        const toFindMinNodeId = _(resBox.value!.allInfo.nodes)
            .map((v) => v.node_id)
            .min();
        const nextNodeId =
            toFindMinNodeId && toFindMinNodeId <= -1 ? toFindMinNodeId - 1 : -1;
        resBox.value!.allInfo.nodes[nextNodeId] = {
            node_id: nextNodeId,
            adcode: addNodeDialog_selectAdcode.value,
            lat: addNodeDialog_latLont.value![0],
            lont: addNodeDialog_latLont.value![1],
            isStation: addNodeDialog_isStation.value!,
        };
        const insertNodeOption: InsertNodeOption = {
            target: "NODE",
            type: "INSERT",
            data: resBox.value!.allInfo.nodes[nextNodeId],
        };
        resBox.value!.optionList.push(insertNodeOption);

        if (addNodeDialog_splitNodepath.value) {
            //对路径折半
            const theNodepath =
                resBox.value!.allInfo.nodepaths[
                    addNodeDialog_splitNodepathId.value!
                ];
            delete resBox.value!.allInfo.nodepaths[
                addNodeDialog_splitNodepathId.value!
            ];
            const startNode = resBox.value!.allInfo.nodes[theNodepath.node1_id];
            const endNode = resBox.value!.allInfo.nodes[theNodepath.node2_id];
            // 插入两条新路径
            const toFindMinNodepathId = _(resBox.value!.allInfo.nodepaths)
                .map((v) => v.nodepath_id)
                .min();
            const nextNodepathId =
                toFindMinNodepathId && toFindMinNodepathId <= -1
                    ? toFindMinNodepathId - 1
                    : -1;
            const nextNextNodepathId = nextNodepathId - 1;
            resBox.value!.allInfo.nodepaths[nextNodepathId] = {
                nodepath_id: nextNodepathId,
                node1_id: startNode.node_id,
                node2_id: nextNodeId,
                speed_limit: theNodepath.speed_limit,
                street_name: theNodepath.street_name,
                adcode: theNodepath.adcode,
            };
            resBox.value!.allInfo.nodepaths[nextNextNodepathId] = {
                nodepath_id: nextNextNodepathId,
                node1_id: nextNodeId,
                node2_id: endNode.node_id,
                speed_limit: theNodepath.speed_limit,
                street_name: theNodepath.street_name,
                adcode: theNodepath.adcode,
            };

            const deleteOption: DeleteNodepathOption = {
                type: "DELETE",
                target: "NODEPATH",
                data: _({ nodepath_id: theNodepath.nodepath_id }).cloneDeep(),
            };
            const insertNodepath1Option: InsertNodepathOption = {
                target: "NODEPATH",
                type: "INSERT",
                data: _(
                    resBox.value!.allInfo.nodepaths[nextNodepathId]
                ).cloneDeep(),
            };
            const insertNodepath2Option: InsertNodepathOption = {
                target: "NODEPATH",
                type: "INSERT",
                data: _(
                    resBox.value!.allInfo.nodepaths[nextNextNodepathId]
                ).cloneDeep(),
            };
            resBox.value!.optionList.push(deleteOption);
            resBox.value!.optionList.push(insertNodepath1Option);
            resBox.value!.optionList.push(insertNodepath2Option);
            _(resBox.value!.allInfo.buslines).forEach((busline) => {
                const index = busline.nodepath_id_list.indexOf(
                    theNodepath.nodepath_id
                );
                if (index === -1) return;
                busline.nodepath_id_list.splice(
                    index,
                    nextNodepathId,
                    nextNextNodepathId
                );
                const updateBuslineOption: UpdateBuslineOption = {
                    target: "BUSLINE",
                    type: "UPDATE",
                    data: _(busline).cloneDeep(),
                };
                resBox.value!.optionList.push(updateBuslineOption);
            });
        }
        addNodeDialog_selectAdcode.value = null;
        addNodeDialog_isStation.value = null;
        addNodeDialogVisible.value = false;
    };
    return {
        addNodeDialogVisible,
        confirmAddNode,
        addNodeDialog_latLont,
        addNodeDialog_splitNodepath,
        addNodeDialog_splitNodepathId,
        addNodeDialog_selectAdcode,
        addNodeDialog_isStation,
    };
}

function useUpdateNodeDialog(places: Ref<{ adcode: number; name: string }[]>) {
    const updateNodeDialogVisible = ref(false);
    const updateNodeDialog_latLont = ref<null | [number, number]>(null);
    const updateNodeDialog_isStation = ref<null | boolean>(null);
    const updateNodeDialog_selectAdcode = ref<null | number>(null);
    let nodeId: null | number = null;
    let resBox: {
        value: {
            allInfo: AllInfo;
            optionList: OptionList;
        } | null;
    } = { value: null };
    localProvide(
        "callUpdateNodeDialog",
        function (arg: clickParam, allInfo: AllInfo, optionList: OptionList) {
            if (!arg.target || arg.target.type === "NODEPATH") {
                // 没有选择到节点
                return;
            }
            // 使用给定的玩意对dialog进行初始化

            resBox.value = { allInfo, optionList };
            const theNode = allInfo.nodes[arg.target.id];
            updateNodeDialog_latLont.value = arg.latLont;
            updateNodeDialog_isStation.value = theNode.isStation;
            updateNodeDialog_selectAdcode.value = theNode.adcode;
            nodeId = theNode.node_id;
            updateNodeDialogVisible.value = true;
        }
    );
    const confirmUpdateNode = () => {
        const theNode = resBox.value!.allInfo.nodes[nodeId!];
        if (
            theNode.isStation === updateNodeDialog_isStation.value! &&
            theNode.adcode === updateNodeDialog_selectAdcode.value!
        ) {
            ElMessage.warning("信息相同，未作修改");
        } else {
            theNode.isStation = updateNodeDialog_isStation.value!;
            theNode.adcode = updateNodeDialog_selectAdcode.value!;
            const updateNodeOption: UpdateNodeOption = {
                target: "NODE",
                type: "UPDATE",
                data: _.cloneDeep(theNode),
            };
            resBox.value!.optionList.push(updateNodeOption);
        }

        updateNodeDialog_isStation.value = null;
        updateNodeDialog_selectAdcode.value = null;
        updateNodeDialogVisible.value = false;
    };
    return {
        updateNodeDialogVisible,
        confirmUpdateNode,
        updateNodeDialog_latLont,
        updateNodeDialog_isStation,
        updateNodeDialog_selectAdcode,
    };
}

// TODO 写到这了
function useAddNodepathDialog(places: Ref<{ adcode: number; name: string }[]>) {
    const addNodepathDialogVisible = ref(false);
    const addNodepathDialog_speed_limit = ref<number>(40);
    const addNodepathDialog_adcode = ref<number | null>(null);
    const addNodepathDialog_street_name = ref<string | null>("");
    const addNodepathDialog_disableSelectAdcode = ref<boolean | null>(false);
    let resBox: {
        value: {
            allInfo: AllInfo;
            optionList: OptionList;
        } | null;
    } = { value: null };
    let nodes: [number, number] | null = null;
    localProvide(
        "callAddNodepathDialog",
        function (
            node1_id: number,
            node2_id: number,
            allInfo: AllInfo,
            optionList: OptionList
        ) {
            resBox.value = { allInfo, optionList };
            // 使用给定的玩意对dialog进行初始化
            const node1 = allInfo.nodes[node1_id];
            const node2 = allInfo.nodes[node2_id];
            nodes = [node1_id, node2_id];
            addNodepathDialog_speed_limit.value = 40;
            addNodepathDialog_adcode.value = null;
            if (node1.adcode === node2.adcode) {
                addNodepathDialog_disableSelectAdcode.value = true;
                addNodepathDialog_adcode.value = node1.adcode;
            } else {
                addNodepathDialog_disableSelectAdcode.value = false;
            }
            addNodepathDialog_street_name.value = "";
            addNodepathDialogVisible.value = true;
        }
    );
    const confirmAddNodepath = () => {
        if (
            addNodepathDialog_adcode.value === null ||
            addNodepathDialog_street_name.value === ""
        ) {
            ElMessage.error("信息不完整！");
            return;
        }
        const toFindMinNodepathId = _(resBox.value!.allInfo.nodepaths)
            .map((v) => v.nodepath_id)
            .min();
        const nextNodepathId =
            toFindMinNodepathId && toFindMinNodepathId <= -1
                ? toFindMinNodepathId - 1
                : -1;
        resBox.value!.allInfo.nodepaths[nextNodepathId] = {
            nodepath_id: nextNodepathId,
            node1_id: nodes![0],
            node2_id: nodes![1],
            speed_limit: addNodepathDialog_speed_limit.value,
            street_name: addNodepathDialog_street_name.value!,
            adcode: addNodepathDialog_adcode.value!,
        };
        const insertNodepathOption: InsertNodepathOption = {
            target: "NODEPATH",
            type: "INSERT",
            data: _.cloneDeep(resBox.value!.allInfo.nodepaths[nextNodepathId]),
        };
        resBox.value!.optionList.push(insertNodepathOption);
        addNodepathDialogVisible.value = false;
    };
    return {
        addNodepathDialogVisible,
        confirmAddNodepath,
        addNodepathDialog_speed_limit,
        addNodepathDialog_adcode,
        addNodepathDialog_street_name,
        addNodepathDialog_disableSelectAdcode,
    };
}

function useUpdateNodepathDialog(
    places: Ref<{ adcode: number; name: string }[]>
) {
    const updateNodepathDialogVisible = ref(false);
    const updateNodepathDialog_speed_limit = ref<number>(40);
    const updateNodepathDialog_adcode = ref<number | null>(null);
    const updateNodepathDialog_street_name = ref<string | null>("");
    const updateNodepathDialog_disableSelectAdcode = ref<boolean | null>(false);
    let resBox: {
        value: {
            allInfo: AllInfo;
            optionList: OptionList;
        } | null;
    } = { value: null };
    let nodepath_id: number | null = null;
    localProvide(
        "callUpdateNodepathDialog",
        function (
            nodepath_id_: number,
            allInfo: AllInfo,
            optionList: OptionList
        ) {
            resBox.value = { allInfo, optionList };
            const nodepath = allInfo.nodepaths[nodepath_id_];
            nodepath_id = nodepath_id_;
            // 使用给定的玩意对dialog进行初始化
            const node1 = allInfo.nodes[nodepath.node1_id];
            const node2 = allInfo.nodes[nodepath.node2_id];
            updateNodepathDialog_speed_limit.value = 40;
            updateNodepathDialog_adcode.value = null;
            if (node1.adcode === node2.adcode) {
                updateNodepathDialog_disableSelectAdcode.value = true;
                updateNodepathDialog_adcode.value = node1.adcode;
            } else {
                updateNodepathDialog_disableSelectAdcode.value = false;
            }
            updateNodepathDialog_street_name.value = "";
            updateNodepathDialogVisible.value = true;
        }
    );
    const confirmUpdateNodepath = () => {
        if (
            updateNodepathDialog_adcode.value === null ||
            updateNodepathDialog_street_name.value === ""
        ) {
            ElMessage.error("信息不完整！");
            return;
        }
        const oriNodepath = resBox.value!.allInfo.nodepaths[nodepath_id!];
        oriNodepath.speed_limit = updateNodepathDialog_speed_limit.value;
        oriNodepath.adcode = updateNodepathDialog_adcode.value;
        oriNodepath.street_name = updateNodepathDialog_street_name.value!;
        const updateNodepathOption: UpdateNodepathOption = {
            target: "NODEPATH",
            type: "UPDATE",
            data: _.cloneDeep(oriNodepath),
        };
        resBox.value?.optionList.push(updateNodepathOption);
        updateNodepathDialogVisible.value = false;
    };
    return {
        updateNodepathDialogVisible,
        confirmUpdateNodepath,
        updateNodepathDialog_speed_limit,
        updateNodepathDialog_adcode,
        updateNodepathDialog_street_name,
        updateNodepathDialog_disableSelectAdcode,
    };
}

function handleClick(
    arg: clickParam,
    optionType: optionType,
    allInfo: AllInfo,
    optionList: OptionList
) {
    if (optionType === "ROAM" || optionType === "DISPLAY_MESSAGE") {
        return;
    }
    // TODO : 干活！
    const optionMapper: Record<optionType, () => void> = {
        ROAM: _.constant(1),
        DISPLAY_MESSAGE: _.constant(1),
        ADD_NODE: () => {
            localInject("callAddNodeDialog")(arg, allInfo, optionList);
        },
        UPDATE_NODE: () => {
            localInject("callUpdateNodeDialog")(arg, allInfo, optionList);
        },
        DELETE_NODE: () => {
            // 很简单，直接写到这里就可以了
            if (!arg.target || arg.target.type === "NODEPATH") {
                // 没有点击到节点
                return;
            }
            // 首先判断其是否被nodepath引用
            const maybeNodepath = _(allInfo.nodepaths)
                .values()
                .find(
                    (v) =>
                        v.node1_id === arg.target!.id ||
                        v.node2_id === arg.target!.id
                );
            if (maybeNodepath) {
                ElMessage.error("存在同该节点相连接的路径！");
                return;
            }

            delete allInfo.nodes[arg.target.id];
            const deleteNodeOption: DeleteNodeOption = {
                target: "NODE",
                type: "DELETE",
                data: { node_id: arg.target.id },
            };
            optionList.push(deleteNodeOption);
        },
        MOVE_NODE: () => {
            const lastSelectedNodeId = localInject("lastSelectedNodeId") as Ref<
                number | null
            >;
            if (lastSelectedNodeId.value === null) {
                // 第一次点击
                if (!arg.target || arg.target.type === "NODEPATH") {
                    // 没有点击到点
                    return;
                }
                lastSelectedNodeId.value = arg.target.id;
                localInject("tip").value = "请点击要移动到的位置";
                return;
            }
            if (arg.target && arg.target.type === "NODE") {
                // 没有点击空白点
                localInject("setDefaultTip")();
                lastSelectedNodeId.value = null;
                return;
            }
            const selectedNodeId = lastSelectedNodeId.value;
            const latLont = arg.latLont;
            allInfo.nodes[selectedNodeId].lat = latLont[0];
            allInfo.nodes[selectedNodeId].lont = latLont[1];
            const updateNodeOption: UpdateNodeOption = {
                target: "NODE",
                type: "UPDATE",
                data: _.cloneDeep(allInfo.nodes[selectedNodeId]),
            };
            optionList.push(updateNodeOption);
            localInject("setDefaultTip")();
            lastSelectedNodeId.value = null;
        },
        ADD_NODEPATH: () => {
            const lastSelectedNodeId = localInject("lastSelectedNodeId") as Ref<
                number | null
            >;
            // 第一次点击，选择点
            if (lastSelectedNodeId.value === null) {
                if (!arg.target || arg.target.type === "NODEPATH") {
                    // 没有点击到点
                    return;
                }
                lastSelectedNodeId.value = arg.target.id;
                localInject("tip").value =
                    "请点击要添加路径的终点或点击地图空白处以重置";
                return;
            }
            if (!arg.target || arg.target.type === "NODEPATH") {
                // 没有点击到下一个节点，默认重置
                localInject("setDefaultTip")();
                lastSelectedNodeId.value = null;
                return;
            }
            const startNodeId = lastSelectedNodeId.value;
            const endNodeId = arg.target.id;
            if (
                _(allInfo.nodepaths).find(
                    (v) =>
                        v.node1_id === startNodeId && v.node2_id === endNodeId
                )
            ) {
                ElMessage.error("路径已存在！");
                return;
            }
            console.log(startNodeId, endNodeId);
            if (startNodeId === endNodeId) {
                ElMessage.error("不能选择同一节点！");
                return;
            }
            localInject("callAddNodepathDialog")(
                startNodeId,
                endNodeId,
                allInfo,
                optionList
            );
            localInject("setDefaultTip")();
            lastSelectedNodeId.value = null;
        },
        DELETE_NODEPATH: () => {
            // TODO 先点击起始点，再点击路径！
            /*
             localProvide("tip", tip);
            localProvide("setDefaultTip", () => {
                tip.value = defaultTips[currentOptionType.value];
            });
            const lastSelectedNodeId = ref<number | null>(null);
            localProvide("lastSelectedNodeId", lastSelectedNodeId);
             */
            const lastSelectedNodeId = localInject("lastSelectedNodeId") as Ref<
                number | null
            >;

            // 第一次点击，选择点
            if (lastSelectedNodeId.value === null) {
                if (!arg.target || arg.target.type === "NODEPATH") {
                    // 没有点击到点
                    return;
                }
                lastSelectedNodeId.value = arg.target.id;
                localInject("tip").value =
                    "请点击要删除的路径或点击地图空白处以重置";
                return;
            }
            if (!arg.target || arg.target.type === "NODE") {
                // 没有点击到路径
                localInject("setDefaultTip")();
                lastSelectedNodeId.value = null;
                return;
            }
            let selectedNodepathId = arg.target.id;
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
                    return;
                }
            }
            // 检查是否被公交线路引用
            const maybeNodepathId = _(allInfo.buslines)
                .values()
                .map((v) => v.nodepath_id_list)
                .flatten()
                .indexOf(selectedNodepathId);
            console.log(maybeNodepathId);
            if (maybeNodepathId !== -1) {
                ElMessage.error("该线路被公交线路引用！");
                return;
            }
            delete allInfo.nodepaths[selectedNodepathId];
            const deleteNodepathOption: DeleteNodepathOption = {
                target: "NODEPATH",
                type: "DELETE",
                data: { nodepath_id: selectedNodepathId },
            };
            optionList.push(deleteNodepathOption);
            localInject("setDefaultTip")();
            lastSelectedNodeId.value = null;
        },
        EDIT_NODEPATH: () => {
            const lastSelectedNodeId = localInject("lastSelectedNodeId") as Ref<
                number | null
            >;
            // 第一次点击，选择点
            if (lastSelectedNodeId.value === null) {
                if (!arg.target || arg.target.type === "NODEPATH") {
                    // 没有点击到点
                    return;
                }
                lastSelectedNodeId.value = arg.target.id;
                localInject("tip").value =
                    "请点击要更新的路径或点击地图空白处以重置";
                return;
            }
            if (!arg.target || arg.target.type === "NODE") {
                // 没有点击到路径
                localInject("setDefaultTip")();
                lastSelectedNodeId.value = null;
                return;
            }
            let selectedNodepathId = arg.target.id;
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
                    return;
                }
            }
            // selectedNodepathId为最终选中的路径
            localInject("callUpdateNodepathDialog")(
                selectedNodepathId,
                allInfo,
                optionList
            );
            localInject("setDefaultTip")();
            lastSelectedNodeId.value = null;
        },
    };
    optionMapper[optionType]();
}

const enableClickEvt = function (
    chart: echarts.ECharts,
    allInfo: Ref<AllInfo>,
    optionType: Ref<optionType>,
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
        console.log("ori", param);
        click1.value = true;
        allParam.value.target = param.data;
    });
    chart.getZr().on("click", (params) => {
        console.log(params);
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
        console.log(latLont);
        allParam.value.latLont = latLont;
        click2.value = true;
    });
    watch([click1, click2], (v) => {
        setTimeout(() => {
            const latLont = allParam.value.latLont;
            const target = allParam.value.target;
            if (v[0]) {
                // 点击到对象
                console.log("点击到对象");
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
                    optionType.value,
                    allInfo.value,
                    optionList.value
                );
            } else if (v[1]) {
                // 点击到地图
                console.log("点击到地图");
                handleClick(
                    {
                        latLont: latLont,
                        target: null,
                    },
                    optionType.value,
                    allInfo.value,
                    optionList.value
                );
            }
            click1.value = false;
            click2.value = false;
        }, 5);
    });
};
const defaultTips: Record<optionType, string> = {
    ROAM: "移动模式，可随意拖动地图进行查看",
    DISPLAY_MESSAGE: "鼠标悬浮以查看节点，路径信息",
    ADD_NODE: "点击地图空白处或路段以添加节点或拆分路段",
    UPDATE_NODE: "点击节点以进行更新",
    DELETE_NODE: "点击节点以删除节点",
    MOVE_NODE: "点击要移动的节点",
    ADD_NODEPATH: "请点击新建路径的起始点",
    DELETE_NODEPATH: "请点击路径的起始点",
    EDIT_NODEPATH: "请点击路径的起始点",
};

export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        places: Object as PropType<{ adcode: number; name: string }[]>,
    },

    setup(props, context) {
        localProvide("triggerRef", function () {
            context.emit("triggerRef");
        });
        const { info, places } = toRefs(props);
        const currentOptionType = ref<optionType>("ROAM");
        const tip = ref(defaultTips[currentOptionType.value]);
        localProvide("tip", tip);
        localProvide("setDefaultTip", () => {
            tip.value = defaultTips[currentOptionType.value];
        });
        const lastSelectedNodeId = ref<number | null>(null);
        localProvide("lastSelectedNodeId", lastSelectedNodeId);
        watch(currentOptionType, (val) => {
            localInject("setDefaultTip")();
            lastSelectedNodeId.value = null;
        });
        const mapLoading = ref(false);
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
            info as Ref,
            currentOptionType,
            visibleScope
        );
        const { nodepathOption, nodepathSeries } = useNodepathOption(
            info as Ref,
            currentOptionType,
            visibleScope
        );
        const tooltip = computed(() => {
            return {
                tooltip: {
                    trigger:
                        currentOptionType.value === "ROAM" ? "none" : "item",
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
                        }
                        // 是线路的时候
                        const {
                            speedLimit,
                            street_name,
                        }: { speedLimit: number; street_name: string } =
                            params.data;
                        const name = places?.value?.find(
                            (v) =>
                                v.adcode === info?.value?.nodepaths[id].adcode
                        )?.name;
                        return `
                            路径ID: ${id < 0 ? "new-" + Math.abs(id) : id}<br />
                            道路名称：${street_name}<br />
                            限速：${speedLimit} km/h<br />
                            所属地区：${name}
                        `;
                    },
                },
            };
        });
        const realOption = computed(() => {
            console.log("重计算");
            return {
                ...bmapOption(),
                ...nodeOption.value,
                ...nodepathOption.value,
                series: [nodeSeries.value, nodepathSeries.value],
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
                enableClickEvt(
                    chart,
                    info as Ref,
                    currentOptionType,
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
                        mapLoading.value = true;
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
                        chart.on("finished", function afterRender() {
                            mapLoading.value = false;
                            chart.off("finished", afterRender);
                        });
                    }, 200)
                );
            },
            true
        );
        watch(visibleScope, (v) => {
            console.log(v);
        });

        return {
            currentOptionType,
            mapLoading,
            tip,
            ...useAddNodeDialog(places as Ref),
            ...useUpdateNodeDialog(places as Ref),
            ...useAddNodepathDialog(places as Ref),
            ...useUpdateNodepathDialog(places as Ref),
            places,
            mapId : elementId
        };
    },
});
</script>

