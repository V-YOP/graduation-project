package yukina.final_design_deprecated.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LatLont {
    private BigDecimal lat;
    private BigDecimal lont;

    public LatLont(BigDecimal lat, BigDecimal lont) {
        this.lat = lat;
        this.lont = lont;
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