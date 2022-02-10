import { computed, defineComponent, onDeactivated, onMounted, onUnmounted, PropType, Ref, ref, shallowRef, toRefs, watch } from "vue";
import * as echarts from "echarts";
import _ from "lodash";
import 'echarts/extension/bmap/bmap'
import {ElMessage} from "element-plus"
function loadBMap(ak:string) {
    return new Promise(function(resolve, reject) {
        //@ts-ignore
        if (typeof BMap !== 'undefined') {
            //@ts-ignore
            resolve(BMap)
            return true
        }
        //@ts-ignore
        window.onBMapCallback = function() {
            //@ts-ignore
            resolve(BMap)
        }
        let script = document.createElement('script')
        script.type = 'text/javascript'
        script.src =
            'http://api.map.baidu.com/api?v=2.0&ak=' + ak + '&callback=onBMapCallback'
        script.onerror = function(ev) {
            alert("地图加载失败！请检查网络连接！")
            reject(ev);
        }
        document.head.appendChild(script)
    })
}

export function useChart(options : Ref[],callbackFn? : (chart : echarts.ECharts) => void , useBaiduMap? : boolean) {
    
    function randomString(e?:number) {  
        e = e || 32;
        let t = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678",
        a = t.length,
        n = "";
        for (let i = 0; i < e; i++) n += t.charAt(Math.floor(Math.random() * a));
        return n
      }
    const elementId = ref(randomString())      
    const chart = shallowRef<echarts.ECharts>(); // 用ref对性能的损耗是致命的
    // 使用onUnmounted会在改变标签页的时候销毁
    /*
    onDeactivated(()=>{
        // 销毁实例
        chart.value?.dispose()
        console.log("销毁")
    })
    */
    onMounted(() => {
        if (chart.value !== undefined) { // 已经初始化了
            console.log("重初始化")
            return;
        }
        const elem = document.getElementById(elementId.value);
        if (!elem) {
            throw new Error(`没有找到元素${elementId.value}`);
        }
        chart.value = echarts.init(document.getElementById(elementId.value)!);
        if (callbackFn) {
            callbackFn(chart.value)
        }
        const resizer = ()=>{chart.value!.resize()}
        window.addEventListener("resize", resizer)
        /*
        onDeactivated(()=>{
            window.removeEventListener("resize", resizer)
        })
        */
        function enableWatcher() {
            options.forEach(option=>{
                watch(option, (val) => {
                    if (val.replaceOption){  // 替换，而非增量
                        console.log("完全重载！")
                        chart.value!.setOption(val, true)
                    }else
                        chart.value!.setOption(val)
                }, { immediate:true, deep: true })
            })
        }
        if (useBaiduMap)
            loadBMap("gFEPyHnP0IHgIdO8W403XlHN56iwqrul").then(()=>{
                enableWatcher()
            }).catch(err=>{
                alert("地图加载失败！")
            })
        else {
            enableWatcher()
        }
    });
    return { chart, elementId };
}

