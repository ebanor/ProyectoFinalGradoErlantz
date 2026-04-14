package com.mikeldi.demo.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modality modality;

    @Column(nullable = false)
    private int maxParticipants;

    private int playersPerTeam;

    private int substitutesPerTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    private String description;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    public enum Modality {
        INDIVIDUAL, DUO, TEAM
    }

    public enum Status {
        OPEN, CLOSED, IN_PROGRESS, FINISHED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public Modality getModality() { return modality; }
    public void setModality(Modality modality) { this.modality = modality; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public int getPlayersPerTeam() { return playersPerTeam; }
    public void setPlayersPerTeam(int playersPerTeam) { this.playersPerTeam = playersPerTeam; }

    public int getSubstitutesPerTeam() { return substitutesPerTeam; }
    public void setSubstitutesPerTeam(int substitutesPerTeam) { this.substitutesPerTeam = substitutesPerTeam; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
}
