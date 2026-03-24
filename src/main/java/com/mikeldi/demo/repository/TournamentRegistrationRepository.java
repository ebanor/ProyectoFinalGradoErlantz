package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.Tournament;
import com.mikeldi.demo.entity.TournamentRegistration;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {

    List<TournamentRegistration> findByTournament(Tournament tournament);
    List<TournamentRegistration> findByTournamentAndStatus(Tournament tournament, TournamentRegistration.Status status);

    Optional<TournamentRegistration> findByTournamentAndUser(Tournament tournament, User user);
    Optional<TournamentRegistration> findByTournamentAndPartner(Tournament tournament, User partner);
    Optional<TournamentRegistration> findByTournamentAndPartnerAndStatus(Tournament tournament, User partner, TournamentRegistration.Status status);
    Optional<TournamentRegistration> findByTournamentAndTeam(Tournament tournament, Team team);

    List<TournamentRegistration> findByPartnerAndStatus(User partner, TournamentRegistration.Status status);

    int countByTournamentAndStatus(Tournament tournament, TournamentRegistration.Status status);
    
    List<TournamentRegistration> findByUserAndStatus(User user, TournamentRegistration.Status status);

}
