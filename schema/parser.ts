#!/usr/bin/env ts-node

// 将shp转换为json并保存
import * as fs from 'fs';
import * as proj4 from 'proj4'
import * as _ from 'lodash';
function coordParse(coord : [number, number]) : [number, number] {
    let res = proj4(`
    PROJCS["WGS_1984_UTM_Zone_50N",GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137.0,298.257223563]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]],PROJECTION["Transverse_Mercator"],PARAMETER["False_Easting",500000.0],PARAMETER["False_Northing",0.0],PARAMETER["Central_Meridian",117.0],PARAMETER["Scale_Factor",0.9996],PARAMETER["Latitude_Of_Origin",0.0],UNIT["Meter",1.0]]
    `)
        .inverse(coord);
    res[0] = Number(res[0].toFixed(6))
    res[1] = Number(res[1].toFixed(6))
    return res;
}
let links = JSON.parse(fs.readFileSync('./beijing/Beijing_Links.json').toString()).features
//let nodes = JSON.parse(fs.readFileSync('./beijing/Beijing_Nodes.json').toString()).features
let minLat = 116.206980;
let maxLat = 116.567954;
let minLont = 39.788708;
let maxLont = 40.052559;
let map : Record<string, Array<string>> = {}
// 生成路网的邻接矩阵
links.forEach((val, i) => {
    console.log(`${i}/${links.length}`)
    let coords : [number, number][] = val.geometry.coordinates.map(coordParse);
    // 删除所有超过这个范围的点
    coords = coords.filter(([lat, lont])=>{
        return lat < maxLat && lat > minLat && lont < maxLont && lont > minLont
    })
    coords.forEach((v, i, arr)=>{
        let v_str = v.join(',');
        if (!(v_str in map)) 
            map[v_str] = [];
        if (i < arr.length - 1) 
            map[v_str].push(arr[i+1].join(','))
        if (i > 1)
            map[v_str].push(arr[i - 1].join(','));
    }) 
});

let matrixJson = JSON.stringify(map);
fs.writeFileSync('mapMatrix.json', matrixJson);

