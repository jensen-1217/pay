package com.jensen.pay.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SDK:https://opendocs.alipay.com/open/54/103419?pathHash=d6bc7c2b
 * @author jensen
 * @date 2024-10-17 6:08
 */
@RestController
@RequestMapping("alipay_easy")
public class AliPayEasyController {
    @Resource
    private Config config;

    /**
     * 当面付 统一创建交易生成二维码接口
     * @param orderNo 订单ID 20150320010101001
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) throws Exception {
        //设置参数
        Factory.setOptions(config);
        //调用API
        AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace()
                .asyncNotify("http://wv7aqa.natappfree.cc/alipay_easy/notify")
                .preCreate("神领物流运费", orderNo, "0.10");
        //App支付实例
        AlipayTradeAppPayResponse pay = Factory.Payment.App().pay("神领物流运费", orderNo, "0.01");
        String body = pay.getBody();
        String qrCode = createQrCode(response.getQrCode());
        System.out.println("qrCode = " + qrCode);
        // 3. 处理响应或异常
        if (ResponseChecker.success(response)) {
            System.out.println("调用成功");
        } else {
            System.err.println("调用失败，原因：" + response.msg + "，" + response.subMsg);
        }
        return qrCode;
    }
    /**
     * 实现统一首收单交易查询
     *
     * @param orderNo 订单ID
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("query")
    public String query(String orderNo) {
        //返回支付状态
        Factory.setOptions(config);
        AlipayTradeQueryResponse response=null;
        try {
            response = Factory.Payment.Common().query(orderNo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //返回TRADE_SUCCESS代表支付成功
        return response.getTradeStatus();
    }

    /**
     * 支付成功后发起退款
     * @param orderNo 商户订单号ID
     * @param refundNo 退款金额，不大于交易金额
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("refund")
    public String refund(String orderNo, String refundNo) throws Exception {
        //返回退款请求 是否成功发起
        Factory.setOptions(config);
        AlipayTradeRefundResponse response = Factory.Payment.Common().refund(orderNo, "0.1");
        //部分退款需要加out_request_no 退款请求号
        //AlipayTradeRefundResponse response = Factory.Payment.Common().optional("out_request_no",refundNo).refund(orderNo, "0.1");
        return response.getFundChange();
    }

    /**
     * 查询退款是否成功
     * @param orderNo 商户订单号
     * @return
     */
    @RequestMapping("refundQuery")
    public String refundQuery(String orderNo,String refundNo) throws Exception {
        //返回退款结果
        Factory.setOptions(config);
        AlipayTradeFastpayRefundQueryResponse response = Factory.Payment.Common().queryRefund(orderNo, (null == refundNo) ? orderNo : refundNo);
        return response.getRefundStatus();
    }

    /**
     * TODO 支付成功后异步通知接口
     * @param request
     * @return
     */
    @RequestMapping("notify")
    public String notify(HttpServletRequest request) throws Exception {
        //1.接收参数，并将参数转为map
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String,String> paramMap=new HashMap();
        parameterMap.forEach((k,v)->{
            paramMap.put(k, Arrays.stream(v).collect(Collectors.joining()));
        });
        //2.验证签名
        Factory.setOptions(config);
        Boolean aBoolean = Factory.Payment.Common().verifyNotify(paramMap);
        if (!aBoolean){
            throw new RuntimeException("验证签名失败");
        }
        return "success";
    }

    /**
     * 关闭用户未支付的订单
     * @throws AlipayApiException
     */
    @RequestMapping("close")
    public void close(String orderNo) throws Exception {
        Factory.setOptions(config);
        AlipayTradeCloseResponse response = Factory.Payment.Common().close(orderNo);
        System.out.println(response);

    }
    public String createQrCode(String qrCode){
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "png");
    }
}
