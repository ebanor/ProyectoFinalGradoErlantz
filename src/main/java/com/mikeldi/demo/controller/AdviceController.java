package com.mikeldi.demo.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdviceController {

    @ModelAttribute("notifDuos")
    public int notifDuos() {
        return 0;
    }

    @ModelAttribute("notifTeams")
    public int notifTeams() {
        return 0;
    }

    @ModelAttribute("notifFriends")
    public int notifFriends() {
        return 0;
    }
}