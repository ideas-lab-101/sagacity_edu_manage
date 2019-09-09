package ltd.linestorm.edu.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.jfinal.MsgControllerAdapter;
import com.jfinal.weixin.sdk.msg.in.*;
import com.jfinal.weixin.sdk.msg.in.event.*;
import com.jfinal.weixin.sdk.msg.in.speech_recognition.InSpeechRecognitionResults;
import com.jfinal.weixin.sdk.msg.out.*;
import com.sagacity.utility.StringTool;
import ltd.linestorm.edu.base.extend.WXMsgType;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**

 * 将此 DemoController 在YourJFinalConfig 中注册路由，

 * 并设置好weixin开发者中心的 URL 与 token ，使 URL 指向该

 * DemoController 继承自父类 WeixinController 的 index

 * 方法即可直接运行看效果，在此基础之上修改相关的方法即可进行实际项目开发

 */
@ControllerBind(controllerKey = "/wx_msg")
public class WxMsgController extends MsgControllerAdapter {

    static Logger logger = Logger.getLogger(WxMsgController.class);
    //private static final String helpStr = "\t发送 help 可获得帮助，发送\"视频\" 可获取视频教程，发送 \"美女\" 可看美女，发送 music 可听音乐 ，发送新闻可看JFinal新版本消息。公众号功能持续完善中";

