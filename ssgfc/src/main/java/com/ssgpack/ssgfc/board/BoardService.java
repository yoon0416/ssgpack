package com.ssgpack.ssgfc.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository br;

    public List<Board> findAll() {
        return br.findAll();
    }

    public Board find(Long id) {
        Board bd = br.findById(id).orElseThrow();
        bd.setHit(bd.getHit() + 1);
        br.save(bd);
        return bd;
    }

    public void insert(Board bd, Long member_id) {
        bd.setIp();
        br.save(bd);
    }

    public void delete(Long id) {
        br.deleteById(id);
    }
}
