package ltd.linestorm.edu.admin.controller;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.sagacity.utility.ConvertUtil;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import ltd.linestorm.edu.admin.auth.SessionDAO;
import ltd.linestorm.edu.admin.auth.ShiroCacheUtils;
import ltd.linestorm.edu.admin.common.AdminBaseController;
import ltd.linestorm.edu.admin.model.UserDao;
import ltd.linestorm.edu.admin.model.sys.EnumDetail;
import ltd.linestorm.edu.admin.model.sys.EnumMaster;
import ltd.linestorm.edu.admin.model.sys.Function;
import ltd.linestorm.edu.admin.model.user.RoleInfo;
import ltd.linestorm.edu.base.extend.CacheKey;
import ltd.linestorm.edu.base.extend.Consts;
import ltd.linestorm.edu.base.extend.ResponseCode;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ControllerBind(controllerKey = "/system",viewPath = "/static/system")
@RequiresAuthentication
public class SystemController extends AdminBaseController{

    @Override
    public void index(){
        renderText("system module");
    }

    public void funcManage(){
        render("funcManage.html");
    }

    public void getFuncTree(){
        int treeLevel = 1;

        String sql = "select f.func_id id,f.func_name name,f.order,f.p_id parent_id,true spread\n" +
                "from sys_function f\n" +
                "where f.p_id=?\n" +
                "order by f.order";
        List<Record> ms = Db.find(sql, 0);
        addSubMenu(ms, treeLevel);

        rest.success().setData(ms);
        renderJson(rest);
    }

    private void addSubMenu(List<Record> ms, int tl){
        tl ++;
        String sql = "select f.func_id id,f.func_name name,f.order,f.p_id parent_id,true spread\n" +
                "from sys_function f\n" +
                "where f.p_id=?\n" +
                "order by f.order";
        for(Record m : ms){ //循环找到下级
            List<Record> subs = Db.find(sql, m.getInt("id"));
            m.set("children", subs);
            if(tl > 2){ //控制展开的层级
                m.set("spread", false);
            }
            if(subs.size()>0){
                addSubMenu(subs, tl);
            }
        }
    }

    public void getFuncInfo(){
        int funcId = getParaToInt("id");

        rest.success().setData(Function.dao.findById(funcId));
        renderJson(rest);
    }

