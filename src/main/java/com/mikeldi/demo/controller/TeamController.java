package com.mikeldi.demo.controller;

import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.TeamMember;
import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.FriendshipService;
import com.mikeldi.demo.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/teams")
public class TeamController {

    @Autowired private TeamService teamService;
    @Autowired private UserRepository userRepository;
    @Autowired private FriendshipService friendshipService;

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        model.addAttribute("teams", teamService.getAllTeams());
        if (principal != null) {
            User user = getUser(principal);
            model.addAttribute("pendingInvitations", teamService.getPendingInvitations(user));
        }
        return "teams/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Team team = teamService.getById(id);
        model.addAttribute("team", team);
        model.addAttribute("confirmedMembers", teamService.getConfirmedMembers(team));
        model.addAttribute("pendingMembers", teamService.getPendingMembers(team));

        if (principal != null) {
            User user = getUser(principal);
            boolean isCaptain = team.getCaptain().equals(user);
            model.addAttribute("isCaptain", isCaptain);
            model.addAttribute("isMember", teamService.isMember(team, user));
            model.addAttribute("myMembership", teamService.getMembership(team, user).orElse(null));
            if (isCaptain) {
                model.addAttribute("friends", friendshipService.getFriends(user));
            }
        }
        return "teams/detail";
    }

    @GetMapping("/new")
    public String createForm() {
        return "teams/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name, Principal principal, RedirectAttributes ra) {
        try {
            User user = getUser(principal);
            Team team = teamService.create(name, user);
            ra.addFlashAttribute("success", "Equipo creado correctamente");
            return "redirect:/teams/" + team.getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams/new";
        }
    }

    @PostMapping("/{id}/invite")
    public String invite(@PathVariable Long id,
                         @RequestParam String username,
                         Principal principal,
                         RedirectAttributes ra) {
        try {
            Team team = teamService.getById(id);
            User captain = getUser(principal);
            User invited = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            teamService.inviteMember(team, captain, invited);
            ra.addFlashAttribute("success", "Invitación enviada a " + username);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams/" + id;
    }

    @PostMapping("/invite/accept/{memberId}")
    public String acceptInvite(@PathVariable Long memberId, Principal principal, RedirectAttributes ra) {
        try {
            User user = getUser(principal);
            teamService.acceptInvitation(memberId, user);
            ra.addFlashAttribute("success", "Te has unido al equipo");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams";
    }

    @PostMapping("/invite/reject/{memberId}")
    public String rejectInvite(@PathVariable Long memberId, Principal principal, RedirectAttributes ra) {
        try {
            User user = getUser(principal);
            teamService.rejectInvitation(memberId, user);
            ra.addFlashAttribute("success", "Invitación rechazada");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams";
    }

    @PostMapping("/{id}/kick")
    public String kick(@PathVariable Long id,
                       @RequestParam String username,
                       Principal principal,
                       RedirectAttributes ra) {
        try {
            Team team = teamService.getById(id);
            User captain = getUser(principal);
            User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            teamService.kickMember(team, captain, target);
            ra.addFlashAttribute("success", username + " ha sido expulsado del equipo");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/leave")
    public String leave(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Team team = teamService.getById(id);
            User user = getUser(principal);
            teamService.leave(team, user);
            ra.addFlashAttribute("success", "Has abandonado el equipo");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams";
    }

    @PostMapping("/{id}/dissolve")
    public String dissolve(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Team team = teamService.getById(id);
            User user = getUser(principal);
            teamService.dissolve(team, user);
            ra.addFlashAttribute("success", "Equipo disuelto");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teams";
    }
}
