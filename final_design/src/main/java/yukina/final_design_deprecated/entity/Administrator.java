package yukina.final_design_deprecated.entity;

import java.io.Serializable;

// entity层关注数据库中的实体，其每个和数据库中一个表对应
// 之后还需编写domain层，其关注前端所直接使用的对象，其应当只使用entity层
// dao层则关注对domain的操作，其实质应当也是一堆mapper


public class Administrator implements Serializable {
    private String admin_id;
    private String admin_passwd;
    private String admin_name;

    public String getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(String admin_id) {
        this.admin_id = admin_id;
    }

    public String getAdmin_passwd() {
        return admin_passwd;
    }

    public void setAdmin_passwd(String admin_passwd) {
        this.admin_passwd = admin_passwd;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }
}
