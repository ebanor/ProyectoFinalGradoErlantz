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
