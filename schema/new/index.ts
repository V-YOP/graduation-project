#!/usr/bin/env ts-node
// ! 为了方便，直接使用同步操作
import * as fs from 'fs';
import * as mysql from 'mysql';
import * as _ from 'lodash';
import { exit } from 'process';

let busTypes = ["本田", "大众", "三菱", "通用", "五菱宏光"]
let streetNames = ["亚伦",    "亚伯",    "亚伯拉罕",    "亚当",    "艾德里安",    "艾登",    "阿尔瓦",    "亚历克斯",    "亚历山大",    "艾伦",    "艾伯特",    "阿尔弗雷德",    "安德鲁",    "安迪",    "安格斯",    "安东尼",    "阿波罗",    "阿诺德",    "亚瑟",    "奥古斯特",    "奥斯汀",    "本",    "本杰明",    "伯特",    "本森",    "比尔",    "比利",    "布莱克",    "鲍伯",    "鲍比",    "布拉德",    "布兰登",    "布兰特",    "布伦特",    "布赖恩",    "布朗",    "布鲁斯",    "迦勒",    "卡梅伦",    "卡尔",    "卡洛斯",    "凯里",    "卡斯帕",    "塞西",    "查尔斯",    "采尼",    "克里斯",    "克里斯蒂安",    "克里斯多夫",    "克拉克",    "柯利弗",    "科迪",    "科尔",    "科林",    "科兹莫",    "丹尼尔",    "丹尼",    "达尔文",    "大卫",    "丹尼斯",    "德里克",    "狄克",    "唐纳德",    "道格拉斯",    "杜克",    "迪伦",    "埃迪",    "埃德加",    "爱迪生",    "艾德蒙",    "爱德华",    "艾德文",    "以利亚",    "艾略特",    "埃尔维斯",    "埃里克",    "伊桑",    "柳真",    "埃文",    "企业英语培训",    "福特",    "弗兰克思",    "弗兰克",    "富兰克林",    "弗瑞德",    "加百利",    "加比",    "加菲尔德",    "加里",    "加文",    "杰弗里",    "乔治",    "基诺",    "格林",    "格林顿",    "汉克",    "哈帝",    "哈里森",    "哈利",    "海顿",    "亨利",    "希尔顿",    "雨果",    "汉克",    "霍华德",    "亨利",    "伊恩",    "伊格纳缇伍兹",    "伊凡",    "艾萨克",    "以赛亚",    "杰克",    "杰克逊",    "雅各布",    "詹姆士",    "詹森",    "杰伊",    "杰弗瑞",    "杰罗姆",    "杰瑞",    "杰西",    "吉姆",    "吉米",    "乔",    "约翰",    "约翰尼",    "乔纳森",    "乔丹",    "约瑟夫",    "约书亚",    "贾斯汀",    "凯斯",    "肯",    "肯尼迪",    "肯尼斯",    "肯尼",    "凯文",    "凯尔",    "兰斯",    "拉里",    "劳伦特",    "劳伦斯",    "利安德尔",    "李",    "雷欧",    "雷纳德",    "利奥波特",    "莱斯利",    "劳伦",    "劳瑞",    "劳瑞恩",    "路易斯",    "路加",    "马库斯",    "马西",    "马克",    "马科斯",    "马尔斯",    "马歇尔",    "马丁",    "马文",    "梅森",    "马修",    "马克斯",    "迈克尔",    "米奇",    "麦克",    "内森",    "纳撒尼尔",    "尼尔",    "尼尔森",    "尼古拉斯",    "尼克",    "诺亚",    "诺曼",    "奥利弗",    "奥斯卡",    "欧文",    "帕特里克",    "保罗",    "彼得",    "菲利普",    "菲比",    "昆廷",    "兰德尔",    "伦道夫",    "兰迪",    "雷",    "雷蒙德",    "列得",    "雷克斯",    "理查德",    "里奇",    "赖利/瑞利",    "罗伯特",    "罗宾",    "鲁宾逊",    "洛克",    "罗杰",    "罗纳",    "罗文",    "罗伊",    "赖安",    "萨姆",    "萨米",    "塞缪尔",    "斯考特",    "肖恩",    "肖恩",    "西德尼",    "西蒙",    "所罗门",    "斯帕克",    "斯宾塞",    "斯派克",    "斯坦利",    "史蒂夫",    "史蒂文",    "斯图尔特",    "斯图亚特",    "特伦斯",    "特里",    "泰德",    "托马斯",    "提姆",    "蒂莫西",    "托德",    "汤米",    "汤姆",    "托马斯",    "托尼",    "泰勒",    "奥特曼",    "尤利塞斯",    "范",    "弗恩",    "弗农",    "维克多",    "文森特",    "华纳",    "沃伦",    "韦恩",    "卫斯理",    "威廉",    "维利",    "扎克",    "圣扎迦利",    "阿比盖尔",    "艾比",    "艾达",    "阿德莱德",    "艾德琳",    "亚历桑德拉",    "艾丽莎",    "艾米",    "亚历克西斯",    "爱丽丝",    "艾丽西娅",    "艾琳娜",    "艾莉森",    "艾莉莎",    "阿曼达",    "艾美",    "安伯",    "阿纳斯塔西娅",    "安德莉亚",    "安琪",    "安吉拉",    "安吉莉亚",    "安吉莉娜",    "安",    "安娜",    "安妮",    "安妮",    "安尼塔",    "艾莉尔",    "阿普里尔",    "艾许莉",    "欧蕊",    "阿维娃",    "笆笆拉",    "芭比",    "贝亚特",    "比阿特丽斯",    "贝基",    "贝拉",    "贝斯",    "贝蒂",    "贝蒂",    "布兰奇",    "邦妮",    "布伦达",    "布莱安娜",    "布兰妮",    "布列塔尼",    "卡米尔",    "莰蒂丝",    "坎蒂",    "卡瑞娜",    "卡门",    "凯罗尔",    "卡罗琳",    "凯丽",    "凯莉",    "卡桑德拉",    "凯西",    "凯瑟琳",    "凯茜",    "切尔西",    "沙琳",    "夏洛特",    "切莉",    "雪莉尔",    "克洛伊",    "克莉丝",    "克里斯蒂娜",    "克里斯汀",    "克里斯蒂",    "辛迪",    "克莱尔",    "克劳迪娅",    "克莱门特",    "克劳瑞丝",    "康妮",    "康斯坦斯",    "科拉",    "科瑞恩",    "科瑞斯特尔",    "戴茜",    "达芙妮",    "达茜",    "戴夫",    "黛比",    "黛博拉",    "黛布拉",    "黛米",    "黛安娜",    "德洛丽丝",    "堂娜",    "多拉",    "桃瑞丝",    "伊迪丝",    "伊迪萨",    "伊莱恩",    "埃莉诺",    "伊丽莎白",    "埃拉",    "爱伦",    "艾莉",    "艾米瑞达",    "艾米丽",    "艾玛",    "伊妮德",    "埃尔莎",    "埃莉卡",    "爱斯特尔",    "爱丝特",    "尤杜拉",    "伊娃",    "伊芙",    "伊夫林",    "芬妮",    "费怡",    "菲奥纳",    "福罗拉",    "弗罗伦丝",    "弗郎西丝",    "弗雷德里卡",    "弗里达",    "上海英语外教",    "吉娜",    "吉莉安",    "格拉蒂丝",    "格罗瑞娅",    "格瑞丝",    "格瑞丝",    "格瑞塔",    "格温多琳",    "汉娜",    "海莉",    "赫柏",    "海伦娜",    "海伦",    "汉纳",    "海蒂",    "希拉里",    "英格丽德",    "伊莎贝拉",    "爱沙拉",    "艾琳",    "艾丽丝",    "艾维",    "杰奎琳",    "小玉",    "詹米",    "简",    "珍妮特",    "贾斯敏",    "姬恩",    "珍娜",    "詹妮弗",    "詹妮",    "杰西卡",    "杰西",    "姬尔",    "琼",    "乔安娜",    "乔斯林",    "乔莉埃特",    "约瑟芬",    "乔茜",    "乔伊",    "乔伊斯",    "朱迪丝",    "朱蒂",    "朱莉娅",    "朱莉安娜",    "朱莉",    "朱恩",    "凯琳",    "卡瑞达",    "凯瑟琳",    "凯特",    "凯西",    "卡蒂",    "卡特里娜",    "凯",    "凯拉",    "凯莉",    "凯尔西",    "特里娜",    "基蒂",    "莱瑞拉",    "蕾西",    "劳拉",    "罗兰",    "莉娜",    "莉迪娅",    "莉莲",    "莉莉",    "琳达",    "琳赛",    "丽莎",    "莉兹",    "洛拉",    "罗琳",    "路易莎",    "路易丝",    "露西娅",    "露茜",    "露西妮",    "露露",    "莉迪娅",    "林恩",    "梅布尔",    "马德琳",    "玛姬",    "玛米",    "曼达",    "曼迪",    "玛格丽特",    "玛丽亚",    "玛丽莲",    "玛莎",    "梅维丝",    "玛丽",    "玛蒂尔达",    "莫琳",    "梅维丝",    "玛克辛",    "梅",    "梅米",    "梅甘",    "梅琳达",    "梅利莎",    "美洛蒂",    "默西迪丝",    "梅瑞狄斯",    "米娅",    "米歇尔",    "米莉",    "米兰达",    "米里亚姆",    "米娅",    "茉莉",    "莫尼卡",    "摩根",    "南茜",    "娜塔莉",    "娜塔莎",    "妮可",    "尼基塔",    "尼娜",    "诺拉",    "诺玛",    "尼迪亚",    "奥克塔维亚",    "奥琳娜",    "奥利维亚",    "奥菲莉娅",    "奥帕",    "帕梅拉",    "帕特丽夏",    "芭迪",    "保拉",    "波琳",    "珀尔",    "帕姬",    "菲洛米娜",    "菲比",    "菲丽丝",    "波莉",    "普里西拉",    "昆蒂娜",    "雷切尔",    "丽贝卡",    "瑞加娜",    "丽塔",    "罗丝",    "洛克萨妮",    "露丝",    "萨布丽娜",    "萨莉",    "桑德拉",    "萨曼莎",    "萨米",    "桑德拉",    "桑迪",    "莎拉",    "萨瓦纳",    "斯佳丽",    "塞尔玛",    "塞琳娜",    "塞丽娜",    "莎伦",    "希拉",    "雪莉",    "雪丽",    "雪莉",    "斯莱瑞",    "西尔维亚",    "索尼亚",    "索菲娅",    "丝塔茜",    "丝特拉",    "斯蒂芬妮",    "苏",    "萨妮",    "苏珊",    "塔玛拉",    "苔米",    "谭雅坦尼娅",    "塔莎",    "特莉萨",    "苔丝",    "蒂凡妮",    "蒂娜",    "棠雅",    "特蕾西",    "厄休拉",    "温妮莎",    "维纳斯",    "维拉",    "维姬",    "维多利亚",    "维尔莉特",    "维吉妮亚",    "维达",    "薇薇安"].map(v => `${v}路`)

