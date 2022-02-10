package yukina.final_design_deprecated.entity;

import java.io.Serializable;
import java.sql.Date;

public class Busline implements Serializable {
    private Integer line_id;
    private String line_name;
    private String data_json;
    private Date start_time;
    private Date end_time;

    public String getLine_name() {
        return line_name;
    }

    public void setLine_name(String line_name) {
        this.line_name = line_name;
    }

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

    public Integer getLine_id() {
        return line_id;
    }

    public void setLine_id(Integer line_id) {
        this.line_id = line_id;
    }

    public String getData_json() {
        return data_json;
    }

    public void setData_json(String data_json) {
        this.data_json = data_json;
    }
}
