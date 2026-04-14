package com.mikeldi.demo.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mikeldi.demo.entity.Game;
import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.repository.GameRepository;

@Controller
@RequestMapping("/games")
public class GamesController {

    private final GameRepository gameRepository;

    public GamesController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    // 👉 Esto hace que "modalities" esté disponible SIEMPRE en todas las vistas
    @ModelAttribute("modalities")
    public Tournament.Modality[] modalities() {
        return Tournament.Modality.values();
    }

    @GetMapping("")
    public String games(Model model) {
        List<Game> games = gameRepository.findAll();
        model.addAttribute("games", games);
        return "games";
    }

    @GetMapping("/add")
    public String showAddGame(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("modalities", Tournament.Modality.values());
        return "games/add";
    }

    @PostMapping("/add")
    public String saveGame(
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam int playersPerTeam,
            @RequestParam int maxSubstitutesPerTeam,
            @RequestParam(required = false) List<String> allowedModalities,
            @RequestParam List<Integer> allowedMaxParticipants,
            RedirectAttributes redirectAttributes) {

        if (gameRepository.findByName(name).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un juego con ese nombre.");
            return "redirect:/games/add";
        }

        Game game = new Game();
        game.setName(name);
        game.setSlug(slug);
        game.setPlayersPerTeam(playersPerTeam);
        game.setMaxSubstitutesPerTeam(maxSubstitutesPerTeam);

        game.setAllowedModalities(
                allowedModalities != null
                        ? allowedModalities.stream()
                                .map(Tournament.Modality::valueOf)
                                .collect(Collectors.toList())
                        : List.of()
        );

        game.setAllowedMaxParticipants(allowedMaxParticipants);

        gameRepository.save(game);
        redirectAttributes.addFlashAttribute("success", "Juego \"" + name + "\" añadido correctamente.");

        return "redirect:/games";
    }

    @GetMapping("/{id}/edit")
    public String showEditGame(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {

        Game game = gameRepository.findById(id).orElse(null);
        if (game == null) {
            redirectAttributes.addFlashAttribute("error", "Juego no encontrado.");
            return "redirect:/games";
        }

        model.addAttribute("game", game);

        // ← Pasa las modalidades activas como Set<String> para comparar en la vista
        Set<String> activeModalities = game.getAllowedModalities() != null
            ? game.getAllowedModalities().stream()
                  .map(Enum::name)
                  .collect(Collectors.toSet())
            : Set.of();

        model.addAttribute("activeModalities", activeModalities);
        return "games/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateGame(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(defaultValue = "0") int playersPerTeam,
            @RequestParam(defaultValue = "0") int maxSubstitutesPerTeam,
            @RequestParam(required = false) List<String> allowedModalities,
            @RequestParam(required = false) List<Integer> allowedMaxParticipants,
            RedirectAttributes redirectAttributes) {

        Game game = gameRepository.findById(id).orElse(null);

        if (game == null) {
            redirectAttributes.addFlashAttribute("error", "Juego no encontrado.");
            return "redirect:/games";
        }

        // Datos básicos
        game.setName(name);
        game.setSlug(slug);

        // Modalidades (String -> Enum)
        List<Tournament.Modality> modalities = (allowedModalities != null)
                ? allowedModalities.stream()
                        .map(Tournament.Modality::valueOf)
                        .collect(Collectors.toList())
                : List.of();

        game.setAllowedModalities(modalities);

        // Lógica: si no hay TEAM, no tiene sentido guardar valores de equipo
        if (modalities.contains(Tournament.Modality.TEAM)) {
            game.setPlayersPerTeam(playersPerTeam);
            game.setMaxSubstitutesPerTeam(maxSubstitutesPerTeam);
        } else {
            game.setPlayersPerTeam(0);
            game.setMaxSubstitutesPerTeam(0);
        }

        // Participantes (evitar null + limpiar duplicados y negativos)
        List<Integer> participants = (allowedMaxParticipants != null)
                ? allowedMaxParticipants.stream()
                        .filter(p -> p != null && p > 0)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
                : List.of();

        game.setAllowedMaxParticipants(participants);

        gameRepository.save(game);

        redirectAttributes.addFlashAttribute(
                "success",
                "Juego \"" + name + "\" actualizado correctamente."
        );

        return "redirect:/games";
    }

    @PostMapping("/{id}/delete")
    public String deleteGame(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Game game = gameRepository.findById(id).orElse(null);

        if (game == null) {
            redirectAttributes.addFlashAttribute("error", "Juego no encontrado.");
            return "redirect:/games";
        }

        String nombre = game.getName();
        gameRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("success", "Juego \"" + nombre + "\" eliminado correctamente.");

        return "redirect:/games";
    }
}
