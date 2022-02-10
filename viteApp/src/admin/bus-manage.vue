<!-- 公交车信息管理，主体是一个表格 -->
<template>
    <el-container style="height: 100%; width: 100%">
        <el-header style="display: flex; align-items: center; width: 100%">
            <h1 style="font-size: 30px">公交车信息管理</h1>
        </el-header>
        <el-main style="margin-left: 50px; padding-top: 50px">
            <!--
                突然犯懒，不想写了
            <el-popover placement="bottom" :width="400" trigger="click">
                <template #reference>
                    <el-button type="text" style="font-size: 20px"
                        >筛选条件<i class="el-icon-arrow-down"></i
                    ></el-button>
                </template>
            </el-popover>
            -->
            <el-pagination
                v-if="allBusLength > pageSize"
                layout="sizes, prev, pager, next, jumper"
                v-model:currentPage="currentPage"
                v-model:pageSize="pageSize"
                :page-sizes="[10, 20, 50, 100, 200]"
                :total="allBusLength"
            >
            </el-pagination>

            <el-table :data="tableData" style="width: 90%">
                <el-table-column prop="bus_id" label="公交车ID">
                    <template #default="scope">
                        {{ scope.row.bus_id < 0 ? `NEW-${scope.row.bus_id}` : scope.row.bus_id}}
                    </template>
                </el-table-column>
                <el-table-column prop="bus_number" label="车牌号">
                </el-table-column>
                <el-table-column prop="bus_type" label="车型">
                </el-table-column>
                <el-table-column prop="places" label="经过地区">
                    <template #default="scope">
                        <el-tag
                            v-for="adcode in scope.row.places"
                            :key="adcode"
                        >
                            {{ adcode2name(adcode) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="buslines" label="所在公交线路">
                    <template #default="scope">
                        <el-tag
                            v-for="line_id in scope.row.buslines"
                            :key="line_id"
                        >
                            {{ line_id2name(line_id) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作">
                    <template #header>
                        <el-button type="primary" size="mini" style="font-size:15px" @click="handleAdd"><i class="el-icon-circle-plus-outline"></i> 添加</el-button>
                    </template>
                    <template #default="scope">
                        <el-button size="mini" @click="handleEdit(scope.row)"
                            >编辑</el-button
                        >

                        <el-popconfirm
                            title="确定删除该公交车吗？地点和公交线路所引用的信息也会被删除！"
                            @confirm="handleDelete(scope.row)"
                        >
                            <template #reference>
                                <el-button size="mini" type="danger"
                                    >删除</el-button
                                >
                            </template>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                v-if="allBusLength > pageSize"
                layout="sizes, prev, pager, next, jumper"
                v-model:currentPage="currentPage"
                v-model:pageSize="pageSize"
                :page-sizes="[10, 20, 50, 100, 200]"
                :total="allBusLength"
            >
            </el-pagination>
        </el-main>
        <el-dialog title="添加公交车信息" v-model="addBusDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>车牌号：</span>
                    <el-input v-model="addBusDialog_bus_number"> </el-input>
                </p>
                <p>
                    <span>车型</span
                    ><el-input v-model="addBusDialog_bus_type"> </el-input>
                </p>
                <p>
                    <span>经过区域：</span>
                    <el-checkbox-group v-model="addBusDialog_adcode">
                        <el-checkbox-button
                            v-for="place in places"
                            :label="place.adcode"
                            :key="place.adcode"
                            >{{ place.name }}</el-checkbox-button
                        >
                    </el-checkbox-group>

                </p>
                <p>
                    <span>所在公交线路（如果有多条线路，使用全角逗号分隔）：</span>
                    <el-input v-model="addBusDialog_line_name"> </el-input>
                </p>
            </el-space>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="addBusDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmAddBus"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>

        <el-dialog title="编辑公交车信息" v-model="editBusDialogVisible">
            <el-space direction="vertical" alignment="left">
                <p>
                    <span>车牌号：</span>
                    <el-input v-model="editBusDialog_bus_number"> </el-input>
                </p>
                <p>
                    <span>车型</span
                    ><el-input v-model="editBusDialog_bus_type"> </el-input>
                </p>
                <p>
                    <span>经过区域：</span>

                    <el-checkbox-group v-model="editBusDialog_adcode">
                        <el-checkbox-button
                            v-for="place in maybePlaces"
                            :label="place.adcode"
                            :key="place.adcode"
                            >{{ place.name }}</el-checkbox-button
                        >
                    </el-checkbox-group>
                </p>
                <p>
                    <span
                        >所在公交线路（如果有多条线路，使用全角逗号分隔）：</span
                    >
                    <el-input v-model="editBusDialog_line_name"> </el-input>
                </p>
            </el-space>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editBusDialogVisible = false"
                        >取 消</el-button
                    >
                    <el-button type="primary" @click="confirmEditBus"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>
    </el-container>
</template>

<script lang="ts">
import axios from "axios";
import { ElMessage } from "element-plus";
import _ from "lodash";
import {
    computed,
    defineComponent,
    inject,
    PropType,
    ref,
    Ref,
    toRefs,
    watch,
} from "vue";
import {
    DeleteBusFromPlaceOption,
    DeleteBusOption,
    InsertBusIntoPlaceOption,
    InsertBusOption,
    OptionList,
    UpdateBuslineOption,
    UpdateBusOption,
} from "../util/adminApi";
import { AllInfo } from "../util/api";

const stor: Record<string, any> = {};

function localProvide(name: string, arg: any) {
    stor[name] = arg;
}
function localInject(name: string) {
    return stor[name];
}
function useAddBusDialog(allInfo : Ref<AllInfo>, adcode2BusIds: Ref<Record<number, number[]>>,places : Ref<{ adcode: number; name: string }[]>, optionList :Ref<OptionList>) {
    const addBusDialogVisible = ref(false);
    const addBusDialog_bus_number = ref("");
    const addBusDialog_bus_type = ref("");
    const addBusDialog_line_name = ref("");
    const addBusDialog_adcode = ref<number[]>([]);
    const handleAdd = function() {
        addBusDialog_bus_number.value + ""
        addBusDialog_bus_type.value + ""
        addBusDialog_line_name.value = ""
        addBusDialog_adcode.value = []
        addBusDialogVisible.value = true;
    }
    const confirmAddBus = function() {
        if (
            addBusDialog_bus_number.value === "" ||
            addBusDialog_bus_type.value === "" ||
            addBusDialog_adcode.value.length === 0 ||
            addBusDialog_line_name.value === ""
        ) {
            ElMessage.error("信息不完整！");
            return;
        }
        const buslineNames = _(addBusDialog_line_name.value)
            .split("，")
            .compact()
            .value();
        const buslines: number[] = [];
        for (let i = 0; i < buslineNames.length; i++) {
            const name = buslineNames[i];
            const targetBusline = _(allInfo.value.buslines).find(
                (busline) => busline.line_name === name
            );
            if (!targetBusline) {
                ElMessage.error("公交线路中存在无效名称！");
                return;
            }
            buslines.push(targetBusline.line_id);
        }

        const maybeMinimumId = _(allInfo.value.buses).map(v=>v.bus_id).min()
        const nextBusId = (maybeMinimumId && maybeMinimumId <= -1) ? maybeMinimumId - 1 : -1
        allInfo.value.buses[nextBusId] = {
            bus_id : nextBusId,
            bus_number : addBusDialog_bus_number.value,
            bus_type : addBusDialog_bus_type.value
        }
        const insertBusOption : InsertBusOption = {
            target: "BUS",
            type : "INSERT",
            data : _.cloneDeep(allInfo.value.buses[nextBusId])
        }
        optionList.value.push(insertBusOption)

        // 插入公交车信息到地区中
        addBusDialog_adcode.value.forEach((adcode) => {
            adcode2BusIds.value[adcode].push(nextBusId)
            const insertBusIntoPlaceOption : InsertBusIntoPlaceOption = {
                target: "BUS_OF_PLACE",
                type : "INSERT",
                data : {bus_id : nextBusId, adcode}
            }
            optionList.value.push(insertBusIntoPlaceOption)
        })
        // 插入到线路中
        buslines.map(line_id=>allInfo.value.buslines[line_id]).forEach(busline=>{
            busline.bus_id_list.push(nextBusId)
            const updateBuslineOption : UpdateBuslineOption = {
                target: "BUSLINE",
                type : "UPDATE",
                data : _.cloneDeep(busline)
            }
            optionList.value.push(updateBuslineOption)
        })
        addBusDialogVisible.value = false;
    }
    return { 
        addBusDialogVisible,
        addBusDialog_bus_number,
        addBusDialog_bus_type,
        addBusDialog_line_name,
        addBusDialog_adcode,
        confirmAddBus,
        handleAdd,

    }
                
}

function useEditBusDialog(adcode2BusIds: Ref<Record<number, number[]>>,) {
    const editBusDialogVisible = ref(false);
    const editBusDialog_bus_number = ref("");
    const editBusDialog_bus_type = ref("");
    const editBusDialog_line_name = ref("");
    const maybePlaces = localInject("maybePlaces") as Ref<
        { adcode: number; name: string }[]
    >;
    const editBusDialog_adcode = ref<number[]>([]);
    let resBox: {
        value: {
            allInfo: AllInfo;
            optionList: OptionList;
            beforeBuslines: number[];
            bus_id: number;
            places: number[],
        } | null;
    } = { value: null };
    localProvide(
        "callEditBusDialog",
        function (
            bus_id: number,
            bus_adcodes: number[],
            bus_buslines: number[],
            allInfo: AllInfo,
            places: { adcode: number; name: string }[],
            optionList: OptionList
        ) {
            resBox.value = {
                allInfo,
                optionList,
                beforeBuslines: bus_buslines,
                bus_id,
                places : bus_adcodes
            };
            const theBus = allInfo.buses[bus_id];
            editBusDialog_bus_number.value = theBus.bus_number;
            editBusDialog_bus_type.value = theBus.bus_type;
            if (bus_buslines)
                editBusDialog_line_name.value = bus_buslines
                    .map((line_id) => allInfo.buslines[line_id].line_name)
                    .join("，");
            else {
                editBusDialog_line_name.value = ""
            }
            editBusDialog_adcode.value = bus_adcodes;
            maybePlaces.value = [...places];
            bus_adcodes.forEach((adcode) => {
                if (!maybePlaces.value.find((v) => v.adcode === adcode))
                    maybePlaces.value.push({
                        adcode,
                        name: `未选择的地区：${adcode}`,
                    });
            });
            editBusDialogVisible.value = true;
        }
    );
    const confirmEditBus = function () {
        if (
            editBusDialog_bus_number.value === "" ||
            editBusDialog_bus_type.value === "" ||
            editBusDialog_adcode.value.length === 0
        ) {
            ElMessage.error("信息不完整！");
            return;
        }
        const buslineNames = _(editBusDialog_line_name.value)
            .split("，")
            .compact()
            .value();
        const afterBuslines: number[] = [];
        for (let i = 0; i < buslineNames.length; i++) {
            const name = buslineNames[i];
            const targetBusline = _(resBox!.value!.allInfo.buslines).find(
                (busline) => busline.line_name === name
            );
            if (!targetBusline) {
                ElMessage.error("公交线路中存在无效名称！");
                return;
            }
            afterBuslines.push(targetBusline.line_id);
        }

        // 编辑原本的数据
        resBox.value!.allInfo.buses[resBox.value!.bus_id] = {
            bus_id: resBox.value!.bus_id,
            bus_number: editBusDialog_bus_number.value,
            bus_type: editBusDialog_bus_type.value,
        };
        const updateBusOption: UpdateBusOption = {
            target: "BUS",
            type: "UPDATE",
            data: _.cloneDeep(
                resBox.value!.allInfo.buses[resBox.value!.bus_id]
            ),
        };
        resBox.value!.optionList.push(updateBusOption);

        // 将bus插入和删除到地点中
        const beforeAdcodes = resBox.value!.places
        const afterAdcodes = editBusDialog_adcode.value
        // 获取原数据与后数据引用的adcode的差集，从这些地点中删除数据
        const adcodeBeDeleted = _.without(beforeAdcodes, ...afterAdcodes)
        const adcodeBeAdded = _.without(afterAdcodes, ...beforeAdcodes)
        adcodeBeDeleted.forEach((adcode)=>{
            adcode2BusIds.value[adcode].splice(adcode2BusIds.value[adcode].indexOf(resBox.value!.bus_id), 1)
            const deleteBusFromPlaceOption : DeleteBusFromPlaceOption = {
                target: "BUS_OF_PLACE",
                type : "DELETE",
                data  :{bus_id : resBox.value!.bus_id, adcode}
            }
            resBox.value!.optionList.push(deleteBusFromPlaceOption)
        })
        adcodeBeAdded.forEach((adcode)=>{
            adcode2BusIds.value[adcode].push(resBox.value!.bus_id)
            const insertBusIntoPlaceOption : InsertBusIntoPlaceOption = {
                target: "BUS_OF_PLACE",
                type : "INSERT",
                data  :{bus_id : resBox.value!.bus_id, adcode}
            }
            resBox.value!.optionList.push(insertBusIntoPlaceOption)
        })
        

        // 获取原数据与后数据引用的公交线路的差集，从这些公交线路中删除数据
        const buslinesBeDeleted = _.without(
            resBox.value!.beforeBuslines,
            ...afterBuslines
        );
        buslinesBeDeleted
            .map((v) => resBox.value!.allInfo.buslines[v])
            .forEach((busline) => {
                const busIndex = busline.bus_id_list.indexOf(resBox.value!.bus_id);
                if (busIndex === -1) {
                    throw new Error("总之出了点问题")
                }
                busline.bus_id_list.splice(busIndex, 1)
                const updateBuslineOption : UpdateBuslineOption = {
                    target: "BUSLINE",
                    type: "UPDATE",
                    data : _.cloneDeep(busline)
                }
                resBox.value!.optionList.push(updateBuslineOption);
            });

        // 获取后数据与原数据引用的公交线路的差集，从这些公交线路中加入该公交车
        if(!resBox.value!.beforeBuslines) {
            resBox.value!.beforeBuslines = []
        }
        const buslinesBeAdded = _.without(
            afterBuslines,
            ...resBox.value!.beforeBuslines
        );
        console.log("公交线路",resBox.value!.beforeBuslines,afterBuslines,buslinesBeDeleted, buslinesBeAdded)

        buslinesBeAdded
            .map((v) => resBox.value!.allInfo.buslines[v])
            .forEach((busline) => {
                busline.bus_id_list.push(resBox.value!.bus_id)
                const updateBuslineOption : UpdateBuslineOption = {
                    target: "BUSLINE",
                    type: "UPDATE",
                    data : _.cloneDeep(busline)
                }
                resBox.value!.optionList.push(updateBuslineOption);
            });
        editBusDialogVisible.value = false;
    };
    return {
        editBusDialogVisible,
        editBusDialog_bus_number,
        editBusDialog_bus_type,
        editBusDialog_line_name,
        editBusDialog_adcode,
        confirmEditBus,
    };
}
function useTable(
    info: Ref<AllInfo>,
    places: Ref<{ adcode: number; name: string }[]>,
    adcode2BusIds: Ref<Record<number, number[]>>,
    optionList: Ref<OptionList>
) {
    const busId2buslineId = computed(() => {
        const res: Record<number, number[]> = {};
        _(info.value.buslines).forEach((v) => {
            const line_id = v.line_id;
            const bus_id_list = v.bus_id_list;
            bus_id_list.forEach((bus_id) => {
                if (res[bus_id]) res[bus_id].push(line_id);
                else {
                    res[bus_id] = [line_id];
                }
            });
        });
        return res;
    });
    const busId2adcodes = computed(() => {
        if (adcode2BusIds.value === null) {
            return {};
        }
        const res: Record<number, number[]> = {};
        _(adcode2BusIds.value).forEach((v, k) => {
            const adcode = Number(k);
            const bus_id_list = v;
            bus_id_list.forEach((bus_id) => {
                if (res[bus_id]) res[bus_id].push(adcode);
                else {
                    res[bus_id] = [adcode];
                }
            });
        });
        return res;
    });

    // TODO 设置加载
    watch(adcode2BusIds, (val) => {
        if (val === null) {
            return;
        }
        // TODO 取消加载
    });
    const currentPage = ref(1);
    const pageSize = ref(20);
    watch(currentPage, (val) => {
        console.log("分页");
    });
    return {
        adcode2name: function (adcode: number) {
            const place = places.value.find((v) => v.adcode === adcode);
            if (!place) return `未选择的地区：${adcode}`;
            return place.name;
        },
        line_id2name: function (line_id: number) {
            return info.value.buslines[line_id].line_name;
        },
        currentPage,
        pageSize,
        allBusLength: computed(() => {
            return _(info.value.buses).size();
        }),
        tableData: computed(() => {
            console.log("计算tableData");
            if (adcode2BusIds.value === null) {
                return null;
            }
            return _(info.value.buses)
                .map((v) => {
                    return {
                        bus_id: v.bus_id,
                        bus_number: v.bus_number,
                        bus_type: v.bus_type,
                        places: busId2adcodes.value[v.bus_id],
                        buslines: busId2buslineId.value[v.bus_id],
                    };
                })
                .slice(
                    (currentPage.value - 1) * pageSize.value,
                    currentPage.value * pageSize.value
                )
                .value();
        }),
        handleDelete(bus: {
            bus_id: number;
            places: number[];
            buslines: number[];
        }) {
            console.log("删除操作", bus);
            // 分为三步：删除公交车，删除地点中的公交车，删除公交线路中的公交车
            // 删除公交车
            delete info.value.buses[bus.bus_id];
            const deleteBusOption: DeleteBusOption = {
                target: "BUS",
                type: "DELETE",
                data: { bus_id: bus.bus_id },
            };
            // 删除地点中的公交车
            bus.places.forEach((adcode) => {
                const index = adcode2BusIds.value[adcode].indexOf(bus.bus_id);
                if (index === -1) {
                    throw new Error("没有在adcode2BusIds中找到该ID！");
                }
                adcode2BusIds.value[adcode].splice(index, 1);
                const deleteBusFromPlaceOption: DeleteBusFromPlaceOption = {
                    target: "BUS_OF_PLACE",
                    type: "DELETE",
                    data: { bus_id: bus.bus_id, adcode },
                };
                optionList.value.push(deleteBusFromPlaceOption);
            });

            // 删除公交线路中的公交车
            bus.buslines.forEach((line_id) => {
                const theList = info.value.buslines[line_id].bus_id_list;
                const index = theList.indexOf(bus.bus_id);
                if (index === -1) {
                    throw new Error("没有在线路中找到公交车ID！");
                }
                theList.splice(index, 1);

                // 属性是UpdateBddusline
                const updateBuslineOption: UpdateBuslineOption = {
                    target: "BUSLINE",
                    type: "UPDATE",
                    data: _.cloneDeep(info.value.buslines[line_id]),
                };
                optionList.value.push(updateBuslineOption);
            });

            optionList.value.push(deleteBusOption); // 这个最后添加
            console.log(optionList.value);
        },
        handleEdit(bus: {
            bus_id: number;
            places: number[];
            buslines: number[];
        }) {
            localInject("callEditBusDialog")(
                bus.bus_id,
                bus.places,
                bus.buslines,
                info.value,
                places.value,
                optionList.value
            );
        },
    };
}

export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        places: Object as PropType<{ adcode: number; name: string }[]>,
    },
    setup(props, context) {
        let { info, places } = toRefs(props); // 因为可能会有改变，用引用
        const optionList = inject("optionList") as Ref<OptionList>;
        const adcode2BusIds = inject("adcode2BusIds") as Ref<
            Record<number, number[]>
        >;
        const maybePlaces = ref<{ adcode: number; name: string }[]>([]);
        localProvide("maybePlaces", maybePlaces);
        return {
            ...useTable(info as Ref, places as Ref, adcode2BusIds, optionList),
            ...useAddBusDialog(info as Ref,adcode2BusIds, places as Ref, optionList),
            ...useEditBusDialog(adcode2BusIds),
            maybePlaces,

        };
    },
});
</script>


<style>
</style>