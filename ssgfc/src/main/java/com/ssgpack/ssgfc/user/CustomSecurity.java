package com.ssgpack.ssgfc.user;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomSecurity {

    public boolean checkVerified(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser().isVerified();
        }
        return false;
    }
}
