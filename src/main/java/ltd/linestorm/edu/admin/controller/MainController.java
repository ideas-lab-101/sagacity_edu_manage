package ltd.linestorm.edu.admin.controller;


import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.sagacity.utility.PropertiesFactoryHelper;
import ltd.linestorm.edu.admin.common.LoginValidator;
import ltd.linestorm.edu.admin.common.AdminBaseController;
import ltd.linestorm.edu.admin.model.user.RoleInfo;
import ltd.linestorm.edu.admin.model.UserDao;
import ltd.linestorm.edu.base.extend.Consts;
import ltd.linestorm.edu.admin.model.user.UserInfo;
import ltd.linestorm.edu.base.utils.CaptchaRender;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;

import java.util.List;

/**
 * @类名字：CommonController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-11-14
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */

@ControllerBind(controllerKey = "/",viewPath = "/static")
@RequiresAuthentication
public class MainController extends AdminBaseController {

    @Override
    public void index(){
        UserDao userInfo = getCurrentUser();

        setAttr("userInfo", userInfo);
        setAttr("resourceUrl", PropertiesFactoryHelper.getInstance()
                .getConfig("resource.url"));

        render("index.html");
    }

    public void desktop(){
        //加载模块列表
        UserDao userInfo = getCurrentUser();
        setAttr("moduleList", RoleInfo.dao.getResources(userInfo.getRole_info().getRole_id(), Consts.FUNC_MODULE));
        render("desktop.html");
    }

    @Clear(ShiroInterceptor.class)
    public void captcha() {
        CaptchaRender img = new CaptchaRender(4);
        this.setSessionAttr(CaptchaRender.DEFAULT_CAPTCHA_MD5_CODE_KEY, img.getMd5RandonCode());
        render(img);
    }

    public void logout() {
        if (SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().logout();
        }
        render("/login.html");
    }

    /**
     * 管理用户登陆，下一步加入短信验证
     */
    @Before(LoginValidator.class)
    @Clear(ShiroInterceptor.class)
    public void login() {
        String loginName = getPara("loginName");
        String password = getPara("password");

        UsernamePasswordToken token = new UsernamePasswordToken(loginName, password
                , false, getRequest().getRemoteAddr());
        Subject subject = SecurityUtils.getSubject();

        try {
            if (!subject.isAuthenticated()) {
                subject.login(token);
            }
            rest.success().setMsg("登录成功");
        } catch (UnknownAccountException une) {
            rest.error("用户名不存在");
        } catch (LockedAccountException lae) {
            rest.error("用户被锁定");
        } catch (ExcessiveAttemptsException exe) {
            rest.error("账户密码错误次数过多，账户已被限制登录10分钟");
        } catch (IncorrectCredentialsException ine) {
            rest.error("用户名或密码不正确");
        }  catch (Exception e) {
            e.printStackTrace();
            rest.error("服务异常，请稍后重试");
        }

        renderJson(rest);
    }

    private String AccountScanCacheName = "AccountScanCache";

    @Clear(ShiroInterceptor.class)
    public void scanLogin(){
        boolean r = false;

        //从缓存中获取传入的key,找到对应用户
        if (CacheKit.get(AccountScanCacheName, getPara("key")) != null){
            UserInfo userInfo = UserInfo.dao.findById(CacheKit.get(AccountScanCacheName, getPara("key")));
            if (userInfo != null){
                r = true;
                data.put("user", userInfo);
                rest.success("验证成功，即将登陆！").setData(data);
            }else{
                rest.error("用户信息错误，请联系管理员！");
            }
        }else{
            rest.error("验证失败，请重扫码！");
        }
        renderJson(rest);

    }

    /**
     * 根据Code加载下级菜单
     */
    public void loadMenuTree(){
        String moduleID = getPara("moduleID");
        UserDao userInfo = getCurrentUser();

        String menuStr = "select mf.func_id id,mf.func_name title,mf.func_css icon,mf.func_url href,true spread\n" +
                "from sys_function mf \n" +
                "inner join sys_role_func rf on rf.func_id = mf.func_id\n" +
                "where mf.func_type='menu' and mf.state=1 and mf.p_id =? and rf.role_id=?\n" +
                "order by mf.order";
        List<Record> menuSet = Db.find(menuStr, moduleID, userInfo.getRole_info().getRole_id());
        for (Record menu : menuSet){
            List<Record> subMenu = Db.find(menuStr, menu.get("id"), userInfo.getRole_info().getRole_id());
            if(subMenu != null){
                menu.set("children", subMenu);
            }
        }
        rest.success().setData(menuSet);
        renderJson(rest);
    }

}
