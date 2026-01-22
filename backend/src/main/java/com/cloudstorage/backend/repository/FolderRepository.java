package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByOwnerAndParentAndDeletedFalse(User owner, Folder parent);
}
