package ltd.linestorm.edu.wxapp.controller;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.weixin.sdk.jfinal.MsgControllerAdapter;
import com.jfinal.weixin.sdk.msg.in.InImageMsg;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.wxaapp.api.WxaMessageApi;

@ControllerBind(controllerKey = "/wxa_msg")
public class WxappMsgController extends MsgControllerAdapter {


    @Override
    protected void processInFollowEvent(InFollowEvent inFollowEvent) {

    }

    @Override
    protected void processInTextMsg(InTextMsg inTextMsg) {
        super.renderDefault();
        WxaMessageApi wxaMessageApi = new WxaMessageApi();
        wxaMessageApi.sendText(inTextMsg.getFromUserName(), "收到消息："+inTextMsg.getContent());
    }

    @Override
    protected void processInImageMsg(InImageMsg inImageMsg) {
        super.renderDefault();
        WxaMessageApi wxaMessageApi = new WxaMessageApi();
        wxaMessageApi.sendImage(inImageMsg.getFromUserName(), inImageMsg.getMediaId());
    }

    @Override
    protected void processInMenuEvent(InMenuEvent inMenuEvent) {

    }
}
