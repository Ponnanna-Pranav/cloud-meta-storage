package com.cloudstorage.backend.service;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FolderRepository;
import com.cloudstorage.backend.repository.UserRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class FolderService {

    private final FolderRepository folderRepo;
    private final UserRepository userRepo;

    public FolderService(FolderRepository folderRepo, UserRepository userRepo) {
        this.folderRepo = folderRepo;
        this.userRepo = userRepo;
    }

    // ✅ CREATE FOLDER
    public Folder create(String name, UUID parentId, String email) {

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Folder name required");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder folder = new Folder();
        folder.setName(name);
        folder.setOwner(user);

        folder.setDeleted(false);

        if (parentId != null) {
            Folder parent = folderRepo.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));

            if (!parent.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Forbidden");
            }
            folder.setParent(parent);
        }

        return folderRepo.save(folder);
    }

    // ✅ LIST FOLDERS
    public List<Folder> list(UUID parentId, String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder parent = null;
        if (parentId != null) {
            parent = folderRepo.findById(parentId).orElseThrow();
        }

        return folderRepo.findByOwnerAndParentAndDeletedFalse(user, parent);
    }

    // 🗑️ TRASH FOLDER
    public void trash(UUID id, String email) {

        Folder folder = folderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Forbidden");
        }

        folder.setDeleted(true);
        folderRepo.save(folder);
    }
}
