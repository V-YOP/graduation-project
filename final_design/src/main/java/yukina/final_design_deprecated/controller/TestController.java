package yukina.final_design_deprecated.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import yukina.final_design_deprecated.domain.BusOfPlaceMapper;
import yukina.final_design_deprecated.entity.*;

import java.util.*;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value="/test", method=RequestMethod.GET)
public class TestController{

    private PlaceMapper placeMapper;
    @Autowired
    public void setPlaceMapper(PlaceMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    @Autowired
    private BusOfPlaceMapper buslineMapper;

    @Autowired
    private PosMapper posMapper;

    @Autowired
    private BusMapper busMapper;

    @RequestMapping("sayHello")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public List<Bus> sayHello() {
        return busMapper.getBusesByID(1);
    }

    @RequestMapping("takeAJson")
    public Map<String, Object> takeAJson(@RequestParam("name") String name) {
        return new HashMap<String, Object>(){{
            put("hello", new String[]{name, "rua"});
            put("rua", new int[]{1,3,2,5,6,6});
            put("rss", new Object[]{1,3,"rus"});
            put("Happy, lucky","smile, yeah");
        }};
    }

    @RequestMapping("takeAJsonList")
    public List<Place> takeAJsonList() {
        return placeMapper.getAllPlace();
    }

}

