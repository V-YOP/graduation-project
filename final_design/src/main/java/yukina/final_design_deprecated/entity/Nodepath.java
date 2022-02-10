package yukina.final_design_deprecated.entity;

import java.io.Serializable;
import java.sql.Date;

public class Nodepath implements Serializable {
    private Integer nodepath_id;
    private Integer node1_id;
    private Integer node2_id;
    private Integer speed_limit;
    private Integer direction;
    private String street_name;

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

    public Integer getNodepath_id() {
        return nodepath_id;
    }

    public void setNodepath_id(Integer nodepath_id) {
        this.nodepath_id = nodepath_id;
    }

    public Integer getNode1_id() {
        return node1_id;
    }

    public void setNode1_id(Integer node1_id) {
        this.node1_id = node1_id;
    }

    public Integer getNode2_id() {
        return node2_id;
    }

    public void setNode2_id(Integer node2_id) {
        this.node2_id = node2_id;
    }

    public Integer getSpeed_limit() {
        return speed_limit;
    }

    public void setSpeed_limit(Integer speed_limit) {
        this.speed_limit = speed_limit;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getStreet_name() {
        return street_name;
    }

    public void setStreet_name(String street_name) {
        this.street_name = street_name;
    }
}
