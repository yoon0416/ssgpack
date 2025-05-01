package com.ssgpack.ssgfc.user;

import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@Service
public class SmsService {

    @Value("${sms_key}")
    private String smsKey;

    @Value("${sms_secret}")
    private String smsSecret;

    public String phoneNumberCheck(String to) throws CoolsmsException {
        String authCode = "";
        Random rand = new Random();

        for (int i = 0; i < 6; i++) {
            authCode += Integer.toString(rand.nextInt(10));
        }

        Message coolsms = new Message(smsKey, smsSecret);

        HashMap<String, String> params = new HashMap<>();
        params.put("to", to);         // 수신번호
        params.put("from", "01054093770");       // 발신번호 (coolsms에 등록한걸로 고정시킴)
        params.put("type", "SMS");
        params.put("text", "인증번호 [" + authCode + "] 입니다.");
        params.put("app_version", "ssgfc 1.0");

        coolsms.send(params);

        return authCode;
    }
}
