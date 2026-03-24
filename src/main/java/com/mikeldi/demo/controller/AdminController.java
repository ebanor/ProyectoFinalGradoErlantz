package com.mikeldi.demo.controller;

import com.mikeldi.demo.entity.Game;
import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.UserRepository;
import com.mikeldi.demo.service.BracketService;
import com.mikeldi.demo.service.GameService;
import com.mikeldi.demo.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private TournamentService tournamentService;
    @Autowired private GameService gameService;
    @Autowired private BracketService bracketService;

    @GetMapping
    public String dashboard(Model model) {
        List<User> users = userRepository.findAll();
        List<Tournament> tournaments = tournamentService.getAllTournaments();
        List<Game> games = gameService.getAllGames();

        long totalUsuarios   = users.size();
        long totalTorneos    = tournaments.size();
        long torneosActivos  = tournaments.stream()
            .filter(t -> t.getStatus() == Tournament.Status.IN_PROGRESS).count();
        long torneosAbiertos = tournaments.stream()
            .filter(t -> t.getStatus() == Tournament.Status.OPEN).count();

        model.addAttribute("users",          users);
        model.addAttribute("games",          games);
        model.addAttribute("totalUsuarios",  totalUsuarios);
        model.addAttribute("totalTorneos",   totalTorneos);
        model.addAttribute("torneosActivos", torneosActivos);
        model.addAttribute("torneosAbiertos",torneosAbiertos);

        return "admin/dashboard";
    }

    // ── USUARIOS ──────────────────────────────────────────────────
    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam User.Role role,
                             RedirectAttributes ra) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setRole(role);
        userRepository.save(user);
        ra.addFlashAttribute("success", "Rol de " + user.getUsername() + " actualizado a " + role);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        userRepository.delete(user);
        ra.addFlashAttribute("success", "Usuario eliminado correctamente");
        return "redirect:/admin";
    }

    // ── JUEGOS ────────────────────────────────────────────────────
    @PostMapping("/games/new")
    public String addGame(@RequestParam String name,
                          @RequestParam int playersPerTeam,
                          @RequestParam int maxSubstitutesPerTeam,
                          RedirectAttributes ra) {
        gameService.upsert(name,
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.DUO, Tournament.Modality.TEAM),
            List.of(4, 8, 16, 32, 64),
            maxSubstitutesPerTeam,
            playersPerTeam);
        ra.addFlashAttribute("success", "Juego añadido correctamente");
        return "redirect:/admin";
    }

    @PostMapping("/games/{id}/delete")
    public String deleteGame(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Game game = gameService.getById(id);
            // Eliminamos solo si no hay torneos asociados
            boolean hasTournaments = tournamentService.getAllTournaments().stream()
                .anyMatch(t -> t.getGame().getId().equals(id));
            if (hasTournaments) {
                ra.addFlashAttribute("error", "No se puede eliminar un juego con torneos asociados");
            } else {
                gameService.delete(id);
                ra.addFlashAttribute("success", "Juego eliminado correctamente");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
