package yukina.final_design.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import yukina.final_design.dao.OverspeedPos
import yukina.final_design.dao.OverspeedPosMapper
import yukina.final_design.dao.PosMapper
import yukina.final_design.service.AdminService
import yukina.final_design.service.InfoGetterService
import yukina.final_design.util.getDistance
import yukina.final_design.util.parseyyyyMMdd2Date
import java.sql.Timestamp


@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(value= ["/test"], method= [RequestMethod.GET])
class TestController @Autowired constructor(
    val infoGetterService: InfoGetterService,
    val adminService: AdminService,
    val overspeedPosMapper: OverspeedPosMapper,
    val posMapper: PosMapper
){
    @RequestMapping("foo")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    // 最短，最长，平均长度
    fun test() =
        overspeedPosMapper.getOverspeedPosesByAdcode(110101).map {
            mapOf(
                "lat" to it.lat,
                "lont" to it.lont
            )
        }

    @RequestMapping("allOverspeedPos")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun ttt(n:Int) =
        overspeedPosMapper.randomGetOverspeedPosesByAdcode(110101, n)
    @RequestMapping("minus")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun another() =
        overspeedPosMapper.getOverspeedPosesByAdcode(110101).joinToString("|") {
            """${it.bus_id},${it.lat},${it.lont},${it.line_id},${it.nodepath_id},${it.update_time}"""
        }
    @RequestMapping("change")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun t5gtt() =
        adminService.CHANGEPASSWD("aaaaaa")
}

