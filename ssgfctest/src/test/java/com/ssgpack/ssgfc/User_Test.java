package com.ssgpack.ssgfc;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;

@SpringBootTest
class User_Test {
	@Autowired UserRepository ur;
	
	
	@Disabled //@Test
	public void insert() {
		User user = new User();
		user.setEmail("2@mail.com");
		user.setNick_name("2");
		user.setPwd("2");
		user.setIp();
		ur.save(user);
	}
	
	@Test
	public void selectAll() {
		List<User> list = ur.findAll();
		for(User u : list) {System.out.println(u);}
	}


}
