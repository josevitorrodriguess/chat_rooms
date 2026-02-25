package com.josevitor.chatrooms.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/me")
    public JwtPrincipal me(@AuthenticationPrincipal JwtPrincipal principal) {
        return principal;
    }
}
