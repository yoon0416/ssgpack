package com.ssgpack.ssgfc.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long>{
       Optional<User> findByEmail(String email); // 이메일로 조회 (로그인 시 사용 등)
       Optional<User> findByKakaoId(Long kakaoId);
       @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.nick_name LIKE %:keyword%")
       Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
       
       // ✅ 게시판(마스터) 관리자 조회
       List<User> findByRoleIn(List<Integer> roles);
}
