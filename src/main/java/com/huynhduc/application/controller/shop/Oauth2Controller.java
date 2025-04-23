package com.huynhduc.application.controller.shop;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class Oauth2Controller {
    @GetMapping("/oauth2")
    public Principal getDat(Principal principal){
        return principal;
    }
}
