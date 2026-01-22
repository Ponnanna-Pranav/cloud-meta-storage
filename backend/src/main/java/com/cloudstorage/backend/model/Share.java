package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Share {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private FileEntity file;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}

