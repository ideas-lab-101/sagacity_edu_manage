package ltd.linestorm.edu.wxapp.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.wxaapp.api.WxaQrcodeApi;
import com.jfinal.wxaapp.api.WxaUserApi;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.FontUtil;
import ltd.linestorm.edu.base.extend.ResultCode;
import ltd.linestorm.edu.service.openapi.Qiniu;
import ltd.linestorm.edu.wxapp.model.WxaUser;
import ltd.linestorm.edu.wxapp.common.WxaBaseController;
import ltd.linestorm.edu.wxapp.common.WxaLoginInterceptor;
import net.sf.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

@ControllerBind(controllerKey = "/wxapp/system", viewPath = "/wxapp")
public class SystemController extends WxaBaseController {

    @Override
    public void index() {

    }

    /**
     * 创建用户并登陆
     */
    @Before(Tx.class)
    @Clear(WxaLoginInterceptor.class)
    public void wxaLogin(){
        boolean r = false;
        String code = getPara("code"); //获取openid
        JSONObject userInfo = JSONObject.fromObject(getPara("userInfo"));
        String openid, unionid, session_key;

        WxaUserApi wxaUserApi = new WxaUserApi();
        ApiResult apiResult = wxaUserApi.getSessionKey(code);
//        System.out.println(apiResult);
        openid = apiResult.getStr("openid");
        session_key = apiResult.getStr("session_key");

        Record user = WxaUser.dao.setUser(openid, userInfo);
        if(user != null){
            r = true;
            //写入缓存
            JSONObject jo = new JSONObject();
            jo.put("AddTime", DateUtils.nowDateTime());
            jo.put("UserInfo", user.toJson());
            CacheKit.put(cacheName, session_key, jo);
        }
        if(r){
            data.put("user", user);
            data.put("session_key", session_key);
        }
        rest.setData(data);
        rest.setCode(r ? ResultCode.SUCCESS: ResultCode.ERROR);
        renderJson(rest);
    }

    /**
     * 获得加密信息(电话)
     */
    @Clear(WxaLoginInterceptor.class)
    public void getWxaInfo(){
        WxaUserApi wxaUserApi = new WxaUserApi();
        ApiResult apiResult = wxaUserApi.getUserInfo(getPara("session_key"), getPara("encryptedData"), getPara("iv"));
//        System.out.println(apiResult);
        if(apiResult != null){
            rest.setData(apiResult);
            rest.setCode(ResultCode.SUCCESS);
        }else{
            rest.setCode(ResultCode.DATA_ERROR);
        }
        renderJson(rest);
    }

    /**
     * 获取微信小程序二维码
     */
    public void getWXSSCode(){
        boolean r = false;
        String scene = "";
        String page = "";
        //生成图片
        try {
            WxaQrcodeApi wxaQrcodeApi = Duang.duang(WxaQrcodeApi.class);
            InputStream inputStream = wxaQrcodeApi.getUnLimit(scene, page, 430);

            String fileName = System.currentTimeMillis()+".png";
            File f2 = new File(PropKit.get("resource.dir")+"qr_code/");
            if (!f2.exists()) {
                f2.mkdirs();
            }
            File file = new File(PropKit.get("resource.dir")+"qr_code/"+fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();

            String message = "";
            String bgImg="", qrCode="";

            qrCode = PropKit.get("resource.url")+"qr_code/"+fileName;
            data.put("qr_code", PropKit.get("resource.url")+"qr_code/"+  mergeImage(bgImg, qrCode, message));
            rest.setData(data);
            r = true;
        }catch (Exception ex){
            ex.printStackTrace();
            r = false;
            rest.setMsg("生成二维码出错！");
        }
        rest.setCode(r? ResultCode.SUCCESS:ResultCode.ERROR);
        renderJson(rest);
    }

    /**
     * 将生成的二维码与活动宣传图片进行组合
     */
    private String mergeImage(String bgImg, String qrCode, String message){

        String coverImg = "cover_"+System.currentTimeMillis()+".png"; //合成的图片地址
        try {
            BufferedImage buffImg = ImageIO.read(new URL(bgImg));
            BufferedImage waterImg = ImageIO.read(new URL(qrCode));
            //创建一个新图，在下方增加白边用于叠加二维码
            int bufferImgWidth = buffImg.getWidth();
            int bufferImgHeight = buffImg.getHeight();
            if(bufferImgWidth > 640){
                bufferImgHeight = (int)bufferImgHeight*640/bufferImgWidth;
                bufferImgWidth = 640;
            }

            BufferedImage bg = new BufferedImage(bufferImgWidth, bufferImgHeight+ 120, BufferedImage.TYPE_INT_RGB);
            // 创建Graphics2D对象，用在底图对象上绘图
            Graphics2D g2d = bg.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, bufferImgWidth, bufferImgHeight+ 120);
            // 在图形和图像中实现混合和透明效果
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));
            //叠加背景图
            g2d.drawImage(buffImg, 0, 0, bufferImgWidth, bufferImgHeight, null);
            //叠加二维码
            g2d.drawImage(waterImg, bufferImgWidth-120, bufferImgHeight+10, 100, 100, null);
            //写文字信息
            g2d.setColor(Color.BLACK);
            Font df = FontUtil.loadFont(PathKit.getWebRootPath() +"/asset/fonts/simsun.ttc", 20);
            g2d.setFont(df);
            g2d.drawString(message, 10, bufferImgHeight + 60);
            g2d.dispose();// 释放图形上下文使用的系统资源
            ImageIO.write(bg, "PNG", new FileOutputStream(PropKit.get("resource.dir")+"qr_code/"+coverImg));

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return coverImg;
    }

    /**
     * 获得上传凭证
     */
    public void GetUploadToken(){
        data.put("token", Qiniu.dao.getUploadToken());
        rest.setCode(ResultCode.SUCCESS);
        rest.setData(data);
        renderJson(rest);
    }
}
