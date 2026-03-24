package com.mikeldi.demo.repository;

import com.mikeldi.demo.entity.Friendship;
import com.mikeldi.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Todas las amistades aceptadas de un usuario
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    // Solicitudes pendientes recibidas
    List<Friendship> findByReceiverAndStatus(User receiver, Friendship.Status status);

    // Solicitudes pendientes enviadas
    List<Friendship> findBySenderAndStatus(User sender, Friendship.Status status);

    // Comprobar si ya existe relación entre dos usuarios
    @Query("SELECT f FROM Friendship f WHERE (f.sender = :a AND f.receiver = :b) OR (f.sender = :b AND f.receiver = :a)")
    Optional<Friendship> findBetween(@Param("a") User a, @Param("b") User b);
}
