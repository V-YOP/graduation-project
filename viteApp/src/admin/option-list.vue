<template>
    <el-container style="height: 100%; width: 100%">
        <el-header style="display: flex; align-items: center; width: 100%">
            <h1 style="font-size: 30px">操作列表</h1>
        </el-header>
        <el-main style="margin-left: 50px; padding-top: 50px">
            <el-table :data="tableData" style="width: 90%">
                <el-table-column prop="type" label="操作种类">
                </el-table-column>
                <el-table-column prop="target" label="操作对象">
                </el-table-column>
                <el-table-column prop="id" label="对象ID"> </el-table-column>
                <el-table-column label="操作">
                    <template #header>
                        <el-button
                            @click="pushOptionList"
                            type="primary"
                            style="margin-left: auto"
                            >提交</el-button
                        >
                    </template>
                    <template #default="scope">
                        
                        <el-popover
                            placement="right"
                            :width="300"
                            trigger="hover"
                        >
                            <template #reference>
                                <el-button size="mini" type="primary"
                                    >详细</el-button
                                >
                            </template>
                            <el-space direction="vertical" alignment="left" :size="0">
                                <p v-for="(v,k) in scope.row.fullInfo" :key="v" style="display: flex; align-items: center">
                                    <span>{{k}}：</span
                                    ><el-tag>{{ v }}</el-tag>
                                </p>
                            </el-space>
                        </el-popover>
                    </template>
                </el-table-column>
            </el-table>
        </el-main>
    </el-container>
</template>

<script lang="ts">
import axios from "axios";
import { ElMessage } from "element-plus";
import { computed, defineComponent, inject, PropType, Ref } from "vue";
import {
    OptionList,
    OptionType,
    Target,
    Option,
    InsertNodeOption,
    InsertNodepathOption,
    InsertBuslineOption,
    InsertBusOption,
    InsertPlaceOption,
} from "../util/adminApi";
import { AllInfo } from "../util/api";
import { getLoginData } from "./useAdmin";

function generateData(option: Option) {
    if (option.type === "DELETE")
        // 为delete，只包括id，或许还包括adcode
        return null;

    const map: Record<Target, () => Record<string, number | string>> = {
        NODE() {
            const data = (option as InsertNodeOption).data;
            return {
                经度: data.lat,
                纬度: data.lont,
                是否站点: data.isStation ? "是" : "否",
            };
        },
        NODEPATH() {
            const data = (option as InsertNodepathOption).data;
            return {
                起始节点ID:
                    data.node1_id < 0 ? `NEW${data.node1_id}` : data.node1_id,
                终止节点ID:
                    data.node2_id < 0 ? `NEW${data.node2_id}` : data.node2_id,
                道路名: data.street_name,
                限速: data.speed_limit,
            };
        },
        BUSLINE() {
            const data = (option as InsertBuslineOption).data;
            return {
                线路名: data.line_name,
                // 起始节点ID: data.start_node_id < 0 ? `NEW${data.start_node_id}` : data.start_node_id,
                // 路径列表: `[${data.nodepath_id_list.map(v=>v<0?`NEW${v}`:v).join(", ")}]`,
                // 公交车列表: `[${data.bus_id_list.map(v=>v<0?`NEW${v}`:v).join(", ")}]`,
            };
        },
        BUSLINE_OF_PLACE() {
            return {};
        },
        BUS_OF_PLACE() {
            return {};
        },
        BUS() {
            const data = (option as InsertBusOption).data;
            return {
                车牌号: data.bus_number,
                车型: data.bus_type,
            };
        },
        PLACE() {
            return {};
        },
    };
    const res = map[option.target]();
    console.log(res)
    return res;
}

export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        places: Object as PropType<{ adcode: number; name: string }[]>,
    },
    setup(props, context) {
        const optionList = inject("optionList") as Ref<OptionList>;
        return {
            tableData: computed(() => {
                return optionList.value
                    .filter(
                        (v) =>
                            v.target !== "BUS_OF_PLACE" &&
                            v.target !== "BUSLINE_OF_PLACE"
                    )
                    .map(({ target, type, data }) => {
                        const transfer: Record<OptionType | Target, string> = {
                            UPDATE: "更新",
                            DELETE: "删除",
                            INSERT: "添加",
                            NODE: "节点",
                            BUS: "公交车",
                            BUSLINE: "公交线路",
                            NODEPATH: "路径",
                            BUSLINE_OF_PLACE: "地点中的公交线路",
                            BUS_OF_PLACE: "地点中的公交车",
                            PLACE: "地点",
                        };

                        const idFieldMap: Record<Target, string> = {
                            NODE: "node_id",
                            BUS: "bus_id",
                            BUSLINE: "line_id",
                            NODEPATH: "nodepath_id",
                            BUSLINE_OF_PLACE: "null",
                            BUS_OF_PLACE: "null",
                            PLACE: "null",
                        };
                        return {
                            target: transfer[target],
                            type: transfer[type],
                            // @ts-ignore
                            id: data[idFieldMap[target]] < 0
                                    ? `NEW${data[idFieldMap[target]]}`
                                    : "" + data[idFieldMap[target]],
                            fullInfo: generateData({ target, type, data }),
                        };
                    });
            }),
            pushOptionList() {
                const loginData = getLoginData();
                if (loginData === null || loginData === undefined) {
                    alert("你并没有登录！");
                    document.location.href = "/";
                    return;
                }
                const token = loginData.token;
                const id = loginData.id;
                axios
                    .post("http://localhost:8080/admin/applyOptions", {
                        id,
                        token,
                        optionList: optionList.value,
                    })
                    .then(() => {
                        ElMessage.success("提交成功！");
                        setTimeout(() => {
                            document.location.href = ".";
                        }, 1000);
                    })
                    .catch((err) => {
                        ElMessage.error("提交失败！");
                    });
            },
        };
    },
});
</script>


<style>
</style>