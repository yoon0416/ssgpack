// ✅ VoteForm.java
package com.ssgpack.ssgfc.vote;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VoteForm {

    // ✅ 투표 제목
    private String title;

    // ✅ 투표 선택지 리스트
    private List<String> contents = new ArrayList<>();

    // ✅ 업로드된 이미지 파일 (폼에서 파일 선택을 통해 전달됨)
    private MultipartFile img;

    // ✅ 투표 마감일 (추가된 필드)
    private LocalDateTime endDate; // 👉 추가!

}
