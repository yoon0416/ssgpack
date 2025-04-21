package com.ssgpack.ssgfc.board.like;

import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable {
    private Long board;
    private Long user;

    public LikeId() {}

    public LikeId(Long board, Long user) {
        this.board = board;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeId)) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(board, likeId.board) && Objects.equals(user, likeId.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, user);
    }
}
