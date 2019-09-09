package ltd.linestorm.edu.admin.controller;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import ltd.linestorm.edu.admin.auth.SessionDAO;
import ltd.linestorm.edu.admin.auth.ShiroCacheUtils;
import ltd.linestorm.edu.admin.auth.ShiroDbRealm;
import ltd.linestorm.edu.admin.common.AdminBaseController;
import ltd.linestorm.edu.admin.model.UserDao;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import java.util.Collection;

@ControllerBind(controllerKey = "/test",viewPath = "/static")
@RequiresAuthentication
public class TestController extends AdminBaseController {

    @Override
    public void index(){
        render("test.html");
    }

    public void clearAllAuth(){

        RealmSecurityManager sm = (RealmSecurityManager)SecurityUtils.getSecurityManager();
        ShiroDbRealm userRealm = (ShiroDbRealm) sm.getRealms().iterator().next();
        System.out.println("isCachingEnabled:"+userRealm.isCachingEnabled());
        System.out.println("isAuthenticationCachingEnabled:"+userRealm.isAuthenticationCachingEnabled());
        System.out.println("authenticationCacheName:"+userRealm.getAuthenticationCacheName());
        System.out.println("isAuthorizationCachingEnabled:"+userRealm.isAuthorizationCachingEnabled());
        System.out.println("authorizationCacheName:"+userRealm.getAuthorizationCacheName());

//        Long t = SecurityUtils.getSubject().getSession().getTimeout();
//        System.out.println("t:"+t);

        ShiroCacheUtils.clearAuthorizationInfoAll();
        rest.success();
        renderJson(rest);
    }

    public void clearAuth(){
        UserDao userInfo = getCurrentUser();
        ShiroCacheUtils.clearAuthorizationInfo(userInfo.getLogin_name());
        rest.success();
        renderJson(rest);
    }

    public void clearAllSession(){
        ShiroCacheUtils.clearSessionAll();
        rest.success();
        renderJson(rest);

    }

    @RequiresRoles("admin")
    public void testAdminFunc(){
        renderText("这个方法是role_id=2调用！");
    }

    @RequiresRoles("administrator")
    public void testAdministratorFunc(){
        renderText("这个方法是role_id=1调用！");
    }

    public void hashPassword(){

        String password = "admin@edu";
        String result = new Md5Hash(password, null,1).toString();
        renderText(result);
    }

    @RequiresPermissions("01-01")
    public void func1(){
        renderText("funcCode:01-01");
    }

    @RequiresPermissions("02-01")
    public void func2(){
        renderText("funcCode:02-01");
    }


    public void md5Test(){
        String password = "admin@hyw";

        String encryptPass = HashKit.sha256(password);

        System.out.println(encryptPass);
    }

}
