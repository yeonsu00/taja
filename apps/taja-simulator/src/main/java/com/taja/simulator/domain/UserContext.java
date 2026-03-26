package com.taja.simulator.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class UserContext {

    private final String personaName;
    private final String personaDescription;

    private String accessToken;
    private String email;
    private String name;

    private List<Long> knownStationIds = new ArrayList<>();
    private Long lastJoinedStationId;
    private Long lastCreatedPostId;
    private Long lastCreatedCommentId;
    private Long lastLikedPostId;

    public UserContext(String personaName, String personaDescription) {
        this.personaName = personaName;
        this.personaDescription = personaDescription;
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.name = "sim_" + uid;
        this.email = "sim_" + uid + "@test.com";
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }
}
