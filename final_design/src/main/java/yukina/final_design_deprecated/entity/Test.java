package yukina.final_design_deprecated.entity;

import java.sql.Date;

public class Test{
    private Integer data;
    private Date update_time;
    @Override
    public String toString() {
        return data +":"+ update_time.toString();
    }
    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }
}
