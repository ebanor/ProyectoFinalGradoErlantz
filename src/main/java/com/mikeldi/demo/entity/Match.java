package com.mikeldi.demo.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tournament tournament;

    private int round; // 1 = primera ronda, 2 = cuartos, etc.
    private int matchNumber; // posición dentro del round

    // Para INDIVIDUAL y DUO → se usan player1/player2
    @ManyToOne
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private User player2;

    // Para TEAM → se usan team1/team2
    @ManyToOne
    @JoinColumn(name = "team1_id")
    private Team team1;

    @ManyToOne
    @JoinColumn(name = "team2_id")
    private Team team2;

    // Ganador
    @ManyToOne
    @JoinColumn(name = "winner_user_id")
    private User winnerUser;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDING, FINISHED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public int getMatchNumber() { return matchNumber; }
    public void setMatchNumber(int matchNumber) { this.matchNumber = matchNumber; }

    public User getPlayer1() { return player1; }
    public void setPlayer1(User player1) { this.player1 = player1; }

    public User getPlayer2() { return player2; }
    public void setPlayer2(User player2) { this.player2 = player2; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public User getWinnerUser() { return winnerUser; }
    public void setWinnerUser(User winnerUser) { this.winnerUser = winnerUser; }

    public Team getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(Team winnerTeam) { this.winnerTeam = winnerTeam; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
