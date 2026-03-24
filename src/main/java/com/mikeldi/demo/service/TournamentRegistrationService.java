package com.mikeldi.demo.service;

import com.mikeldi.demo.entity.*;
import com.mikeldi.demo.repository.FriendshipRepository;
import com.mikeldi.demo.repository.TeamMemberRepository;
import com.mikeldi.demo.repository.TournamentRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentRegistrationService {

    @Autowired private TournamentRegistrationRepository registrationRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;

    public TournamentRegistration getById(Long id) {
        return registrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));
    }

    public List<TournamentRegistration> getConfirmedRegistrations(Tournament tournament) {
        return registrationRepository.findByTournamentAndStatus(tournament, TournamentRegistration.Status.CONFIRMED);
    }

    public int countConfirmed(Tournament tournament) {
        return registrationRepository.countByTournamentAndStatus(tournament, TournamentRegistration.Status.CONFIRMED);
    }

    public boolean isRegistered(Tournament tournament, User user) {
        if (registrationRepository.findByTournamentAndUser(tournament, user).isPresent()) return true;
        return registrationRepository.findByTournamentAndPartnerAndStatus(
            tournament, user, TournamentRegistration.Status.CONFIRMED).isPresent();
    }

    // ── INDIVIDUAL ────────────────────────────────────────────────
    public void join(Tournament tournament, User user) {
        if (tournament.getStatus() != Tournament.Status.OPEN)
            throw new RuntimeException("El torneo no está abierto");
        if (tournament.getModality() != Tournament.Modality.INDIVIDUAL)
            throw new RuntimeException("Este método solo es para torneos individuales");
        if (tournament.getOrganizer().equals(user))
            throw new RuntimeException("El organizador no puede inscribirse");
        if (isRegistered(tournament, user))
            throw new RuntimeException("Ya estás inscrito en este torneo");
        if (countConfirmed(tournament) >= tournament.getMaxParticipants())
            throw new RuntimeException("El torneo está lleno");

        TournamentRegistration reg = new TournamentRegistration();
        reg.setTournament(tournament);
        reg.setUser(user);
        reg.setStatus(TournamentRegistration.Status.CONFIRMED);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
    }

    // ── DUO ───────────────────────────────────────────────────────
    public void joinDuo(Tournament tournament, User user, User partner) {
        if (tournament.getStatus() != Tournament.Status.OPEN)
            throw new RuntimeException("El torneo no está abierto");
        if (tournament.getModality() != Tournament.Modality.DUO)
            throw new RuntimeException("Este método solo es para torneos en duo");
        if (tournament.getOrganizer().equals(user))
            throw new RuntimeException("El organizador no puede inscribirse");
        if (user.equals(partner))
            throw new RuntimeException("No puedes invitarte a ti mismo");

        friendshipRepository.findBetween(user, partner)
            .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED)
            .orElseThrow(() -> new RuntimeException("Solo puedes invitar a amigos"));

        if (registrationRepository.findByTournamentAndUser(tournament, user).isPresent())
            throw new RuntimeException("Ya tienes una inscripción en este torneo");
        if (registrationRepository.findByTournamentAndUser(tournament, partner).isPresent())
            throw new RuntimeException("Tu compañero ya está inscrito en este torneo");
        if (registrationRepository.findByTournamentAndPartner(tournament, partner).isPresent())
            throw new RuntimeException("Tu compañero ya tiene una invitación pendiente");
        if (countConfirmed(tournament) >= tournament.getMaxParticipants())
            throw new RuntimeException("El torneo está lleno");

        TournamentRegistration reg = new TournamentRegistration();
        reg.setTournament(tournament);
        reg.setUser(user);
        reg.setPartner(partner);
        reg.setStatus(TournamentRegistration.Status.PENDING);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
    }

    public void acceptDuoInvitation(Long registrationId, User partner) {
        TournamentRegistration reg = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));
        if (!reg.getPartner().equals(partner))
            throw new RuntimeException("No autorizado");
        if (reg.getStatus() != TournamentRegistration.Status.PENDING)
            throw new RuntimeException("Esta invitación ya no está pendiente");
        if (countConfirmed(reg.getTournament()) >= reg.getTournament().getMaxParticipants())
            throw new RuntimeException("El torneo está lleno");
        reg.setStatus(TournamentRegistration.Status.CONFIRMED);
        registrationRepository.save(reg);
    }

    public void rejectDuoInvitation(Long registrationId, User partner) {
        TournamentRegistration reg = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));
        if (!reg.getPartner().equals(partner))
            throw new RuntimeException("No autorizado");
        registrationRepository.delete(reg);
    }

    public Optional<TournamentRegistration> getMyDuoRegistration(Tournament tournament, User user) {
        Optional<TournamentRegistration> asInitiator = registrationRepository.findByTournamentAndUser(tournament, user);
        if (asInitiator.isPresent()) return asInitiator;
        return registrationRepository.findByTournamentAndPartnerAndStatus(
            tournament, user, TournamentRegistration.Status.CONFIRMED);
    }

    public Optional<TournamentRegistration> getPendingDuoInvitation(Tournament tournament, User user) {
        return registrationRepository.findByTournamentAndPartnerAndStatus(
            tournament, user, TournamentRegistration.Status.PENDING);
    }

    public List<TournamentRegistration> getAllPendingDuoInvitations(User user) {
        return registrationRepository.findByPartnerAndStatus(user, TournamentRegistration.Status.PENDING);
    }

    // ── TEAM ──────────────────────────────────────────────────────
    public void joinTeam(Tournament tournament, User captain, Team team) {
        if (tournament.getStatus() != Tournament.Status.OPEN)
            throw new RuntimeException("El torneo no está abierto");
        if (tournament.getModality() != Tournament.Modality.TEAM)
            throw new RuntimeException("Este método solo es para torneos por equipos");
        if (tournament.getOrganizer().equals(captain))
            throw new RuntimeException("El organizador no puede inscribir un equipo");
        if (!team.getCaptain().equals(captain))
            throw new RuntimeException("Solo el capitán puede inscribir al equipo");
        if (registrationRepository.findByTournamentAndTeam(tournament, team).isPresent())
            throw new RuntimeException("Este equipo ya está inscrito en el torneo");

        int confirmedMembers = teamMemberRepository.countByTeamAndStatus(team, TeamMember.Status.CONFIRMED);
        if (confirmedMembers < tournament.getPlayersPerTeam())
            throw new RuntimeException("El equipo necesita al menos " + tournament.getPlayersPerTeam() + " miembros confirmados");

        if (countConfirmed(tournament) >= tournament.getMaxParticipants())
            throw new RuntimeException("El torneo está lleno");

        TournamentRegistration reg = new TournamentRegistration();
        reg.setTournament(tournament);
        reg.setUser(captain);
        reg.setTeam(team);
        reg.setStatus(TournamentRegistration.Status.CONFIRMED);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
    }

    public void leaveTeam(Tournament tournament, User captain, Team team) {
        if (!team.getCaptain().equals(captain))
            throw new RuntimeException("Solo el capitán puede retirar al equipo");
        TournamentRegistration reg = registrationRepository.findByTournamentAndTeam(tournament, team)
            .orElseThrow(() -> new RuntimeException("El equipo no está inscrito en este torneo"));
        registrationRepository.delete(reg);
    }

    public Optional<TournamentRegistration> getTeamRegistration(Tournament tournament, Team team) {
        return registrationRepository.findByTournamentAndTeam(tournament, team);
    }

    // ── LEAVE individual y duo ────────────────────────────────────
    public void leave(Tournament tournament, User user) {
        Optional<TournamentRegistration> asInitiator = registrationRepository.findByTournamentAndUser(tournament, user);
        if (asInitiator.isPresent()) {
            registrationRepository.delete(asInitiator.get());
            return;
        }
        Optional<TournamentRegistration> asPartner = registrationRepository.findByTournamentAndPartnerAndStatus(
            tournament, user, TournamentRegistration.Status.CONFIRMED);
        if (asPartner.isPresent()) {
            registrationRepository.delete(asPartner.get());
            return;
        }
        throw new RuntimeException("No estás inscrito en este torneo");
    }
    
    public List<TournamentRegistration> getMyRegistrations(User user) {
        return registrationRepository.findByUserAndStatus(user, TournamentRegistration.Status.CONFIRMED);
    }

}
