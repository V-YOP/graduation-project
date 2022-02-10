// 定义相应工具函数，从而将超速信息分组
import _ from "lodash"
import { AllInfo, overspeedPos } from "../util/api"
import moment from "moment"
import * as math from "mathjs"
// 根据相关信息获取……cyka blyat

export function getBusInfo(bus_id : number, startInfo : AllInfo, endInfo : AllInfo) {
    let bus_number : null | string = null
    let bus_type : null | string = null
    
    if (endInfo.buses[bus_id]) {
        bus_number = endInfo.buses[bus_id].bus_number
        bus_type = endInfo.buses[bus_id].bus_type
        return {bus_number, bus_type}
    }
    else if (startInfo.buses[bus_id]) {
        bus_number =  startInfo.buses[bus_id].bus_number
        bus_type =  startInfo.buses[bus_id].bus_type
        return {bus_number, bus_type}
    }
    return {bus_number, bus_type}
}
export function getNodepathInfo(nodepath_id : number, startInfo : AllInfo, endInfo : AllInfo) {
    let street_name: null | string = null
    let speed_limit: null | number = null
    if (endInfo.nodepaths[nodepath_id]) {
        street_name = endInfo.nodepaths[nodepath_id].street_name
        speed_limit = endInfo.nodepaths[nodepath_id].speed_limit
        return {street_name, speed_limit}
    }
    else if (startInfo.nodepaths[nodepath_id]) {
        street_name = startInfo.nodepaths[nodepath_id].street_name
        speed_limit = startInfo.nodepaths[nodepath_id].speed_limit
        return {street_name, speed_limit}
    }
    return {street_name, speed_limit}
}

export function getBuslineInfo(line_id : number, startInfo : AllInfo, endInfo : AllInfo) {
    let line_name: null | string = null
    if (endInfo.buslines[line_id]) {
        line_name = endInfo.buslines[line_id].line_name
        return {line_name,}
    }
    else if (startInfo.buslines[line_id]) {
        line_name = startInfo.buslines[line_id].line_name
        return {line_name,}
    }
    return {line_name, }
}

export function getMergedInfo(startInfo : AllInfo, endInfo : AllInfo) : AllInfo {
    if (!startInfo && !endInfo) {
        throw new Error("选择的时间段没有信息！")
    }
    if (!startInfo) {
        return _.cloneDeep(endInfo)
    }
    if (!endInfo) {
        return _.cloneDeep(startInfo)
    }
    const {buses, buslines, nodes, nodepaths} = _.cloneDeep(startInfo)
    const copiedEndInfo = _.cloneDeep(endInfo)
    return {
        buses : _.assign(buses, copiedEndInfo.buses),
        buslines : _.assign(buslines, copiedEndInfo.buslines),
        nodes : _.assign(nodes, copiedEndInfo.nodes),
        nodepaths : _.assign(nodepaths, copiedEndInfo.nodepaths) 
    }
}

export function getRelativeInfo(overspeedInfo : overspeedPos, startInfo : AllInfo, endInfo : AllInfo) {
    let { bus_id, nodepath_id } = overspeedInfo;
    // 公交车信息
    const {bus_number, bus_type} = getBusInfo(bus_id,startInfo,endInfo)
    const { speed_limit,street_name} = getNodepathInfo(nodepath_id,startInfo,endInfo)
    return {street_name, speed_limit, bus_number, bus_type}
}


// 就实体种类来说，有地区，公交车，线路，路径

// 先提供groupBy

type Entity = "BUS" | "LINE" | "NODEPATH"

type Time= "VARIOUS_DAY_CONSTANT_TIME" | "CONSTANT_DAYS_letIOUS_TIME"

// 用于第一次筛选
export function gatherWith(
    overspeedPoses: overspeedPos[],
    entity: Entity
): Record <number, overspeedPos[]> {
    const map = {
        BUS: "bus_id",
        LINE: "line_id",
        NODEPATH: "nodepath_id"
    }
    const realEntity = map[entity]
    return _(overspeedPoses).groupBy(realEntity).value()
}

