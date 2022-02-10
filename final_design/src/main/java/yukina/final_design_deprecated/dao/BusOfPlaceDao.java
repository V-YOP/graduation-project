package yukina.final_design_deprecated.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import yukina.final_design_deprecated.domain.BusAndAllNode;
import yukina.final_design_deprecated.domain.BusOfPlace;
import yukina.final_design_deprecated.domain.BusOfPlaceMapper;
import yukina.final_design_deprecated.domain.LatLont;
import yukina.final_design_deprecated.entity.*;

import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BusOfPlaceDao {
    @Autowired
    private BusOfPlaceMapper busOfPlaceMapper;
    @Autowired
    private NodeMapper nodeMapper;
    @Autowired
    private NodepathMapper nodepathMapper;
    @Autowired
    private PosMapper posMapper;

    public List<BusAndAllNode> getBusAndNodeByPlaceName(String place_name) {

        // 获取数据花费大约1s
        List<BusOfPlace> busOfPlaces = busOfPlaceMapper.getBusOfPlace(place_name);
        List<BusAndAllNode> res = new ArrayList<>();
        busOfPlaces.forEach(busOfPlace -> {
            try {
                int bus_id = busOfPlace.getBus_id();
                int line_id = busOfPlace.getLine_id();
                String line_name = busOfPlace.getLine_name();
                //System.out.println(busOfPlace.getData_json());
                JSONObject jsonObj = new JSONObject(busOfPlace.getData_json());
                JSONArray outerArray = jsonObj.getJSONArray("data");
                int startNodeID = outerArray.getJSONArray(0).getInt(0);
                boolean startNodeIsStation = outerArray.getJSONArray(0).getBoolean(1);
                List<Nodepath> nodepathList = new ArrayList<>();
                Map<Integer, LatLont> nodePosMap = new HashMap<>();
                List<Boolean> isStationList = new ArrayList<>();
                isStationList.add(startNodeIsStation);
                for (int i = 1; i < outerArray.length(); i++) {
                    Integer nodepathID = outerArray.getJSONArray(i).getInt(0);
                    Boolean isStation = outerArray.getJSONArray(i).getBoolean(1);
                    Nodepath nodepath = nodepathMapper.getNodepathByID(nodepathID);
                    Integer node1_id = nodepath.getNode1_id();
                    Integer node2_id = nodepath.getNode2_id();
                    Node node1 = nodeMapper.getNodeByID(node1_id);
                    Node node2 = nodeMapper.getNodeByID(node2_id);
                    LatLont node1_latlont = new LatLont(node1.getLat(),node1.getLont());
                    LatLont node2_latlont = new LatLont(node2.getLat(),node2.getLont());
                    nodePosMap.put(node1_id, node1_latlont);
                    nodePosMap.put(node2_id, node2_latlont);
                    nodepathList.add(nodepath);
                    isStationList.add(isStation);
                }
                res.add(new BusAndAllNode(bus_id, line_id, line_name, startNodeID, nodepathList, nodePosMap, isStationList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return res;
    }
    public List<BusAndAllNode> getBusAndNodeByPlaceNameHistory(String place_name, Date date) {
        List<BusOfPlace> busOfPlaces = busOfPlaceMapper.getBusOfPlaceHistory(place_name, date);
        List<BusAndAllNode> res = new ArrayList<>();
        busOfPlaces.forEach(busOfPlace -> {
            try {
                int bus_id = busOfPlace.getBus_id();
                int line_id = busOfPlace.getLine_id();
                String line_name = busOfPlace.getLine_name();
                //System.out.println(busOfPlace.getData_json());
                JSONObject jsonObj = new JSONObject(busOfPlace.getData_json());
                JSONArray outerArray = jsonObj.getJSONArray("data");
                int startNodeID = outerArray.getJSONArray(0).getInt(0);
                boolean startNodeIsStation = outerArray.getJSONArray(0).getBoolean(1);
                List<Nodepath> nodepathList = new ArrayList<>();
                Map<Integer, LatLont> nodePosMap = new HashMap<>();
                List<Boolean> isStationList = new ArrayList<>();
                isStationList.add(startNodeIsStation);
                for (int i = 1; i < outerArray.length(); i++) {
                    Integer nodepathID = outerArray.getJSONArray(i).getInt(0);
                    Boolean isStation = outerArray.getJSONArray(i).getBoolean(1);
                    Nodepath nodepath = nodepathMapper.getNodepathByIDHistory(nodepathID, date);
                    Integer node1_id = nodepath.getNode1_id();
                    Integer node2_id = nodepath.getNode2_id();
                    Node node1 = nodeMapper.getNodeByIDHistory(node1_id, date);
                    Node node2 = nodeMapper.getNodeByIDHistory(node2_id, date);
                    LatLont node1_latlont = new LatLont(node1.getLat(),node1.getLont());
                    LatLont node2_latlont = new LatLont(node2.getLat(),node2.getLont());
                    nodePosMap.put(node1_id, node1_latlont);
                    nodePosMap.put(node2_id, node2_latlont);
                    nodepathList.add(nodepath);
                    isStationList.add(isStation);
                }
                res.add(new BusAndAllNode(bus_id, line_id, line_name, startNodeID, nodepathList, nodePosMap, isStationList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return res;
    }
    public List<Pos> getOldPosByID(String busIDList) throws JSONException {
        JSONArray arr = new JSONArray(busIDList);
        Integer[] res = new Integer[arr.length()];
        for (int i = 0; i < arr.length(); i++)
            res[i] = arr.getInt(i);
        return posMapper.getPosesByIDsLong(res);
    }
    public List<Pos> getPosesByIDsBetween(String busIdList, Date start_time, Date end_time) throws JSONException {
        JSONArray arr = new JSONArray(busIdList);
        Integer[] res = new Integer[arr.length()];
        for (int i = 0; i < arr.length(); i++)
            res[i] = arr.getInt(i);
        return posMapper.getPosesByIDsBetween(res, start_time, end_time);
    }
}
