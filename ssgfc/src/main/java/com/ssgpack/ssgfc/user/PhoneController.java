package com.ssgpack.ssgfc.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@RestController
@RequiredArgsConstructor
public class PhoneController {

    private final SmsService smsService;

    // 인증번호 요청 (예: /phone/send?to=01012345678)
    @GetMapping("/phone/send")
    public String sendAuthCode(@RequestParam String to) throws CoolsmsException {
        return smsService.phoneNumberCheck(to);
    }
}
