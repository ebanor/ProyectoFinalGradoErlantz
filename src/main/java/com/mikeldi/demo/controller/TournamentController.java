package com.mikeldi.demo.controller;

import com.mikeldi.demo.entity.*;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.*;
import com.mikeldi.demo.entity.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

    @Autowired private TournamentService tournamentService;
    @Autowired private GameService gameService;
    @Autowired private TournamentRegistrationService registrationService;
    @Autowired private UserRepository userRepository;
    @Autowired private FriendshipService friendshipService;
    @Autowired private TeamService teamService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tournaments", tournamentService.getAllTournaments());
        return "tournaments/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Tournament tournament = tournamentService.getById(id);
        model.addAttribute("tournament", tournament);

        List<TournamentRegistration> registrations = registrationService.getConfirmedRegistrations(tournament);
        model.addAttribute("registrations", registrations);
        model.addAttribute("confirmedCount", registrations.size());

        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            if (currentUser != null) {
                model.addAttribute("isRegistered", registrationService.isRegistered(tournament, currentUser));
                model.addAttribute("isOrganizer", tournament.getOrganizer().equals(currentUser));

                if (tournament.getModality() == Tournament.Modality.DUO) {
                    model.addAttribute("myDuoRegistration",
                        registrationService.getMyDuoRegistration(tournament, currentUser).orElse(null));
                    model.addAttribute("pendingDuoInvitation",
                        registrationService.getPendingDuoInvitation(tournament, currentUser).orElse(null));
                    model.addAttribute("friends", friendshipService.getFriends(currentUser));
                }

                if (tournament.getModality() == Tournament.Modality.TEAM) {
                    List<Team> myTeams = teamService.getTeamsByCaptain(currentUser);
                    model.addAttribute("myTeams", myTeams);
                    if (!myTeams.isEmpty()) {
                        model.addAttribute("myTeamRegistration",
                            registrationService.getTeamRegistration(tournament, myTeams.get(0)).orElse(null));
                    }
                }
            }
        }

        if (tournament.getStatus() == Tournament.Status.IN_PROGRESS
                || tournament.getStatus() == Tournament.Status.FINISHED) {
            List<Match> matches = bracketService.getMatchesByTournament(tournament);
            model.addAttribute("matches", matches);
            List<Integer> rounds = matches.stream()
                    .map(Match::getRound)
                    .distinct()
                    .sorted()
                    .toList();
            model.addAttribute("rounds", rounds);
        }

        return "tournaments/detail";
    }


    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tournament", new Tournament());
        model.addAttribute("games", gameService.getAllGames());
        return "tournaments/form";
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/new")
    public String create(@RequestParam Long gameId,
                         @RequestParam Tournament.Modality modality,
                         @RequestParam(defaultValue = "0") int substitutesPerTeam,
                         @RequestParam(defaultValue = "0") int playersPerTeam,
                         @ModelAttribute Tournament tournament) {
        Game game = gameService.getById(gameId);
        tournament.setGame(game);
        tournament.setModality(modality);
        tournament.setSubstitutesPerTeam(substitutesPerTeam);
        tournament.setPlayersPerTeam(playersPerTeam);
        tournamentService.create(tournament);
        return "redirect:/tournaments";
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Tournament tournament = tournamentService.getById(id);
        if (!tournament.getOrganizer().getUsername().equals(principal.getName())
                && !SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/tournaments";
        }
        model.addAttribute("tournament", tournament);
        model.addAttribute("games", gameService.getAllGames());
        if (tournament.getStartDate() != null) {
            model.addAttribute("startDateFormatted",
                tournament.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        } else {
            model.addAttribute("startDateFormatted", "");
        }
        return "tournaments/edit";
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam(required = false) String description,
                       @RequestParam Tournament.Status status,
                       @RequestParam(required = false)
                       @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                       Principal principal) {
        Tournament tournament = tournamentService.getById(id);
        if (!tournament.getOrganizer().getUsername().equals(principal.getName())
                && !SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/tournaments";
        }
        tournament.setName(name);
        tournament.setDescription(description);
        tournament.setStatus(status);
        tournament.setStartDate(startDate);
        tournamentService.save(tournament);
        return "redirect:/tournaments/" + id;
    }

    // ── INDIVIDUAL ────────────────────────────────────────────────
    @PostMapping("/{id}/join")
    public String join(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            registrationService.join(tournament, user);
            ra.addFlashAttribute("success", "Te has inscrito correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/leave")
    public String leave(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            registrationService.leave(tournament, user);
            ra.addFlashAttribute("success", "Has abandonado el torneo");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

    // ── DUO ───────────────────────────────────────────────────────
    @PostMapping("/{id}/join-duo")
    public String joinDuo(@PathVariable Long id,
                          @RequestParam String partnerUsername,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            User partner = userRepository.findByUsername(partnerUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            registrationService.joinDuo(tournament, user, partner);
            ra.addFlashAttribute("success", "Invitación enviada a " + partnerUsername);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/duo-invite/accept/{regId}")
    public String acceptDuo(@PathVariable Long regId, Principal principal, RedirectAttributes ra) {
        try {
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            TournamentRegistration reg = registrationService.getById(regId);
            registrationService.acceptDuoInvitation(regId, user);
            ra.addFlashAttribute("success", "Te has unido al duo");
            return "redirect:/tournaments/" + reg.getTournament().getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/tournaments";
        }
    }

    @PostMapping("/duo-invite/reject/{regId}")
    public String rejectDuo(@PathVariable Long regId, Principal principal, RedirectAttributes ra) {
        try {
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            TournamentRegistration reg = registrationService.getById(regId);
            Long tournamentId = reg.getTournament().getId();
            registrationService.rejectDuoInvitation(regId, user);
            ra.addFlashAttribute("success", "Invitación rechazada");
            return "redirect:/tournaments/" + tournamentId;
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/tournaments";
        }
    }

    // ── TEAM ──────────────────────────────────────────────────────
    @PostMapping("/{id}/join-team")
    public String joinTeam(@PathVariable Long id,
                           @RequestParam Long teamId,
                           Principal principal,
                           RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Team team = teamService.getById(teamId);
            registrationService.joinTeam(tournament, user, team);
            ra.addFlashAttribute("success", "Equipo inscrito correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/leave-team")
    public String leaveTeam(@PathVariable Long id,
                            @RequestParam Long teamId,
                            Principal principal,
                            RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Team team = teamService.getById(teamId);
            registrationService.leaveTeam(tournament, user, team);
            ra.addFlashAttribute("success", "Equipo retirado del torneo");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }
    
 // ── BRACKETS ─────────────────────────────────────────────────
    @Autowired private BracketService bracketService;

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/generate-bracket")
    public String generateBracket(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            tournament.setStatus(Tournament.Status.IN_PROGRESS);
            tournamentService.save(tournament);
            bracketService.generateBracket(tournament);
            ra.addFlashAttribute("success", "Bracket generado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{id}/match/{matchId}/result")
    public String reportResult(@PathVariable Long id,
                               @PathVariable Long matchId,
                               @RequestParam Long winnerId,
                               Principal principal,
                               RedirectAttributes ra) {
        try {
            Tournament tournament = tournamentService.getById(id);
            boolean isTeam = tournament.getModality() == Tournament.Modality.TEAM;
            bracketService.reportResult(matchId, winnerId, isTeam);
            ra.addFlashAttribute("success", "Resultado guardado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }

}
