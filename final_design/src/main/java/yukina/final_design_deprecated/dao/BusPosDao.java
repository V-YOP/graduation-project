package yukina.final_design_deprecated.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yukina.final_design_deprecated.entity.Pos;
import yukina.final_design_deprecated.entity.PosMapper;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BusPosDao {
    private PosMapper posMapper;
    @Autowired
    public void setPosMapper(PosMapper posMapper) {
        this.posMapper = posMapper;
    }

    public void uploadPos(Integer bus_id, Integer speed, BigDecimal lat, BigDecimal lont) {
        try {
            posMapper.uploadPos(bus_id, speed, lat, lont);
        }
        catch (Exception e) {}
    }
    public List<Pos> getPosByIDsPresent(Integer[] bus_ids) {
        return posMapper.getPosesByIDsPresent(bus_ids);
    }
    public List<Pos> getPosByIDsLong(Integer[] bus_ids) {
        return posMapper.getPosesByIDsLong(bus_ids);
    }
}
