package com.ssgpack.ssgfc;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;

@SpringBootTest
class SsgfcApplicationTests {
	 @Autowired
	    private UserRepository userRepository;

	    @Test //@Disabled
	    void insertUserOnlyTest() {
	        User user = new User();
	        user.setEmail("test123@gmail.com");
	        user.setNick_name("testnick");
	        user.setPwd("1234");

	        try {
	            user.setIp(InetAddress.getLocalHost().getHostAddress());
	        } catch (UnknownHostException e) {
	            throw new RuntimeException("IP 가져오기 실패", e);
	        }

	        userRepository.save(user);
	    }
	}