package com.jensen.pay;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@SpringBootTest
class PayApplicationTests {
    @Resource
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
        //生成二维码
        String url = "http://route.showapi.com/887-1" +
                "?showapi_appid=1703708" +
                "&showapi_sign=7aa109cae76e4767bd6de64a11b10e85" +
                "&content=http://www.itcast.cn" +
                "&size=8" +
                "&imgExtName=png";
        String showApiSring = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(showApiSring);
        String qrcodeUrl = jsonObject.getJSONObject("showapi_res_body").getString("imgUrl");
        System.out.println(qrcodeUrl);
    }
    @Test
    public void generateImg() {
        QrCodeUtil.generate("http://www.baidu.com", 500, 500, FileUtil.file("E:\\files\\qrcodeUtil.jpg"));
    }
    @Test
    public void generateImg2() {
        String imageBase64 = QrCodeUtil.generateAsBase64("http://www.baidu.com", new QrConfig(500, 500), "png");
        System.out.println(imageBase64);
    }

}
