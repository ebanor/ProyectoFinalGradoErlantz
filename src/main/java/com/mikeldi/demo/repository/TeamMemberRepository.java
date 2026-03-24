package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.TeamMember;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByTeamAndUser(Team team, User user);
    List<TeamMember> findByTeamAndStatus(Team team, TeamMember.Status status);
    List<TeamMember> findByUserAndStatus(User user, TeamMember.Status status);
    int countByTeamAndStatus(Team team, TeamMember.Status status);
    boolean existsByTeamAndUser(Team team, User user);
}
