#!/usr/bin/env ts-node
// ! 为了方便，直接使用同步操作
import * as fs from 'fs';
import * as mysql from 'mysql';
import * as _ from 'lodash';
import { exit } from 'process';

let busTypes = ["本田","大众","三菱","通用","五菱宏光"]
function generateBus(i : number) {
    function pad(num:number|string, n:number) {
        let len = num.toString().length;
        while(len < n) {
          num = "0" + num;
          len++;
        }
        return num;
      }
    return `("京AX${pad(i, 4)}", "${busTypes[_.random(busTypes.length - 1)]}")`
}
let initSqlStr = fs.readFileSync('./entity.sql').toString()

let connection = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'aaaaaaaa',
    database: 'yukinaDB',
    multipleStatements: true
});

connection.connect((err, res) => {
    if (err) throw err;
    console.log("成功连接数据库")
    connection.query(initSqlStr, () => {
        console.log("成功初始化数据库中各表");
        generateData();
    });
});

// * 利用路网数据重构后的信息的json来向数据库中插入数据
function generateData() {
    // 50条线路
    let paths : string[][] =  _(JSON.parse(fs.readFileSync('../paths.json').toString())).shuffle().slice(0, 50).value();
    // paths中存在一些null，清理掉
    for (let i = 0 ; i < paths.length; i++) {
        paths[i] = _.compact(paths[i])
    }
    let poses = _(paths).flatMap().uniq().value();
    let insertPlaceSql = `
    insert ignore into place(place_name, lat, lont)
    values ("北京市-北京市-东城区", 116.397128, 39.916527);`
    let i = 1;
    let pos2nodeId : Record<string, number> = {}
    let coord2nodepathId : Record<string, number> = {}
    let insertNodeSql = `
    insert into node(lat, lont)
    values 
    ${poses.map(pos=>{
        pos2nodeId[pos] = i++;
        return `(${pos})`}).join(',\n')};
    `
    let stations = _(poses).shuffle().slice(0, 400).value();
    let stationSet = new Set<string>(); 
    stations.forEach(station=>stationSet.add(station));
    let insertStationSql = `
    insert ignore into station(node_id)
    values
    ${stations.map(v=>`(${pos2nodeId[v]})`).join(',\n')};
    `
    let pairs : [string,string][] = []
    for (let i = 0; i < paths.length; i++) {
        let path = paths[i]
        for (let j = 0; j < path.length - 1; j++) {
            pairs.push([path[j], path[j + 1]]);
        }
    }
    i = 1;
    let insertNodepathSql = `
    insert into nodepath
    (node1_id,node2_id,speed_limit,direction,street_name)
    values
    ${pairs.map(v=>{
        coord2nodepathId[v[0]+v[1]] = i++;
        return `(
            ${pos2nodeId[v[0]]},
            ${pos2nodeId[v[1]]},
            ${_.random(20, 120, false)},
            0,
            "TODO:随机名称"
            )`
    }).join(',\n')};
    `
    // 50条线路，每条10辆公交车
    let insertBusSql = `
        insert ignore into bus(bus_number, bus_type)
        values
        ${_.range(1,501).map(generateBus).join(',')};
    `
    let insertBuslineSql = `
    insert into busline (line_name,data_json)
    values
    ${paths.map((path, i) => {
        i += 1;
        let nodepaths = []
        let isStation = []
        for (let i = 1; i < path.length; i++) {
            nodepaths.push(path[i-1]+path[i]);
            isStation.push(stationSet.has(path[i]));
        }
        let startNode = path[0]; 
        let startNodeIsStation = stationSet.has(startNode);
        
        let json = {data:[[pos2nodeId[startNode], startNodeIsStation]].concat(
            _.zip(nodepaths.map(nodepath=>coord2nodepathId[nodepath]), isStation)
        )}
        return `("${i}路", '${JSON.stringify(json)}')`
    }).join(',')};
    `
    let insertLineOfSpaceSql = `
    insert into line_place(line_id, place_id)
    values 
    ${_.range(1, 51).map(i=>`(${i}, 1)`).join(',')};
    `
    let insertBusOfLineSql = `
    insert into bus_line(line_id, bus_id)
    values
    ${_.range(1, 501).map(bus_id => {
        let line_id = Math.floor((bus_id-1) / 10) + 1;
        return `(${line_id}, ${bus_id})`;
    }).join(',')};
    `

    let sql = 'use yukinaDB;' +
    insertPlaceSql +
     insertNodeSql +
     insertStationSql +
      insertNodepathSql +
       insertBusSql +
        insertBuslineSql +
         insertLineOfSpaceSql +
         insertBusOfLineSql
    connection.query(sql, function(err){
        if (err) throw err;
        console.log("成功插入到数据库中！")
        exit(0);
    })
}


