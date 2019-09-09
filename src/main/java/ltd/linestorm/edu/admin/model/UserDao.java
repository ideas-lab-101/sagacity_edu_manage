package ltd.linestorm.edu.admin.model;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDao {

    private String user_id;
    private String caption;
    private String login_name;
    private String email;
    private String tel;
    private String avatar_url;

    private Role role_info;

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Role{
        private int role_id;
        private String role_name;
        private String role_desc;
    }
}
