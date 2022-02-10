package yukina.final_design_deprecated.domain;


import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class BusOfPlace implements Serializable {
    private String place_name;
    private Integer bus_id;
    private String data_json;
    private String line_name;
    private Integer line_id;

    public Integer getLine_id() {
        return line_id;
    }

    public void setLine_id(Integer line_id) {
        this.line_id = line_id;
    }

    public String getLine_name() {
        return line_name;
    }

    public void setLine_name(String line_name) {
        this.line_name = line_name;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public Integer getBus_id() {
        return bus_id;
    }

    public void setBus_id(Integer bus_id) {
        this.bus_id = bus_id;
    }

    public String getData_json() {
        return data_json;
    }

    public void setData_json(String data_json) {
        this.data_json = data_json;
    }
}
