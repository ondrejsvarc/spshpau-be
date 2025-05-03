package com.spshpau.be.services.wrappers;

import com.spshpau.be.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchedUser {
    private User user;
    private double score;
}
