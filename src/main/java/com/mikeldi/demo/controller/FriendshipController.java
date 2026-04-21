package com.mikeldi.demo.controller;

import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/friends")
public class FriendshipController {

    @Autowired private FriendshipService friendshipService;
    @Autowired private UserRepository userRepository;

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping
    public String friendsPage(Model model, Principal principal) {
        User user = getUser(principal);
        model.addAttribute("friends", friendshipService.getFriendDTOs(user));
        model.addAttribute("pending", friendshipService.getPendingReceived(user));
        return "friends/index";
    }

    @PostMapping("/add")
    public String sendRequest(@RequestParam String username,
                              Principal principal,
                              RedirectAttributes ra) {
        try {
            friendshipService.sendRequest(getUser(principal), username);
            ra.addFlashAttribute("success", "Solicitud enviada a " + username);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/friends";
    }

    @PostMapping("/accept/{id}")
    public String accept(@PathVariable Long id, Principal principal) {
        friendshipService.acceptRequest(id, getUser(principal));
        return "redirect:/friends";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, Principal principal) {
        friendshipService.rejectRequest(id, getUser(principal));
        return "redirect:/friends";
    }
    
    @PostMapping("/remove/{id}")
    public String removeFriend(@PathVariable Long id, RedirectAttributes ra) {
        friendshipService.remove(id);
        ra.addFlashAttribute("success", "Amigo eliminado.");
        return "redirect:/friends";
    }
}
