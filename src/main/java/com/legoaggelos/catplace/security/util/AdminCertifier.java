package com.legoaggelos.catplace.security.util;

import org.springframework.security.core.Authentication;

public class AdminCertifier {
    public static boolean isAdmin(Authentication authentication) {
        if (authentication==null) {
            return false;
        }
        return authentication.getAuthorities().toString().contains("ADMIN");
    }
}
