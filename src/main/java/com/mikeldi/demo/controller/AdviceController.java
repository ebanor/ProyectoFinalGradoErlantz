package com.mikeldi.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.UserRepository;

@ControllerAdvice
public class AdviceController {

	@Autowired
    private UserRepository userRepository;
	
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
    
    @ModelAttribute("avatarUrl")
    public String avatarUrl(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
            .map(User::getAvatarUrl)
            .orElse(null);
    }
}