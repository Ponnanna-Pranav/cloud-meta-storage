package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.service.FolderService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "http://localhost:3000")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    // ✅ CREATE FOLDER (JSON BODY)
    @PostMapping
    public Folder createFolder(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        return folderService.create(
                body.get("name"),
                null,
                principal.getName()
        );
    }

    // ✅ LIST FOLDERS
    @GetMapping
    public List<Folder> list(
            @RequestParam(required = false) UUID parentId,
            Principal principal
    ) {
        return folderService.list(parentId, principal.getName());
    }

    // 🗑️ TRASH FOLDER
    @DeleteMapping("/{id}")
    public void trash(@PathVariable UUID id, Principal principal) {
        folderService.trash(id, principal.getName());
    }
}
