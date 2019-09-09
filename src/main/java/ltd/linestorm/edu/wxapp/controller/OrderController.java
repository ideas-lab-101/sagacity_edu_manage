package ltd.linestorm.edu.wxapp.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.kit.IpKit;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.jfinal.weixin.sdk.utils.PaymentException;
import com.jfinal.wxaapp.api.WxaOrder;
import com.jfinal.wxaapp.api.WxaPayApi;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.IPUtil;
import ltd.linestorm.edu.base.extend.OrderState;
import ltd.linestorm.edu.admin.model.order.OrderInfo;
import ltd.linestorm.edu.base.extend.ResultCode;
import ltd.linestorm.edu.wxapp.common.WxaBaseController;
import ltd.linestorm.edu.wxapp.common.WxaLoginInterceptor;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@ControllerBind(controllerKey = "/wxapp/order", viewPath = "/wxapp")
public class OrderController extends WxaBaseController {

    private static String appid = PropKit.get("wxa.appid");
    private static String partner = PropKit.get("wxPay.partner");
    private static String partnerKey = PropKit.get("wxPay.partnerKey");
    private static String certPath = PathKit.getWebRootPath() + PropKit.get("wxPay.certPath");
    private static String certPass = PropKit.get("wxPay.certPass");
    private static String notify_url = PropKit.get("base.url") + "wxapp/order/payNotify";

    @Override
    public void index() {

    }

    /**
     * 生成订单
     */
    @Before(Tx.class)
    public void genOrder(){
        boolean r = false;
        String msg = "";

        JSONObject userInfo = getCurrentUser(getPara("token"));
        double cost = 0.01f;

        int orderState = OrderState.PrePay_Confirm;
        String orderCode = OrderInfo.dao.genOrderCode("PO");
        if(cost > 0.0f){
            OrderInfo oi = new OrderInfo().set("data_type", "PO").set("data_id", 1).set("order_state", orderState)
                    .set("order_code", orderCode).set("order_date", DateUtils.nowDate()).set("order_time", DateUtils.getTimeShort())
                    .set("user_id", userInfo.get("user_id")).set("total_pay", Math.round(cost*100));
            r = oi.save();
            data.put("order", oi);
            rest.setData(data);
        }
        if(r){
            msg = "订单生成成功！";
        }else{
            msg = "订单生成失败！";
        }
        rest.setMsg(msg);
        rest.setCode(r? ResultCode.SUCCESS:ResultCode.ERROR);
        renderJson(rest);
    }

    public void queryOrder(){
        String orderCode = getPara("orderCode");

        OrderInfo oi = OrderInfo.dao.findFirst("select * from pay_order where order_code=?", orderCode);
        if(oi != null){
            data.put("order", oi);
            rest.setData(data);
            rest.setCode(ResultCode.SUCCESS);
        }else{
            rest.setCode(ResultCode.DATA_ERROR);
            rest.setMsg("订单无效……");
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void orderPay(){
        JSONObject userInfo = getCurrentUser(getPara("token"));

        WxaOrder order = new WxaOrder(appid, partner, partnerKey);
        order.setBody(getPara("body","enroll cost"));
        order.setNotifyUrl(notify_url);
        order.setOutTradeNo(getPara("out_trade_no"));
        order.setTotalFee(getPara("total_fee"));
        order.setOpenId(userInfo.getString("open_id"));
        String ip = IpKit.getRealIp(getRequest()); //IPV6会出现问题
        if (StrKit.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        try {
            ip = IPUtil.hexToIP(IPUtil.ipToHex(ip));
        }catch (Exception ex){
            ip = "127.0.0.1";
        }
        order.setSpbillCreateIp(ip);

        //发起支付
        WxaPayApi wxaPayApi = new WxaPayApi();
        try{
            Map<String, String> packageParams = wxaPayApi.unifiedOrder(order);
//            System.out.println(packageParams);
            String prepay_id = packageParams.get("package").split("=")[1];
            OrderInfo oi = OrderInfo.dao.findFirst("select * from pay_order where order_code=?",getPara("out_trade_no"));
            oi.set("ip", ip).set("prepay_id", prepay_id).update();
            data.put("packageParams", packageParams);
            rest.setData(data);
            rest.setCode(ResultCode.SUCCESS);
        } catch (PaymentException ex){
            rest.setMsg(ex.getReturnMsg());
            rest.setCode(ResultCode.ERROR);
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    @Clear(WxaLoginInterceptor.class)
    public void payNotify(){
        String xmlMsg = HttpKit.readData(getRequest());
        Map<String, String> params = PaymentKit.xmlToMap(xmlMsg);

        String result_code  = params.get("result_code");
        // 总金额
        String totalFee     = params.get("total_fee");
        // 商户订单号
        String orderId      = params.get("out_trade_no");
        // 微信支付订单号
        String transId      = params.get("transaction_id");
        // 支付完成时间，格式为yyyyMMddHHmmss
        String timeEnd      = params.get("time_end");

        // 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态
        // 避免已经成功、关闭、退款的订单被再次更新
        if(PaymentKit.verifyNotify(params, partnerKey)){
            if (("SUCCESS").equals(result_code)) {
                //在数据库中，更新订单信息
                System.out.println("订单号：" + orderId);
                OrderInfo oi = OrderInfo.dao.findFirst("select * from pay_order where order_code=?", orderId);
                if(oi != null && oi.get("trans_id") == null){
                    oi.set("order_state", OrderState.FeedBack_Confirm).set("trans_id", transId)
                            .set("total_fee", totalFee).set("time_end", timeEnd).update();
                    if(Double.parseDouble(totalFee) == oi.getBigDecimal("total_pay").doubleValue() ){
                        //根据订单类型反写业务数据
                        switch (oi.getStr("data_type")){
                            case "CO":
//                              UserEnroll.dao.findById(oi.get("data_id"))
//                                    .set("order_code", orderId).set("state", 2).update();
                                break;
                            case "PO":
                                break;
                            default:
                                break;
                        }
                        //向用户推送模板消息

                        //向平台管理员推送
                    }else{ //订单异常
                        oi.set("order_state", OrderState.Order_Error).update();
                    }
                    //向微信支付的反馈信息
                    Map<String, String> xml = new HashMap<String, String>();
                    xml.put("return_code", "SUCCESS");
                    xml.put("return_msg", "OK");
                    renderText(PaymentKit.toXml(xml));
                    return;
                }
            }
        }
        renderText("");
    }
}
