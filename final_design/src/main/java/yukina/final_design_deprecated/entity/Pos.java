package yukina.final_design_deprecated.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

@Data
public class Pos implements Serializable {
    private Integer bus_id;
    private Timestamp update_time;
    private Integer speed;
    private BigDecimal lat;
    private BigDecimal lont;


    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getBus_id() {
        return bus_id;
    }

    public void setBus_id(Integer bus_id) {
        this.bus_id = bus_id;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLont() {
        return lont;
    }

    public void setLont(BigDecimal lont) {
        this.lont = lont;
    }
}