    /**
     * 根据id 完成新增和修改
     */
    @Before(Tx.class)
    public void saveFunc(){
        boolean r = false;
        Record form = new Record().setColumns(ConvertUtil.jsonStrToMap(getPara("formData")));
        if(form.get("func_id").equals("0")){ //判断新增或更新动作
            form.remove("func_id");
            r = Db.save("sys_function", "func_id", form);
            int id = form.getInt("func_id"); //获取新增数据的主键
            r = Function.dao.findById(id).set("order", id).update();
        }else {
            r = Db.update("sys_function", "func_id", form);
        }
        if(r){
            rest.success("操作成功！");
        }else{
            rest.error("操作失败！");
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delFunc(){
        boolean r = false;
        int id = getParaToInt("id");
        if(Db.find("select * from sys_role_func where func_id=?",id).size()>0){
            rest.error("资源已分配，不允许删除！");
        }else if(Db.find("select * from sys_function where p_id=?",id).size()>0){
            rest.error("包含下级资源，不允许删除！");
        }else {
            r = Function.dao.deleteById(id);
            if(r){
                rest.success("资源删除成功！");
            }else{
                rest.error("资源删除失败！");
            }
        }
        renderJson(rest);
    }

    /**
     * 调整同级顺序
     */
    @Before({Tx.class})
    public void setFuncOrder(){
        boolean r = false;
        int id = getParaToInt("id");
        String type = getPara("type");

        Function source, target;
        int sourceOrder, targetOrder;
        source = Function.dao.findById(id);
        sourceOrder = source.getInt("order");

        if(type.equals(Consts.ORDER_UP)){
            String sqlUp = "select dp.* \n" +
                    "from sys_function dp\n" +
                    "left join sys_function dp1 on dp1.p_id=dp.p_id\n" +
                    "where dp1.func_id=? and dp1.order > dp.order order by dp.order DESC limit 1";
            target = Function.dao.findFirst(sqlUp, id);
            if(target != null){
                targetOrder = target.getInt("order");
                r = target.set("order", sourceOrder).update();
                r = source.set("order", targetOrder).update();
            }
        }else if(type.equals(Consts.ORDER_DOWN)){
            String sqlDown = "select dp.* \n" +
                    "from sys_function dp\n" +
                    "left join sys_function dp1 on dp1.p_id=dp.p_id\n" +
                    "where dp1.func_id=? and dp1.order < dp.order order by dp.order ASC limit 1";
            target = Function.dao.findFirst(sqlDown, id);
            if(target != null){
                targetOrder = target.getInt("order");
                r = target.set("order", sourceOrder).update();
                r = source.set("order", targetOrder).update();
            }
        }

        if(r){
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

    /**
     * 角色授权
     */
    public void roleFunc(){
        setAttr("roleInfo", RoleInfo.dao.findById(getPara("roleId")));
        render("roleFunc.html");
    }

    public void getRoleFunc(){
        int roleId = getParaToInt("roleId");
        String sql = "select f.func_id,f.p_id,f.func_code,f.func_name,IFNULL(rf.mapping_id,0) mapping_id\n" +
                "from sys_function f\n" +
                "left join sys_role_func rf on rf.func_id=f.func_id and rf.role_id=?\n" +
                "where f.state=1\n" +
                "order by f.order";

        data.put(ResponseCode.LIST, Db.find(sql, roleId));
        rest.success().setData(data);
        renderJson(rest);
    }

    @Before(Tx.class)
    public void setRoleFunc(){
        boolean r = false;

        int roleId = getParaToInt("roleId");
        int funcId = getParaToInt("funcId");

        if(getParaToBoolean("state")){ //添加权限
            r = Db.save("sys_role_func", "mapping_id", new Record().set("role_id", roleId).set("func_id", funcId));
        }else{ //删除权限
            r = Db.update("delete from sys_role_func where role_id=? and func_id=?", roleId, funcId)>0? true:false;
        }
        if(r){
            //清除权限缓存
            ShiroCacheUtils.clearAuthorizationInfoAll();
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

    /**
     *  在线用户管理
     *
     */


    public void onlineManage(){
        render("onlineManage.html");
    }

    /**
     * 获取session有效用户
     */
    public void getActiveSessionList(){
        List<Record> onlines = new ArrayList();

        Collection<Session> sessions =  SessionDAO.me.getActiveSessions();
        for (Session session : sessions){
            Record record = new Record().set("id", session.getId());
            record.set("lastAccess",session.getLastAccessTime());
            PrincipalCollection principals = (SimplePrincipalCollection)session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
            UserDao u_info = (UserDao)principals.getPrimaryPrincipal();
            record.set("loginName", u_info.getLogin_name());
            record.set("roleName", u_info.getRole_info().getRole_name());
            onlines.add(record);
        }

        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            int pageIndex = getParaToInt("pageIndex");
            data.put(ResponseCode.LIST, onlines.subList((pageIndex-1) * pageSize,
                    (pageIndex * pageSize > onlines.size() ? onlines.size() : pageIndex * pageSize)));
            data.put(ResponseCode.TotalPage, Math.ceil((double)onlines.size() / pageSize));
            data.put(ResponseCode.PageIndex, getParaToInt("pageIndex"));
            data.put(ResponseCode.TotalRow, onlines.size());
        }else{
            data.put(ResponseCode.LIST, onlines);
        }
        rest.success().setData(data);
        renderJson(rest);
    }

    public void kickOffUser(){
        String sessionId = getPara("sessionId");

        Session session = SessionDAO.me.getActiveSession(sessionId);
        if(session != null){
            session.setTimeout(0l);
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }


    /**
     * 码表管理
     *
     */
    public void enumManage(){
        render("enumManage.html");
    }

    public void getEnumMaster(){
        String sqlSelect = "select em.*\n";
        String sqlFrom = "from sys_enum_master em\n" +
                "where 1=1\n" +
                "order by em.add_time DESC";
        if (StringTool.notNull(getPara("page")) && !StringTool.isBlank(getPara("page"))){
            Page<Record> dataList = Db.paginate(getParaToInt("page", 1),
                    getParaToInt("pageSize", 20), sqlSelect, sqlFrom);
            rest.success().setData(dataList);
        }else {
            data.put(ResponseCode.LIST, Db.find(sqlSelect + "\n" + sqlFrom));
            rest.success().setData(data);
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void addEnumMaster(){
        boolean r = false;

        EnumMaster em = new EnumMaster();
        r = em.set("master_name", getPara("name")).set("master_code", StringTool.generateMixString(6))
                .set("add_time", DateUtils.nowDateTime()).set("state", 1).save();
        if(r){
            rest.success("码表增加成功！").setData(em);
        }else{
            rest.error("码表增加失败！");
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void modifyEnumMaster(){
        boolean r = false;
        int master_id = getParaToInt("master_id");

        EnumMaster em = EnumMaster.dao.findById(master_id).set("master_name", getPara("name"));
        r = em.update();
        if(r){
            rest.success("码表修改成功！").setData(em);
        }else{
            rest.error("码表修改失败！");
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delEnumMaster(){
        boolean r = false;
        int master_id = getParaToInt("master_id");

        if( EnumMaster.dao.checkData(master_id)){
            rest.error("不允许删除！");
        }else{
            r = EnumMaster.dao.deleteById(master_id);
            if(r){
                rest.success("码表删除成功！");
            }else{
                rest.error("码表删除失败！");
            }
        }
        renderJson(rest);
    }

    public void getEnumDetail(){
        int master_id = getParaToInt("master_id");

        String sqlSelect = "select ed.* \n";
        String sqlFrom = "from sys_enum_detail ed\n" +
                "where ed.master_id=?\n" +
                "order by ed.detail_code";
        if (StringTool.notNull(getPara("page")) && !StringTool.isBlank(getPara("page"))){
            Page<Record> dataList = Db.paginateByCache(CacheKey.CACHE_SYSETEM, "enum_"+ master_id, getParaToInt("page", 1),
                    getParaToInt("pageSize", 20), sqlSelect, sqlFrom, master_id);
            rest.success().setData(dataList);
        }else {
            data.put(ResponseCode.LIST, Db.findByCache(CacheKey.CACHE_SYSETEM, "enum_"+ master_id,sqlSelect + "\n" + sqlFrom, master_id));
            rest.success().setData(data);
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void setEnumDetailState(){
        boolean r = false;
        int detail_id = getParaToInt("detail_id");
        int state = getParaToBoolean("state")? 1:0;

        r = EnumDetail.dao.findById(detail_id).set("state", state).update();
        if(r){
            CacheKit.remove(CacheKey.CACHE_SYSETEM, "enum_"+ getParaToInt("master_id"));
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void addEnumDetail(){
        boolean r = false;
        int master_id = getParaToInt("master_id");

        r = new EnumDetail().set("master_id", master_id).set("detail_name", getPara("name"))
                .set("add_time", DateUtils.nowDateTime()).set("state", 1).save();
        if(r){
            CacheKit.remove(CacheKey.CACHE_SYSETEM, "enum_"+ master_id);
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void modifyEnumDetail(){
        boolean r = false;

        Record form = new Record().setColumns(ConvertUtil.jsonStrToMap(getPara("formData")));
        r = Db.update("sys_enum_detail", "detail_id", form);
        if(r){
            CacheKit.remove(CacheKey.CACHE_SYSETEM, "enum_"+ form.getInt("master_id"));
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delEnumDetail(){
        boolean r = false;
        int detail_id = getParaToInt("detail_id");

        r = EnumDetail.dao.deleteById(detail_id);
        if(r){
            CacheKit.remove(CacheKey.CACHE_SYSETEM, "enum_"+ getParaToInt("master_id"));
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }
}
