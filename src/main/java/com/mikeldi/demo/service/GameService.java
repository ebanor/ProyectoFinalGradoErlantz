package com.mikeldi.demo.service;

import com.mikeldi.demo.entity.Game;
import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.repository.GameRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public List<Game> getAllGames() { return gameRepository.findAll(); }

    public Game getById(Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
    }

    @PostConstruct
    public void seedGames() {
        upsert("League of Legends",
            List.of(Tournament.Modality.TEAM),
            List.of(4, 8, 16, 32), 3, 5);

        upsert("Valorant",
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.TEAM),
            List.of(4, 8, 16, 32, 64), 1, 5);

        upsert("Fortnite",
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.DUO, Tournament.Modality.TEAM),
            List.of(16, 32, 64, 100, 128, 256), 3, 4);

        upsert("Counter-Strike 2",
            List.of(Tournament.Modality.TEAM),
            List.of(4, 8, 16, 32), 5, 5);

        upsert("FIFA 25",
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.DUO),
            List.of(8, 16, 32, 64, 128), 0, 0);

        upsert("Rocket League",
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.DUO, Tournament.Modality.TEAM),
            List.of(4, 8, 16, 32, 64), 2, 3);

        upsert("Chess",
            List.of(Tournament.Modality.INDIVIDUAL),
            List.of(8, 16, 32, 64, 128), 0, 0);

        upsert("Clash Royale",
            List.of(Tournament.Modality.INDIVIDUAL, Tournament.Modality.DUO),
            List.of(8, 16, 32, 64, 128), 0, 0);
    }

    @Transactional
    public void upsert(String name, List<Tournament.Modality> modalities,
                       List<Integer> maxParticipants, int maxSubs, int playersPerTeam) {
        Game g = gameRepository.findByName(name).orElse(new Game());
        g.setName(name);
        g.setAllowedModalities(modalities);
        g.setAllowedMaxParticipants(maxParticipants);
        g.setMaxSubstitutesPerTeam(maxSubs);
        g.setPlayersPerTeam(playersPerTeam);
        gameRepository.save(g);
    }
    
    public void delete(Long id) {
        gameRepository.deleteById(id);
    }

}
