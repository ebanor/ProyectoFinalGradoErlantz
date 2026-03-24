package com.mikeldi.demo.service;

import com.mikeldi.demo.entity.*;
import com.mikeldi.demo.repository.MatchRepository;
import com.mikeldi.demo.repository.TournamentRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BracketService {

    @Autowired private MatchRepository matchRepository;
    @Autowired private TournamentRegistrationRepository registrationRepository;

    public void generateBracket(Tournament tournament) {
        List<TournamentRegistration> registrations = registrationRepository
                .findByTournamentAndStatus(tournament, TournamentRegistration.Status.CONFIRMED);

        Collections.shuffle(registrations); // orden aleatorio

        int round = 1;
        int matchNumber = 1;

        if (tournament.getModality() == Tournament.Modality.TEAM) {
            for (int i = 0; i + 1 < registrations.size(); i += 2) {
                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(round);
                match.setMatchNumber(matchNumber++);
                match.setTeam1(registrations.get(i).getTeam());
                match.setTeam2(registrations.get(i + 1).getTeam());
                match.setStatus(Match.Status.PENDING);
                matchRepository.save(match);
            }
        } else {
            // INDIVIDUAL y DUO → usar user
            for (int i = 0; i + 1 < registrations.size(); i += 2) {
                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(round);
                match.setMatchNumber(matchNumber++);
                match.setPlayer1(registrations.get(i).getUser());
                match.setPlayer2(registrations.get(i + 1).getUser());
                match.setStatus(Match.Status.PENDING);
                matchRepository.save(match);
            }
        }
    }

    public void reportResult(Long matchId, Long winnerId, boolean isTeam) {
        Match match = matchRepository.findById(matchId).orElseThrow();

        if (isTeam) {
            Team winner = new Team();
            winner.setId(winnerId);
            match.setWinnerTeam(winner);
        } else {
            User winner = new User();
            winner.setId(winnerId);
            match.setWinnerUser(winner);
        }

        match.setStatus(Match.Status.FINISHED);
        matchRepository.save(match);

        // Comprobar si el round está completo → generar siguiente
        advanceIfRoundComplete(match.getTournament(), match.getRound());
    }

    private void advanceIfRoundComplete(Tournament tournament, int round) {
        List<Match> roundMatches = matchRepository.findByTournamentAndRound(tournament, round);
        boolean allFinished = roundMatches.stream()
                .allMatch(m -> m.getStatus() == Match.Status.FINISHED);

        if (!allFinished) return;

        // Recoger ganadores y crear siguiente ronda
        int nextRound = round + 1;
        int matchNumber = 1;

        if (tournament.getModality() == Tournament.Modality.TEAM) {
            for (int i = 0; i + 1 < roundMatches.size(); i += 2) {
                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(nextRound);
                match.setMatchNumber(matchNumber++);
                match.setTeam1(roundMatches.get(i).getWinnerTeam());
                match.setTeam2(roundMatches.get(i + 1).getWinnerTeam());
                match.setStatus(Match.Status.PENDING);
                matchRepository.save(match);
            }
        } else {
            for (int i = 0; i + 1 < roundMatches.size(); i += 2) {
                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(nextRound);
                match.setMatchNumber(matchNumber++);
                match.setPlayer1(roundMatches.get(i).getWinnerUser());
                match.setPlayer2(roundMatches.get(i + 1).getWinnerUser());
                match.setStatus(Match.Status.PENDING);
                matchRepository.save(match);
            }
        }

        // Si solo queda 1 partido en la ronda anterior → torneo terminado
        if (roundMatches.size() == 1) {
            tournament.setStatus(Tournament.Status.FINISHED);
        }
    }

    public List<Match> getMatchesByTournament(Tournament tournament) {
        return matchRepository.findByTournamentOrderByRoundAscMatchNumberAsc(tournament);
    }
}