type time = [number,number]
function addMinute(time : time, minute : number) : time {
    const newTime : time = [...time]
    newTime[1] += minute
    newTime[0] += Math.floor(time[1] / 60)
    newTime[1] = newTime[1] % 60
    return newTime
}
function between(time : time, start_time : time, end_time : time) : boolean {
    return time[0] >= start_time[0] && time[0] <= end_time[0]
    && time[1] >= start_time[1] && time[1] <= end_time[1]
}

// 将超速信息按照时间段进行分组
export function gatherWithletiousDay_ConstantTime(
    overspeedPoses: overspeedPos[],
    splitter : 15 | 30 | 60,
    start_time : time, // [hour, minute] [0,0]
    end_time : time // [24,0]
) : Record<string, overspeedPos[]>{
    // 确保起始时间和终止时间的差splitter的倍数
    overspeedPoses.filter(v=>{
        const time = moment(v.update_time)
        return time.hour() >= start_time[0] && time.hour() <= end_time[0]
        && time.minute() >= start_time[1] && time.minute() <= end_time[1]
    })
    const fullTime = end_time[1] - start_time[1] + (end_time[0] - start_time[0])*60
    const SegNum = fullTime/splitter
    if (!_.isInteger(SegNum)) {
        throw new Error("时间差应当为splitter的倍数")
    }
    // 分组
    // key为H-0,H-1,H-2,H-3
    const res : Record<string, overspeedPos[]> = {}
    _.range(0, SegNum).forEach((i)=>{
        const segStartTime = addMinute(start_time, i * splitter)
        const segEndTime = addMinute(segStartTime, splitter)
        const index = `${segStartTime[0]}-${segStartTime[1]/splitter}`
        const segRes : overspeedPos[] = []
        res[index] = overspeedPoses.filter(v=>{
            const time = moment(v.update_time)
            return between([time.hour(),time.minute()], segStartTime, segEndTime)
        })
    })
    return res;
}

// 特定日期的各时间段
// 返回结果为长度为24*60/splitter的数组，元素为该时间段的值
// 这函数没法用
export function gatherWithConstantDays_letiousTime(
    overspeedPoses: overspeedPos[],
    splitter : 15 | 30 | 60,
    days_ : string[] // "YYYY-MM-dd"[]
) : overspeedPos[][] {
    const days = days_.map(v=>{
        const startTime = moment(v)
        const endTime = moment(v).hour(startTime.hour()+24)
        return [startTime,endTime]
    }) // 转换成moment对象
    const res : overspeedPos[][] = (_.range(0,(24 * 60) / splitter).map(v=>[]))
    // 第一次筛选，获取处在这些days里的时间的超速
    overspeedPoses.filter(v=>{
        return days.some(([startTime,endTime])=>{
            return moment(v.update_time).isBetween(startTime, endTime)
        })
    }).forEach(v=>{
        const theTime = moment(v.update_time)
        const fullMinutes = theTime.hour() * 60 + theTime.minute()
        res[Math.floor(fullMinutes / splitter)].push(v)
    })
    return res
}

export function countByDays(overspeedPoses : overspeedPos[], computer : (overspeedPos:overspeedPos)=>number) {
    const res : Record<string, number> = {}
    overspeedPoses.forEach(v=>{
        const date = v.update_time.substring(0,10)
        if (res[date])
            res[date]+= computer(v);
        else res[date] = computer(v);
    })
    console.log(res)
    return _(res).toPairs().value();
}

export function getComputer(overspeedFactorExpr : string) : (overspeedPos: overspeedPos) => number {
    console.log("生成expr", overspeedFactorExpr)
    return new Function("overspeedPos", `
        const A = overspeedPos.a;
        const LIMIT = overspeedPos.speed_limit;
        const V = overspeedPos.speed;
        return ${overspeedFactorExpr};
    `) as (overspeedPos: overspeedPos) => number;
}

