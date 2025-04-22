package com.ssgpack.ssgfc.vote;

import java.io.Serializable;
import java.util.Objects;

public class UserVoteId implements Serializable {

    private Long user;
    private Long voteTitle;

    public UserVoteId() {}

    public UserVoteId(Long user, Long voteTitle) {
        this.user = user;
        this.voteTitle = voteTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserVoteId)) return false;
        UserVoteId that = (UserVoteId) o;
        return Objects.equals(user, that.user) && Objects.equals(voteTitle, that.voteTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, voteTitle);
    }
}
