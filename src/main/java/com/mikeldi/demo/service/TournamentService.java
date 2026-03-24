package com.mikeldi.demo.service;

import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.TournamentRepository;
import com.mikeldi.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Tournament getById(Long id) {
        return tournamentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
    }

    public Tournament create(Tournament tournament) {
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();

        User organizer = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        tournament.setOrganizer(organizer);
        tournament.setStatus(Tournament.Status.OPEN);

        return tournamentRepository.save(tournament);
    }

    public void delete(Long id) {
        tournamentRepository.deleteById(id);
    }

    public List<Tournament> getMyTournaments() {
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();

        User organizer = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return tournamentRepository.findByOrganizer(organizer);
    }
    
    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

}
