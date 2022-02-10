<!-- 
展示总览的界面
其中应当展示当前选择地点——用tag，总共线路，节点，路径，公交车，站点的数量，以及对其的修改的数量。
-->
<template>
    <el-header>
        <h1 style="font-size: 30px">地区总览</h1>
    </el-header>
    <el-main id="main">
        <el-space direction="vertical" alignment="left">
            <p>
                <span>操作地区：</span>
                <el-space>
                    <el-tag v-for="place in places" :key="place.adcode">
                        {{ place.name }}
                    </el-tag>
                </el-space>
            </p>
            <el-space :size="100">
                <div>
                    <h2>公交车</h2>
                    <el-table :data="[busCounter]" style="width: 100%">
                        <el-table-column align="center" prop="0" label="总数">
                        </el-table-column>
                        <el-table-column align="center" prop="1" label="插入数">
                        </el-table-column>
                        <el-table-column align="center" prop="2" label="修改数">
                        </el-table-column>
                        <el-table-column align="center" prop="3" label="删除数">
                        </el-table-column>
                    </el-table>
                </div>
                <div>
                    <h2>线路</h2>
                    <el-table :data="[buslineCounter]" style="width: 100%">
                        <el-table-column align="center" prop="0" label="总数">
                        </el-table-column>
                        <el-table-column align="center" prop="1" label="插入数">
                        </el-table-column>
                        <el-table-column align="center" prop="2" label="修改数">
                        </el-table-column>
                        <el-table-column align="center" prop="3" label="删除数">
                        </el-table-column>
                    </el-table>
                </div>
            </el-space>
            <el-space :size="100">
                <div>
                    <h2>节点</h2>
                    <el-table :data="[nodeCounter]" style="width: 100%">
                        <el-table-column align="center" prop="0" label="总数">
                        </el-table-column>
                        <el-table-column align="center" prop="1" label="插入数">
                        </el-table-column>
                        <el-table-column align="center" prop="2" label="修改数">
                        </el-table-column>
                        <el-table-column align="center" prop="3" label="删除数">
                        </el-table-column>
                    </el-table>
                </div>
                <div>
                    <h2>路径</h2>
                    <el-table :data="[nodepathCounter]" style="width: 100%">
                        <el-table-column align="center" prop="0" label="总数">
                        </el-table-column>
                        <el-table-column align="center" prop="1" label="插入数">
                        </el-table-column>
                        <el-table-column align="center" prop="2" label="修改数">
                        </el-table-column>
                        <el-table-column align="center" prop="3" label="删除数">
                        </el-table-column>
                    </el-table>
                </div>
            </el-space>
        </el-space>
    </el-main>
</template>

<script lang="ts">
import { computed, defineComponent, inject, PropType, Ref, toRefs, watch } from "vue";
import { OptionList, OptionType, Target } from "../util/adminApi";
import { AllInfo } from "../util/api";
import * as _ from "lodash";

// 生成各个数据的counter
function useCounter(info: Ref<AllInfo>, optionList: Ref<OptionList>) {
    function countAndSpreate(target: Target) {
        const map: Record<OptionType, number> = {
            INSERT: 0,
            UPDATE: 1,
            DELETE: 2,
        };
        let res = [0, 0, 0]; // 插入，更新，删除
        optionList.value
            .filter((v) => v.target === target)
            .forEach((v) => {
                res[map[v.type]]++;
            });
        return res;
    }
    const { buses, buslines, nodes, nodepaths } = toRefs(info.value);
    const oriBusesSize = _(buses.value).size()
    const oriBuslinesSize = _(buslines.value).size()
    const oriNodesSize = _(nodes.value).size()
    const oriNodepathsSize = _(nodepaths.value).size()
    const busCounter = computed(() => [
        oriBusesSize,
        ...countAndSpreate("BUS"),
    ]);
    const buslineCounter = computed(() => [
        oriBuslinesSize,
        ...countAndSpreate("BUSLINE"),
    ]);
    const nodeCounter = computed(() => [
        oriNodesSize,
        ...countAndSpreate("NODE"),
    ]);
    const nodepathCounter = computed(() => [
        oriNodepathsSize,
        ...countAndSpreate("NODEPATH"),
    ]);
    return { busCounter, buslineCounter, nodeCounter, nodepathCounter };
}

export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        places: Object as PropType<{ adcode: number; name: string }[]>,
    },
    setup(props, context) {
        let { info, places } = toRefs(props);
        const optionList = inject("optionList") as Ref<OptionList>
        if (
            !info ||
            !optionList ||
            !places ||
            !info.value ||
            !optionList.value ||
            !places.value
        ) {
            throw new Error("impossible!");
        }
        const counters = useCounter(
            info as Ref<AllInfo>,
            optionList as Ref<OptionList>
        );

        return {
            ...counters,
            places,
        };
    },
});
</script>

<style scoped>
#main {
    padding-left: 50px;
    margin-top: 30px;
}
</style>