	private void feedbackMsg(InMsg inMsg, String msgType, String msgContent, String value1){
		switch (msgType) {
			case WXMsgType.Text : {
				OutTextMsg outMsg = new OutTextMsg(inMsg);
				outMsg.setContent(msgContent);
				render(outMsg);
				break;
			}
			case WXMsgType.RichText : {
				OutNewsMsg outMsg = new OutNewsMsg(inMsg);
				try {
					JSONObject jo = JSON.parseObject(msgContent);
					News news = new News();
					news.setTitle(jo.getString("title"));
					news.setDescription(jo.getString("description"));
					news.setPicUrl(jo.getString("picUrl"));
					news.setUrl(jo.getString("url"));
					outMsg.addNews(news);
					render(outMsg);
				}catch (Exception ex){
					renderOutTextMsg("图文格式解析错误");
				}
				break;
			}
			case WXMsgType.News : {
				OutNewsMsg outMsg = new OutNewsMsg(inMsg);
				try {
					JSONArray ja = JSON.parseArray(msgContent);
					for(int i=0; i<ja.size(); i++){
						JSONObject jo = ja.getJSONObject(i);
						News news = new News();
						news.setTitle(jo.getString("title"));
						news.setDescription(jo.getString("description"));
						news.setPicUrl(jo.getString("picUrl"));
						news.setUrl(jo.getString("url"));
						outMsg.addNews(news);
					}
					render(outMsg);
				}catch (Exception ex){
					renderOutTextMsg("新闻格式解析错误");
				}
				break;
			}
			case WXMsgType.Music : {
				OutMusicMsg outMsg = new OutMusicMsg(inMsg);
				try {
					JSONObject jo = JSON.parseObject(msgContent);
					outMsg.setTitle(jo.getString("title"));
					outMsg.setDescription(jo.getString("description"));
					outMsg.setMusicUrl(jo.getString("musicUrl"));
					outMsg.setHqMusicUrl(jo.getString("hqMusicUrl"));
					outMsg.setFuncFlag(jo.getBoolean("funcFlag"));
					render(outMsg);
				}catch (Exception ex){
					renderOutTextMsg("音乐格式解析错误");
				}
				break;
			}
            case WXMsgType.Image : {
                OutImageMsg outMsg = new OutImageMsg(inMsg);
                try {
                    JSONObject jo = JSON.parseObject(msgContent);
                    outMsg.setMediaId(jo.getString("mediaId"));
                    render(outMsg);
                }catch (Exception ex){
                    renderOutTextMsg("图片格式解析错误");
                }
                break;
            }
			case WXMsgType.Video : {
				OutVideoMsg outMsg = new OutVideoMsg(inMsg);
				try {
					JSONObject jo = JSON.parseObject(msgContent);
					outMsg.setTitle(jo.getString("title"));
					outMsg.setDescription(jo.getString("description"));
					outMsg.setMediaId(jo.getString("mediaId"));
					render(outMsg);
				}catch (Exception ex){
					renderOutTextMsg("视频格式解析错误");
				}
				break;
			}
			case WXMsgType.Voice : {
				OutVoiceMsg outMsg = new OutVoiceMsg(inMsg);
				try {
					JSONObject jo = JSON.parseObject(msgContent);
					outMsg.setMediaId(jo.getString("mediaId"));
					render(outMsg);
				}catch (Exception ex){
					renderOutTextMsg("音频格式解析错误");
				}
				break;
			}
			case WXMsgType.Function : {
				OutNewsMsg outMsg = new OutNewsMsg(inMsg);
				try {
					JSONObject jo = JSON.parseObject(msgContent);
					String className = jo.getString("className");
					String methodName = jo.getString("methodName");
					String defaultValue = jo.getString("defaultValue");
					Class clz = Class.forName(className);
					Object obj = clz.newInstance();
					//获取方法
					Method m = obj.getClass().getDeclaredMethod(methodName, String.class);
					//调用方法
                    String params = StringTool.notNull(value1) && StringTool.notBlank(value1) ? value1 : defaultValue;
                    List<Map<String,String>> rList = (List<Map<String,String>>) m.invoke(obj, params);
                    for(int i=0; i<rList.size(); i++){
						Map<String,String> map = rList.get(i);
						News news = new News();
						news.setTitle(map.get("title"));
						news.setDescription(map.get("description"));
						news.setPicUrl(map.get("picUrl"));
						news.setUrl(map.get("url"));
						outMsg.addNews(news);
					}
					render(outMsg);
				}catch (Exception ex){
					ex.printStackTrace();
					renderOutTextMsg("函数格式解析错误");
				}
				break;
			}
			default:
				renderOutTextMsg("消息类型未定义");
		}
	}
	/**
	 * 实现父类抽象方法，处理文本消息
	 * 本例子中根据消息中的不同文本内容分别做出了不同的响应，同时也是为了测试 jfinal weixin sdk的基本功能：
	 *     本方法仅测试了 OutTextMsg、OutNewsMsg、OutMusicMsg 三种类型的OutMsg，
	 *     其它类型的消息会在随后的方法中进行测试
	 */
	protected void processInTextMsg(InTextMsg inTextMsg) {

		String msgContent = inTextMsg.getContent().trim();
		String key = "";
		String value1 = "";
		if(msgContent.contains(" ")){
			key = msgContent.split(" ")[0];
			value1 = msgContent.split(" ")[1];
		}else{
			key = msgContent;
		}

		Record rm = Db.findFirst("select * from wx_msg where state=1 and msg_key=?"
				, key);
		if(rm != null){
			feedbackMsg(inTextMsg, rm.get("msg_type").toString(), rm.get("msg_content").toString(), value1);
		}else {
			renderOutTextMsg("【小善】你给我说了："+ msgContent);
		}

	}

	/**

	 * 实现父类抽象方法，处理自定义菜单事件

	 */

	protected void processInMenuEvent(InMenuEvent inMenuEvent) {
		String openID = inMenuEvent.getFromUserName();
		String clickKEventKey = inMenuEvent.getEventKey();

		//renderOutTextMsg("用户：" + UserApi.getUserInfo(openID) + "，菜单Key：" + inMenuEvent.getEventKey());

		Record rm = Db.findFirst("select wm.* from wx_msg wm \n" +
						"left join wx_menu m on m.MsgID=wm.MsgID where m.Key=?"
				, clickKEventKey);
		if(rm != null){
			feedbackMsg(inMenuEvent, rm.get("MsgType").toString(), rm.get("MsgContent").toString(), "");
		}else {
			renderOutTextMsg("【小青】你点击了菜单："+ inMenuEvent.getEventKey());
		}

	}

