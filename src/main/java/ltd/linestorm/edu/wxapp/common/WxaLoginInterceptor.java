package ltd.linestorm.edu.wxapp.common;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import ltd.linestorm.edu.base.extend.ResponseCode;
import ltd.linestorm.edu.base.extend.ResultCode;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @类名字：LoginInterceptor
 * @类描述：web端登录拦截器
 * @author:Mula.Liu
 * @版本信息：
 * @日期：2016-09-18
 * 
 */
public class WxaLoginInterceptor implements Interceptor {

	private String cacheName = "WXACache";
	protected Map<String,Object> responseData = new HashMap<String, Object>();

	/**
	 * 绕过拦截器的方法名(注意:包括命名空间)
	 */
	private String[] throughMethods = { };

	/**
	 * @方法名:intercept
	 * @方法描述： 检测eCache中的用户信息，如没有则转向登陆页
	 * @author: Mula.liu
	 * @return: void
	 * @version: 2013-5-7 上午11:00:57
	 */
	public void intercept(Invocation arg0) {
		// TODO Auto-generated method stub
		// 需要带AuthRedirect
		Controller controller = arg0.getController();
		if (checkUserCache(controller) || throughIntercept(arg0.getActionKey())) {
			arg0.invoke();
		} else {
			responseData.put(ResponseCode.CODE, ResultCode.TOKEN_INVALID);
			responseData.put(ResponseCode.MSG, "token失效，请重新登陆");
			controller.renderJson(responseData);
		}
	}

	/**
	 * 检查ehcache中的用户信息是否存在
	 * @param controller
	 * @return
	 */
	public boolean checkUserCache(Controller controller){

		String token = controller.getPara("token");
		JSONObject jo = CacheKit.get(cacheName, token);
		if (jo != null){
			return true;
		}
		return false;
	}

	/**
	 * @方法名:canVisit
	 * @方法描述：验证绕过拦截器方法列表
	 * @author: Carl.Wu
	 * @return: boolean
	 * @version: 2013-5-7 下午12:26:21
	 */
	public boolean throughIntercept(String actionKey) {
		if (throughMethods != null) {
			for (String name : throughMethods) {
				System.out.println(name);
				if(name.contains("*")){
					if (actionKey.contains(name.replace("*", ""))) {
						return true;
					}
				}else{
					if (actionKey.equals(name)) {
						return true;
					}
				}
			}
			return false;
		} else
			return true;

	}
}
