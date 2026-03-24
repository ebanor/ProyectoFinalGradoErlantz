package com.mikeldi.demo.controller;

import com.mikeldi.demo.entity.*;
import com.mikeldi.demo.repository.MatchRepository;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/stats")
public class StatsController {

    @Autowired private UserRepository userRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private TournamentRegistrationService registrationService;
    @Autowired private TeamService teamService;
    @Autowired private FriendshipService friendshipService;

    @GetMapping
    public String stats(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ── Estadísticas personales ───────────────────────────────
        List<TournamentRegistration> myRegs = registrationService.getMyRegistrations(user);
        long torneosTotales  = myRegs.size();
        long torneosGanados  = matchRepository.findByWinnerUserAndStatus(user, Match.Status.FINISHED).stream()
            .map(m -> m.getTournament().getId()).distinct().count();
        long partidosJugados = matchRepository.findFinishedMatchesByUser(user).size();
        long partidosGanados = matchRepository.findByWinnerUserAndStatus(user, Match.Status.FINISHED).size();

        // Victorias como equipo
        List<Team> myTeams = teamService.getTeamsByCaptain(user);
        long victoriasEquipo = 0;
        for (Team t : myTeams) {
            victoriasEquipo += matchRepository.findByWinnerTeamAndStatus(t, Match.Status.FINISHED).size();
        }

        long totalAmigos = friendshipService.getFriends(user).size();
        long totalEquipos = myTeams.size() + teamService.getAllTeams().stream()
            .filter(t -> !t.getCaptain().equals(user) && teamService.isMember(t, user))
            .count();

        model.addAttribute("user",            user);
        model.addAttribute("torneosTotales",  torneosTotales);
        model.addAttribute("torneosGanados",  torneosGanados);
        model.addAttribute("partidosJugados", partidosJugados);
        model.addAttribute("partidosGanados", partidosGanados);
        model.addAttribute("victoriasEquipo", victoriasEquipo);
        model.addAttribute("totalAmigos",     totalAmigos);
        model.addAttribute("totalEquipos",    totalEquipos);

        // ── Top 10 global ─────────────────────────────────────────
        List<Object[]> raw = matchRepository.findTop10UsersByWins();
        List<Object[]> top10 = raw.size() > 10 ? raw.subList(0, 10) : raw;
        model.addAttribute("top10", top10);

        return "stats";
    }
}
