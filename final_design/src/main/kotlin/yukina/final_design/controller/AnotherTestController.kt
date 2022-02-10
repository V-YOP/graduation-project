package yukina.final_design.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.beans.factory.annotation.Autowired
import yukina.final_design.service.AdminService

@RestController
@RequestMapping(value = ["/anotherTest"], method = [RequestMethod.GET])
class AnotherTestController @Autowired constructor(
    private val adminService: AdminService
) {
    @RequestMapping("/hello")
    fun hello(): String {
        return "HHW"
    }
}