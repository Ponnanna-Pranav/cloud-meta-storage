package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.model.*;
import com.cloudstorage.backend.repository.*;
import com.cloudstorage.backend.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}
)
public class FileController {

    private final FileService fileService;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;

    public FileController(
            FileService fileService,
            UserRepository userRepository,
            FolderRepository folderRepository
    ) {
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
    }

    // UPLOAD
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "folderId", required = false) String folderId,
            Principal principal
    ) throws Exception {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        UUID folderUUID = null;
        if (folderId != null && !folderId.isBlank()) {
            folderUUID = UUID.fromString(folderId);
        }

        fileService.uploadMultipart(file, folderUUID, user);

        return ResponseEntity.ok().build();
    }

    // LIST
    @GetMapping
    public List<Map<String, Object>> listFiles(
            @RequestParam(required = false) UUID folderId,
            Principal principal
    ) {
        return fileService.listFiles(principal.getName(), folderId);
    }

    // VIEW
    @GetMapping("/{id}/view")
    public Map<String, Object> view(@PathVariable UUID id, Principal principal) {
        return fileService.viewFile(id, principal.getName());
    }

    // DOWNLOAD
    @GetMapping("/{id}/download")
    public Map<String, Object> download(@PathVariable UUID id, Principal principal) {
        return fileService.downloadFile(id, principal.getName());
    }

    // LIST TRASH
    @GetMapping("/trash")
    public List<Map<String, Object>> listTrash(Principal principal) {
        return fileService.listTrash(principal.getName());
    }

    // RESTORE
    @PutMapping("/{id}/restore")
    public void restore(@PathVariable UUID id, Principal principal) {
        fileService.restoreFile(id, principal.getName());
    }

    // PERMANENT DELETE
    @DeleteMapping("/{id}/permanent")
    public void permanentDelete(@PathVariable UUID id, Principal principal) {
        fileService.permanentlyDelete(id, principal.getName());
    }

    // EMPTY TRASH
    @DeleteMapping("/trash/empty")
    public void emptyTrash(Principal principal) {
        fileService.emptyTrash(principal.getName());
    }



    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id, Principal principal) {
        fileService.deleteFile(id, principal.getName());
    }
}