export function getWeekday2Dates(dates : string[]) {
    const res : string[][] = _.range(0,7).map(v=>[])
    dates.forEach(date=>{
        const weekday = new Date(date).getDay()
        res[weekday].push(date)
    })
    res.push(res.shift()!) // 将星期日移到它应该在的位置……
    return res;
}

export function countByWeekDayAndHour(overspeedPoses : overspeedPos[], computer : (overspeedPos:overspeedPos)=>number) {
    const daySet = new Set<string>()
    const res : number[][] = new Array(7) // 一维代表星期几-1
    _.range(0,7).forEach(i=>{
        res[i] = new Array(24)
        _.range(0,24).forEach(j=>res[i][j] = 0)
    }) // 二维代表每个小时
    overspeedPoses.forEach(v=>{
        const date = v.update_time.substring(0,10)
        const hour = Number(v.update_time.substring(11, 13))
        if (!daySet.has(date))
            daySet.add(date)
    })
    const date2WeekDay :Record<string, number> = {}
    daySet.forEach(v=>{
        date2WeekDay[v] = new Date(v).getDay() - 1
    })
    _(date2WeekDay).forEach((v,k)=>{
        if (v === -1)
        date2WeekDay[k] = 6
    })

    overspeedPoses.forEach(v=>{
        const date = v.update_time.substring(0,10)
        const hour = Number(v.update_time.substring(11, 13))
        res[date2WeekDay[date]][hour] += computer(v)
    })
    const anotherRes : [number,number,number][] = []
    for (let i = 0; i < 7; i++)
        for (let j = 0; j < 24; j++) {
            anotherRes.push([i, j, res[i][j]])
        }
    console.log(anotherRes)
    return anotherRes;
}

export function getDiffDate(start : string, end : string) : string[]{
    function getDate (datestr : string) {
        let temp : any[]= datestr.split("-");
        if (temp[1] === '01') {
            temp[0] = parseInt(temp[0],10) - 1;
            temp[1] = '12';
        } else {
            temp[1] = parseInt(temp[1],10) - 1;
        }
        let date = new Date(temp[0], temp[1], temp[2]);
        return date;
    }
    let startTime = getDate(start);
    let endTime = getDate(end);
    let dateArr = [];
    while ((endTime.getTime() - startTime.getTime()) >= 0) {
        let year = startTime.getFullYear();
        let month = startTime.getMonth().toString().length === 1 ? "0" + (parseInt(startTime.getMonth().toString(),10) + 1) : (startTime.getMonth() + 1);
        let day = startTime.getDate().toString().length === 1 ? "0" + startTime.getDate() : startTime.getDate();
        dateArr.push(year + "-" + month + "-" + day);
        startTime.setDate(startTime.getDate() + 1);
 
    }
    return dateArr;
}

export function getMinAndMaxDate(overspeedPoses : overspeedPos[]) : [string,string] {
    const minDate = _(overspeedPoses).minBy(v=>v.update_time)!.update_time
    const maxDate = _(overspeedPoses).maxBy(v=>v.update_time)!.update_time
    return [minDate!.substring(0,10),maxDate!.substring(0,10)]
}

export function countByDayAndHour(overspeedPoses : overspeedPos[], dateRange : [string, string],computer : (overspeedPos:overspeedPos)=>number) : Record<string, number[]> {
    const days = getDiffDate(...dateRange)
    const res : Record<string, number[]> = {}
    days.forEach(day=>{
        res[day] = _.range(0,24).map(v=>0)
    })
    overspeedPoses.forEach(v=>{
        const date = v.update_time.substring(0,10)
        const hour = Number(v.update_time.substring(11, 13))
        if (!res[date]) return;
        res[date][hour] += computer(v)
    })
    return res;
}