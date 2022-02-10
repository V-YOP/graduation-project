import { Graph, GraphEdge, GraphVertex } from "ss-graph"

export function getPath(start_node : number, end_node : number, nodepaths : [number,number][]) : number[][]{
    const nodeSet = new Set<number>()
    nodeSet.add(start_node)
    nodeSet.add(end_node)
    nodepaths.forEach(([a,b])=>{
        nodeSet.add(a)
        nodeSet.add(b)
    })
    const graph = new Graph(true) 
    const id2Vertex : Record<number, GraphVertex> = {}
    nodeSet.forEach(nodeId=>{
        id2Vertex[nodeId] = new GraphVertex(nodeId)
    })
    nodepaths.forEach((([a,b])=>{
        graph.addEdge(new GraphEdge(id2Vertex[a], id2Vertex[b]))
    }))
    const iterator = graph.findAllPath(id2Vertex[start_node], id2Vertex[end_node])
    const res : number[][] = []
    while (true) {
        const innerRes = iterator.next()
        if (innerRes.done)
            break;
        res.push(innerRes.value.map(v=>{
            return v.value
        }))
    }
    return res
}

console.log(getPath(0, 0, [[0,1],[1,2],[1,0]]))