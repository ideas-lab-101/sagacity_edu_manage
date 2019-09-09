package ltd.linestorm.edu.admin.common;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import ltd.linestorm.edu.admin.model.UserDao;
import ltd.linestorm.edu.base.extend.ResponseCode;
import ltd.linestorm.edu.base.extend.RestResult;
import org.apache.shiro.SecurityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @类名字：BaseController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */

@Before(ShiroInterceptor.class)
public abstract class AdminBaseController extends Controller {
	
	protected int pageSize = 20;
	protected static String ROOTPATH = PathKit.getWebRootPath();
    protected HashMap<String, Object> data = new HashMap<String, Object>(); //用于存放数据
	public RestResult rest = new RestResult();

//	public String cacheName = "WebCache";
	
	public abstract void index();
	
	@Override
	public void render(String view) {
		super.render(view);
	}

	public UserDao getCurrentUser(){
        UserDao userDao = (UserDao)SecurityUtils.getSubject().getPrincipal();

		return  userDao;
	}

	/**
	 * 通用的转换方法，适应layui的data-table定义
	 * @param page
	 * @return
	 */
//	public Map<String, Object> convertPageData(Page page){
//		data.put(ResponseCode.LIST, page.getList());
//		data.put(ResponseCode.TotalRow, page.getTotalRow());
//		data.put(ResponseCode.PageNumber, page.getPageNumber());
//		data.put(ResponseCode.PageSize, page.getPageSize());
//		data.put(ResponseCode.TotalPage, page.getTotalPage());
//		return data;
//	}

}