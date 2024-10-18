package com.jensen.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jensen
 * @date 2024-10-17 22:27
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "restkeeper.easyalipay")
public class AliPayProperties {
    //请求协议
    private String protocol;
    //请求网关
    private String gatewayHost;
    //签名类型RSA2
    private String signType;
    //应用ID
    private String appId;
    //应用私钥
    private String merchantPrivateKey;
    //支付宝公钥
    private String alipayPublicKey;
    //设置AES密钥
    private String encryptKey;
}
