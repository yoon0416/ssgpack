package com.ssgpack.ssgfc.vote;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VoteForm {
    private String title;
    private List<String> contents = new ArrayList<>();
}
