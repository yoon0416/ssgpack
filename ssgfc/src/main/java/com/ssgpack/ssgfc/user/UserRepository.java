package com.ssgpack.ssgfc.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long>{
	    Optional<User> findByEmail(String email); // 이메일로 조회 (로그인 시 사용 등)
	    Optional<User> findByKakaoId(Long kakaoId);

}
