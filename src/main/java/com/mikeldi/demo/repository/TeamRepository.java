package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Team;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    List<Team> findByCaptain(User captain);
}