	/**

	 * 实现父类抽象方法，处理图片消息

	 */

	protected void processInImageMsg(InImageMsg inImageMsg) {

		OutImageMsg outMsg = new OutImageMsg(inImageMsg);

		// 将刚发过来的图片再发回去

		outMsg.setMediaId(inImageMsg.getMediaId());

		render(outMsg);

	}



	/**

	 * 实现父类抽象方法，处理语音消息

	 */

	protected void processInVoiceMsg(InVoiceMsg inVoiceMsg) {


		OutVoiceMsg outMsg = new OutVoiceMsg(inVoiceMsg);


		// 将刚发过来的语音再发回去


		outMsg.setMediaId(inVoiceMsg.getMediaId());


		render(outMsg);


	}



	/**

	 * 实现父类抽象方法，处理视频消息

	 */


	protected void processInVideoMsg(InVideoMsg inVideoMsg) {


		/* 腾讯 api 有 bug，无法回复视频消息，暂时回复文本消息代码测试

		OutVideoMsg outMsg = new OutVideoMsg(inVideoMsg);

		outMsg.setTitle("OutVideoMsg 发送");

		outMsg.setDescription("刚刚发来的视频再发回去");

		// 将刚发过来的视频再发回去，经测试证明是腾讯官方的 api 有 bug，待 api bug 却除后再试

		outMsg.setMediaId(inVideoMsg.getMediaId());

		render(outMsg);

		*/


		OutTextMsg outMsg = new OutTextMsg(inVideoMsg);

		outMsg.setContent("\t视频消息已成功接收，该视频的 mediaId 为: " + inVideoMsg.getMediaId());

		render(outMsg);


	}



	@Override

	protected void processInShortVideoMsg(InShortVideoMsg inShortVideoMsg) {

		OutTextMsg outMsg = new OutTextMsg(inShortVideoMsg);

		outMsg.setContent("\t视频消息已成功接收，该视频的 mediaId 为: " + inShortVideoMsg.getMediaId());

		render(outMsg);

	}

	/**

	 * 实现父类抽象方法，处理地址位置消息

	 */

	protected void processInLocationMsg(InLocationMsg inLocationMsg) {

		OutTextMsg outMsg = new OutTextMsg(inLocationMsg);

		outMsg.setContent("已收到地理位置消息:" +

							"\nlocation_X = " + inLocationMsg.getLocation_X() +

							"\nlocation_Y = " + inLocationMsg.getLocation_Y() +

							"\nscale = " + inLocationMsg.getScale() +

							"\nlabel = " + inLocationMsg.getLabel());

		render(outMsg);

	}



	/**

	 * 实现父类抽象方法，处理链接消息

	 * 特别注意：测试时需要发送我的收藏中的曾经收藏过的图文消息，直接发送链接地址会当做文本消息来发送

	 */

	protected void processInLinkMsg(InLinkMsg inLinkMsg) {

		OutNewsMsg outMsg = new OutNewsMsg(inLinkMsg);

		outMsg.addNews("链接消息已成功接收", "链接使用图文消息的方式发回给你，还可以使用文本方式发回。点击图文消息可跳转到链接地址页面，是不是很好玩 :)" , "http://mmbiz.qpic.cn/mmbiz/zz3Q6WSrzq1ibBkhSA1BibMuMxLuHIvUfiaGsK7CC4kIzeh178IYSHbYQ5eg9tVxgEcbegAu22Qhwgl5IhZFWWXUw/0", inLinkMsg.getUrl());

		render(outMsg);

	}


	@Override
	protected void processInCustomEvent(InCustomEvent inCustomEvent) {

		System.out.println("processInCustomEvent() 方法测试成功");

	}


