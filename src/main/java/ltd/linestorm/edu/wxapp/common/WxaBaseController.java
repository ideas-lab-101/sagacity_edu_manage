package ltd.linestorm.edu.wxapp.common;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import ltd.linestorm.edu.base.extend.ResponseCode;
import ltd.linestorm.edu.base.extend.RestResult;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @类名字：AppBaseController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */
@Before(WxaLoginInterceptor.class)
public abstract class WxaBaseController extends Controller {
	
	protected int pageSize = 20;
	public RestResult rest = new RestResult();
    protected HashMap<String, Object> data = new HashMap<String, Object>();
	protected static String ROOTPATH = PathKit.getWebRootPath();
	protected String cacheName = "WXACache";
	
	public abstract void index();
	
	@Override
	public void render(String view) {
		super.render(view);
	}

	public JSONObject getCurrentUser(String token){
		JSONObject jo = CacheKit.get(cacheName, token);
		if(jo!= null){
			return jo.getJSONObject("UserInfo");
		}else{
			return null;
		}
	}

    public HashMap<String, Object> convertPageData(Page page){
        data.put(ResponseCode.LIST, page.getList());
        data.put(ResponseCode.TotalRow, page.getTotalRow());
        data.put(ResponseCode.PageNumber, page.getPageNumber());
        data.put(ResponseCode.PageSize, page.getPageSize());
        data.put(ResponseCode.LastPage, page.isLastPage());
        return data;
    }

}