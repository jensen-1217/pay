package com.jensen.pay.config;


import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jensen
 * @date 2024-10-17 22:27
 */
@Configuration
@Data
public class MyAlipayConfig {
    @Bean
    public Config config(AliPayProperties payProperties){
        Config config=new Config();
        config.protocol=payProperties.getProtocol();
        config.gatewayHost=payProperties.getGatewayHost();
        config.signType=payProperties.getSignType();
        config.appId=payProperties.getAppId();
        config.merchantPrivateKey=payProperties.getMerchantPrivateKey();
        config.alipayPublicKey=payProperties.getAlipayPublicKey();
        config.notifyUrl="";
        config.encryptKey="";
        return config;
    }
}
