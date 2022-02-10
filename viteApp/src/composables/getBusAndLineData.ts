import axios from "axios";
import * as _ from "lodash";
import { ref, Ref, shallowRef } from "vue";
import api, { AllInfo } from "../util/api";
import { BusAndAllNodeAdapter, busAndLine } from "../util/messageAdapter";
/**
 * @param places 
 * @param date 
 */
export default function(places: {
    NAME_CHN: string;
    level?: string;
    adcode: number;
    x: number;
    y: number;
}[], date : string | null) {
    // TODO 或许这里之后要改成能够获取多个区/县……这修改并不难
    let {x, y, adcode} = _.last(places)!;
    let place_name = places.map(place=>place.NAME_CHN).join("-");
    const info = shallowRef<AllInfo>();
    api.getAllInfo(adcode, date, res=>{
        info.value = res;
    })
    return {x, y, adcode, place_name, info}
}

/**
 * @deprecated
 * @param places 
 * @param date 
 * @returns 
 */
function ori(places: {
    NAME_CHN: string;
    level?: string;
    adcode: number;
    x: number;
    y: number;
}[], date? : string) {
        const busAndLineData = ref<busAndLine>();
        const busIdList = ref<number[]>();
        // * 获取区/县
        let {x, y} = _.last(places)!;
        let place_name = places.map(place=>place.NAME_CHN).join("-");
        api.getBusAndLineInfo(place_name, (res) => {
            console.log("成功获取线路和公交车信息")
            busAndLineData.value = res;
            busIdList.value = _(busAndLineData.value.busId2LineId)
                .map((lineId, busId) => Number(busId))
                .value();
        }, date);
        
        return {busAndLineData, busIdList, place: {x, y, place_name}}
}