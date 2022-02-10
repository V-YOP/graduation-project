package yukina.final_design.controller

import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import yukina.final_design.dao.AdminMapper
import yukina.final_design.service.AdminService
import java.net.HttpCookie
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

// TODO 需要加一个filter给它
@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(value= ["/admin"], method= [RequestMethod.POST])
class AdminController @Autowired constructor(
    val adminService: AdminService
){
    @RequestMapping("login")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun login(@RequestBody body : Map<String,String>): Map<String, String> {
        val id = body["id"]!!
        val passwd = body["passwd"]!!
        val (res, identifier) = adminService.login(id, passwd) // identifier为加盐后MD5加密生成的值，直接拿它设置cookie
        if (res != null) { // 登陆成功，cookie让前端写
            return mapOf("status" to "success!", "id" to id, "name" to res.admin_name, "token" to identifier)
        }
        return mapOf("status" to "failed!")
    }
    @RequestMapping("changePasswd")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun changePasswd(@RequestBody body : Map<String,String>): Map<String, String> {
        val id = body["id"]!!
        val oldPasswd = body["oldPasswd"]!!
        val newPasswd = body["newPasswd"]!!
        val res = adminService.changePasswd(id, oldPasswd,newPasswd) // identifier为加盐后MD5加密生成的值，直接拿它设置cookie
        return  mapOf(
            "status" to (if (res!=null) "success!" else "failed!"),
            "token" to (res ?: "")
        )
    }
    @RequestMapping("changeName")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun changeName(@RequestBody body : Map<String,String>): Map<String, String> {
        val id = body["id"]!!
        val token = body["token"]!!
        val newName = body["newName"]!!
        val res = adminService.changeName(id, token,newName) // identifier为加盐后MD5加密生成的值，直接拿它设置cookie
        return  mapOf(
            "status" to (if (res!=null) "success!" else "failed!")
        )
    }

    data class Param (
        val id: String,
        val token : String,
        val optionList : List<AdminService.Option>
    )

    @RequestMapping("applyOptions")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun applyOptions(@RequestBody body : Param): Map<String, String> {
        val (id, token ,optionList) = body
        val theToken = adminService.getToken(id)
        if (token != theToken) {
            return mapOf(
                "status" to "error"
            )
        }
        adminService.execute(optionList)
        println("执行成功！")
        return mapOf(
            "status" to "success"
        )
    }
    // 临时编写
    /*
    @RequestMapping("register")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun addAdmin(@RequestParam("id") id : String, @RequestParam("passwd") passwd : String, @RequestParam("name") name : String) {
        adminService.tmpAddAdmin(id, passwd, name)
    }
    */

}