package com.mikeldi.demo.dto;

public class FriendDTO {

    private Long friendshipId;
    private String username;

    public FriendDTO(Long friendshipId, String username) {
        this.friendshipId = friendshipId;
        this.username = username;
    }

    public Long getFriendshipId() { return friendshipId; }
    public String getUsername()   { return username; }
}