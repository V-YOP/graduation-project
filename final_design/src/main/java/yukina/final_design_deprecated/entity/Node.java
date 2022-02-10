package yukina.final_design_deprecated.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class Node implements Serializable {
    private Integer node_id;
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


    public Integer getNode_id() {
        return node_id;
    }

    public void setNode_id(Integer node_id) {
        this.node_id = node_id;
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
