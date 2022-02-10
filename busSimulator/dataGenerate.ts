#!/usr/bin/env ts-node
// 生成所需json
// @ts-ignore
const axios = require("axios")
// @ts-ignore
const fs = require("fs")
import * as _ from "lodash";
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
type Node_ = {
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
    nodes: Array<Node_>,
    nodepaths: Array<Nodepath>
}
type AllInfo = {
    buses: Record<number, Bus>,
    buslines: Record<number, Busline>,
    nodes: Record<number, Node_>,
    nodepaths: Record<number, Nodepath>,
}
axios.get("http://localhost:8080/api/getInfo?adcode=110101").catch(err => console.log(err))
    .then(res => {
        let { buses: buses_, buslines: buslines_, nodes: nodes_, nodepaths: nodepaths_ } = res.data as BeforeInfo;
        let buses: Record<number, Bus> = {}
        let buslines: Record<number, Busline> = {}
        let nodes: Record<number, Node_> = {}
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
        doSomething({ buses, buslines, nodes, nodepaths });
    })

// 生成python所使用的json
function doSomething({ buses, buslines, nodes, nodepaths } : AllInfo) {
    let res : {
        bus_id : number,
        line_id : number,
        nodeList : {
            node_id : number,
            lat : number, 
            lont: number, 
            isStation : boolean
        }[],
        speedLimitList : number[]
    }[] = []
    // 从buslines入手
    _(buslines).forEach(({bus_id_list, nodepath_id_list, start_node_id, line_id} : Busline)=>{
        let nodeIdList = [start_node_id]
            .concat(nodepath_id_list
                .map(nodepath_id=>nodepaths[nodepath_id].node2_id)) // 
        let nodeList = 
            nodeIdList.map(node_id=>nodes[node_id])
            .map(({node_id, lat, lont, isStation})=>{
                return {node_id, lat, lont, isStation};
            })
        let speedLimitList = nodepath_id_list
        .map(nodepath_id=>nodepaths[nodepath_id].speed_limit)
        bus_id_list.forEach(bus_id=>{
            res.push({
                bus_id,
                line_id,
                nodeList,
                speedLimitList
            })
        })
    })
    fs.writeFileSync("data.json", JSON.stringify(res))
}