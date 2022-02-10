import * as fs from 'fs';
import * as _ from 'lodash';
import { random, range } from 'lodash';
import { exit } from 'process';
// 根据路网邻接矩阵生成随机数量线路
let matrixJson : Record<string,  Array<string>> = JSON.parse(fs.readFileSync('./mapMatrix.json').toString())
let matrix = _(matrixJson).keys().value();
function splitPosStr(str: string):[number,number] {
    let a, b = str.split(',');
    return [Number(a),Number(b)]
}
let res = []
// 总之超多
let i = 0;
let j = 0;
_(matrix).shuffle().forEach((startPos,k)=>{
    let set = new Set<string>();
    set.add(startPos);
    let path = [startPos];
    let retry = 0;
    while (retry < 10000 && path.length < 1000) {
        retry++;
        let coords = matrixJson[_.last(path)];
        if (!coords) return;
        let rand = Math.floor(random(1, true) * coords.length);
        if (set.has(coords[rand])) continue; // 简单粗暴，直接重来
        set.add(coords[rand]);
        path.push(coords[rand])
    }
    i += 1;
    console.log(`当前进度：${i}/${matrix.length}，已生成${j}条线路，当前线路长度为${path.length}`)
    if (path.length < 100) return;
    j += 1;
    
    res.push(path)
});
console.log(`完成！生成线路总数为${res.length}，写入文件……`)
fs.writeFileSync("paths.json", JSON.stringify(res))