	/**

	 * 实现父类抽象方法，处理关注/取消关注消息

	 */
    @Before(Tx.class)
	protected void processInFollowEvent(InFollowEvent inFollowEvent) {

		String openid = inFollowEvent.getFromUserName();
		OutTextMsg outMsg = new OutTextMsg(inFollowEvent);


        if (InFollowEvent.EVENT_INFOLLOW_SUBSCRIBE.equals(inFollowEvent.getEvent())) { //关注公众号的业务处理
            ApiResult result = UserApi.getUserInfo(openid);
            String nickname = result.getStr("nickname");
            int sexID = result.getInt("sex") == 1 ? 14 : 15;
            String imgURL = result.getStr("headimgurl");

			Record rm = Db.findFirst("select * from wx_msg where MsgID=?"
					, 1);
			if(rm != null) {
                outMsg.setContent("【小善】："+ rm.get("MsgContent").toString());
            }else {
                outMsg.setContent("【小善】：终于等到你" + nickname);
            }
        }else if (InFollowEvent.EVENT_INFOLLOW_UNSUBSCRIBE.equals(inFollowEvent.getEvent())) {//取消关注公众号的业务处理
			//取消关注公众号的业务处理，清除用户信息中的gOpenID绑定
			Db.update("update sys_users set gOpenID=NULL where gOpenID=?", openid);
        }
        render(outMsg);

	}

	/**

	 * 实现父类抽象方法，处理扫描带参数二维码事件

	 */
    @Before(Tx.class)
	protected void processInQrCodeEvent(InQrCodeEvent inQrCodeEvent) {

		String openid = inQrCodeEvent.getFromUserName();
        String terminalCode = inQrCodeEvent.getEventKey();

	}

	// 微信会员卡激活事件


	/**

	 * 实现父类抽象方法，处理上报地理位置事件

	 */

	protected void processInLocationEvent(InLocationEvent inLocationEvent) {

		OutTextMsg outMsg = new OutTextMsg(inLocationEvent);

		outMsg.setContent("processInLocationEvent() 方法测试成功");

		render(outMsg);

	}


	@Override
    // 群发完成事件
	protected void processInMassEvent(InMassEvent inMassEvent) {

		System.out.println("processInMassEvent() 方法测试成功");

	}


	/**

	 * 实现父类抽象方法，处理接收语音识别结果

	 */

	protected void processInSpeechRecognitionResults(InSpeechRecognitionResults inSpeechRecognitionResults) {

		renderOutTextMsg("语音识别结果： " + inSpeechRecognitionResults.getRecognition());

	}





	// 处理接收到的模板消息是否送达成功通知事件


	protected void processInTemplateMsgEvent(InTemplateMsgEvent inTemplateMsgEvent) {

		String status = inTemplateMsgEvent.getStatus();

		renderOutTextMsg("模板消息是否接收成功：" + status);

	}


	@Override
	protected void processInShakearoundUserShakeEvent(InShakearoundUserShakeEvent inShakearoundUserShakeEvent) {

		logger.debug("摇一摇周边设备信息通知事件：" + inShakearoundUserShakeEvent.getFromUserName());

		OutTextMsg outMsg = new OutTextMsg(inShakearoundUserShakeEvent);

		outMsg.setContent("摇一摇周边设备信息通知事件UUID：" + inShakearoundUserShakeEvent.getUuid());

		render(outMsg);

	}


	@Override
	protected void processInVerifySuccessEvent(InVerifySuccessEvent inVerifySuccessEvent) {

		logger.debug("资质认证成功通知事件：" + inVerifySuccessEvent.getFromUserName());

		OutTextMsg outMsg = new OutTextMsg(inVerifySuccessEvent);

		outMsg.setContent("资质认证成功通知事件：" + inVerifySuccessEvent.getExpiredTime());

		render(outMsg);

	}


	@Override
	protected void processInVerifyFailEvent(InVerifyFailEvent inVerifyFailEvent){

		logger.debug("资质认证失败通知事件：" + inVerifyFailEvent.getFromUserName());

		OutTextMsg outMsg = new OutTextMsg(inVerifyFailEvent);

		outMsg.setContent("资质认证失败通知事件：" + inVerifyFailEvent.getFailReason());

		render(outMsg);

	}


}
