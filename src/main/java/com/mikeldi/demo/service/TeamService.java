package com.mikeldi.demo.service;

import com.mikeldi.demo.entity.Friendship;
import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.TeamMember;
import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.FriendshipRepository;
import com.mikeldi.demo.repository.TeamMemberRepository;
import com.mikeldi.demo.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private FriendshipRepository friendshipRepository;

    public Team getById(Long id) {
        return teamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    // ── CREAR EQUIPO ──────────────────────────────────────────────
    public Team create(String name, User captain) {
        if (teamRepository.findByName(name).isPresent())
            throw new RuntimeException("Ya existe un equipo con ese nombre");

        Team team = new Team();
        team.setName(name);
        team.setCaptain(captain);
        team = teamRepository.save(team);

        // El capitán es miembro CONFIRMED automáticamente
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(captain);
        member.setStatus(TeamMember.Status.CONFIRMED);
        member.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(member);

        return team;
    }

    // ── INVITAR MIEMBRO ───────────────────────────────────────────
    public void inviteMember(Team team, User captain, User invited) {
        if (!team.getCaptain().equals(captain))
            throw new RuntimeException("Solo el capitán puede invitar miembros");
        if (captain.equals(invited))
            throw new RuntimeException("Ya eres miembro del equipo");

        friendshipRepository.findBetween(captain, invited)
            .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED)
            .orElseThrow(() -> new RuntimeException("Solo puedes invitar a amigos"));

        if (teamMemberRepository.existsByTeamAndUser(team, invited))
            throw new RuntimeException("Este usuario ya está en el equipo o tiene una invitación pendiente");

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(invited);
        member.setStatus(TeamMember.Status.PENDING);
        member.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(member);
    }

    // ── ACEPTAR INVITACIÓN ────────────────────────────────────────
    public void acceptInvitation(Long memberId, User user) {
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));
        if (!member.getUser().equals(user))
            throw new RuntimeException("No autorizado");
        if (member.getStatus() != TeamMember.Status.PENDING)
            throw new RuntimeException("Esta invitación ya no está pendiente");
        member.setStatus(TeamMember.Status.CONFIRMED);
        teamMemberRepository.save(member);
    }

    // ── RECHAZAR INVITACIÓN ───────────────────────────────────────
    public void rejectInvitation(Long memberId, User user) {
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));
        if (!member.getUser().equals(user))
            throw new RuntimeException("No autorizado");
        teamMemberRepository.delete(member);
    }

    // ── EXPULSAR MIEMBRO ──────────────────────────────────────────
    public void kickMember(Team team, User captain, User target) {
        if (!team.getCaptain().equals(captain))
            throw new RuntimeException("Solo el capitán puede expulsar miembros");
        if (captain.equals(target))
            throw new RuntimeException("El capitán no puede expulsarse a sí mismo");
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, target)
            .orElseThrow(() -> new RuntimeException("El usuario no pertenece al equipo"));
        teamMemberRepository.delete(member);
    }

    // ── DISOLVER EQUIPO ───────────────────────────────────────────
    public void dissolve(Team team, User captain) {
        if (!team.getCaptain().equals(captain))
            throw new RuntimeException("Solo el capitán puede disolver el equipo");
        teamRepository.delete(team);
    }

    // ── ABANDONAR EQUIPO ──────────────────────────────────────────
    public void leave(Team team, User user) {
        if (team.getCaptain().equals(user))
            throw new RuntimeException("El capitán no puede abandonar el equipo. Disuelve el equipo si quieres.");
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
            .orElseThrow(() -> new RuntimeException("No perteneces a este equipo"));
        teamMemberRepository.delete(member);
    }

    public List<TeamMember> getConfirmedMembers(Team team) {
        return teamMemberRepository.findByTeamAndStatus(team, TeamMember.Status.CONFIRMED);
    }

    public List<TeamMember> getPendingMembers(Team team) {
        return teamMemberRepository.findByTeamAndStatus(team, TeamMember.Status.PENDING);
    }

    public List<TeamMember> getPendingInvitations(User user) {
        return teamMemberRepository.findByUserAndStatus(user, TeamMember.Status.PENDING);
    }

    public int countConfirmedMembers(Team team) {
        return teamMemberRepository.countByTeamAndStatus(team, TeamMember.Status.CONFIRMED);
    }

    public boolean isMember(Team team, User user) {
        return teamMemberRepository.existsByTeamAndUser(team, user);
    }

    public Optional<TeamMember> getMembership(Team team, User user) {
        return teamMemberRepository.findByTeamAndUser(team, user);
    }
    
    public List<Team> getTeamsByCaptain(User captain) {
        return teamRepository.findByCaptain(captain);
    }
}
