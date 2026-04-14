package com.mikeldi.demo.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = true)
    private String slug;

    @ElementCollection(targetClass = Tournament.Modality.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "game_modalities", joinColumns = @JoinColumn(name = "game_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "modality")
    private List<Tournament.Modality> allowedModalities;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_max_participants", joinColumns = @JoinColumn(name = "game_id"))
    @OrderColumn(name = "idx")
    @Column(name = "value")
    private List<Integer> allowedMaxParticipants;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tournament> tournaments = new ArrayList<>();

    private int maxSubstitutesPerTeam;

    private int playersPerTeam;

    public String getAllowedModalitiesStr() {
        if (allowedModalities == null) return "";
        return allowedModalities.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public String getAllowedMaxParticipantsStr() {
        if (allowedMaxParticipants == null) return "";
        return allowedMaxParticipants.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public List<Tournament.Modality> getAllowedModalities() { return allowedModalities; }
    public void setAllowedModalities(List<Tournament.Modality> m) { this.allowedModalities = m; }

    public List<Integer> getAllowedMaxParticipants() { return allowedMaxParticipants; }
    public void setAllowedMaxParticipants(List<Integer> p) { this.allowedMaxParticipants = p; }

    public int getMaxSubstitutesPerTeam() { return maxSubstitutesPerTeam; }
    public void setMaxSubstitutesPerTeam(int s) { this.maxSubstitutesPerTeam = s; }

    public int getPlayersPerTeam() { return playersPerTeam; }
    public void setPlayersPerTeam(int p) { this.playersPerTeam = p; }
}
