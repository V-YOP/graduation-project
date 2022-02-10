/* eslint-disable camelcase */
/**
 * @deprecated
 * 转换这破格式为更易读的玩意。
 * 应当初始化bus_id对line_id的映射，busId2LineId（多对一）
 * 各个node_id对node值的映射，nodeId2Node（一对一）
 * Line对其所包括的node_id有序集合的映射，nodeIdsOfLine
 * nodeid对nodepath的映射，nodeId2Nodepath
 * 标识是否展示line的映射lineDisplay，Record<line_id, boolean>
 *
 * @param {*} ori
 */
export type busAndLine = {
  busId2LineId: Record<number, number>,
  nodeId2Node: Record<number, { lat: number, lont: number, isStation: boolean }>,
  nodeIdsOfLine: Record<number, number[]>,
  nodeId2Nodepath: Record<string, { node1_id: number, node2_id: number, direction: number, speed_limit: number, street_name: string }>,
  lineDisplay: Record<number, boolean>
};
export function BusAndAllNodeAdapter(ori: Array<{
  bus_id: number,
  line_id: number,
  line_name: string,
  startNodeID: number,
  nodepathList: Array<{
    nodepath_id: number,
    node1_id: number,
    node2_id: number,
    speed_limit: number,
    direction: number,
    street_name: string,
    start_time: string,
    end_time: string
  }>,
  nodePosMap: Record<number, { lat: number, lont: number }>,
  isStationList: Array<boolean>
}>): busAndLine {
  if (ori.constructor !== Array) { throw new Error('???') }
  let busId2LineId: Record<number, number> = {}
  let nodeId2Node: Record<number, { lat: number, lont: number, isStation: boolean }> = {}
  let nodeIdsOfLine: Record<number, number[]> = {}
  let nodeId2Nodepath: Record<string, { node1_id: number, node2_id: number, direction: number, speed_limit: number, street_name: string }> = {} // 其实现为两个nodeid通过字符-相连作为key
  let lineDisplay: Record<number, boolean> = {}
  // 初始化busId2LineId，nodeId2Node和nodeId2Nodepath
  ori.forEach((val) => {
    let bus_id = val.bus_id
    let line_id = val.line_id
    busId2LineId[bus_id] = line_id
    val.nodepathList.forEach((nodepath) => {
      let node1_id = nodepath.node1_id
      let node2_id = nodepath.node2_id
      nodeId2Nodepath[node1_id + '-' + node2_id] = {
        'node1_id': node1_id,
        'node2_id': node2_id,
        'direction': nodepath.direction,
        'speed_limit': nodepath.speed_limit,
        'street_name': nodepath.street_name
      }
      if (nodepath.direction === 2) {
        // eslint-disable-next-line camelcase
        nodeId2Nodepath[node2_id + '-' + node1_id] = nodeId2Nodepath[node1_id + '-' + node2_id]
      }

      nodeId2Node[node1_id] = {
        lat: val.nodePosMap[node1_id].lat,
        lont: val.nodePosMap[node1_id].lont,
        isStation: true
      }
      nodeId2Node[node2_id] = {
        lat: val.nodePosMap[node2_id].lat,
        lont: val.nodePosMap[node2_id].lont,
        isStation: true
      }
    })
    // 初始化nodesOfLine，补充nodeId2Node的isStation项
    let nodeIdsOfALine: Array<number> = []
    let nodeids = [val.startNodeID]
    let isStationList = [val.isStationList[0]]
    val.nodepathList.forEach((nodepath, index) => {
      let lastNodeId = nodeids[nodeids.length - 1]
      let node1_id = nodepath.node1_id
      let node2_id = nodepath.node2_id
      let isStation = val.isStationList[index + 1]
      let nextNodeId = null
      if (lastNodeId === node1_id) { nextNodeId = node2_id } else { nextNodeId = node1_id }
      nodeids.push(nextNodeId)
      isStationList.push(isStation)
    })
    nodeids.forEach((node_id, index) => {
      nodeId2Node[node_id].isStation = isStationList[index]
      nodeIdsOfALine.push(node_id)
    })
    nodeIdsOfLine[line_id] = nodeIdsOfALine
    lineDisplay[line_id] = true
  })
  return {
    busId2LineId,
    nodeId2Node,
    nodeIdsOfLine,
    nodeId2Nodepath,
    lineDisplay
  }
}
