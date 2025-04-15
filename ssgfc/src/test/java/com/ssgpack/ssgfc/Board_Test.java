package com.ssgpack.ssgfc;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ssgpack.ssgfc.board.Board;
import com.ssgpack.ssgfc.board.BoardRepository;

@SpringBootTest
class Board_Test {
	@Autowired BoardRepository br;
	
	@Disabled //@Test //
	public void insert() {
		Board bd = new Board();
		bd.setTitle("2");
		bd.setContent("2");
		bd.setIp();
		br.save(bd);
	}

	@Test
	public void selectAll() {
		List<Board> list = br.findAll();
		for(Board b : list) {System.out.println(b);}
	}
}