let busId = 1;
function generateBus(i: number) {
    function pad(num: number | string, n: number) {
        let len = num.toString().length;
        while (len < n) {
            num = "0" + num;
            len++;
        }
        return num;
    }
    return `(${busId++}, "京AX${pad(i, 4)}", "${busTypes[_.random(busTypes.length - 1)]}")`
}
let initSqlStr = fs.readFileSync('./db.sql').toString()

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
        generateData(200, 20);
    });
});

// * 利用路网数据重构后的信息的json来向数据库中插入数据
function generateData(pathNum: number, busNumPerPath: number) {
    // 获取信息
    let paths: string[][] = _(JSON.parse(fs.readFileSync('./BeiJingLinks.json').toString()))
        //.sort((a,b)=>b.length - a.length) // 排序以选择最长线路
        .shuffle()
        .slice(0, pathNum)
        .value();
    // paths中存在一些null，清理掉
    for (let i = 0; i < paths.length; i++) {
        let tmp = _.compact(paths[i])
        let anotherRes: string[] = [tmp[0]];
        for (let i = 1; i < tmp.length; i++) {
            if (tmp[i] === _.last(anotherRes)) continue;
            anotherRes.push(tmp[i])
        }
        paths[i] = anotherRes;
    }

    // 初始化地点信息
    let insertPlaceSql = `
    insert into place(adcode, place_name, lat, lont)
    values (110101, "北京市-北京市-东城区", 116.397128, 39.916527),
    (110102, "北京市-北京市-西城区", 116.372514, 39.918124);`


    // 初始化节点信息
    // paths为原始数据的二维数组  
    let poses = _(paths).flatMap().uniq().value(); // 将线路信息展平，唯一化
    let ID = 1; // 作为ID的标识，SQL中自增的ID是从1开始的
    let pos2nodeId: Record<string, number> = {} // 位置（使用半角逗号分隔的经纬度字符串）与节点ID对应的表
    let stationSet = new Set<string>(_(poses).shuffle().slice(0, Math.floor(poses.length / 10)).value());
    let insertNodeSql =  // 插入节点数据的SQL
        `
    insert into node(node_id, lat, lont, isStation, adcode)
    values 
    ${poses.map(pos => {
            pos2nodeId[pos] = ID;
            return `(${ID++}, ${pos}, ${stationSet.has(pos) ? 1 : 0}, 110101)` // 此时的ID为i
        }).join(',\n')};
    `;


    // 初始化路径信息
    let coord2nodepathId: Record<string, number> = {} // 节点对对应nodepathId的表
    let pairs = _.flatMap(paths.map(path => _.zip(_.dropRight(path), _.drop(path)))).filter(v => v[0] !== v[1]) // 获取所有节点对，这里的dropRight方法获取除尾部的元素，drop方法获取除首部的元素，zip方法将两数组元素一一对应组成新数组，将其展平则为所有节点对
    ID = 1;
    let insertNodepathSql = `
    insert into nodepath
    (nodepath_id, node1_id,node2_id,speed_limit,adcode,street_name)
    values
    ${_.compact(pairs.map(v => { // compact方法的作用是删除数组中的null，false，undefined
        // 如果已经存在，则不再重新添加ß
        if (coord2nodepathId[`${v[0]}-${v[1]}`])
            return null;
        coord2nodepathId[`${v[0]}-${v[1]}`] = ID;
        return `(
            ${ID++},
            ${pos2nodeId[v[0]]},
            ${pos2nodeId[v[1]]},
            ${_.random(20, 120, false) /* 速度为20到120 */}, 
            110101,
            "${_.sample(streetNames) /* 获取随机名称 */}"
            )`
    })).join(',\n')};
    `
    let insertBusSql = `
        insert into bus(bus_id, bus_number, bus_type)
        values
        ${_.range(1, busNumPerPath * pathNum + 1).map(generateBus).join(',')};
    `
    ID = 1;
    let insertBuslineSql = `
    insert into busline (line_id, line_name,start_node_id, bus_id_list, nodepath_id_list)
    values
    ${paths.map((path, i) => {
        // i 从0到199
        let line_name = `${i + 1}路`
        let nodepaths = []
        let start_node_id = pos2nodeId[path[0]]
        let bus_id_list = _.range(i * busNumPerPath + 1, (i + 1) * busNumPerPath + 1)
        let nodepath_list = _.range(1, path.length).map(i => coord2nodepathId[`${path[i - 1]}-${path[i]}`]);
        return `(${ID++}, "${line_name}", ${start_node_id}, '${JSON.stringify(bus_id_list)}', '${JSON.stringify(nodepath_list)}')`
    }).join(',')};
    `

    let insertLineOfPlaceSql = `
    insert into line_of_place(line_id, adcode)
    values 
    ${_.range(1, pathNum + 1).map(i => `(${i}, 110101)`).join(',')};
    `
    let insertBusOfPlaceSql = `
    insert into bus_of_place (adcode, bus_id) 
    values
    ${_.range(1, busNumPerPath * pathNum + 1).map(i => `(110101, ${i})`)};
    `

    let sql = 'use yukinaDB;' +
        insertPlaceSql +
        insertNodeSql +
        insertNodepathSql +
        insertBusSql +
        insertBuslineSql +
        insertLineOfPlaceSql +
        insertBusOfPlaceSql +
        // 这个密码对应aaaaaaaa
        `
        insert into administrator(admin_id,admin_name,admin_passwd)
        values
        ("yukina", "yukina", "9a5a5f2391b7b16cabc2c759002aad51");
        `
    connection.query(sql, function (err) {
        if (err) throw err;
        console.log("成功插入到数据库中！")
        exit(0);
    })
}


