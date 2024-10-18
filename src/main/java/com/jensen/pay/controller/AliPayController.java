package com.jensen.pay.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于支付宝通用版SDK完成支付相关接口
 * <p>
 * 支付宝SDK说明：https://opendocs.alipay.com/open/54/103419
 * <p>
 * 支付宝当面付API文档：https://open.alipay.com/api/detail?code=I1080300001000041016&index=0#api-detail-content
 *
 * @author jensen
 * @date 2024-10-17 6:08
 */
@RestController
@RequestMapping("alipay")
public class AliPayController {
    /**
     * 当面付 统一创建交易生成二维码接口
     *
     * @param orderNo 订单ID 20150320010101001
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) {
        //演示： 返回二维码链接
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //request.setBizContent();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        AlipayClient alipayClient = client();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount("1");
        model.setSubject("Iphone16plus 加长版");
        request.setBizModel(model);
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        String qrCode = response.getQrCode();
        return createQrCode(qrCode);
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
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        AlipayClient alipayClient = client();
        model.setOutTradeNo(orderNo);
        // 设置查询选项
        //List<String> queryOptions = new ArrayList<String>();
        //queryOptions.add("trade_settle_info");
        //model.setQueryOptions(queryOptions);
        request.setBizModel(model);
        // 第三方代调用模式下请设置app_auth_token
        // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }
        String tradeStatus = response.getTradeStatus();
        return tradeStatus;
    }

    /**
     * 支付成功后发起退款
     *
     * @param orderNo  商户订单号ID
     * @param refundNo 退款金额，不大于交易金额
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("refund")
    public String refund(String orderNo, String refundNo) throws AlipayApiException {
        //返回退款请求 是否成功发起
        // 构造请求参数以调用接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        // 初始化SDK
        AlipayClient alipayClient = client();
        // 设置商户订单号
        model.setOutTradeNo(orderNo);
        // 设置退款金额
        model.setRefundAmount(refundNo);
        request.setBizModel(model);
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);Local variable 'fundChange' is redundant
        }
        return response.getFundChange();
    }

    /**
     * 查询退款是否成功
     *
     * @param orderNo 商户订单号
     * @return
     */
    @RequestMapping("refundQuery")
    public String refundQuery(String orderNo, String refundNo) throws AlipayApiException {
        //返回退款结果
        if (null == refundNo) {
            refundNo = orderNo;
        }
        //初始化SDK
        AlipayClient alipayClient = client();
        //构造请求参数以调用接口
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(orderNo);
        model.setOutRequestNo(refundNo);
        request.setBizModel(model);
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }
        return response.getRefundStatus();
    }

    /**
     * 关闭用户未支付的订单
     *
     * @throws AlipayApiException
     */
    @RequestMapping("close")
    public void close(String orderNo) throws AlipayApiException {
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayClient alipayClient = client();
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
    }

    public AlipayClient client() {
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        //设置应用ID
        alipayConfig.setAppId("2021*****7077");
        //设置应用私钥
        alipayConfig.setPrivateKey("MIIEvgIBADANB************************z7+8nKREq5+VrodZFfC");
        //设置请求格式，固定值json
        alipayConfig.setFormat("json");
        //设置字符集
        alipayConfig.setCharset("GBK");
        //设置签名类型
        alipayConfig.setSignType("RSA2");
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey("MIIBIjANBgkqhkiG9*********fJ3fczli******tBN6o6jQpufgNYjvq+SoNC4U*8P9GT6*oW/+MwvHgMs*gEDzQIDAQAB");
        //实例化客户端
        try {
            return new DefaultAlipayClient(alipayConfig);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            System.out.println("初始化支付宝配置错误");
            throw new RuntimeException("初始化支付宝配置错误");
        }
    }

    public String createQrCode(String qrCode) {
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "png");
    }
}
