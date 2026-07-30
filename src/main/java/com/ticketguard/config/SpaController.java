package com.ticketguard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/login",
        "/register",
        "/dashboard",
        "/events/**",
        "/seats/**",
        "/payment",
        "/my-bookings",
        "/my-profile",
        "/payment-methods",
        "/notifications",
        "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
