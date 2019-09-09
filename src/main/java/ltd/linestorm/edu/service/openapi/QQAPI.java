package ltd.linestorm.edu.service.openapi;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.HttpKit;
import com.sagacity.utility.PropertiesFactoryHelper;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class QQAPI {

    String baseV1 = PropertiesFactoryHelper.getInstance().getConfig("qq.baseV1");
    String key = PropertiesFactoryHelper.getInstance().getConfig("qq.key");

    public final static QQAPI dao = new QQAPI();

    /**
     * 根据关键字查询地址
     * @param address
     * @return
     */
    public Map<String,String> getPlaceData(String address){
        Map<String, String> map = null;
        try{
            String serviceURL = baseV1 + "?" + "address=" + URLEncoder.encode(address, "UTF-8")+ "&key=" + key;
            String result = HttpKit.get(serviceURL);
            JSONObject jo = JSON.parseObject(result);
            if(jo.getIntValue("status") == 0){
                JSONObject place = jo.getJSONObject("result");
                map = new HashMap<String, String>();
                map.put("name", place.getString("title"));
                map.put("location", place.getJSONObject("location").get("lat")+","+place.getJSONObject("location").get("lng"));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return map;
    }
}
