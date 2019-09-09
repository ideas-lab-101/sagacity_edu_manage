/**
 * Copyright (C) 2014 陕西威尔伯乐信息技术有限公司
 * @Package com.wellbole.web.core.realm  
 * @Title: ShiroDbRealm.java  
 * @Description: 基于db实现的shiro realm   
 * @author 李飞 (lifei@wellbole.com)    
 * @date 2014年9月8日  下午10:10:38  
 * @since V1.0.0 
 *
 * Modification History:
 * Date         Author      Version     Description
 * -------------------------------------------------------------
 * 2014年9月8日      李飞                       V1.0.0        新建文件   
 */
package ltd.linestorm.edu.admin.auth;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import ltd.linestorm.edu.admin.model.sys.Function;
import ltd.linestorm.edu.admin.model.UserDao;
import ltd.linestorm.edu.admin.model.user.RoleInfo;
import ltd.linestorm.edu.admin.model.user.UserInfo;
import ltd.linestorm.edu.base.extend.CacheKey;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**  
 * @ClassName: ShiroDbRealm  
 * @Description: 基于db实现的shiro realm 
 * @author 李飞 (lifei@wellbole.com)   
 * @date 2014年9月8日 下午10:10:38
 * @since V1.0.0  
 */
public class ShiroDbRealm extends AuthorizingRealm {
    
    public ShiroDbRealm(){
        /** 设置自定义用户密码token*/
        setAuthenticationTokenClass(UsernamePasswordToken.class);
//        setCachingEnabled(true);
        /** 认证缓存设置 */
//        setAuthenticationCachingEnabled(true);
//        setAuthenticationCacheName(CacheKey.CACHE_SHIRO_AUTHENTICATION);
        /** 授权缓存设置 */
//        setAuthorizationCachingEnabled(true);
//        setAuthorizationCacheName(CacheKey.CACHE_SHIRO_AUTHORIZATION);

    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        super.setCacheManager(cacheManager);
        ShiroCacheUtils.setCacheManager(cacheManager);
    }

    /**
     * 认证回调函数,登录时调用.
     */    
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token){
        UsernamePasswordToken authToken = (UsernamePasswordToken) token;
        String accountName = authToken.getUsername();

        UserInfo user = UserInfo.dao.findByName(accountName);
        if (null == user) {
            throw new UnknownAccountException("用户名不存在");
        }
        if (user.getInt("state") == 0) {
            throw new LockedAccountException("用户被锁定");
        }
        //此处可加入对账号的更多规则

        Record ui = Db.findFirst(SqlKit.sql("user.getUserInfo"), user.get("user_id"));
        ui.set("role_info", RoleInfo.dao.findById(ui.getInt("role_id")));
        UserDao userDao = JSONObject.parseObject(ui.toJson(), UserDao.class);

        return new SimpleAuthenticationInfo(userDao, user.get("password"), getName());
    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //User user = (User) principals.fromRealm(getName()).iterator().next();
        UserDao  userDao = (UserDao) principals.fromRealm(getName()).iterator().next();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //加入权限
        UserDao.Role role = userDao.getRole_info();
        //角色的名称及时角色的值
        info.addRole(role.getRole_name());
        addRoleResource(role, info);
        return info;
    }

    public void addRoleResource(UserDao.Role role, SimpleAuthorizationInfo info){
        List<Function> resources = RoleInfo.dao.getResources(role.getRole_id(), null);
        for(Function resource : resources ){
            //资源代码就是权限值，类似user：list
            info.addStringPermission(resource.getStr("func_code"));
        }
    }

    /**
     * 更新用户授权信息缓存，使用ShiroCacheUtils
     */
    public void clearCachedAuthorizationInfo(String principal) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        clearCachedAuthorizationInfo(principals);
    }

    /**
     * 清除所有用户授权信息缓存，使用ShiroCacheUtils
     */
    public void clearAllCachedAuthorizationInfo() {
        Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
        if (cache != null) {
            for (Object key : cache.keys()) {
                cache.remove(key);
            }
        }
    }
}
