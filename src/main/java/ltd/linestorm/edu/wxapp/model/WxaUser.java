package ltd.linestorm.edu.wxapp.model;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import ltd.linestorm.edu.admin.model.user.UserInfo;
import ltd.linestorm.edu.admin.model.user.UserProfile;
import ltd.linestorm.edu.base.extend.Consts;
import ltd.linestorm.edu.base.extend.RoleType;
import net.sf.json.JSONObject;

import java.util.UUID;

@TableBind(tableName="wxa_user", pkName="id")
public class WxaUser extends Model<WxaUser> {

    public final static WxaUser dao = new WxaUser();

    @Before(Tx.class)
    public Record setUser(String openid, JSONObject userInfo){
        WxaUser wu = WxaUser.dao.findFirst("select user.* from wxa_user user where user.open_id=?", openid);
        if(wu == null){ //创建新用户
            //生成到系统用户表
            UserInfo ui = new UserInfo().set("user_id", UUID.randomUUID().toString()).set("role_id", RoleType.USER)
                    .set("caption", userInfo.get("nickName")).set("login_name", "").set("password", StringTool.generateMixString(6))
                    .set("add_time", DateUtils.nowDateTime()).set("creator_id", Consts.DEFAULT_ADMINID)
                    .set("state", 0);
            ui.save(); //此时创建的用户未启用，绑定手机号后启用
            wu = new WxaUser().set("user_id", ui.get("user_id")).set("open_id", openid)
                    .set("nick_name", userInfo.get("nickName")).set("gender", userInfo.get("gender"))
                    .set("avatar_url", userInfo.get("avatarUrl")).set("country", userInfo.get("country"))
                    .set("city", userInfo.get("city")).set("created_at", DateUtils.nowDateTime()).set("state", 1);
            wu.save();
            new UserProfile().set("user_id", ui.get("user_id")).set("avatar_url", userInfo.get("avatarUrl"))
                    .set("tel", "").set("email", "").set("point", 0).save();
            //设置表
        }else{ //更新用户信息
            wu.set("nick_name", userInfo.get("nickName")).set("gender", userInfo.get("gender")).set("avatar_url", userInfo.get("avatarUrl"))
                    .set("country", userInfo.get("country")).set("city", userInfo.get("city"))
                    .set("updated_at", DateUtils.nowDateTime());
            wu.update();
        }
        String sql = SqlKit.sql("user.getUserInfo") + " where wa.open_id=?";
        return Db.findFirst(sql, openid);
    }
}
