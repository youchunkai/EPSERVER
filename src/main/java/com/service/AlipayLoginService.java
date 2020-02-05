package com.service;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;

@Service(value = "AlipayLoginService")
public class AlipayLoginService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlipayLoginService.class);

    /**Alipay客户端*/
    private AlipayClient alipayClient;

    /**支付宝网关*/
    private static final String ALIPAY_BORDER_DEV = "https://openapi.alipaydev.com/gateway.do";
    private static final String ALIPAY_BORDER_PROD = "https://openapi.alipay.com/gateway.do";
    /**appID**/
    private static final String APP_ID_DEV = "2018070260541124";
    private static final String APP_ID_PROD = "2018070260541124";
    /**私钥*/
    private static final String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCcZurY5CpKwg+oNsMvj2XYu0iSzPcuJl6e4Q7GPTpZ+Y0ws7saxLzkoUdvEJgeNeZFBpq8UaOKjEOdFxRk3k7wpF8F+MCQP9TbEOvfiBLb8EbMQfHh0LRZO0oXI+tAfJ26SN/hQZNf5pB3GgrEcMMnk1mqaYd9WK3xqh2uhYycVy60GQVs+PN8d3ApgfdfMFTd160oA3Ss577Hg9t5GKXCrLTK/Gwi5U7ZTH0r5z50XKOSc9whsnzGo3/b73wcXc5M5hr5hS2VcceSYUorBYWEOtxnhEtHmwkutdLxD/HCkM3DnsRE5F0DZQgjc1Q1VXqd9ce2vWCMg4Y1g4soydB3AgMBAAECggEAcjT06Mwz1cM/i6XxgZlE1a5soEXreVerYHkXQMsVAP6pixazWiivhoP+lauYaPcS3vSjqUA0G8ew19UynzV2j2J3wPSUKMFzvUPt+ZxzqQcbq6u75RlxmHci8yLXdo+I3a65TNOFulGyzEXH9wogd5WxtC67MVzAJ+gBJJ+AvegGPxpqClr1HjzXUM7ptLCzreFFMeIWr37ML284ytEj74GpB8RaeASW/8BJD9XPcXFjeNC5zdMYVkbQb1+IZuTSL958qrm+DPMK8Q63M37XQ4jiNTulR9cCd1h7+1nyUxJZcTU6mfi9l3kYIXHpb17+VX2XW+k8PWlkQdlE8GUCgQKBgQDbvKk3HcDit3tvvBq462cBHwNlRbZplj1chv7wlyUmaZ1pZjktkKjrGvOBPc2uudOEtIuUAa2tYAVyb5wOB+vCNMYR7FllJnJywrQlpaKzOtXxDX7+IGYPEa33HUvy1ZtBgEsQnw0smywZBb9OouLj6Dy7zqlduAW+vApQj6/w0wKBgQC2NoNbbAlAIx996j712kSIOEFOXzk55dmRKjRrNs2sXWUYgK3VviYOTSiUG8+djjIMlOBkuEzYFLZS+Yy72M3WO6Q0e2B1odUB8CYfdZrpNk6MsvXWw23A8rDLtzt99RCy2FiwOn4iilZLJ5Pz7wLR//OFerDapxkzr5RpBZN7TQKBgEsjRhGswOpFp5Xbkp+tDznY6wZUQj30u+LJ3p9Sb8ek0cjaxa6JCtnqC14kThLzRBrayefAcy0X6oTpGUnK95kfBqPWV/M4pGYNoiBFdU8oTUPyQ3yOg5efigc4I2JUHJTXU9PJL24TWY7s4i/3jvIz1aC62DtN09kW38gQei81AoGAGj2TgTD3zBLC0EFRu9opEGz5iq3P8fSV9n/beK01xWLV0keFwUhW9qxJ8bpnlcsKjiFKh3a7KkshTqW9jLfWPMnuBdjWtVoO8O6Crvkiy/8UJNMI8FrIjYblyxCiMqt+MiNR8l8aFyNQ9hXHCtQa1d9EVK6slXCHApiAiPdSm+kCgYBBD8k9L5j7970LZXDT+PemXnfumV3TWBEXKE5l5VnZaoLSIFjzs6ZKrChXw7AeWXnTRmyAZG+FOssBKh2Rg4doIh6vRfzhRcJ6QM9izWj6jWWYqck3iGPJaw6ixh1XTXpRrTu2p6DvifX9RXjKz7QETYiNTwgW4d3OBW0VzJQCrg==";
    /**公钥*/
    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhGuNet3qIP3Q1oJ6QKvtAIcrI6VZeo5236VdVfa5rDc0g/Vgw4iu6bmkr0f2euilLqsKjOQQkyHYS+v9s2VeG0rnju6/aiKvQAOInGji6+E7D/xNQsFvm1CyxaatHNIPw6i4HahGYKSQbl8dz0WEX00M3famAb7iE9VsEq+cobZk0AzHtQGBxvL6+TsfjHxdvmv9pDXYXvO584PAjbS3D/9y9HVMxvleVQYtZxgyLJivA+IzoCyGg7z4lQGEQiPz7DyhM3/k1WNg20IE2rXdTUAYunigKU0pvmfKnSbJkpXkkpbO7C5qLu4KPT+U8LZheGW6angfHVfIg43W3qVwVQIDAQAB";

    @Override
    public void afterPropertiesSet() throws Exception {
        alipayClient = new DefaultAlipayClient(ALIPAY_BORDER_PROD, APP_ID_PROD, APP_PRIVATE_KEY, "json", "UTF-8", ALIPAY_PUBLIC_KEY, "RSA2");
    }

    /**
     * 根据auth_code获取用户的user_id和access_token
     * @param authCode
     * @return
     */
    public String getAccessToken(String authCode) {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setCode(authCode);
        request.setGrantType("authorization_code");
        try {
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
            return oauthTokenResponse.getAccessToken();
        } catch (Exception e) {
            LOGGER.error("使用authCode获取信息失败！", e);
            return null;
        }
    }

    /**
     * 根据access_token获取用户信息
     * @param token
     * @return
     */
    public HashMap<String, Object> getUserInfoByToken(String token) {
        AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest ();
        try {
            AlipayUserInfoShareResponse response =  alipayClient.execute(request, token);
            if (response.isSuccess()) {
                //打印响应信息
            	LOGGER.info(JSON.toJSON(response).toString());
                //封装支付宝对象信息
                HashMap<String, Object> alipayUser = new HashMap<String, Object>();
                alipayUser.put("Address",response.getAddress());
                alipayUser.put("CertNo",response.getCertNo());
                alipayUser.put("City",response.getCity());
                alipayUser.put("CollegeName",response.getCollegeName());
                alipayUser.put("Degree",response.getDegree());
                alipayUser.put("Mobile",response.getMobile());
                alipayUser.put("Phone",response.getPhone());
                alipayUser.put("Province",response.getProvince());
                alipayUser.put("UserName",response.getUserName());
                alipayUser.put("NickName",response.getNickName());
                alipayUser.put("UserId",response.getUserId());
                alipayUser.put("userType",response.getUserType());
                alipayUser.put("isstudentcertified",response.getIsStudentCertified());
                alipayUser.put("iscertified",response.getIsCertified());
                alipayUser.put("gender",response.getGender());
                alipayUser.put("avatar",response.getAvatar());
                
                return alipayUser;
            }
            LOGGER.error("根据 access_token获取用户信息失败!");
            return null;

        } catch (Exception e) {
            LOGGER.error("根据 access_token获取用户信息抛出异常！", e);
            return null;
        }
    }
}
