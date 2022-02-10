package yukina.final_design_deprecated.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class Place implements Serializable {
    private Integer place_id;
    private String place_name;
    private BigDecimal lat;
    private BigDecimal lont;
    private Date start_time;
    private Date end_time;

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public Integer getPlace_id() {
        return place_id;
    }

    public void setPlace_id(Integer place_id) {
        this.place_id = place_id;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
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
