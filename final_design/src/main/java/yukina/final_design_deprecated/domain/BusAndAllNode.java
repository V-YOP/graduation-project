package yukina.final_design_deprecated.domain;

import lombok.Data;
import yukina.final_design_deprecated.entity.Nodepath;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

// 前端所需要的实体
@Data
public class BusAndAllNode implements Serializable {

    private Integer bus_id;
    private Integer line_id;
    private String line_name;
    private Integer startNodeID;
    private List<Nodepath> nodepathList;
    private Map<Integer, LatLont> nodePosMap;
    private List<Boolean> isStationList;

    public String getLine_name() {
        return line_name;
    }

    public void setLine_name(String line_name) {
        this.line_name = line_name;
    }

    public BusAndAllNode(Integer bus_id, Integer line_id, String line_name, Integer startNodeID, List<Nodepath> nodepathList, Map<Integer, LatLont> nodePosMap, List<Boolean> isStationList) {
        this.bus_id = bus_id;
        this.line_id = line_id;
        this.line_name = line_name;
        this.startNodeID = startNodeID;
        this.nodepathList = nodepathList;
        this.nodePosMap = nodePosMap;
        this.isStationList = isStationList;
    }

    public Integer getStartNodeID() {
        return startNodeID;
    }

    public void setStartNodeID(Integer startNodeID) {
        this.startNodeID = startNodeID;
    }

    public Map<Integer, LatLont> getNodePosMap() {
        return nodePosMap;
    }

    public void setNodePosMap(Map<Integer, LatLont> nodePosMap) {
        this.nodePosMap = nodePosMap;
    }

    public List<Boolean> getIsStationList() {
        return isStationList;
    }

    public void setIsStationList(List<Boolean> isStationList) {
        this.isStationList = isStationList;
    }

    public Integer getBus_id() {
        return bus_id;
    }

    public void setBus_id(Integer bus_id) {
        this.bus_id = bus_id;
    }



    public List<Nodepath> getNodepathList() {
        return nodepathList;
    }

    public void setNodepathList(List<Nodepath> nodepathList) {
        this.nodepathList = nodepathList;
    }
}
