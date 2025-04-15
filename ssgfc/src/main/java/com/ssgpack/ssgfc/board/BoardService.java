package com.ssgpack.ssgfc.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
	@Autowired BoardRepository br;
	//@Autowired UserRepositoy ur;
	
	public List<Board> findAll() {
		return br.findAll();
	}
	
	public Board find(Long id) {
		Board bd = br.findById(id).get();
		bd.setHit(bd.getHit()+1);
		br.save(bd);
		return bd;
	}
	
	public void insert(Board bd, Long member_id) {

		bd.setIp();
		br.save(bd);
	}
	
	
}
