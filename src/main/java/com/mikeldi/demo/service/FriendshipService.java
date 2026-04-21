package com.mikeldi.demo.service;

import com.mikeldi.demo.dto.FriendDTO;
import com.mikeldi.demo.entity.Friendship;
import com.mikeldi.demo.entity.User;
import com.mikeldi.demo.repository.FriendshipRepository;
import com.mikeldi.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private UserRepository userRepository;

    // Devuelve la lista de amigos aceptados
    public List<User> getFriends(User user) {
        return friendshipRepository.findAcceptedFriendships(user).stream()
            .map(f -> f.getSender().equals(user) ? f.getReceiver() : f.getSender())
            .collect(Collectors.toList());
    }

    // Solicitudes recibidas pendientes
    public List<Friendship> getPendingReceived(User user) {
        return friendshipRepository.findByReceiverAndStatus(user, Friendship.Status.PENDING);
    }

    // Enviar solicitud
    public void sendRequest(User sender, String receiverUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (sender.equals(receiver)) throw new RuntimeException("No puedes añadirte a ti mismo");
        friendshipRepository.findBetween(sender, receiver).ifPresent(f -> {
            throw new RuntimeException("Ya existe una solicitud entre estos usuarios");
        });
        Friendship f = new Friendship();
        f.setSender(sender);
        f.setReceiver(receiver);
        f.setStatus(Friendship.Status.PENDING);
        friendshipRepository.save(f);
    }

    // Aceptar solicitud
    public void acceptRequest(Long friendshipId, User user) {
        Friendship f = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        if (!f.getReceiver().equals(user)) throw new RuntimeException("No autorizado");
        f.setStatus(Friendship.Status.ACCEPTED);
        friendshipRepository.save(f);
    }

    // Rechazar solicitud
    public void rejectRequest(Long friendshipId, User user) {
        Friendship f = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        if (!f.getReceiver().equals(user)) throw new RuntimeException("No autorizado");
        friendshipRepository.delete(f);
    }
    
    // Eliminar amistad
    @Transactional
    public void remove(Long friendshipId) {
        friendshipRepository.deleteById(friendshipId);
    }
    
    public List<FriendDTO> getFriendDTOs(User user) {
        return friendshipRepository.findAcceptedFriendships(user)
            .stream()
            .map(f -> {
                User friend = f.getSender().equals(user) ? f.getReceiver() : f.getSender();
                return new FriendDTO(f.getId(), friend.getUsername(), friend.getAvatarUrl());
            })
            .toList();
    }
}
