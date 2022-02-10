import axios from "axios";
import * as _ from "lodash";
import {loginURL, changePasswdURL, changeNameURL} from "../config"
export default {
    login(id : string, passwd : string, callbackFn : (status : string)=>void, failFn : (err : Error) => void) {
        function setCookie(name : string, value : string) {
            let cookie = name + "=" + value + ";";
            document.cookie = cookie;
        }
        axios.post(loginURL, {id, passwd}, {headers:{'Content-Type': 'application/json; charset=utf-8'}}).catch(err=>failFn(err)).then((res)=>{
            if (!res) {return;};
            setCookie("id",res.data.id)
            setCookie("token",res.data.token)
            setCookie("name", res.data.name)
            callbackFn(res.data.status);
        })
    },
    loginCheck() : {name : string,id : string, token : string} | undefined | null {
        function getCookie(cname : string) {
            let name = cname + "=";
            let cookie = document.cookie.split(';');
            for(let i = 0, len = cookie.length; i < len; i++) {
                let c = cookie[i].trim();
                if (c.indexOf(name) == 0) {
                return c.substring(name.length, c.length);
                }
            }
            return "";
            }
        // TODO : 检查cookie是否存在，若存在就直接过
        if (getCookie("token") !== "")
            return {name : getCookie("name"), id : getCookie("id"), token : getCookie("token")}
        return null;
    },
    changePasswd(oldPasswd: string,newPasswd: string, successFn:(newToken : string)=>void, failFn:()=>void) {
        const ori = this.loginCheck()
        if (!ori) return;
        console.log(oldPasswd, newPasswd, ori)
        const {id,name} = ori;
        axios.post(changePasswdURL, {id, oldPasswd, newPasswd})
            .then((res_)=> {
                console.log(res_)
                const res : {status : string, token : string} = res_.data;
                if (res.status === "failed!") {
                    failFn()
                    return
                }
                successFn(res.token)
            })
            .catch((err)=>failFn())
    },
    changeName(token: string,newName: string, successFn:()=>void, failFn:()=>void) {
        const ori = this.loginCheck()
        if (!ori) return;
        const {id,name} = ori;
        axios.post(changeNameURL, {id, token, newName})
            .then((res)=>successFn())
            .catch((err)=>failFn())
    }
}

export type OptionType = "UPDATE" | "DELETE" | "INSERT"
export type Target = "NODE" | "NODEPATH" | "BUSLINE" | "PLACE" | "BUS" | "BUS_OF_PLACE" | "BUSLINE_OF_PLACE"
export interface Option {
  type : OptionType,
  target : Target,
  data : Object
}
export interface InsertOption extends Option {
  type : "INSERT"
}
export interface UpdateOption extends Option {
  type : "UPDATE"
}
export interface DeleteOption extends Option {
  type : "DELETE"
}
// 这样设计有点蛋疼……
// 可以将操作里的各种id都设置成临时的，由后端进行处理。


// 插入操作
export interface InsertBusOption extends InsertOption {
    target : "BUS",
    data : {bus_id : number, bus_number : string, bus_type : string}
}
export interface InsertBusIntoPlaceOption extends InsertOption{
    target : "BUS_OF_PLACE",
    data : {bus_id : number, adcode : number}
}

export interface InsertPlaceOption extends InsertOption {
    target : "PLACE",
    data : {adcode : number, place_name : string, lat : number, lont : number}
}

export interface InsertNodeOption extends InsertOption {
  target : "NODE",
  data : {node_id : number, lat : number, lont : number, isStation : boolean, adcode : number}
}

export interface InsertNodepathOption extends InsertOption {
    target : "NODEPATH",
    data : {nodepath_id : number, node1_id : number, node2_id : number, speed_limit : number, adcode : number, street_name : string}
}

export interface InsertBuslineOption extends InsertOption {
    target : "BUSLINE",
    data : {line_id : number, line_name : string, start_node_id : number, bus_id_list : number[], nodepath_id_list : number[]}
}

export interface InsertLineIntoPlaceOption extends InsertOption {
    target : "BUSLINE_OF_PLACE",
    data : {adcode : number, line_id : number}
}


// 删除操作
export interface DeleteBusOption extends DeleteOption {
    target : "BUS",
    data : {bus_id : number}
}
export interface DeleteBusFromPlaceOption extends DeleteOption{
    target : "BUS_OF_PLACE",
    data : {bus_id : number, adcode : number}
}

