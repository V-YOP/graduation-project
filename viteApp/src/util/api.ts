import axios from 'axios';
import { valuesIn } from 'lodash';
import * as config from "../config"
import * as _ from 'lodash'
import { BusAndAllNodeAdapter } from "./messageAdapter"
import { busAndLine } from './messageAdapter';
export type pos = {
    bus_id: number,
    update_time: string,
    speed: string,
    lat: number,
    lont: number
}
export type overspeedPos = {
    bus_id: number,
    speed: number,
    a : number,
    speed_limit : number,
    nodepath_id: number,
    adcode: number,
    line_id : number,
    lat: number, 
    lont: number
    update_time: string
}
type Bus = {
    bus_id: number,
    bus_number: string,
    bus_type: string
}
type BeforeBusline = {
    line_id: number,
    line_name: string,
    start_node_id: number,
    bus_id_list: string, // JSON字符串
    nodepath_id_list: string, // 同上
}
type Busline = {
    line_id: number,
    line_name: string,
    start_node_id: number,
    bus_id_list: number[], // JSON字符串
    nodepath_id_list: number[], // 同上
}
type Node = {
    node_id: number,
    adcode: number,
    lat: number,
    lont: number,
    isStation: boolean
}
type Nodepath = {
    nodepath_id: number,
    node1_id: number,
    node2_id: number,
    speed_limit: number,
    adcode: number,
    street_name: string
}
type BeforeInfo = {
    buses: Array<Bus>,
    buslines: Array<BeforeBusline>,
    nodes: Array<Node>,
    nodepaths: Array<Nodepath>
}
export type AllInfo = {
    buses: Record<number, Bus>,
    buslines: Record<number, Busline>,
    nodes: Record<number, Node>,
    nodepaths: Record<number, Nodepath>,
}
export function TODO(reason: string = "not implemented!") {
    throw new Error(reason);
}
function convertInfo(ori : BeforeInfo) : AllInfo {
    let { buses: buses_, buslines: buslines_, nodes: nodes_, nodepaths: nodepaths_ } = ori;
    let buses: Record<number, Bus> = {}
    let buslines: Record<number, Busline> = {}
    let nodes: Record<number, Node> = {}
    let nodepaths: Record<number, Nodepath> = {}
    buses_.forEach(bus => buses[bus.bus_id] = bus)
    buslines_.forEach(busline => {
        buslines[busline.line_id] = {
            line_id: busline.line_id,
            line_name: busline.line_name,
            start_node_id: busline.start_node_id,
            bus_id_list: JSON.parse(busline.bus_id_list),
            nodepath_id_list: JSON.parse(busline.nodepath_id_list)
        }
    });
    nodes_.forEach(node => nodes[node.node_id] = node);
    nodepaths_.forEach(nodepath => nodepaths[nodepath.nodepath_id] = nodepath)
    return { buses, buslines, nodes, nodepaths };
}

// 合并两个info
function mergeInfo(infoA : BeforeInfo, infoB : BeforeInfo) : BeforeInfo {
    return {
        buses : _.uniqBy(infoA.buses.concat(infoB.buses), (bus)=>bus.bus_id),
        buslines : _.uniqBy(infoA.buslines.concat(infoB.buslines), (busline)=>busline.line_id),
        nodes : _.uniqBy(infoA.nodes.concat(infoB.nodes), (node)=>node.node_id),
        nodepaths : _.uniqBy(infoA.nodepaths.concat(infoB.nodepaths), (nodepath)=>nodepath.nodepath_id)
    }
}

export default {
    // 用于管理
    getInfos(adcodes: number[], callbackFn: (data: AllInfo) => void) {
        axios.get(`${config.getInfos}?adcodes=${adcodes.join(",")}`).catch(err => console.log(err))
            .then((res) => {
                if (!res) return;
                let beforeInfos = res.data as BeforeInfo[]
                // 对这些信息进行合并
                //callbackFn(convertInfo());
                if (beforeInfos.length === 0) {
                    throw new Error("接受为空？？")
                }
                callbackFn(convertInfo(beforeInfos.reduce((pre,cur)=>{return mergeInfo(pre,cur)})));
            })
    },
    getAllInfo(adcode: number, date: string | null, callbackFn: (data: AllInfo) => void) {
        axios.get(`${config.getInfo}?adcode=${adcode}${date ? `&date=${date}` : ''}`).catch(err => console.log(err))
            .then((res) => {
                if (!res) return;
                callbackFn(convertInfo(res.data as BeforeInfo));
            })
    },

    /**
     * @deprecated
     * @param place_name 
     * @param date yyyy-MM-dd形式字符串
     */
    getBusAndLineInfo(place_name: string, callbackFn: (data: busAndLine) => void, date?: string) {
        let axiosObj;
        if (date)
            axiosObj = axios.get(`${config.getBusAndLineInfoHistoryURL}?place_name=${place_name}&query_time=${date}`).catch(err => console.log(err))
        else axiosObj = axios.get(`${config.getBusAndLineInfoURL}?place_name=${place_name}`).catch(err => console.log(err));
        axiosObj.then((res) => {
            if (res)
                callbackFn(BusAndAllNodeAdapter(res.data));
        })
    },

    /**
     * 请求可能超长
     * @param bus_ids 
     * @param start_time yyyy-MM-dd HH:mm:ss格式字符串
     * @param end_time yyyy-MM-dd HH:mm:ss格式字符串
     */
    getoverspeedPosesBetween(bus_ids: number[], start_time: string, end_time: string, callbackFn: (poses: pos[]) => void) {
        axios.get(`${config.getPosesByBusIDBetweenURL}?busIdList=${encodeURI(JSON.stringify(bus_ids))}&startTime=${start_time}&endTime=${end_time}`)
            .catch(err => console.log(err))
            .then((res) => {
                if (!res) return;
                callbackFn(res.data)
            });
    },
    getoverspeedPosesByAdcodeBetween(adcode : number, start_time: string, end_time: string, callbackFn: (poses: overspeedPos[]) => void) {
        console.log("请求", `${config.getoverspeedPosesByAdcodeBetweenURL}?adcode=${adcode}&startTime=${start_time}&endTime=${end_time}`)
        axios.get(`${config.getoverspeedPosesByAdcodeBetweenURL}?adcode=${adcode}&startTime=${start_time}&endTime=${end_time}`)
            .catch(err => console.log(err))
            .then((res) => {
                if (!res) return;
                callbackFn(res.data)
            });
    },

    /**
     * 生成获取信息的ws
     * @param bus_ids 
     * @param callbackFn 
     * @returns WebSocket对象
     */
    posInfoGetterWS(bus_ids: number[], onMessage: (poses: pos[], overspeedPos: overspeedPos[]) => void) {
        // * busList不能太长，所以原本使用的将busList塞到参数里的手段是不行的
        let ws = new WebSocket(`${config.getPosWSURL}`);
        ws.onopen = () => {
            console.log("连接建立！")
            ws.send(JSON.stringify({
                type: "busList",
                data: bus_ids
            }));
        }
        ws.onmessage = function (res) {
            let data: { poses: pos[], overspeedPoses: overspeedPos[] } = JSON.parse(res.data)
            if (!data) return;
            console.log(data);
            onMessage(data.poses, data.overspeedPoses);
        };
        ws.onerror = function (err) {
            console.log("websocket错误！", err)
        }
        ws.onclose = function (ev) {
            console.log("断开", ev)
        }
        return ws;
    },


}