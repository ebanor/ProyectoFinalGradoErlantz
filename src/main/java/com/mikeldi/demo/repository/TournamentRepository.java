package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByOrganizer(User organizer);

    List<Tournament> findByStatus(Tournament.Status status);

    List<Tournament> findByGame(String game);

    List<Tournament> findByModality(Tournament.Modality modality);
}
