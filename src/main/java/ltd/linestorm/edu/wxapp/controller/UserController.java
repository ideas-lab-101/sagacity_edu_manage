package ltd.linestorm.edu.wxapp.controller;


import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.sagacity.utility.ConvertUtil;
import com.sagacity.utility.DateUtils;
import ltd.linestorm.edu.admin.model.user.UserInfo;
import ltd.linestorm.edu.admin.model.user.UserProfile;
import ltd.linestorm.edu.base.extend.ResultCode;
import ltd.linestorm.edu.wxapp.common.WxaBaseController;
import ltd.linestorm.edu.wxapp.common.WxaLoginInterceptor;
import net.sf.json.JSONObject;

import java.util.Map;

@ControllerBind(controllerKey = "/wxapp/user", viewPath = "/wxapp")
public class UserController extends WxaBaseController {

    @Override
    public void index() {

    }

    @Before(Tx.class)
    @Clear(WxaLoginInterceptor.class)
    public void bindUser(){
        boolean r=false;
        JSONObject userInfo = getCurrentUser(getPara("token"));
        Map<String, Object> formData = ConvertUtil.jsonStrToMap(getPara("formData"));

        UserInfo ui = UserInfo.dao.findById(userInfo.get("user_id"));
        UserProfile up = UserProfile.dao.findFirst("select * from user_profile where user_id=?", userInfo.get("user_id"));
        if(ui != null){
            r = ui.set("caption", formData.get("name")).set("login_name", formData.get("tel"))
                    .set("state", 1).update();
            r = up.set("tel", formData.get("tel")).set("email", formData.get("email"))
                    .set("sign", formData.get("sign")).set("update_time", DateUtils.nowDateTime()).update();
        }
        if(r){
            String sql = SqlKit.sql("user.getUserInfo") + " where u.user_id=?";
            data.put("user", Db.findFirst(sql, userInfo.get("user_Id") ));

            rest.setData(data);
            rest.setMsg("更新成功！");
        }else{
            rest.setMsg("更新失败！");
        }
        rest.setCode(r? ResultCode.SUCCESS: ResultCode.ERROR);
        renderJson(rest);
    }

}
