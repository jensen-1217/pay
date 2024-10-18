package com.jensen.pay.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import javax.servlet.http.HttpServletRequest;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.file.IOUtils;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.jensen.pay.config.WXPayConfigCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jensen
 * @date 2024-10-17 6:08
 */
@Slf4j
@RestController
@RequestMapping("wxpay")
public class WxPayController {
    /**
     * 当面付 统一创建交易生成二维码接口
     *
     * @param orderNo 订单ID 20150320010101001
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());
        Map<String,String> paramMap = new HashMap();
        paramMap.put("body","神领物流运费");
        paramMap.put("out_trade_no",orderNo);
        paramMap.put("total_fee","1");
        paramMap.put("spbill_create_ip","123.12.12.123");
        paramMap.put("notify_url","http://wv7aqa.natappfree.cc/wxpay/notify");
        paramMap.put("trade_type","NATIVE ");
        Map<String, String> result = wxPay.unifiedOrder(paramMap);
        String code_url = result.get("code_url");
        return createQrCode(code_url);
    }

    /**
     * 实现统一首收单交易查询
     *
     * @param orderNo 订单ID
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("query")
    public String query(String orderNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());
        Map<String,String> paramMap = new HashMap();
        paramMap.put("out_trade_no",orderNo);
        Map<String, String> result = wxPay.orderQuery(paramMap);
        String trade_state = result.get("trade_state");
        return trade_state;
    }

    /**
     * 支付成功后发起退款
     *
     * @param orderNo  商户订单号ID
     * @param refundNo 退款金额，不大于交易金额
     *                 4200002363202410188410528946
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("refund")
    public Map<String, String> refund(String orderNo, String refundNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());
        Map<String,String> paramMap = new HashMap();
        paramMap.put("out_trade_no",orderNo);
        paramMap.put("out_refund_no",refundNo);
        //paramMap.put("transaction_id","4200002363202410188410528946");
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        Map<String, String> result = wxPay.refund(paramMap);
        return result;
    }

    /**
     * 查询退款是否成功
     *
     * @param orderNo 商户订单号
     * @return
     */
    @RequestMapping("refundQuery")
    public String refundQuery(String orderNo, String refundNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());
        Map<String,String> paramMap = new HashMap();
        paramMap.put("out_trade_no",orderNo);
        paramMap.put("out_refund_no",refundNo);
        Map<String, String> result = wxPay.refundQuery(paramMap);
        return result.get("refund_status_0");
    }

    /**
     * TODO 支付成功后异步通知接口
     *
     * @param request
     * @return
     */
    @RequestMapping("notify")
    public String notify(HttpServletRequest request) {
        try {
            Map<String,String> resultMap = new HashMap();
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            // 加入自己处理订单的业务逻辑，需要判断订单是否已经支付过，否则可能会重复调用
            WXPay wxPay = new WXPay(new WXPayConfigCustom());
            //1.验证签名
            boolean signatureValid = wxPay.isResponseSignatureValid(map);
            if (!signatureValid){
                System.out.println("验证签名失败");
                resultMap.put("return_code","FAIL");
                resultMap.put("return_msg","验证签名失败");
                return WXPayUtil.mapToXml(resultMap);
            }
            //2.根据交易单Id 查询数据库中的交易单
            System.out.println("根据交易单Id 查询数据库中的交易单:"+map.get("out_trade_no"));
            //3.对比金额是否一致
            System.out.println("对比金额是否一致:"+map.get("total_fee"));
            //加分布式锁
            //4.判断交易单的状态
            System.out.println(map.get("判断交易单的状态"+"result_code"));
            //5.根据支付的结果通知 修改交易单的状态
            //解锁
            resultMap.put("return_code","SUCCESS");
            resultMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(resultMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭用户未支付的订单
     *
     * @throws AlipayApiException
     */
    @RequestMapping("close")
    public void close(String orderNo) throws Exception {


    }

    public String createQrCode(String qrCode) {
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "png");
    }
}
