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

	@Disabled //@Test
	public void selectAll() {
		List<Board> list = br.findAll();
		for(Board b : list) {System.out.println(b);}
	}
	
	// 게시물 101개 만들기용
	@Disabled //@Test
    public void insertMultipleBoards() {
        User user = new User();
        user.setId(2L); 

        for (int i = 1; i <= 101; i++) {
            Board bd = new Board();
            bd.setTitle("TEST " + i);
            bd.setContent("TEST_CONTENT " + i);
            bd.setIp();
            bd.setUser(user);
            br.save(bd);
        }
        
        System.out.println("게시글 총 개수: " + br.count());
    }
}
