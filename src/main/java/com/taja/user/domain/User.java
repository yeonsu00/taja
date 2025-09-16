package com.taja.user.domain;

import java.time.LocalDateTime;

public class User {

    private Long userId;

    private String name;

    private String email;

    private String password;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
