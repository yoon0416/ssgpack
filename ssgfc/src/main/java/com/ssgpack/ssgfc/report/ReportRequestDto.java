package com.ssgpack.ssgfc.report;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ReportRequestDto {
    private String reason;
    private Long boardId;   
    private Long parentId;  
}
