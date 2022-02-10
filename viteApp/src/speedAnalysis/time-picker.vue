<!-- 用一个时间段，呼出一个dialog，通过这个dialog筛选查看时间，提供每日，全选，全不选，每周一二三四五六七，然后抛出事件来更新时间 -->
<template>
    <el-button type="primary" @click="callDialog">修改查看时间段</el-button>
    <el-dialog title="修改查看时间段" v-model="dialogVisible" append-to-body>
        <el-space direction="vertical" alignment="left">
            <el-button-group>
                <el-button @click="selectAll" size="mini">全选</el-button>
                <el-button @click="selectReverse" size="mini">反选</el-button>
                <el-button @click="selectNone" size="mini">全不选</el-button>
            </el-button-group>

            <el-checkbox-group v-model="selectedTimes">
                <el-checkbox-button
                    v-for="time in allTimes"
                    :label="time"
                    :key="time"
                    >{{ time2label[time] }}</el-checkbox-button
                >
            </el-checkbox-group>
        </el-space>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="confirm">确 定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, Ref, SetupContext, toRefs } from "vue";
import _ from "lodash";
import { getDiffDate, getWeekday2Dates } from "./util";
function useDialog(modelValue: Ref<number[]>, context: SetupContext) {
    const dialogVisible = ref(false);
    const selectedTimes = ref<number[]>([]);
    const allTimes = _.range(0, 24);
    const time2label: Record<number, string> = _(allTimes)
        .map((timeNum) => {
            const startTime = _.padStart("" + timeNum, 2, "0") + ":00";
            const endTime = _.padStart("" + (timeNum + 1), 2, "0") + ":00";
            return [timeNum, startTime + "-" + endTime] as [number, string];
        })
        .fromPairs()
        .value();
    function callDialog() {
        selectedTimes.value = _.cloneDeep(modelValue.value); // 保证不会联动改变
        dialogVisible.value = true;
    }
    function confirm() {
        context.emit("update:modelValue", _.cloneDeep(selectedTimes.value));
        dialogVisible.value = false;
    }
    function selectAll() {
        selectedTimes.value = _.cloneDeep(allTimes);
    }
    function selectNone() {
        selectedTimes.value = [];
    }
    function selectReverse() {
        allTimes.forEach((time) => {
            const index = selectedTimes.value.indexOf(time);
            if (index === -1) {
                // 日期不在区间里，push它
                selectedTimes.value.push(time);
            } else {
                selectedTimes.value.splice(index, 1);
            }
        });
    }

    return {
        dialogVisible,
        callDialog,
        confirm,
        selectAll,
        selectNone,
        selectReverse,
        time2label,
        selectedTimes,
        allTimes,
    };
}

export default defineComponent({
    props: {
        modelValue: Array as PropType<number[]>, // 当前选择的时间
    },
    setup(props, context) {
        const { modelValue } = toRefs(props);
        return {
            ...useDialog(modelValue as Ref, context),
        };
    },
});
</script>

<style>
</style>