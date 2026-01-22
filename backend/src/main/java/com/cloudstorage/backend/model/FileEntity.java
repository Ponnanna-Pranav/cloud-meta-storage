package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private Long size;
    private String storageKey;

    private String type;   // ⭐ REQUIRED

    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "original_folder_id")
    private Folder originalFolder;

    private LocalDateTime createdAt = LocalDateTime.now();
}
