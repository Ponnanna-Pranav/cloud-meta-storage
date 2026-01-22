package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByOwnerAndFolderIsNullAndDeletedFalse(User owner);

    List<FileEntity> findByOwnerAndFolderAndDeletedFalse(User owner, Folder folder);

    Optional<FileEntity> findByIdAndOwnerAndDeletedFalse(UUID id, User owner);

    Optional<FileEntity> findByIdAndOwner(UUID id, User owner);

    List<FileEntity> findByOwnerAndDeletedTrue(User owner);

    List<FileEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime time);
}