export interface DeletePlaceOption extends DeleteOption {
    target : "PLACE",
    data : {adcode : number}
}

export interface DeleteNodeOption extends DeleteOption {
  target : "NODE",
  data : {node_id : number}
}

export interface DeleteNodepathOption extends DeleteOption {
    target : "NODEPATH",
    data : {nodepath_id : number}
}

export interface DeleteBuslineOption extends DeleteOption {
    target : "BUSLINE",
    data : {line_id : number}
}

export interface DeleteLineFromPlaceOption extends DeleteOption {
    target : "BUSLINE_OF_PLACE",
    data : {adcode : number, line_id : number}
}


// 更新操作
export interface UpdateBusOption extends UpdateOption {
    target : "BUS",
    data : {bus_id : number, bus_number : string, bus_type : string}
}

export interface UpdatePlaceOption extends UpdateOption {
    target : "PLACE",
    data : {adcode : number, place_name : string, lat : number, lont : number}
}

export interface UpdateNodeOption extends UpdateOption {
  target : "NODE",
  data : {node_id : number, lat : number, lont : number, isStation : boolean, adcode : number}
}

export interface UpdateNodepathOption extends UpdateOption {
    target : "NODEPATH",
    data : {nodepath_id : number, node1_id : number, node2_id : number, speed_limit : number, adcode : number, street_name : string}
}

export interface UpdateBuslineOption extends UpdateOption {
    target : "BUSLINE",
    data : {line_id : number, line_name : string, start_node_id : number, bus_id_list : number[], nodepath_id_list : number[]}
}

export type OptionList = Array<Option>; // 要返回给后端的玩意。前端对这个要先进行转码，即对每一个操作都将其编号转换成真正的编号

// 最后push的时候，添加一个id用来标识各个请求的相对顺序，以保证不会在后端重排的过程中对target，type相同的操作顺序搞错导致问题

function getType(options: OptionList, type : OptionType) {
    return options.filter((opt)=>opt.type===type)
}
export function getInsertOptions(options: OptionList) {
    let res : {
        "BUS" : InsertBusOption[],
        "BUSLINE" : InsertBuslineOption[],
        "NODE" : InsertNodeOption[],
        "NODEPATH" : InsertNodepathOption[],
        "PLACE" : InsertPlaceOption[],
        "BUS_OF_PLACE" : InsertBusIntoPlaceOption[],
        "BUSLINE_OF_PLACE" : InsertLineIntoPlaceOption[]
    } = {
        "BUS" : [],
        "BUSLINE" : [],
        "NODE" : [],
        "NODEPATH" : [],
        "PLACE" : [],
        "BUS_OF_PLACE" : [],
        "BUSLINE_OF_PLACE" : []
    };
    (getType(options, "INSERT") as InsertOption[]).forEach((opt)=>{
        // @ts-ignore
        res[opt.target].push(opt)
    })
    return res;
}
export function getDeleteOptions(options: OptionList) {
    let res : {
        "BUS" : DeleteBusOption[],
        "BUSLINE" : DeleteBuslineOption[],
        "NODE" : DeleteNodeOption[],
        "NODEPATH" : DeleteNodepathOption[],
        "PLACE" : DeletePlaceOption[],
        "BUS_OF_PLACE" : DeleteBusFromPlaceOption[],
        "BUSLINE_OF_PLACE" : DeleteLineFromPlaceOption[]
    } = {
        "BUS" : [],
        "BUSLINE" : [],
        "NODE" : [],
        "NODEPATH" : [],
        "PLACE" : [],
        "BUS_OF_PLACE" : [],
        "BUSLINE_OF_PLACE" : []
    };
    (getType(options, "DELETE") as DeleteOption[]).forEach((opt)=>{
        // @ts-ignore
        res[opt.target].push(opt)
    })
    return res;
}
export function getUpdateOptions(options: OptionList) {
    let res : {
        "BUS" : UpdateBusOption[],
        "BUSLINE" : UpdateBuslineOption[],
        "NODE" : UpdateNodeOption[],
        "NODEPATH" : UpdateNodepathOption[],
        "PLACE" : UpdatePlaceOption[]
    } = {
        "BUS" : [],
        "BUSLINE" : [],
        "NODE" : [],
        "NODEPATH" : [],
        "PLACE" : []
    };
    (getType(options, "UPDATE") as UpdateOption[]).forEach((opt)=>{
        // @ts-ignore
        res[opt.target]!.push(opt)
    })
    return res;
}

