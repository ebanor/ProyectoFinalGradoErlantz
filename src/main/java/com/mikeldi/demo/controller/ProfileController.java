package com.mikeldi.demo.controller;

import com.mikeldi.demo.dto.FriendDTO;
import com.mikeldi.demo.entity.*;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserRepository userRepository;
    @Autowired private TournamentRegistrationService registrationService;
    @Autowired private TeamService teamService;
    @Autowired private FriendshipService friendshipService;
    @Autowired private BracketService bracketService;
    @Autowired private UserService userService;

    @GetMapping
    public String profile(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Torneos inscritos
        List<TournamentRegistration> myRegistrations = registrationService.getMyRegistrations(user);
        model.addAttribute("myRegistrations", myRegistrations);

        // Equipos donde es capitán
        List<Team> myTeams = teamService.getTeamsByCaptain(user);
        model.addAttribute("myTeams", myTeams);

        // Equipos donde es miembro (no capitán)
        List<TeamMember> myMemberships = teamService.getPendingInvitations(user); // reutilizamos
        List<Team> memberTeams = teamService.getAllTeams().stream()
            .filter(t -> !t.getCaptain().equals(user) && teamService.isMember(t, user))
            .toList();
        model.addAttribute("memberTeams", memberTeams);

        // Amigos
        List<FriendDTO> friends = friendshipService.getFriendDTOs(user);
        model.addAttribute("friends", friends);

        // Estadísticas básicas
        long torneosTotales = myRegistrations.size();
        long torneosGanados = myRegistrations.stream()
            .filter(r -> {
                Tournament t = r.getTournament();
                if (t.getStatus() != Tournament.Status.FINISHED) return false;
                List<Match> matches = bracketService.getMatchesByTournament(t);
                return matches.stream()
                    .filter(m -> m.getStatus() == Match.Status.FINISHED)
                    .anyMatch(m -> m.getRound() == matches.stream().mapToInt(Match::getRound).max().orElse(0)
                        && m.getWinnerUser() != null && m.getWinnerUser().equals(user));
            }).count();

        model.addAttribute("torneosTotales", torneosTotales);
        model.addAttribute("torneosGanados", torneosGanados);
        model.addAttribute("totalAmigos", friends.size());
        model.addAttribute("user", user);

        return "profile";
    }
    
    @PostMapping("/avatar")
    public String updateAvatar(
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) MultipartFile avatarFile,
            Principal principal,
            RedirectAttributes ra) throws IOException {

        User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String filename = principal.getName() + "_" + System.currentTimeMillis()
                    + "_" + avatarFile.getOriginalFilename();
            Path uploadDir = Paths.get("/app/uploads/avatars/");
            Files.createDirectories(uploadDir);
            avatarFile.transferTo(uploadDir.resolve(filename));
            user.setAvatarUrl("/uploads/avatars/" + filename);

        } else if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);
        ra.addFlashAttribute("success", "Avatar actualizado correctamente");
        return "redirect:/profile";
    }
}
