package com.ssgpack.ssgfc;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardRepository;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;

@SpringBootTest
class Board_Test {
	@Autowired BoardRepository br;
	@Autowired UserRepository ur;
	
	@Disabled //@Test
	public void insert() {
		User user = new User();
		user.setId(1L);
		
		Board bd = new Board();
		bd.setTitle("2");
		bd.setContent("2");
		bd.setIp();
		bd.setUser(user);
		
		br.save(bd);
	}

	@Test
	public void selectAll() {
		List<Board> list = br.findAll();
		for(Board b : list) {System.out.println(b);}
	}
}
