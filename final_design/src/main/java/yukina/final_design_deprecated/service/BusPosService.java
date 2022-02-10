package yukina.final_design_deprecated.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import yukina.final_design_deprecated.dao.BusPosDao;
import yukina.final_design_deprecated.entity.Pos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Component
public class BusPosService {

    private BusPosDao busPosDao;
    private Map<Integer, List<Pos>> busPoses; // bus id -> posList
    BusPosService() {
        busPoses = new ConcurrentHashMap<Integer, List<Pos>>();
    }
    @Autowired
    public void setBusPosDao(BusPosDao busPosDao) {
        this.busPosDao = busPosDao;
    }
    public boolean illegal(JSONObject data) {
        //TODO
        return false;
    }

    public void uploadMessage(JSONObject data) throws JSONException {
        Integer speed = (Integer) data.get("speed");
        Integer bus_id = (Integer) data.get("bus_id");
        Double lat = (Double) data.get("lat");
        Double lont = (Double) data.get("lont");
        busPosDao.uploadPos(bus_id, speed, new BigDecimal(lat.toString()), new BigDecimal(lont.toString()));

    }
    public List<Pos> getPosOfIDsPresent(Integer[] bus_ids) {
        return busPosDao.getPosByIDsPresent(bus_ids);
    }
    //TODO
    public List<Pos> getPosOfIDsLong(Integer[] bus_ids) {
        return busPosDao.getPosByIDsLong(bus_ids);
    }
}
