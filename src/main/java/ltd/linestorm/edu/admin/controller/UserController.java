package ltd.linestorm.edu.admin.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.sagacity.utility.StringTool;
import ltd.linestorm.edu.admin.common.AdminBaseController;
import ltd.linestorm.edu.admin.model.user.RoleInfo;
import ltd.linestorm.edu.base.extend.ResponseCode;
import ltd.linestorm.edu.base.extend.ResultCode;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import java.util.List;

@ControllerBind(controllerKey = "/user",viewPath = "/static/user")
@RequiresAuthentication
public class UserController extends AdminBaseController {

    @Override
    public void index(){

        render("userManage.html");
    }



    public void getUserList(){

        String sql_select = SqlKit.sql("user.getUserList-select");
        String sql_from = SqlKit.sql("user.getUserList-from");
        sql_from += "where 1=1";

        if (StringTool.notNull(getPara("pageIndex")) && !StringTool.isBlank(getPara("pageIndex"))){
            Page<Record> dataList = Db.paginate(getParaToInt("pageIndex", 1),
                    getParaToInt("pageSize", 10), sql_select, sql_from);
//            convertPageData(dataList);
            rest.success().setData(dataList);
        }else {
            data.put(ResponseCode.LIST, Db.find(sql_select + "\n" + sql_from));
            rest.success().setData(data);
        }
//        rest.success().setData(data);
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delUser(){

    }

    @Before(Tx.class)
    public void setUserState(){

    }

    /**
     * 角色管理部分
     */

    public void roleManage(){
        render("roleManage.html");
    }

    public void getRoleList(){
        List<RoleInfo> dataList = RoleInfo.dao.findAll();
        data.put(ResponseCode.LIST, dataList);

        rest.success().setData(data);
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delRole(){

    }

    @Before(Tx.class)
    public void saveRole(){

    }

    @Before(Tx.class)
    public void setRoleState(){

    }
}
