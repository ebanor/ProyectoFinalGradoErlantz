package com.mikeldi.demo.config;

import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.FriendshipService;
import com.mikeldi.demo.service.TeamService;
import com.mikeldi.demo.service.TournamentRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class NotificationAdvice {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendshipService friendshipService;
    @Autowired private TeamService teamService;
    @Autowired private TournamentRegistrationService registrationService;

    @ModelAttribute
    public void addNotifications(Principal principal, Model model) {
        if (principal == null) return;

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return;

        int friendRequests  = friendshipService.getPendingReceived(user).size();
        int teamInvitations = teamService.getPendingInvitations(user).size();
        int duoInvitations  = registrationService.getAllPendingDuoInvitations(user).size();

        int total = friendRequests + teamInvitations + duoInvitations;

        model.addAttribute("notifFriends", friendRequests);
        model.addAttribute("notifTeams",   teamInvitations);
        model.addAttribute("notifDuos",    duoInvitations);
        model.addAttribute("notifTotal",   total);
    }
}
