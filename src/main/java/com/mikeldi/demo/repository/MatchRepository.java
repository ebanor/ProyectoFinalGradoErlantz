package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Match;
import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTournamentOrderByRoundAscMatchNumberAsc(Tournament tournament);

    List<Match> findByTournamentAndRound(Tournament tournament, int round);

    @Query("SELECT m FROM Match m WHERE (m.player1 = :user OR m.player2 = :user) AND m.status = 'FINISHED'")
    List<Match> findFinishedMatchesByUser(@Param("user") User user);

    List<Match> findByWinnerUserAndStatus(User user, Match.Status status);

    List<Match> findByWinnerTeamAndStatus(Team team, Match.Status status);

    @Query("SELECT m.winnerUser, COUNT(m) as wins FROM Match m WHERE m.winnerUser IS NOT NULL GROUP BY m.winnerUser ORDER BY wins DESC")
    List<Object[]> findTop10UsersByWins();
}
