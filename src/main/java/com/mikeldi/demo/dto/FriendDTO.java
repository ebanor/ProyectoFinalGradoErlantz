package com.mikeldi.demo.dto;

public class FriendDTO {

    private Long friendshipId;
    private String username;
    private String avatarUrl;

    public FriendDTO(Long friendshipId, String username, String avatarUrl) {
        this.friendshipId = friendshipId;
        this.username = username;
        this.avatarUrl    = avatarUrl;
    }

    public Long getFriendshipId() { return friendshipId; }
    public String getUsername()   { return username; }
    public String getAvatarUrl()  { return avatarUrl; }
}