<template>
    <el-container style="height: 100%; width: 100%">
        <el-dialog
            title="管理员登录"
            v-model="loginDialogVisible"
            width="20%"
            append-to-body>
            <el-form status-icon>
                <el-form-item label="用户名">
                    <el-input
                        v-model="loginForm.id"
                        autocomplete="off"
                    ></el-input>
                </el-form-item>
                <el-form-item label="密码">
                    <el-input
                        type="password"
                        v-model="loginForm.passwd"
                        autocomplete="off"
                    ></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="loginDialogVisible = false"
                        >关闭</el-button
                    >
                    <!-- ! 这里的历史信息指单日历史信息 -->
                    <el-button type="primary" @click="handleLoginBtn"
                        >登录</el-button
                    >
                </span>
            </template>
        </el-dialog>

        <el-dialog
            title="选择地点"
            v-model="dialogVisible"
            width="30%"
            append-to-body
        >
            <span v-html="msg"></span>
            <el-radio-group
                v-model="selectType"
                style="margin-top: 10px; margin-bottom: 10px"
            >
                <el-radio-button label="实时信息"></el-radio-button>
                <el-radio-button label="历史信息"></el-radio-button>
                <el-radio-button label="超速分析信息"></el-radio-button>
            </el-radio-group>
            <el-date-picker
                v-if="selectType === `历史信息`"
                v-model="singleDayValue"
                type="date"
                :disabled-date="disabledDate"
                placeholder="选择日期"
            >
            </el-date-picker>
            <el-date-picker
                v-if="selectType === `超速分析信息`"
                v-model="multiDayValue"
                type="daterange"
                :disabled-date="disabledDate"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
            >
            </el-date-picker>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">返 回</el-button>
                    <!-- ! 这里的历史信息指单日历史信息 -->

                    <el-button type="primary" @click="handleClick"
                        >确 定</el-button
                    >
                </span>
            </template>
        </el-dialog>
        <el-dialog
            title="查看历史信息"
            v-model="historySelectDialogVisible"
            width="30%"
            append-to-body
        >
            <!-- 在这里插入内容 -->
            <el-switch
                style="display: block"
                v-model="notSingleDay"
                active-text="多日信息"
                inactive-text="单日信息"
            >
            </el-switch
            ><br />
            <el-date-picker
                v-if="!notSingleDay"
                v-model="singleDayValue"
                type="date"
                :disabled-date="disabledDate"
                placeholder="选择日期"
            >
            </el-date-picker>
            <el-date-picker
                v-if="notSingleDay"
                v-model="multiDayValue"
                type="daterange"
                :disabled-date="disabledDate"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
            >
            </el-date-picker>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="historySelectDialogVisible = false"
                        >返 回</el-button
                    >
                    <el-button type="primary" @click="handleSelectHistoryBtn"
                        >确认</el-button
                    >
                </span>
            </template>
        </el-dialog>
        <el-header id="header">
            <el-link
                type="primary"
                :underline="false"
                style="font-size: 30px; line-height: 65px; float: left"
                >公交车超速分析系统</el-link
            >
            <el-button
                :underline="false"
                type="text"
                style="font-size: 30px; float: right"
                @click="openLoginDialog"
                >{{ loginStatus ? loginStatus.name : "管理员登录" }}</el-button
            >
        </el-header>
        <el-main style="padding: 0">
            <div id="map-container"></div>
        </el-main>
    </el-container>
</template>

<script lang="ts">
// * 该页面提供钻取功能，为用户第一个看到的页面

import { Scene } from "@antv/l7";
import { DrillDownLayer } from "@antv/l7-district";
import { Mapbox } from "@antv/l7-maps";
import _ from "lodash";
import { computed, defineComponent, onMounted, ref, watch } from "vue";
import {
    ElNotification,
    ElMessageBox,
    ElMessage,
    ElLoading,
} from "element-plus";
import $ from "jquery";
import { useRouter, useRoute } from "vue-router";
import adminApi from "./util/adminApi";
export default defineComponent({
    setup(props, context) {
        const loginStatus = ref<
            undefined | null | { name: string; id: string; token: string }
        >(adminApi.loginCheck());
        let singleDayValue = ref();
        let multiDayValue = ref();
        let notSingleDay = ref(false);
        let loading = ref(true);
        watch(notSingleDay, function handler(val) {
            if (val) {
                // 选择了多日，将单日信息置空
                multiDayValue.value = null;
            } else {
                singleDayValue.value = null;
            }
        });
        let router = useRouter(); // 很奇怪，似乎router必须要在这里获取，否则要报错
        let msg = computed(() => {
            let names = places.value.map((place) => place.NAME_CHN);
            if (names[1] === names[2]) names.splice(2, 1);
            if (names[0] === names[1]) names.splice(1, 1);
            let msg = names.join("-");
            return `您选择查看<code>${msg}</code>的——`;
        });
        let dialogVisible = ref(false);
        let historySelectDialogVisible = ref(false);
        let places = ref<
            {
                NAME_CHN: string;
                level?: string;
                adcode: number;
                x: number;
                y: number;
            }[]
        >([]);
        function handleRealTimeBtn() {
            let placeStr = encodeURI(
                JSON.stringify(
                    places.value.map(({ NAME_CHN, level, adcode, x, y }) => {
                        return { NAME_CHN, level, adcode, x, y };
                    })
                )
            );
            dialogVisible.value = false;
            router.push({
                path: "/real-time",
                query: {
                    places: placeStr,
                },
            });
        }
        function handleHistoryBtn() {
            dialogVisible.value = false;
            // 清空信息
            notSingleDay.value = false;
            singleDayValue.value = null;
            multiDayValue.value = null;
            historySelectDialogVisible.value = true;
        }
        function handleSelectHistoryBtn() {
            if (
                (!notSingleDay.value && !singleDayValue.value) ||
                (notSingleDay.value && !multiDayValue.value)
            ) {
                ElMessage("请选择查看日期！");
                return;
            }
            let placeStr = encodeURI(
                JSON.stringify(
                    places.value.map(({ NAME_CHN, level, adcode, x, y }) => {
                        return { NAME_CHN, level, adcode, x, y };
                    })
                )
            );
            // 给月份和日期前补0
            let format = function (num: number) {
                return _.padStart("" + num, 2, "0");
            };
            if (!notSingleDay.value) {
                // * 说明选择的是单日
                let date: Date = singleDayValue.value;
                router.push({
                    path: "single-day-history",
                    query: {
                        places: placeStr,
                        date: `${date.getFullYear()}-${format(
                            date.getMonth() + 1
                        )}-${format(date.getDate())}`,
                    },
                });
            } else {
                let date: [Date, Date] = multiDayValue.value;
                router.push({
                    path: "multi-day-history",
                    query: {
                        places: placeStr,
                        date0: `${date[0].getFullYear()}-${format(
                            date[0].getMonth() + 1
                        )}-${format(date[0].getDate())}`,
                        date1: `${date[1].getFullYear()}-${format(
                            date[1].getMonth() + 1
                        )}-${format(date[1].getDate())}`,
                    },
                });
            }
            historySelectDialogVisible.value = false;
        }

        function handleClick() {
            dialogVisible.value = true;
        }

        onMounted(() => {
            if (loginStatus.value)
                ElNotification({
                    type: "info",
                    title: "你好",
                    offset: 100,
                    dangerouslyUseHTMLString: true,
                    message: `你好，管理员${loginStatus.value.name}`,
                });
            else
                ElNotification({
                    type: "info",
                    title: "关于系统的使用",
                    offset: 100,
                    dangerouslyUseHTMLString: true,
                    message:
                        "使用鼠标<b>单击</b>行政区域以进入或选择该区域，<b>双击</b>地图空白位置以返回上一级行政区划地图",
                });
            const colors = [
                "#B8E1FF",
                "#7DAAFF",
                "#7DBBDD",
                "#3D76DD",
                "#0047A5",
                "#001D70",
            ];
            const scene = new Scene({
                id: "map-container",
                map: new Mapbox({
                    center: [116.2825, 39.9],
                    pitch: 0,
                    style: "blank",
                    zoom: 3,
                    minZoom: 3,
                    maxZoom: 10,
                }),
            });
            scene.on("loaded", () => {
                let drillDownLayer = new DrillDownLayer(scene, {
                    data: [],
                    viewStart: "Country",
                    viewEnd: "City",
                    fill: {
                        color: {
                            field: "NAME_CHN",
                            values: colors,
                        },
                    },
                    onClick(property, type) {
                        let props = property.feature.properties;
                        if (!("level" in props)) {
                            // * 全国，选择省
                            places.value![0] = props;
                        } else if (
                            props.level === "province" ||
                            props.level === "city"
                        ) {
                            places.value![1] = props; // * 省/市， 选择市（如果本级为市，则只有一个子区域，即本身）
                        } else if (props.level === "district") {
                            places.value![2] = props; // * 市，选择区
                        }
                        if (type === "City") {
                            console.log("合法点击");
                            handleClick();
                        }
                    },
                    popup: {
                        enable: true,
                        Html: (props) => {
                            return `<span id="name">${props.NAME_CHN}</span>`;
                        },
                    },
                });
            });
        });
        const loginDialogVisible = ref(false);
        const loginForm = ref({ id: "", passwd: "" });
        const selectType = ref("实时信息");
        return {
            selectType,
            handleClick() {
                let type = selectType.value;
                if (type === "实时信息") {
                    handleRealTimeBtn();
                    return;
                }
                notSingleDay.value = type === "超速分析信息";
                handleSelectHistoryBtn();
            },
            loginStatus,
            msg,
            dialogVisible,
            handleRealTimeBtn,
            handleHistoryBtn,
            handleSelectHistoryBtn,
            historySelectDialogVisible,
            notSingleDay,
            singleDayValue,
            multiDayValue,
            loading,
            disabledDate(time: Date) {
                // TODO: 应当要求只能选择昨日及之前的数据，这里应测试需要不设这个限制
                // * 这样干的原因是不必为单日信息功能编写限制选择时间段的功能
                // let minus = 60 * 60 * 24 * 1000;
                let minus = 0;
                return time.getTime() > Date.now() - minus;
            },
            loginDialogVisible,
            openLoginDialog() {
                if (adminApi.loginCheck())
                    router.push({
                        path: "admin",
                    });
                loginDialogVisible.value = true;
            },
            loginForm,
            handleLoginBtn() {
                let { id, passwd } = loginForm.value;
                adminApi.login(
                    id,
                    passwd,
                    (status) => {
                        // 登录后
                        if (status === "success!") {
                            ElMessage.success("登陆成功！");
                            router.push({
                                path: "admin",
                            });
                        } else {
                            ElMessage.error(
                                "登录失败！请检查输入的用户名和密码！"
                            );
                        }
                    },
                    (err) => {
                        ElMessage.error("登录失败！请检查网络连接！");
                    }
                );
            },
        };
    },
});
</script>
<style scoped>
#main {
    padding: 0;
    height: 100%;
    width: 100%;
}
#header {
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
        "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
    border-bottom: 1px solid #dcdfe6;
}

el-aside {
    color: #333;
}
</style>
<style>
html,
body {
    height: 100%;
    width: 100%;
    padding: 0;
    border: 0;
    margin: 0;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
        "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}
#map-container {
    overflow: hidden;
    height: 100%;
    width: 100%;
    position: relative;
}
</style>